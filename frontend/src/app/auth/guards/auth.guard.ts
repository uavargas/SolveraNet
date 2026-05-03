import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated() && authService.isTokenValid()) {
    return true;
  }

  // Si no está autenticado o el token expiró, cerramos la sesión para limpiar el estado y redirigimos
  authService.logout();
  return router.createUrlTree(['/login']);
};
