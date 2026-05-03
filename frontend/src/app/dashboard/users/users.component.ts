import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth/services/auth.service';
import { User, UserCreateDto, UserService } from '../services/user.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);

  readonly users = this.userService.users;
  readonly isLoading = this.userService.isLoading;
  readonly isAdmin = computed(() => this.authService.userRole() === 'ADMIN');

  // Modal State
  readonly isModalOpen = signal<boolean>(false);
  readonly modalMode = signal<'CREATE' | 'EDIT'>('CREATE');
  readonly editingUserId = signal<string | null>(null);
  
  // Form State
  readonly formData = signal<UserCreateDto>({ email: '', password: '', role: 'USER' });
  readonly isSubmitting = signal<boolean>(false);
  readonly errorMessage = signal<string>('');

  readonly roles = ['ADMIN', 'TECHNICIAN', 'USER'];

  ngOnInit(): void {
    this.userService.fetchUsers().subscribe();
  }

  openCreateModal(): void {
    if (!this.isAdmin()) {
      this.errorMessage.set('No tienes permiso para crear usuarios.');
      return;
    }
    this.modalMode.set('CREATE');
    this.editingUserId.set(null);
    this.formData.set({ email: '', password: '', role: 'USER' });
    this.errorMessage.set('');
    this.isModalOpen.set(true);
  }

  openEditModal(user: User): void {
    this.modalMode.set('EDIT');
    this.editingUserId.set(user.id);
    this.formData.set({ email: user.email, password: '', role: user.role });
    this.errorMessage.set('');
    this.isModalOpen.set(true);
  }

  closeModal(): void {
    this.isModalOpen.set(false);
  }

  updateFormData(field: keyof UserCreateDto, value: string): void {
    this.formData.update(data => ({ ...data, [field]: value }));
  }

  onSubmit(): void {
    const data = this.formData();
    if (!data.email || !data.role || (this.modalMode() === 'CREATE' && !data.password)) {
      this.errorMessage.set('Por favor completa todos los campos requeridos.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    const request$ = this.modalMode() === 'CREATE'
      ? this.userService.createUser(data)
      : this.userService.updateUser(this.editingUserId()!, data);

    request$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeModal();
      },
      error: (err) => {
        this.errorMessage.set(err.message || 'Error al guardar el usuario.');
        this.isSubmitting.set(false);
      }
    });
  }

  toggleActive(user: User): void {
    this.userService.deactivateUser(user.id).subscribe();
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'ADMIN': return 'Administrador';
      case 'TECHNICIAN': return 'Técnico';
      case 'USER': return 'Usuario';
      default: return role;
    }
  }
}
