import { apiClient } from "../axios";

export interface AlertRule {
  id: string;
  name: string;
  description?: string;
  projectId: string;
  serviceId?: string;
  metricName: string;
  condition: {
    operator: "GT" | "LT" | "EQ" | "GTE" | "LTE";
    threshold: number;
    duration: string; // e.g., "5m"
  };
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAlertRuleRequest {
  name: string;
  description?: string;
  projectId: string;
  serviceId?: string;
  metricName: string;
  condition: {
    operator: "GT" | "LT" | "EQ" | "GTE" | "LTE";
    threshold: number;
    duration: string;
  };
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  enabled?: boolean;
}

export interface UpdateAlertRuleRequest {
  name?: string;
  description?: string;
  serviceId?: string;
  metricName?: string;
  condition?: {
    operator: "GT" | "LT" | "EQ" | "GTE" | "LTE";
    threshold: number;
    duration: string;
  };
  severity?: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  enabled?: boolean;
}

// List alert rules
export async function getAlertRules(
  projectId: string,
  serviceId?: string
): Promise<AlertRule[]> {
  const response = await apiClient.get<AlertRule[]>("/api/alerts/rules", {
    params: { projectId, serviceId },
  });
  return response.data;
}

// Get alert rule
export async function getAlertRule(id: string): Promise<AlertRule> {
  const response = await apiClient.get<AlertRule>(`/api/alerts/rules/${id}`);
  return response.data;
}

// Create alert rule
export async function createAlertRule(
  data: CreateAlertRuleRequest
): Promise<AlertRule> {
  const response = await apiClient.post<AlertRule>("/api/alerts/rules", data);
  return response.data;
}

// Update alert rule
export async function updateAlertRule(
  id: string,
  data: UpdateAlertRuleRequest
): Promise<AlertRule> {
  const response = await apiClient.put<AlertRule>(
    `/api/alerts/rules/${id}`,
    data
  );
  return response.data;
}

// Delete alert rule
export async function deleteAlertRule(id: string): Promise<void> {
  await apiClient.delete(`/api/alerts/rules/${id}`);
}
