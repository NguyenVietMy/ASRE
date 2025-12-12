"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  getServices,
  getServiceOverview,
  type Service,
  type ServiceOverview,
} from "@/lib/api/services";
import { Loader2, ExternalLink, Activity } from "lucide-react";
import toast from "react-hot-toast";

export default function ServicesPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();
  const [services, setServices] = useState<Service[]>([]);
  const [loading, setLoading] = useState(true);

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

  const loadServices = async () => {
    if (!project) return;
    try {
      setLoading(true);
      const data = await getServices(project.id);
      setServices(data);
    } catch (error: any) {
      toast.error(error.message || "Failed to load services");
    } finally {
      setLoading(false);
    }
  };

  const handleServiceClick = (service: Service) => {
    router.push(`/dashboard/projects/${project?.id}/services/${service.id}`);
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

  if (!user) {
    return null;
  }

  if (!project) {
    return (
      <div className="min-h-screen bg-background text-foreground">
        <AppNavigation />
        <main className="pt-[112px]">
          <div className="max-w-[1920px] mx-auto px-6 py-8">
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-12 text-center">
              <p className="text-muted-foreground mb-4">
                Please select a project first.
              </p>
              <button
                onClick={() => router.push("/dashboard/projects")}
                className="text-primary hover:text-primary/80"
              >
                Go to Projects
              </button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">Services</h1>
            <p className="text-muted-foreground">
              Services discovered in {project.name}
            </p>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-primary" />
            </div>
          ) : services.length === 0 ? (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-12 text-center">
              <Activity className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-2">
                No services found yet.
              </p>
              <p className="text-sm text-muted-foreground">
                Services are automatically discovered when you start ingesting
                metrics or logs.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {services.map((service) => (
                <ServiceCard
                  key={service.id}
                  service={service}
                  projectId={project.id}
                  onClick={() => handleServiceClick(service)}
                />
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

interface ServiceCardProps {
  service: Service;
  projectId: string;
  onClick: () => void;
}

function ServiceCard({ service, projectId, onClick }: ServiceCardProps) {
  const [overview, setOverview] = useState<ServiceOverview | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOverview();
  }, [service.id, projectId]);

  const loadOverview = async () => {
    try {
      setLoading(true);
      const data = await getServiceOverview(projectId, service.id);
      setOverview(data);
    } catch (error) {
      // Error handling is done via toast in parent component
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      onClick={onClick}
      className="bg-[#0A0A0A] border border-border rounded-lg p-6 cursor-pointer transition-all hover:border-primary/50 group"
    >
      <div className="flex items-start justify-between mb-4">
        <div className="flex-1">
          <h3 className="text-lg font-semibold mb-1">{service.name}</h3>
          <p className="text-xs text-muted-foreground">
            Created {new Date(service.createdAt).toLocaleDateString()}
          </p>
        </div>
        <ExternalLink className="w-4 h-4 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity" />
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-4">
          <Loader2 className="w-4 h-4 animate-spin text-primary" />
        </div>
      ) : overview ? (
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-muted-foreground mb-1">Metrics</p>
            <p className="text-lg font-semibold">
              {overview.metrics.totalMetrics}
            </p>
          </div>
          <div>
            <p className="text-muted-foreground mb-1">Logs</p>
            <p className="text-lg font-semibold">
              {overview.metrics.totalLogs}
            </p>
          </div>
        </div>
      ) : null}
    </div>
  );
}
