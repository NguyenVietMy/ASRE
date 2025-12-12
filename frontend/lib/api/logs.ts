import { apiClient } from "../axios";

export interface LogEntry {
  id: string;
  timestamp: string;
  level: "ERROR" | "WARN" | "INFO" | "DEBUG";
  message: string;
  serviceId?: string;
  traceId?: string;
  tags?: Record<string, string>;
}

export interface LogQueryResponse {
  logs: LogEntry[];
  nextToken?: string; // For pagination
  totalCount?: number;
}

export interface LogQueryParams {
  startTime: string; // ISO 8601
  endTime: string; // ISO 8601
  searchText?: string;
  serviceId?: string;
  level?: "ERROR" | "WARN" | "INFO" | "DEBUG";
  traceId?: string;
  limit?: number; // Default: 50
  paginationToken?: string;
}

export interface LogContext {
  before: LogEntry[];
  target: LogEntry;
  after: LogEntry[];
}

export interface LogVolumeDataPoint {
  timestamp: string;
  count: number;
  level?: string;
}

export interface LogVolumeResponse {
  dataPoints: LogVolumeDataPoint[];
}

export interface ErrorSpike {
  timestamp: string;
  count: number;
  severity: "low" | "medium" | "high";
}

export interface ErrorSpikesResponse {
  spikes: ErrorSpike[];
}

// Query logs with pagination
export async function queryLogs(
  projectId: string,
  params: LogQueryParams
): Promise<LogQueryResponse> {
  const response = await apiClient.get<LogQueryResponse>("/api/logs/query", {
    params,
    headers: {
      "X-Project-ID": projectId,
    },
  });
  return response.data;
}

// Get log context (before/after)
export async function getLogContext(
  projectId: string,
  logId: string
): Promise<LogContext> {
  const response = await apiClient.get<LogContext>(
    `/api/logs/${logId}/context`,
    {
      headers: {
        "X-Project-ID": projectId,
      },
    }
  );
  return response.data;
}

// Get log volume aggregation
export async function getLogVolume(
  projectId: string,
  startTime: string,
  endTime: string,
  rollupPeriod?: string,
  level?: string
): Promise<LogVolumeResponse> {
  const response = await apiClient.get<LogVolumeResponse>("/api/logs/volume", {
    params: {
      startTime,
      endTime,
      rollupPeriod,
      level,
    },
    headers: {
      "X-Project-ID": projectId,
    },
  });
  return response.data;
}

// Detect error spikes
export async function detectErrorSpikes(
  projectId: string,
  startTime: string,
  endTime: string,
  threshold?: number
): Promise<ErrorSpikesResponse> {
  const response = await apiClient.get<ErrorSpikesResponse>(
    "/api/logs/error-spikes",
    {
      params: {
        startTime,
        endTime,
        threshold,
      },
      headers: {
        "X-Project-ID": projectId,
      },
    }
  );
  return response.data;
}

// Get logs by trace ID
export async function getLogsByTrace(
  projectId: string,
  traceId: string
): Promise<LogEntry[]> {
  const response = await apiClient.get<LogEntry[]>("/api/logs/trace", {
    params: { traceId },
    headers: {
      "X-Project-ID": projectId,
    },
  });
  return response.data;
}
