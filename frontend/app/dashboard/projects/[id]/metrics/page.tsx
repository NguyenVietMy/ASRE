"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter, useSearchParams } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  queryMetric,
  type MetricQueryResponse,
  type MetricQueryParams,
} from "@/lib/api/metrics";
import { getServices } from "@/lib/api/services";
import { getServiceMetrics } from "@/lib/api/services";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import { Button } from "@/components/Button";
import { Loader2, Calendar, Search } from "lucide-react";
import toast from "react-hot-toast";

const metricQuerySchema = z.object({
  metricName: z.string().min(1, "Metric name is required"),
  aggregation: z.enum(["AVG", "SUM", "MIN", "MAX", "P95", "P99"]).optional(),
  rollupPeriod: z.string().optional(),
  serviceId: z.string().optional(),
});

type MetricQueryFormData = z.infer<typeof metricQuerySchema>;

export default function MetricsPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();
  const searchParams = useSearchParams();

  const [data, setData] = useState<MetricQueryResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [services, setServices] = useState<Array<{ id: string; name: string }>>(
    []
  );
  const [availableMetrics, setAvailableMetrics] = useState<string[]>([]);
  const [timeRange, setTimeRange] = useState<{ start: Date; end: Date }>({
    start: new Date(Date.now() - 12 * 60 * 60 * 1000), // 12 hours ago
    end: new Date(),
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    watch,
    setValue,
  } = useForm<MetricQueryFormData>({
    resolver: zodResolver(metricQuerySchema),
    defaultValues: {
      metricName: searchParams.get("metric") || "",
      serviceId: searchParams.get("serviceId") || "",
      aggregation: "AVG",
      rollupPeriod: "5m",
    },
  });

  const selectedServiceId = watch("serviceId");

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (project && user) {
      loadServices();
    }
  }, [project, user]);

  useEffect(() => {
    if (project && selectedServiceId) {
      loadAvailableMetrics(selectedServiceId);
    } else {
      setAvailableMetrics([]);
    }
  }, [project, selectedServiceId]);

  const loadServices = async () => {
    if (!project) return;
    try {
      const data = await getServices(project.id);
      setServices(data.map((s) => ({ id: s.id, name: s.name })));
    } catch (error) {
      // Silently fail - services filter is optional
    }
  };

  const loadAvailableMetrics = async (serviceId: string) => {
    if (!project) return;
    try {
      const metrics = await getServiceMetrics(project.id, serviceId);
      setAvailableMetrics(metrics);
    } catch (error) {
      // Silently fail - metrics will be empty
    }
  };

  const handleQuery = async (formData: MetricQueryFormData) => {
    if (!project) return;
    try {
      setLoading(true);
      const params: MetricQueryParams = {
        metricName: formData.metricName,
        startTime: timeRange.start.toISOString(),
        endTime: timeRange.end.toISOString(),
        aggregation: formData.aggregation,
        rollupPeriod: formData.rollupPeriod,
        serviceId: formData.serviceId || undefined,
      };
      const result = await queryMetric(project.id, params);
      setData(result);
    } catch (error: any) {
      toast.error(error.message || "Failed to query metrics");
    } finally {
      setLoading(false);
    }
  };

  const handleTimeRangeChange = (hours: number) => {
    const end = new Date();
    const start = new Date(end.getTime() - hours * 60 * 60 * 1000);
    setTimeRange({ start, end });
  };

  if (authLoading || projectLoading) {
    return (
      <div className="min-h-screen bg-background text-foreground">
        <AppNavigation />
        <main className="pt-[112px] flex items-center justify-center min-h-[calc(100vh-112px)]">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </main>
      </div>
    );
  }

  if (!user || !project) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">Metrics Explorer</h1>
            <p className="text-muted-foreground">
              Query and visualize time-series metrics
            </p>
          </div>

          {/* Query Form */}
          <div className="bg-[#0A0A0A] border border-border rounded-lg p-6 mb-6">
            <form onSubmit={handleSubmit(handleQuery)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label
                    htmlFor="serviceId"
                    className="block text-sm font-medium mb-2"
                  >
                    Service (Optional)
                  </label>
                  <select
                    id="serviceId"
                    {...register("serviceId")}
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                  >
                    <option value="">All Services</option>
                    {services.map((service) => (
                      <option key={service.id} value={service.id}>
                        {service.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label
                    htmlFor="metricName"
                    className="block text-sm font-medium mb-2"
                  >
                    Metric Name *
                  </label>
                  {selectedServiceId && availableMetrics.length > 0 ? (
                    <select
                      id="metricName"
                      {...register("metricName")}
                      className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                    >
                      <option value="">Select a metric</option>
                      {availableMetrics.map((metric) => (
                        <option key={metric} value={metric}>
                          {metric}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <input
                      id="metricName"
                      type="text"
                      {...register("metricName")}
                      placeholder="e.g., http_server_duration"
                      className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                    />
                  )}
                  {errors.metricName && (
                    <p className="mt-1 text-sm text-destructive">
                      {errors.metricName.message}
                    </p>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="aggregation"
                    className="block text-sm font-medium mb-2"
                  >
                    Aggregation
                  </label>
                  <select
                    id="aggregation"
                    {...register("aggregation")}
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                  >
                    <option value="AVG">Average</option>
                    <option value="SUM">Sum</option>
                    <option value="MIN">Minimum</option>
                    <option value="MAX">Maximum</option>
                    <option value="P95">95th Percentile</option>
                    <option value="P99">99th Percentile</option>
                  </select>
                </div>

                <div>
                  <label
                    htmlFor="rollupPeriod"
                    className="block text-sm font-medium mb-2"
                  >
                    Rollup Period
                  </label>
                  <select
                    id="rollupPeriod"
                    {...register("rollupPeriod")}
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                  >
                    <option value="1m">1 minute</option>
                    <option value="5m">5 minutes</option>
                    <option value="15m">15 minutes</option>
                    <option value="1h">1 hour</option>
                    <option value="6h">6 hours</option>
                    <option value="24h">24 hours</option>
                  </select>
                </div>
              </div>

              {/* Time Range */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Time Range
                </label>
                <div className="flex items-center gap-2 flex-wrap">
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(1)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 1 hour
                  </button>
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(6)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 6 hours
                  </button>
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(12)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 12 hours
                  </button>
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(24)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 24 hours
                  </button>
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(168)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 7 days
                  </button>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground ml-4">
                    <Calendar className="w-4 h-4" />
                    <span>
                      {timeRange.start.toLocaleString()} -{" "}
                      {timeRange.end.toLocaleString()}
                    </span>
                  </div>
                </div>
              </div>

              <div className="flex justify-end">
                <Button
                  type="submit"
                  variant="primary"
                  disabled={isSubmitting || loading}
                  icon={Search}
                >
                  {loading ? "Querying..." : "Query Metrics"}
                </Button>
              </div>
            </form>
          </div>

          {/* Chart */}
          {data && (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
              <div className="mb-4">
                <h2 className="text-lg font-semibold mb-2">
                  {data.metadata.metricName}
                </h2>
                <p className="text-sm text-muted-foreground">
                  {data.metadata.aggregation} • {data.metadata.rollupPeriod}
                </p>
              </div>
              {data.dataPoints.length > 0 ? (
                <ResponsiveContainer width="100%" height={400}>
                  <LineChart data={data.dataPoints}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#2A2A2C" />
                    <XAxis
                      dataKey="timestamp"
                      stroke="#A1A1AA"
                      fontSize={12}
                      tick={{ fill: "#A1A1AA" }}
                      tickFormatter={(value) =>
                        new Date(value).toLocaleTimeString()
                      }
                    />
                    <YAxis
                      stroke="#A1A1AA"
                      fontSize={12}
                      tick={{ fill: "#A1A1AA" }}
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "#1A1B1E",
                        border: "1px solid #2A2A2C",
                        borderRadius: "0.5rem",
                      }}
                      labelStyle={{ color: "#FFFFFF" }}
                      formatter={(value: number) => [value.toFixed(2), "Value"]}
                      labelFormatter={(label) =>
                        new Date(label).toLocaleString()
                      }
                    />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="value"
                      stroke="#5B78FF"
                      strokeWidth={2}
                      dot={false}
                      name="Value"
                    />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex items-center justify-center py-12 text-muted-foreground">
                  No data points available for the selected time range
                </div>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
