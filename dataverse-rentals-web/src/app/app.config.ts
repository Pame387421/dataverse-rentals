import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter,  withHashLocation  } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';


import { routes } from './app.routes';
import { jwtInterceptor } from './nucleo/interceptores/jwt-interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideRouter(routes, withHashLocation()),
    provideHttpClient(withInterceptors([jwtInterceptor]))
  ]
};
