import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface User {
  id: string;
  email: string;
  role: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserCreateDto {
  email: string;
  password?: string; // Optional because we don't send it on update
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly BASE_URL = 'http://localhost:8080/api/v1/users';

  // State
  private readonly _users = signal<User[]>([]);
  readonly users = computed(() => this._users());

  private readonly _isLoading = signal<boolean>(false);
  readonly isLoading = computed(() => this._isLoading());

  fetchUsers(): Observable<User[]> {
    this._isLoading.set(true);
    return this.http.get<User[]>(this.BASE_URL).pipe(
      tap({
        next: (users) => {
          this._users.set(users);
          this._isLoading.set(false);
        },
        error: () => this._isLoading.set(false)
      })
    );
  }

  createUser(data: UserCreateDto): Observable<User> {
    return this.http.post<User>(this.BASE_URL, data).pipe(
      tap((newUser) => {
        this._users.update(users => [...users, newUser]);
      })
    );
  }

  updateUser(id: string, data: UserCreateDto): Observable<User> {
    return this.http.put<User>(`${this.BASE_URL}/${id}`, { email: data.email, role: data.role }).pipe(
      tap((updatedUser) => {
        this._users.update(users => 
          users.map(u => u.id === id ? updatedUser : u)
        );
      })
    );
  }

  deactivateUser(id: string): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}/${id}/deactivate`, {}).pipe(
      tap(() => {
        this._users.update(users => 
          users.map(u => u.id === id ? { ...u, isActive: !u.isActive } : u)
        );
      })
    );
  }
}
