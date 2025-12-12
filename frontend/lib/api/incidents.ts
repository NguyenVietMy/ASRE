import { apiClient } from "../axios";

export interface Incident {
  id: string;
  alertRuleId: string;
  projectId: string;
  serviceId?: string;
  status: "OPEN" | "ACKNOWLEDGED" | "RESOLVED" | "CLOSED";
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  title: string;
  description?: string;
  triggeredAt: string;
  acknowledgedAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  metadata?: Record<string, unknown>;
}

export interface IncidentNote {
  id: string;
  incidentId: string;
  content: string;
  createdAt: string;
  createdBy: string;
}

export interface UpdateIncidentRequest {
  status?: "OPEN" | "ACKNOWLEDGED" | "RESOLVED" | "CLOSED";
}

export interface CreateIncidentNoteRequest {
  content: string;
}

// List incidents
export async function getIncidents(params?: {
  projectId?: string;
  serviceId?: string;
  status?: string;
  severity?: string;
}): Promise<Incident[]> {
  const response = await apiClient.get<Incident[]>("/api/alerts/incidents", {
    params,
  });
  return response.data;
}

// Get incident details
export async function getIncident(id: string): Promise<Incident> {
  const response = await apiClient.get<Incident>(`/api/alerts/incidents/${id}`);
  return response.data;
}

// Update incident status
export async function updateIncident(
  id: string,
  data: UpdateIncidentRequest
): Promise<Incident> {
  const response = await apiClient.patch<Incident>(
    `/api/alerts/incidents/${id}`,
    data
  );
  return response.data;
}

// Add incident note
export async function addIncidentNote(
  id: string,
  data: CreateIncidentNoteRequest
): Promise<IncidentNote> {
  const response = await apiClient.post<IncidentNote>(
    `/api/alerts/incidents/${id}/notes`,
    data
  );
  return response.data;
}
