import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

type Role = 'ADMIN' | 'TECHNICIAN' | 'USER';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Signals
  readonly email = signal<string>('');
  readonly password = signal<string>('');
  readonly selectedRole = signal<Role>('ADMIN');
  readonly isLoading = signal<boolean>(false);
  readonly errorMessage = signal<string>('');
  readonly isPasswordVisible = signal<boolean>(false);

  readonly roles: { key: Role; label: string }[] = [
    { key: 'ADMIN', label: 'Admin' },
    { key: 'TECHNICIAN', label: 'Técnico' },
    { key: 'USER', label: 'Usuario' }
  ];

  selectRole(role: Role): void {
    this.selectedRole.set(role);
    this.errorMessage.set('');
  }

  togglePasswordVisibility(): void {
    this.isPasswordVisible.update((v) => !v);
  }

  onLogin(): void {
    if (!this.email() || !this.password()) {
      this.errorMessage.set('Por favor completa todos los campos.');
      return;
    }
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService
      .login({ email: this.email(), password: this.password(), role: this.selectedRole() })
      .subscribe({
        next: (response) => {
          this.authService.setToken(response.token);
          this.isLoading.set(false);
          this.router.navigate(['/dashboard']);
        },
        error: (err: Error) => {
          this.errorMessage.set(err.message);
          this.isLoading.set(false);
        }
      });
  }
}
