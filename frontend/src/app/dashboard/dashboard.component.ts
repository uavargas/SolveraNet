import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';
import { UsersComponent } from './users/users.component';

interface Asset {
  id: number;
  name: string;
  type: string;
  status: 'ASSIGNED' | 'MAINTENANCE' | 'AVAILABLE';
  user: string;
  lastUpdate: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, UsersComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly userEmail = this.authService.userEmail;
  readonly userRole = this.authService.userRole;
  readonly isAdmin = computed(() => this.userRole() === 'ADMIN');

  readonly activeNav = signal<string>('dashboard');
  readonly isMobileMenuOpen = signal<boolean>(false);

  toggleMobileMenu(): void {
    this.isMobileMenuOpen.update(v => !v);
  }

  readonly assets = signal<Asset[]>([
    { id: 1, name: 'Laptop Dell XPS 15', type: 'Laptop', status: 'ASSIGNED', user: 'Carlos Mendoza', lastUpdate: 'Hace 12 min' },
    { id: 2, name: 'Monitor LG 27" 4K', type: 'Monitor', status: 'MAINTENANCE', user: 'Soporte TI', lastUpdate: 'Hace 1h' },
    { id: 3, name: 'Router Cisco RV340', type: 'Red', status: 'AVAILABLE', user: '—', lastUpdate: 'Hace 3h' },
    { id: 4, name: 'MacBook Air M2', type: 'Laptop', status: 'ASSIGNED', user: 'Ana García', lastUpdate: 'Hace 5h' },
    { id: 5, name: 'UPS APC Smart 1500', type: 'Energía', status: 'AVAILABLE', user: '—', lastUpdate: 'Hace 1d' },
  ]);

  readonly stats = computed(() => ({
    total: 342,
    assigned: 287,
    maintenance: 31,
    available: 24,
    assignedPct: Math.round((287 / 342) * 100),
    maintenancePct: Math.round((31 / 342) * 100),
    availablePct: Math.round((24 / 342) * 100),
  }));

  readonly recentActivity = signal([
    { text: 'Laptop HP EliteBook agregada al inventario', time: 'Hace 25 min', color: '#4A90C8' },
    { text: 'Monitor Samsung en mantenimiento preventivo', time: 'Hace 2h', color: '#f97316' },
    { text: 'Router TP-Link disponible tras revisión', time: 'Hace 4h', color: '#22c55e' },
    { text: 'iPad Pro asignado a Laura Torres', time: 'Hace 6h', color: '#4A90C8' },
  ]);

  readonly navItems = [
    { key: 'dashboard', label: 'Dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { key: 'assets', label: 'Activos', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4' },
    { key: 'movements', label: 'Movimientos', icon: 'M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4' },
    { key: 'users', label: 'Usuarios', icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z' },
    { key: 'reports', label: 'Reportes', icon: 'M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z' },
  ];
  readonly visibleNavItems = computed(() =>
    this.navItems.filter(item => item.key !== 'users' || this.isAdmin())
  );
  getBadgeClass(status: string): string {
    switch (status) {
      case 'ASSIGNED': return 'status-badge badge-assigned';
      case 'MAINTENANCE': return 'status-badge badge-maintenance';
      case 'AVAILABLE': return 'status-badge badge-available';
      default: return 'status-badge';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ASSIGNED': return 'Asignado';
      case 'MAINTENANCE': return 'Mantenim.';
      case 'AVAILABLE': return 'Disponible';
      default: return status;
    }
  }

  setActiveNav(key: string): void {
    this.activeNav.set(key);
  }

  logout(): void {
    this.authService.logout();
  }
}
