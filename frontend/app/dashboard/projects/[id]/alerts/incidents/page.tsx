"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  getIncidents,
  updateIncident,
  type Incident,
} from "@/lib/api/incidents";
import { Button } from "@/components/Button";
import {
  Loader2,
  AlertCircle,
  CheckCircle,
  XCircle,
  Clock,
} from "lucide-react";
import toast from "react-hot-toast";
import Link from "next/link";

export default function IncidentsPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();

  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [severityFilter, setSeverityFilter] = useState<string>("");

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (project && user) {
      loadIncidents();
    }
  }, [project, user, statusFilter, severityFilter]);

  const loadIncidents = async () => {
    if (!project) return;
    try {
      setLoading(true);
      const data = await getIncidents({
        projectId: project.id,
        status: statusFilter || undefined,
        severity: severityFilter || undefined,
      });
      setIncidents(data);
    } catch (error: any) {
      toast.error(error.message || "Failed to load incidents");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (
    incidentId: string,
    newStatus: Incident["status"]
  ) => {
    try {
      await updateIncident(incidentId, { status: newStatus });
      toast.success("Incident status updated");
      await loadIncidents();
    } catch (error: any) {
      toast.error(error.message || "Failed to update incident");
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

  const getStatusIcon = (status: Incident["status"]) => {
    switch (status) {
      case "OPEN":
        return <AlertCircle className="w-4 h-4 text-destructive" />;
      case "ACKNOWLEDGED":
        return <Clock className="w-4 h-4 text-yellow-500" />;
      case "RESOLVED":
        return <CheckCircle className="w-4 h-4 text-primary" />;
      case "CLOSED":
        return <XCircle className="w-4 h-4 text-muted-foreground" />;
    }
  };

  const getSeverityColor = (severity: Incident["severity"]) => {
    switch (severity) {
      case "CRITICAL":
        return "bg-destructive/20 text-destructive border-destructive/50";
      case "HIGH":
        return "bg-orange-500/20 text-orange-500 border-orange-500/50";
      case "MEDIUM":
        return "bg-yellow-500/20 text-yellow-500 border-yellow-500/50";
      case "LOW":
        return "bg-blue-500/20 text-blue-500 border-blue-500/50";
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">Incidents</h1>
            <p className="text-muted-foreground">
              Monitor and manage incidents for {project.name}
            </p>
          </div>

          {/* Filters */}
          <div className="bg-[#0A0A0A] border border-border rounded-lg p-4 mb-6">
            <div className="flex items-center gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">Status</label>
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="bg-secondary border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                >
                  <option value="">All Statuses</option>
                  <option value="OPEN">Open</option>
                  <option value="ACKNOWLEDGED">Acknowledged</option>
                  <option value="RESOLVED">Resolved</option>
                  <option value="CLOSED">Closed</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">
                  Severity
                </label>
                <select
                  value={severityFilter}
                  onChange={(e) => setSeverityFilter(e.target.value)}
                  className="bg-secondary border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                >
                  <option value="">All Severities</option>
                  <option value="CRITICAL">Critical</option>
                  <option value="HIGH">High</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="LOW">Low</option>
                </select>
              </div>
            </div>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-primary" />
            </div>
          ) : incidents.length === 0 ? (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-12 text-center">
              <p className="text-muted-foreground">No incidents found</p>
            </div>
          ) : (
            <div className="space-y-4">
              {incidents.map((incident) => (
                <Link
                  key={incident.id}
                  href={`/dashboard/projects/${project.id}/alerts/incidents/${incident.id}`}
                  className="block"
                >
                  <div className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          {getStatusIcon(incident.status)}
                          <h3 className="text-lg font-semibold">
                            {incident.title}
                          </h3>
                          <span
                            className={`px-2 py-1 text-xs rounded border ${getSeverityColor(
                              incident.severity
                            )}`}
                          >
                            {incident.severity}
                          </span>
                          <span className="px-2 py-1 text-xs bg-secondary text-muted-foreground rounded">
                            {incident.status}
                          </span>
                        </div>
                        {incident.description && (
                          <p className="text-sm text-muted-foreground mb-4">
                            {incident.description}
                          </p>
                        )}
                        <div className="flex items-center gap-4 text-xs text-muted-foreground">
                          <span>
                            Triggered:{" "}
                            {new Date(incident.triggeredAt).toLocaleString()}
                          </span>
                          {incident.acknowledgedAt && (
                            <span>
                              Acknowledged:{" "}
                              {new Date(
                                incident.acknowledgedAt
                              ).toLocaleString()}
                            </span>
                          )}
                          {incident.resolvedAt && (
                            <span>
                              Resolved:{" "}
                              {new Date(incident.resolvedAt).toLocaleString()}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2 ml-4">
                        {incident.status === "OPEN" && (
                          <Button
                            variant="secondary"
                            onClick={(e) => {
                              e.preventDefault();
                              handleStatusChange(incident.id, "ACKNOWLEDGED");
                            }}
                          >
                            Acknowledge
                          </Button>
                        )}
                        {incident.status !== "RESOLVED" &&
                          incident.status !== "CLOSED" && (
                            <Button
                              variant="primary"
                              onClick={(e) => {
                                e.preventDefault();
                                handleStatusChange(incident.id, "RESOLVED");
                              }}
                            >
                              Resolve
                            </Button>
                          )}
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
