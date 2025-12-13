"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter, useParams } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  getService,
  getServiceOverview,
  getServiceMetrics,
  type Service,
  type ServiceOverview,
} from "@/lib/api/services";
import { Loader2, ArrowLeft } from "lucide-react";
import toast from "react-hot-toast";

export default function ServiceDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();
  const params = useParams();
  const serviceId = params.serviceId as string;

  const [service, setService] = useState<Service | null>(null);
  const [overview, setOverview] = useState<ServiceOverview | null>(null);
  const [metrics, setMetrics] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (project && serviceId && user) {
      loadServiceData();
    }
  }, [project, serviceId, user]);

  const loadServiceData = async () => {
    if (!project) return;
    try {
      setLoading(true);
      const [serviceData, overviewData, metricsData] = await Promise.all([
        getService(project.id, serviceId),
        getServiceOverview(project.id, serviceId),
        getServiceMetrics(project.id, serviceId),
      ]);
      setService(serviceData);
      setOverview(overviewData);
      setMetrics(metricsData);
    } catch (error: any) {
      toast.error(error.message || "Failed to load service data");
    } finally {
      setLoading(false);
    }
  };

  if (authLoading || projectLoading || loading) {
    return (
      <div className="min-h-screen bg-background text-foreground">
        <AppNavigation />
        <main className="pt-[112px] flex items-center justify-center min-h-[calc(100vh-112px)]">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </main>
      </div>
    );
  }

  if (!user || !project || !service) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <button
            onClick={() =>
              router.push(`/dashboard/projects/${project.id}/services`)
            }
            className="flex items-center gap-2 text-muted-foreground hover:text-foreground mb-6 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Services
          </button>

          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">{service.name}</h1>
            <p className="text-muted-foreground">
              Service overview and metrics
            </p>
          </div>

          {overview && (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <p className="text-sm text-muted-foreground mb-2">
                  Open Incidents
                </p>
                <p className="text-3xl font-semibold">
                  {overview.openIncidents}
                </p>
              </div>
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <p className="text-sm text-muted-foreground mb-2">Error Rate</p>
                <p className="text-3xl font-semibold">
                  {overview.errorRate != null
                    ? `${(overview.errorRate * 100).toFixed(2)}%`
                    : "N/A"}
                </p>
              </div>
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <p className="text-sm text-muted-foreground mb-2">
                  P95 Latency
                </p>
                <p className="text-3xl font-semibold">
                  {overview.p95Latency != null
                    ? `${overview.p95Latency.toFixed(2)}ms`
                    : "N/A"}
                </p>
              </div>
            </div>
          )}

          {metrics.length > 0 && (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
              <h2 className="text-lg font-semibold mb-4">Available Metrics</h2>
              <div className="flex flex-wrap gap-2">
                {metrics.map((metric) => (
                  <button
                    key={metric}
                    onClick={() =>
                      router.push(
                        `/dashboard/projects/${
                          project.id
                        }/metrics?metric=${encodeURIComponent(
                          metric
                        )}&serviceId=${serviceId}`
                      )
                    }
                    className="px-3 py-1.5 bg-secondary border border-border rounded-lg text-sm hover:border-primary/50 transition-colors"
                  >
                    {metric}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
