"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  getAlertRules,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  type AlertRule,
} from "@/lib/api/alerts";
import { getServices } from "@/lib/api/services";
import { Button } from "@/components/Button";
import {
  Loader2,
  Plus,
  Edit,
  Trash2,
  ToggleLeft,
  ToggleRight,
} from "lucide-react";
import toast from "react-hot-toast";
import { CreateAlertRuleDialog } from "@/components/alerts/CreateAlertRuleDialog";
import { EditAlertRuleDialog } from "@/components/alerts/EditAlertRuleDialog";

export default function AlertRulesPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();

  const [rules, setRules] = useState<AlertRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [services, setServices] = useState<Array<{ id: string; name: string }>>(
    []
  );
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editRule, setEditRule] = useState<AlertRule | null>(null);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (project && user) {
      loadData();
    }
  }, [project, user]);

  const loadData = async () => {
    if (!project) return;
    try {
      setLoading(true);
      const [rulesData, servicesData] = await Promise.all([
        getAlertRules(project.id),
        getServices(project.id),
      ]);
      setRules(rulesData);
      setServices(servicesData.map((s) => ({ id: s.id, name: s.name })));
    } catch (error: any) {
      toast.error(error.message || "Failed to load alert rules");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRule = async (data: any) => {
    if (!project) return;
    try {
      const newRule = await createAlertRule({
        ...data,
        projectId: project.id,
      });
      toast.success("Alert rule created successfully");
      setRules((prev) => [newRule, ...prev]);
      setCreateDialogOpen(false);
    } catch (error: any) {
      toast.error(error.message || "Failed to create alert rule");
      throw error;
    }
  };

  const handleUpdateRule = async (id: string, data: any) => {
    try {
      const updatedRule = await updateAlertRule(id, data);
      toast.success("Alert rule updated successfully");
      setRules((prev) => prev.map((r) => (r.id === id ? updatedRule : r)));
      setEditRule(null);
    } catch (error: any) {
      toast.error(error.message || "Failed to update alert rule");
      throw error;
    }
  };

  const handleDeleteRule = async (id: string) => {
    if (!confirm("Are you sure you want to delete this alert rule?")) {
      return;
    }
    try {
      await deleteAlertRule(id);
      toast.success("Alert rule deleted successfully");
      setRules((prev) => prev.filter((r) => r.id !== id));
    } catch (error: any) {
      toast.error(error.message || "Failed to delete alert rule");
    }
  };

  const handleToggleRule = async (rule: AlertRule) => {
    try {
      await updateAlertRule(rule.id, { enabled: !rule.enabled });
      toast.success(`Alert rule ${rule.enabled ? "disabled" : "enabled"}`);
      setRules((prev) =>
        prev.map((r) => (r.id === rule.id ? { ...r, enabled: !r.enabled } : r))
      );
    } catch (error: any) {
      toast.error(error.message || "Failed to toggle alert rule");
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
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-3xl font-semibold mb-2">Alert Rules</h1>
              <p className="text-muted-foreground">
                Manage alert rules for {project.name}
              </p>
            </div>
            <Button
              variant="primary"
              icon={Plus}
              onClick={() => setCreateDialogOpen(true)}
            >
              Create Rule
            </Button>
          </div>

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="w-8 h-8 animate-spin text-primary" />
            </div>
          ) : rules.length === 0 ? (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-12 text-center">
              <p className="text-muted-foreground mb-4">
                No alert rules yet. Create your first rule to get started.
              </p>
              <Button
                variant="primary"
                icon={Plus}
                onClick={() => setCreateDialogOpen(true)}
              >
                Create Alert Rule
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              {rules.map((rule) => (
                <div
                  key={rule.id}
                  className="bg-[#0A0A0A] border border-border rounded-lg p-6"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <h3 className="text-lg font-semibold">{rule.name}</h3>
                        <span
                          className={`px-2 py-1 text-xs rounded ${
                            rule.severity === "CRITICAL"
                              ? "bg-destructive/20 text-destructive"
                              : rule.severity === "HIGH"
                              ? "bg-orange-500/20 text-orange-500"
                              : rule.severity === "MEDIUM"
                              ? "bg-yellow-500/20 text-yellow-500"
                              : "bg-blue-500/20 text-blue-500"
                          }`}
                        >
                          {rule.severity}
                        </span>
                        {rule.enabled ? (
                          <span className="px-2 py-1 text-xs bg-primary/20 text-primary rounded">
                            Active
                          </span>
                        ) : (
                          <span className="px-2 py-1 text-xs bg-muted text-muted-foreground rounded">
                            Disabled
                          </span>
                        )}
                      </div>
                      {rule.description && (
                        <p className="text-sm text-muted-foreground mb-4">
                          {rule.description}
                        </p>
                      )}
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                        <div>
                          <p className="text-muted-foreground mb-1">Metric</p>
                          <p className="font-medium">{rule.metricName}</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground mb-1">
                            Condition
                          </p>
                          <p className="font-medium">
                            {rule.condition.operator} {rule.condition.threshold}{" "}
                            for {rule.condition.duration}
                          </p>
                        </div>
                        {rule.serviceId && (
                          <div>
                            <p className="text-muted-foreground mb-1">
                              Service
                            </p>
                            <p className="font-medium">
                              {services.find((s) => s.id === rule.serviceId)
                                ?.name || rule.serviceId}
                            </p>
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-2 ml-4">
                      <button
                        onClick={() => handleToggleRule(rule)}
                        className="p-2 hover:bg-secondary rounded-lg transition-colors"
                        title={rule.enabled ? "Disable" : "Enable"}
                      >
                        {rule.enabled ? (
                          <ToggleRight className="w-5 h-5 text-primary" />
                        ) : (
                          <ToggleLeft className="w-5 h-5 text-muted-foreground" />
                        )}
                      </button>
                      <button
                        onClick={() => setEditRule(rule)}
                        className="p-2 hover:bg-secondary rounded-lg transition-colors"
                        title="Edit"
                      >
                        <Edit className="w-4 h-4 text-muted-foreground" />
                      </button>
                      <button
                        onClick={() => handleDeleteRule(rule.id)}
                        className="p-2 hover:bg-destructive/20 rounded-lg transition-colors"
                        title="Delete"
                      >
                        <Trash2 className="w-4 h-4 text-destructive" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      <CreateAlertRuleDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSubmit={handleCreateRule}
        services={services}
      />

      {editRule && (
        <EditAlertRuleDialog
          open={!!editRule}
          onClose={() => setEditRule(null)}
          onSubmit={(data) => handleUpdateRule(editRule.id, data)}
          rule={editRule}
          services={services}
        />
      )}
    </div>
  );
}
