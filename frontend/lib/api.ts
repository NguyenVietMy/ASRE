import { apiClient } from "./axios";

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
}

export interface User {
  id: string;
  email: string;
  role: string;
}

// Auth endpoints (don't use interceptors for these, handle manually)
export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>(
    "/api/auth/register",
    data
  );
  return response.data;
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>("/api/auth/login", data);
  return response.data;
}

export async function logout(): Promise<void> {
  await apiClient.post("/api/auth/logout");
}

export async function refreshToken(): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>("/api/auth/refresh");
  return response.data;
}
