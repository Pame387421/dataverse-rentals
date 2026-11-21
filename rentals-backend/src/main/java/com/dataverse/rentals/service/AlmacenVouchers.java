package com.dataverse.rentals.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AlmacenVouchers extends AlmacenPdf {

    public AlmacenVouchers(@Value("${app.uploads.dir:uploads}") String ruta) {
        super(ruta, "vouchers");
    }
}