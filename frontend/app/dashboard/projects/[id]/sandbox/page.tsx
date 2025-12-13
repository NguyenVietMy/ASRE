"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter, useParams } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import { Button } from "@/components/Button";
import { Loader2 } from "lucide-react";
import toast from "react-hot-toast";

interface MetricInput {
  name: string;
  value: number;
  serviceId: string;
  timestamp: string;
  tags: Record<string, string>;
}

interface LogInput {
  level: string;
  message: string;
  serviceId: string;
  timestamp: string;
  traceId?: string;
  context?: Record<string, any>;
}

export default function SandboxPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();
  const params = useParams();
  const projectId = params.id as string;

  const [apiKey, setApiKey] = useState<string>("");
  const [showApiKeyInput, setShowApiKeyInput] = useState(false);
  const [activeTab, setActiveTab] = useState<"metrics" | "logs">("metrics");
  const [submitting, setSubmitting] = useState(false);

  // Metrics form state
  const [metrics, setMetrics] = useState<MetricInput[]>([
    {
      name: "cpu.usage",
      value: 75.5,
      serviceId: "",
      timestamp: new Date().toISOString(),
      tags: { environment: "production" },
    },
  ]);

  // Logs form state
  const [logs, setLogs] = useState<LogInput[]>([
    {
      level: "INFO",
      message: "Sample log message",
      serviceId: "",
      timestamp: new Date().toISOString(),
      traceId: undefined,
      context: { userId: "123" },
    },
  ]);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  const handleAddMetric = () => {
    setMetrics([
      ...metrics,
      {
        name: "",
        value: 0,
        serviceId: "",
        timestamp: new Date().toISOString(),
        tags: {},
      },
    ]);
  };

  const handleRemoveMetric = (index: number) => {
    setMetrics(metrics.filter((_, i) => i !== index));
  };

  const handleMetricChange = (
    index: number,
    field: keyof MetricInput,
    value: any
  ) => {
    const updated = [...metrics];
    if (field === "tags") {
      updated[index].tags = value;
    } else {
      (updated[index] as any)[field] = value;
    }
    setMetrics(updated);
  };

  const handleAddLog = () => {
    setLogs([
      ...logs,
      {
        level: "INFO",
        message: "",
        serviceId: "",
        timestamp: new Date().toISOString(),
      },
    ]);
  };

  const handleRemoveLog = (index: number) => {
    setLogs(logs.filter((_, i) => i !== index));
  };

  const handleLogChange = (
    index: number,
    field: keyof LogInput,
    value: any
  ) => {
    const updated = [...logs];
    (updated[index] as any)[field] = value;
    setLogs(updated);
  };

  const handleSubmitMetrics = async () => {
    if (!projectId || projectId === "undefined") {
      toast.error("Project ID is missing");
      return;
    }

    if (!apiKey || apiKey.trim() === "") {
      toast.error("Please enter your API key");
      return;
    }

    // Validate metrics
    const validMetrics = metrics.filter((m) => m.name && m.value !== undefined);

    if (validMetrics.length === 0) {
      toast.error("Please add at least one valid metric");
      return;
    }

    try {
      setSubmitting(true);

      const payload = {
        metrics: validMetrics.map((m) => ({
          name: m.name,
          value: m.value,
          serviceId: m.serviceId || null,
          timestamp: m.timestamp || new Date().toISOString(),
          tags: m.tags || {},
        })),
      };

      // Use API key authentication
      const response = await fetch(
        `${
          process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
        }/api/ingest/metrics`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "X-API-Key": apiKey.trim(),
          },
          body: JSON.stringify(payload),
        }
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP ${response.status}`);
      }

      toast.success(`Successfully submitted ${validMetrics.length} metric(s)`);
      // Reset form
      setMetrics([
        {
          name: "cpu.usage",
          value: 75.5,
          serviceId: "",
          timestamp: new Date().toISOString(),
          tags: { environment: "production" },
        },
      ]);
    } catch (error: any) {
      toast.error(error.message || "Failed to submit metrics");
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmitLogs = async () => {
    if (!projectId || projectId === "undefined") {
      toast.error("Project ID is missing");
      return;
    }

    if (!apiKey || apiKey.trim() === "") {
      toast.error("Please enter your API key");
      return;
    }

    const validLogs = logs.filter((l) => l.level && l.message);

    if (validLogs.length === 0) {
      toast.error("Please add at least one valid log");
      return;
    }

    try {
      setSubmitting(true);

      const payload = {
        logs: validLogs.map((l) => ({
          level: l.level,
          message: l.message,
          serviceId: l.serviceId && l.serviceId.trim() ? l.serviceId : null,
          timestamp: l.timestamp || new Date().toISOString(),
          traceId: l.traceId && l.traceId.trim() ? l.traceId : null,
          context: l.context || null,
        })),
      };

      // Use API key authentication
      const response = await fetch(
        `${
          process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
        }/api/ingest/logs`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "X-API-Key": apiKey.trim(),
          },
          body: JSON.stringify(payload),
        }
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP ${response.status}`);
      }

      toast.success(`Successfully submitted ${validLogs.length} log(s)`);
      // Reset form
      setLogs([
        {
          level: "INFO",
          message: "Sample log message",
          serviceId: "",
          timestamp: new Date().toISOString(),
        },
      ]);
    } catch (error: any) {
      toast.error(error.message || "Failed to submit logs");
    } finally {
      setSubmitting(false);
    }
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
        <div className="max-w-6xl mx-auto px-6 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">Sandbox</h1>
            <p className="text-muted-foreground">
              Test metrics and logs ingestion for {project.name}
            </p>
          </div>

          {/* API Key Input */}
          <div className="bg-[#0A0A0A] border border-border rounded-lg p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold">API Key</h2>
              <button
                onClick={() => setShowApiKeyInput(!showApiKeyInput)}
                className="text-sm text-primary hover:text-primary/80"
              >
                {showApiKeyInput ? "Hide" : "Show"} Input
              </button>
            </div>
            <p className="text-sm text-muted-foreground mb-4">
              Enter your API key from Project Settings. The key is only used for
              this session and is not stored.
            </p>
            {showApiKeyInput && (
              <div className="space-y-2">
                <input
                  type="password"
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder="Paste your API key here (asre_sk_...)"
                  className="w-full bg-secondary border border-border rounded-lg px-4 py-3 font-mono text-sm"
                />
                <p className="text-xs text-muted-foreground">
                  Get your API key from Project Settings → API Key section
                </p>
              </div>
            )}
          </div>

          {/* Tabs */}
          <div className="flex gap-2 mb-6 border-b border-border">
            <button
              onClick={() => setActiveTab("metrics")}
              className={`px-4 py-2 font-medium transition-colors ${
                activeTab === "metrics"
                  ? "border-b-2 border-primary text-primary"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Metrics
            </button>
            <button
              onClick={() => setActiveTab("logs")}
              className={`px-4 py-2 font-medium transition-colors ${
                activeTab === "logs"
                  ? "border-b-2 border-primary text-primary"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Logs
            </button>
          </div>

          {/* Metrics Form */}
          {activeTab === "metrics" && (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold">Submit Metrics</h2>
                <Button variant="secondary" onClick={handleAddMetric}>
                  Add Metric
                </Button>
              </div>

              <div className="space-y-4 mb-6">
                {metrics.map((metric, index) => (
                  <div
                    key={index}
                    className="bg-secondary border border-border rounded-lg p-4"
                  >
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="font-medium">Metric {index + 1}</h3>
                      {metrics.length > 1 && (
                        <button
                          onClick={() => handleRemoveMetric(index)}
                          className="text-destructive hover:text-destructive/80 text-sm"
                        >
                          Remove
                        </button>
                      )}
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Name *
                        </label>
                        <input
                          type="text"
                          value={metric.name}
                          onChange={(e) =>
                            handleMetricChange(index, "name", e.target.value)
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                          placeholder="cpu.usage"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Value *
                        </label>
                        <input
                          type="number"
                          step="0.01"
                          value={metric.value}
                          onChange={(e) =>
                            handleMetricChange(
                              index,
                              "value",
                              parseFloat(e.target.value)
                            )
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Service ID
                        </label>
                        <input
                          type="text"
                          value={metric.serviceId}
                          onChange={(e) =>
                            handleMetricChange(
                              index,
                              "serviceId",
                              e.target.value
                            )
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                          placeholder="Leave empty for auto-discovery"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Timestamp
                        </label>
                        <input
                          type="datetime-local"
                          value={
                            metric.timestamp
                              ? new Date(metric.timestamp)
                                  .toISOString()
                                  .slice(0, 16)
                              : ""
                          }
                          onChange={(e) =>
                            handleMetricChange(
                              index,
                              "timestamp",
                              new Date(e.target.value).toISOString()
                            )
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="flex justify-end">
                <Button
                  variant="primary"
                  onClick={handleSubmitMetrics}
                  disabled={submitting}
                >
                  {submitting ? "Submitting..." : "Submit Metrics"}
                </Button>
              </div>
            </div>
          )}

          {/* Logs Form */}
          {activeTab === "logs" && (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold">Submit Logs</h2>
                <Button variant="secondary" onClick={handleAddLog}>
                  Add Log
                </Button>
              </div>

              <div className="space-y-4 mb-6">
                {logs.map((log, index) => (
                  <div
                    key={index}
                    className="bg-secondary border border-border rounded-lg p-4"
                  >
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="font-medium">Log {index + 1}</h3>
                      {logs.length > 1 && (
                        <button
                          onClick={() => handleRemoveLog(index)}
                          className="text-destructive hover:text-destructive/80 text-sm"
                        >
                          Remove
                        </button>
                      )}
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Level *
                        </label>
                        <select
                          value={log.level}
                          onChange={(e) =>
                            handleLogChange(index, "level", e.target.value)
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                        >
                          <option value="DEBUG">DEBUG</option>
                          <option value="INFO">INFO</option>
                          <option value="WARN">WARN</option>
                          <option value="ERROR">ERROR</option>
                          <option value="FATAL">FATAL</option>
                        </select>
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Service ID
                        </label>
                        <input
                          type="text"
                          value={log.serviceId}
                          onChange={(e) =>
                            handleLogChange(index, "serviceId", e.target.value)
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                          placeholder="Leave empty for auto-discovery"
                        />
                      </div>
                      <div className="col-span-2">
                        <label className="block text-sm font-medium mb-2">
                          Message *
                        </label>
                        <textarea
                          value={log.message}
                          onChange={(e) =>
                            handleLogChange(index, "message", e.target.value)
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                          rows={3}
                          placeholder="Log message"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Trace ID
                        </label>
                        <input
                          type="text"
                          value={log.traceId || ""}
                          onChange={(e) =>
                            handleLogChange(index, "traceId", e.target.value)
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                          placeholder="Optional"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-2">
                          Timestamp
                        </label>
                        <input
                          type="datetime-local"
                          value={
                            log.timestamp
                              ? new Date(log.timestamp)
                                  .toISOString()
                                  .slice(0, 16)
                              : ""
                          }
                          onChange={(e) =>
                            handleLogChange(
                              index,
                              "timestamp",
                              new Date(e.target.value).toISOString()
                            )
                          }
                          className="w-full bg-background border border-border rounded-lg px-3 py-2"
                        />
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="flex justify-end">
                <Button
                  variant="primary"
                  onClick={handleSubmitLogs}
                  disabled={submitting}
                >
                  {submitting ? "Submitting..." : "Submit Logs"}
                </Button>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
