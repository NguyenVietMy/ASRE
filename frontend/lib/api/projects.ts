import { apiClient } from "../axios";

export interface Project {
  id: string;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
}

export interface ApiKeyResponse {
  apiKey?: string; // Only present on creation/regeneration
  maskedApiKey: string;
}

export interface ApiKeyUsage {
  requestCount: number;
  errorCount: number;
  lastUsedAt?: string;
}

// List user's projects
export async function getProjects(): Promise<Project[]> {
  const response = await apiClient.get<Project[]>("/api/projects");
  return response.data;
}

// Get project details
export async function getProject(id: string): Promise<Project> {
  const response = await apiClient.get<Project>(`/api/projects/${id}`);
  return response.data;
}

// Create new project
export async function createProject(
  data: CreateProjectRequest
): Promise<Project & { apiKey: string }> {
  const response = await apiClient.post<Project & { apiKey: string }>(
    "/api/projects",
    data
  );
  return response.data;
}

// Update project
export async function updateProject(
  id: string,
  data: UpdateProjectRequest
): Promise<Project> {
  if (!id || id === "undefined") {
    throw new Error("Project ID is required");
  }
  const response = await apiClient.patch<Project>(`/api/projects/${id}`, data);
  return response.data;
}

// Delete project (soft delete)
export async function deleteProject(id: string): Promise<void> {
  await apiClient.delete(`/api/projects/${id}`);
}

// Get masked API key
export async function getApiKey(id: string): Promise<ApiKeyResponse> {
  const response = await apiClient.get<ApiKeyResponse>(
    `/api/projects/${id}/api-key`
  );
  return response.data;
}

// Regenerate API key
export async function regenerateApiKey(
  id: string
): Promise<ApiKeyResponse & { apiKey: string }> {
  const response = await apiClient.post<ApiKeyResponse & { apiKey: string }>(
    `/api/projects/${id}/api-key/regenerate`
  );
  return response.data;
}

// Get API key usage stats
export async function getApiKeyUsage(id: string): Promise<ApiKeyUsage> {
  const response = await apiClient.get<ApiKeyUsage>(
    `/api/projects/${id}/api-key/usage`
  );
  return response.data;
}
