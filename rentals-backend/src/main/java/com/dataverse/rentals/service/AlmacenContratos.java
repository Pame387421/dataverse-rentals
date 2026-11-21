package com.dataverse.rentals.service;

import com.dataverse.rentals.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.UUID;

/** Guarda los contratos en PDF en disco (fuera de la carpeta pública). En la base solo queda el nombre del archivo. */
@Slf4j
@Service
public class AlmacenContratos {

    private static final long MAX_BYTES = 10L * 1024 * 1024;

    private final Path carpeta;

    public AlmacenContratos(@Value("${app.uploads.dir:uploads}") String ruta) {
        this.carpeta = Path.of(ruta).toAbsolutePath().normalize().resolve("contratos");
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de contratos: " + carpeta, e);
        }
    }

    /** Valida y guarda el PDF. Devuelve el nombre con el que quedó guardado. */
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Seleccione un archivo PDF.");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El archivo supera los 10 MB permitidos.");
        }
        String original = archivo.getOriginalFilename();
        if (original == null || !original.toLowerCase().endsWith(".pdf")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Solo se permiten archivos PDF.");
        }

        try (InputStream in = archivo.getInputStream()) {
            String cabecera = new String(in.readNBytes(5), StandardCharsets.US_ASCII);
            if (!"%PDF-".equals(cabecera)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El archivo no es un PDF válido.");
            }
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo.");
        }

        String nombre = UUID.randomUUID() + ".pdf";
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, carpeta.resolve(nombre));
        } catch (IOException e) {
            log.error("No se pudo guardar el contrato", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo.");
        }
        return nombre;
    }

    /** Devuelve el archivo, o null si no existe en el servidor. */
    public Resource cargar(String nombre) {
        Path ruta = resolver(nombre);
        return ruta != null && Files.isRegularFile(ruta) ? new FileSystemResource(ruta) : null;
    }

    public void eliminar(String nombre) {
        Path ruta = resolver(nombre);
        if (ruta == null) return;
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            log.warn("No se pudo borrar el archivo {}", ruta, e);
        }
    }

    /** Solo se acepta el nombre del archivo, y debe quedar dentro de la carpeta de contratos. */
    private Path resolver(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        try {
            Path archivo = Path.of(nombre.trim()).getFileName();
            if (archivo == null) return null;
            Path ruta = carpeta.resolve(archivo).normalize();
            return ruta.startsWith(carpeta) ? ruta : null;
        } catch (InvalidPathException e) {
            return null;
        }
    }
}