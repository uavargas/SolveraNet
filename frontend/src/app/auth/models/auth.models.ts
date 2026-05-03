export interface LoginRequest {
  email: string;
  password: string;
  role?: 'ADMIN' | 'TECHNICIAN' | 'USER';
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  role: 'ADMIN' | 'TECHNICIAN' | 'USER';
}
