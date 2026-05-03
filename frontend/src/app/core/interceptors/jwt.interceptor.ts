import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../auth/services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Exclude auth routes from adding JWT header
  const isAuthRoute = req.url.includes('/auth/');
  if (!token || isAuthRoute) {
    return next(req);
  }

  const clonedReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${token}`)
  });

  return next(clonedReq);
};
