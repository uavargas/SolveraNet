import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { AuthResponse, LoginRequest } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly BASE_URL = 'http://localhost:8080/api/v1/auth';

  // Token lives in a private Signal — NO localStorage/sessionStorage
  private readonly _token = signal<string>('');
  private readonly _userRole = signal<'ADMIN' | 'TECHNICIAN' | 'USER' | null>(null);
  private readonly _userEmail = signal<string>('');

  // Public computed signals
  readonly isAuthenticated = computed(() => this._token() !== '');
  readonly userRole = computed(() => this._userRole());
  readonly userEmail = computed(() => this._userEmail());

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}/login`, credentials).pipe(
      tap((response) => {
        this._token.set(response.token);
        this._userRole.set(response.role);
        this._userEmail.set(response.email);
      }),
      catchError((error) => {
        const message =
          error?.error?.message ||
          error?.message ||
          'Error al iniciar sesión. Verifica tus credenciales.';
        return throwError(() => new Error(message));
      })
    );
  }

  setToken(token: string): void {
    this._token.set(token);
  }

  getToken(): string {
    return this._token();
  }

  isTokenValid(): boolean {
    const token = this._token();
    if (!token) return false;

    try {
      // Un JWT tiene 3 partes separadas por puntos. La segunda es el payload.
      const payloadBase64 = token.split('.')[1];
      // Reemplazamos los caracteres de base64url a base64 estándar
      const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      const decodedJson = atob(base64);
      const decoded = JSON.parse(decodedJson);

      if (decoded.exp) {
        const expirationDate = new Date(decoded.exp * 1000);
        return expirationDate > new Date(); // True si no ha expirado
      }
      return true; // Si no tiene fecha de expiración, lo damos por válido
    } catch (e) {
      return false; // Si hay error al decodificar, el token es inválido
    }
  }

  logout(): void {
    this._token.set('');
    this._userRole.set(null);
    this._userEmail.set('');
    this.router.navigate(['/login']);
  }
}
