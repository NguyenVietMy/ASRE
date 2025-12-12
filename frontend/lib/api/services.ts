import { apiClient } from "../axios";

export interface Service {
  id: string;
  name: string;
  projectId: string;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceOverview {
  service: Service;
  metrics: {
    totalMetrics: number;
    totalLogs: number;
    lastIngestionAt?: string;
  };
}

// List services for a project
export async function getServices(projectId: string): Promise<Service[]> {
  const response = await apiClient.get<Service[]>("/api/services", {
    params: { projectId },
  });
  return response.data;
}

// Get service details
export async function getService(
  projectId: string,
  serviceId: string
): Promise<Service> {
  const response = await apiClient.get<Service>(`/api/services/${serviceId}`, {
    params: { projectId },
  });
  return response.data;
}

// Get service overview with metrics
export async function getServiceOverview(
  projectId: string,
  serviceId: string
): Promise<ServiceOverview> {
  const response = await apiClient.get<ServiceOverview>(
    `/api/services/${serviceId}/overview`,
    {
      params: { projectId },
    }
  );
  return response.data;
}

// List metric names for a service
export async function getServiceMetrics(
  projectId: string,
  serviceId: string
): Promise<string[]> {
  const response = await apiClient.get<string[]>(
    `/api/services/${serviceId}/metrics`,
    {
      params: { projectId },
    }
  );
  return response.data;
}
