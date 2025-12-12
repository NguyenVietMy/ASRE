import { apiClient } from "../axios";

export interface MetricDataPoint {
  timestamp: string;
  value: number;
}

export interface MetricQueryResponse {
  dataPoints: MetricDataPoint[];
  metadata: {
    metricName: string;
    aggregation: string;
    rollupPeriod: string;
  };
}

export interface MetricQueryParams {
  metricName: string;
  startTime: string; // ISO 8601
  endTime: string; // ISO 8601
  aggregation?: "AVG" | "SUM" | "MIN" | "MAX" | "P95" | "P99";
  rollupPeriod?: string; // e.g., "5m", "1h"
  serviceId?: string;
  tags?: Record<string, string>;
}

export interface MultipleMetricQuery {
  metrics: Array<{
    metricName: string;
    aggregation?: string;
    rollupPeriod?: string;
    serviceId?: string;
    tags?: Record<string, string>;
  }>;
  startTime: string;
  endTime: string;
  alignment?: boolean;
}

export interface MultipleMetricResponse {
  results: Array<{
    metricName: string;
    dataPoints: MetricDataPoint[];
  }>;
}

export interface HistogramData {
  buckets: Array<{
    bucket: string;
    count: number;
  }>;
}

export interface AnomalyDataPoint extends MetricDataPoint {
  isAnomaly: boolean;
  anomalyScore?: number;
}

export interface AnomalyResponse {
  dataPoints: AnomalyDataPoint[];
  threshold: number;
}

// Query single metric
export async function queryMetric(
  projectId: string,
  params: MetricQueryParams
): Promise<MetricQueryResponse> {
  const response = await apiClient.get<MetricQueryResponse>(
    "/api/metrics/query",
    {
      params: {
        ...params,
      },
      headers: {
        "X-Project-ID": projectId,
      },
    }
  );
  return response.data;
}

// Query multiple metrics with alignment
export async function queryMultipleMetrics(
  projectId: string,
  query: MultipleMetricQuery
): Promise<MultipleMetricResponse> {
  const response = await apiClient.post<MultipleMetricResponse>(
    "/api/metrics/query-multiple",
    query,
    {
      headers: {
        "X-Project-ID": projectId,
      },
    }
  );
  return response.data;
}

// Get histogram data
export async function getHistogram(
  projectId: string,
  metricName: string,
  startTime: string,
  endTime: string,
  buckets?: number
): Promise<HistogramData> {
  const response = await apiClient.get<HistogramData>(
    "/api/metrics/histogram",
    {
      params: {
        metricName,
        startTime,
        endTime,
        buckets,
      },
      headers: {
        "X-Project-ID": projectId,
      },
    }
  );
  return response.data;
}

// Detect anomalies
export async function detectAnomalies(
  projectId: string,
  metricName: string,
  startTime: string,
  endTime: string,
  sensitivity?: number
): Promise<AnomalyResponse> {
  const response = await apiClient.get<AnomalyResponse>(
    "/api/metrics/anomalies",
    {
      params: {
        metricName,
        startTime,
        endTime,
        sensitivity,
      },
      headers: {
        "X-Project-ID": projectId,
      },
    }
  );
  return response.data;
}
