"use client";

import React, { useEffect } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter, useParams } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import { Loader2 } from "lucide-react";

export default function ProjectDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();
  const params = useParams();

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

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
                Project not found or you don't have access to it.
              </p>
              <button
                onClick={() => router.push("/dashboard/projects")}
                className="text-primary hover:text-primary/80"
              >
                Back to Projects
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
            <h1 className="text-3xl font-semibold mb-2">{project.name}</h1>
            {project.description && (
              <p className="text-muted-foreground">{project.description}</p>
            )}
          </div>

          {/* Quick Links */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <button
              onClick={() =>
                router.push(`/dashboard/projects/${project.id}/services`)
              }
              className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all text-left"
            >
              <h3 className="font-semibold mb-2">Services</h3>
              <p className="text-sm text-muted-foreground">
                View and manage services
              </p>
            </button>
            <button
              onClick={() =>
                router.push(`/dashboard/projects/${project.id}/metrics`)
              }
              className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all text-left"
            >
              <h3 className="font-semibold mb-2">Metrics</h3>
              <p className="text-sm text-muted-foreground">
                Explore metrics and charts
              </p>
            </button>
            <button
              onClick={() =>
                router.push(`/dashboard/projects/${project.id}/logs`)
              }
              className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all text-left"
            >
              <h3 className="font-semibold mb-2">Logs</h3>
              <p className="text-sm text-muted-foreground">
                Search and analyze logs
              </p>
            </button>
            <button
              onClick={() =>
                router.push(`/dashboard/projects/${project.id}/alerts`)
              }
              className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all text-left"
            >
              <h3 className="font-semibold mb-2">Alerts</h3>
              <p className="text-sm text-muted-foreground">
                Manage alert rules and incidents
              </p>
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
