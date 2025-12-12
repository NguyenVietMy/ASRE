"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter, useParams } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  getIncident,
  updateIncident,
  addIncidentNote,
  type Incident,
  type IncidentNote,
} from "@/lib/api/incidents";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/Button";
import {
  Loader2,
  ArrowLeft,
  CheckCircle,
  XCircle,
  Clock,
  AlertCircle,
} from "lucide-react";
import toast from "react-hot-toast";

const noteSchema = z.object({
  content: z.string().min(1, "Note content is required"),
});

type NoteFormData = z.infer<typeof noteSchema>;

export default function IncidentDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();
  const params = useParams();
  const incidentId = params.incidentId as string;

  const [incident, setIncident] = useState<Incident | null>(null);
  const [notes, setNotes] = useState<IncidentNote[]>([]);
  const [loading, setLoading] = useState(true);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<NoteFormData>({
    resolver: zodResolver(noteSchema),
  });

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (project && incidentId && user) {
      loadIncident();
    }
  }, [project, incidentId, user]);

  const loadIncident = async () => {
    try {
      setLoading(true);
      const data = await getIncident(incidentId);
      setIncident(data);
      // Notes would come from the incident or a separate endpoint
      // For now, we'll assume they're in metadata or we'll add a separate endpoint later
    } catch (error: any) {
      toast.error(error.message || "Failed to load incident");
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus: Incident["status"]) => {
    if (!incident) return;
    try {
      const updated = await updateIncident(incident.id, { status: newStatus });
      setIncident(updated);
      toast.success("Incident status updated");
    } catch (error: any) {
      toast.error(error.message || "Failed to update incident");
    }
  };

  const handleAddNote = async (data: NoteFormData) => {
    if (!incident) return;
    try {
      const note = await addIncidentNote(incident.id, data);
      setNotes((prev) => [note, ...prev]);
      reset();
      toast.success("Note added");
    } catch (error: any) {
      toast.error(error.message || "Failed to add note");
      throw error;
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

  if (!user || !project || !incident) {
    return null;
  }

  const getStatusIcon = (status: Incident["status"]) => {
    switch (status) {
      case "OPEN":
        return <AlertCircle className="w-5 h-5 text-destructive" />;
      case "ACKNOWLEDGED":
        return <Clock className="w-5 h-5 text-yellow-500" />;
      case "RESOLVED":
        return <CheckCircle className="w-5 h-5 text-primary" />;
      case "CLOSED":
        return <XCircle className="w-5 h-5 text-muted-foreground" />;
    }
  };

  const timeline = [
    { event: "Triggered", time: incident.triggeredAt, icon: AlertCircle },
    incident.acknowledgedAt && {
      event: "Acknowledged",
      time: incident.acknowledgedAt,
      icon: Clock,
    },
    incident.resolvedAt && {
      event: "Resolved",
      time: incident.resolvedAt,
      icon: CheckCircle,
    },
    incident.closedAt && {
      event: "Closed",
      time: incident.closedAt,
      icon: XCircle,
    },
  ].filter(Boolean);

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <button
            onClick={() =>
              router.push(`/dashboard/projects/${project.id}/alerts/incidents`)
            }
            className="flex items-center gap-2 text-muted-foreground hover:text-foreground mb-6 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Incidents
          </button>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Main Content */}
            <div className="lg:col-span-2 space-y-6">
              {/* Incident Header */}
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    {getStatusIcon(incident.status)}
                    <div>
                      <h1 className="text-2xl font-semibold mb-1">
                        {incident.title}
                      </h1>
                      <p className="text-sm text-muted-foreground">
                        {incident.status} • {incident.severity}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {incident.status === "OPEN" && (
                      <Button
                        variant="secondary"
                        onClick={() => handleStatusChange("ACKNOWLEDGED")}
                      >
                        Acknowledge
                      </Button>
                    )}
                    {incident.status !== "RESOLVED" &&
                      incident.status !== "CLOSED" && (
                        <Button
                          variant="primary"
                          onClick={() => handleStatusChange("RESOLVED")}
                        >
                          Resolve
                        </Button>
                      )}
                  </div>
                </div>
                {incident.description && (
                  <p className="text-muted-foreground">
                    {incident.description}
                  </p>
                )}
              </div>

              {/* Timeline */}
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <h2 className="text-lg font-semibold mb-4">Timeline</h2>
                <div className="space-y-4">
                  {timeline.map((item: any, index) => (
                    <div key={index} className="flex items-start gap-4">
                      <div className="mt-1">
                        <item.icon className="w-4 h-4 text-muted-foreground" />
                      </div>
                      <div className="flex-1">
                        <p className="font-medium">{item.event}</p>
                        <p className="text-sm text-muted-foreground">
                          {new Date(item.time).toLocaleString()}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Notes */}
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <h2 className="text-lg font-semibold mb-4">Notes</h2>
                <form onSubmit={handleSubmit(handleAddNote)} className="mb-4">
                  <textarea
                    {...register("content")}
                    rows={3}
                    placeholder="Add a note..."
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all resize-none mb-2"
                  />
                  {errors.content && (
                    <p className="text-sm text-destructive mb-2">
                      {errors.content.message}
                    </p>
                  )}
                  <Button
                    type="submit"
                    variant="primary"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "Adding..." : "Add Note"}
                  </Button>
                </form>
                {notes.length === 0 ? (
                  <p className="text-muted-foreground text-sm">No notes yet</p>
                ) : (
                  <div className="space-y-4">
                    {notes.map((note) => (
                      <div
                        key={note.id}
                        className="bg-secondary border border-border rounded-lg p-4"
                      >
                        <p className="text-sm mb-2">{note.content}</p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(note.createdAt).toLocaleString()}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <h3 className="text-sm font-semibold mb-4">Details</h3>
                <div className="space-y-3 text-sm">
                  <div>
                    <p className="text-muted-foreground mb-1">Alert Rule ID</p>
                    <p className="font-mono text-xs">{incident.alertRuleId}</p>
                  </div>
                  {incident.serviceId && (
                    <div>
                      <p className="text-muted-foreground mb-1">Service</p>
                      <p>{incident.serviceId}</p>
                    </div>
                  )}
                  <div>
                    <p className="text-muted-foreground mb-1">Severity</p>
                    <p>{incident.severity}</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground mb-1">Status</p>
                    <p>{incident.status}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
