"use client";

import React, { useEffect } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter, useParams } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import { Loader2, AlertTriangle, Bell } from "lucide-react";

export default function AlertsOverviewPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();
  const params = useParams();
  const projectId = params.id as string;

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

  if (!user || !project) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">Alerts</h1>
            <p className="text-muted-foreground">
              Manage alert rules and incidents for {project.name}
            </p>
          </div>

          {/* Tabs */}
          <div className="flex gap-2 mb-6 border-b border-border">
            <button
              onClick={() =>
                router.push(`/dashboard/projects/${projectId}/alerts/rules`)
              }
              className="px-4 py-2 font-medium transition-colors border-b-2 border-primary text-primary"
            >
              Alert Rules
            </button>
            <button
              onClick={() =>
                router.push(`/dashboard/projects/${projectId}/alerts/incidents`)
              }
              className="px-4 py-2 font-medium transition-colors text-muted-foreground hover:text-foreground"
            >
              Incidents
            </button>
          </div>

          {/* Overview Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <button
              onClick={() =>
                router.push(`/dashboard/projects/${projectId}/alerts/rules`)
              }
              className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all text-left"
            >
              <div className="flex items-center gap-4 mb-4">
                <div className="p-3 bg-primary/10 rounded-lg">
                  <Bell className="w-6 h-6 text-primary" />
                </div>
                <div>
                  <h3 className="font-semibold text-lg">Alert Rules</h3>
                  <p className="text-sm text-muted-foreground">
                    Configure alert conditions
                  </p>
                </div>
              </div>
              <p className="text-sm text-muted-foreground">
                Create and manage alert rules that trigger incidents when
                conditions are met.
              </p>
            </button>

            <button
              onClick={() =>
                router.push(`/dashboard/projects/${projectId}/alerts/incidents`)
              }
              className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all text-left"
            >
              <div className="flex items-center gap-4 mb-4">
                <div className="p-3 bg-destructive/10 rounded-lg">
                  <AlertTriangle className="w-6 h-6 text-destructive" />
                </div>
                <div>
                  <h3 className="font-semibold text-lg">Incidents</h3>
                  <p className="text-sm text-muted-foreground">
                    View and manage incidents
                  </p>
                </div>
              </div>
              <p className="text-sm text-muted-foreground">
                Monitor active incidents, add notes, and resolve alerts
                triggered by your rules.
              </p>
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
