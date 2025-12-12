"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter, useParams } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  updateProject,
  getApiKey,
  regenerateApiKey,
  getApiKeyUsage,
  type ApiKeyResponse,
  type ApiKeyUsage,
} from "@/lib/api/projects";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/Button";
import { Loader2, Copy, RefreshCw, Eye, EyeOff } from "lucide-react";
import toast from "react-hot-toast";

const updateProjectSchema = z.object({
  name: z
    .string()
    .min(1, "Project name is required")
    .min(3, "Project name must be at least 3 characters")
    .max(100, "Project name must be less than 100 characters"),
  description: z
    .string()
    .max(500, "Description must be less than 500 characters")
    .optional(),
});

type UpdateProjectFormData = z.infer<typeof updateProjectSchema>;

export default function ProjectSettingsPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading, refreshProject } = useProject();
  const router = useRouter();
  const params = useParams();
  const projectIdFromUrl = params.id as string;
  const [apiKey, setApiKey] = useState<ApiKeyResponse | null>(null);
  const [apiKeyUsage, setApiKeyUsage] = useState<ApiKeyUsage | null>(null);
  const [loadingApiKey, setLoadingApiKey] = useState(false);
  const [loadingUsage, setLoadingUsage] = useState(false);
  const [showApiKey, setShowApiKey] = useState(false);
  const [copied, setCopied] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<UpdateProjectFormData>({
    resolver: zodResolver(updateProjectSchema),
    defaultValues: {
      name: project?.name || "",
      description: project?.description || "",
    },
  });

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (project) {
      reset({
        name: project.name,
        description: project.description || "",
      });
      loadApiKey();
      loadApiKeyUsage();
    }
  }, [project, reset]);

  const loadApiKey = async () => {
    const projectId = project?.id || projectIdFromUrl;
    if (!projectId || projectId === "undefined") return;
    try {
      setLoadingApiKey(true);
      const data = await getApiKey(projectId);
      setApiKey(data);
    } catch (error: any) {
      toast.error(error.message || "Failed to load API key");
    } finally {
      setLoadingApiKey(false);
    }
  };

  const loadApiKeyUsage = async () => {
    const projectId = project?.id || projectIdFromUrl;
    if (!projectId || projectId === "undefined") return;
    try {
      setLoadingUsage(true);
      const data = await getApiKeyUsage(projectId);
      setApiKeyUsage(data);
    } catch (error: any) {
      setApiKeyUsage(null);
      // Don't show toast for this - it's not critical
    } finally {
      setLoadingUsage(false);
    }
  };

  const handleUpdateProject = async (data: UpdateProjectFormData) => {
    // Use project ID from context or fallback to URL param
    const projectId = project?.id || projectIdFromUrl;

    if (!projectId || projectId === "undefined" || projectId === undefined) {
      toast.error("Project ID is missing. Please refresh the page.");
      return;
    }

    try {
      await updateProject(projectId, data);
      toast.success("Project updated successfully");
      await refreshProject();
    } catch (error: any) {
      const errorMessage =
        error.response?.data?.message ||
        error.message ||
        "Failed to update project";
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleRegenerateApiKey = async () => {
    const projectId = project?.id || projectIdFromUrl;
    if (!projectId || projectId === "undefined") {
      toast.error("Project ID is missing. Please refresh the page.");
      return;
    }

    if (
      !confirm(
        "Are you sure you want to regenerate the API key? The old key will no longer work."
      )
    ) {
      return;
    }

    try {
      const data = await regenerateApiKey(projectId);
      setApiKey(data);
      setShowApiKey(true);
      toast.success("API key regenerated successfully");
    } catch (error: any) {
      toast.error(error.message || "Failed to regenerate API key");
    }
  };

  const handleCopyApiKey = () => {
    const keyToCopy = (apiKey as any)?.apiKey || apiKey?.maskedApiKey;
    if (keyToCopy) {
      navigator.clipboard.writeText(keyToCopy);
      setCopied(true);
      toast.success("API key copied to clipboard");
      setTimeout(() => setCopied(false), 2000);
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
        <div className="max-w-4xl mx-auto px-6 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">Project Settings</h1>
            <p className="text-muted-foreground">
              Manage your project configuration and API keys
            </p>
          </div>

          <div className="space-y-6">
            {/* Project Details */}
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
              <h2 className="text-lg font-semibold mb-4">Project Details</h2>
              <form
                onSubmit={handleSubmit(handleUpdateProject)}
                className="space-y-4"
              >
                <div>
                  <label
                    htmlFor="name"
                    className="block text-sm font-medium mb-2"
                  >
                    Project Name *
                  </label>
                  <input
                    id="name"
                    type="text"
                    {...register("name")}
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                  />
                  {errors.name && (
                    <p className="mt-1 text-sm text-destructive">
                      {errors.name.message}
                    </p>
                  )}
                </div>

                <div>
                  <label
                    htmlFor="description"
                    className="block text-sm font-medium mb-2"
                  >
                    Description
                  </label>
                  <textarea
                    id="description"
                    {...register("description")}
                    rows={3}
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all resize-none"
                    placeholder="Optional project description"
                  />
                  {errors.description && (
                    <p className="mt-1 text-sm text-destructive">
                      {errors.description.message}
                    </p>
                  )}
                </div>

                <div className="flex justify-end">
                  <Button
                    type="submit"
                    variant="primary"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "Saving..." : "Save Changes"}
                  </Button>
                </div>
              </form>
            </div>

            {/* API Key */}
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h2 className="text-lg font-semibold">API Key</h2>
                  <p className="text-sm text-muted-foreground mt-1">
                    Use this key to authenticate API requests
                  </p>
                </div>
                <Button
                  variant="secondary"
                  icon={RefreshCw}
                  onClick={handleRegenerateApiKey}
                  disabled={loadingApiKey}
                >
                  Regenerate
                </Button>
              </div>

              {loadingApiKey ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="w-6 h-6 animate-spin text-primary" />
                </div>
              ) : apiKey ? (
                <div className="space-y-4">
                  <div className="flex items-center gap-2">
                    <div className="flex-1 bg-secondary border border-border rounded-lg px-4 py-3 font-mono text-sm">
                      {showApiKey && (apiKey as any).apiKey
                        ? (apiKey as any).apiKey
                        : apiKey.maskedApiKey}
                    </div>
                    <button
                      onClick={() => setShowApiKey(!showApiKey)}
                      className="p-2 hover:bg-secondary rounded-lg transition-colors"
                      title={showApiKey ? "Hide" : "Show"}
                    >
                      {showApiKey ? (
                        <EyeOff className="w-4 h-4" />
                      ) : (
                        <Eye className="w-4 h-4" />
                      )}
                    </button>
                    <button
                      onClick={handleCopyApiKey}
                      className="p-2 hover:bg-secondary rounded-lg transition-colors"
                      title="Copy"
                    >
                      <Copy
                        className={`w-4 h-4 ${copied ? "text-primary" : ""}`}
                      />
                    </button>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Keep your API key secure. Never share it publicly.
                  </p>
                </div>
              ) : null}
            </div>

            {/* API Key Usage */}
            {apiKeyUsage && (
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
                <h2 className="text-lg font-semibold mb-4">API Key Usage</h2>
                {loadingUsage ? (
                  <div className="flex items-center justify-center py-8">
                    <Loader2 className="w-6 h-6 animate-spin text-primary" />
                  </div>
                ) : apiKeyUsage ? (
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">
                        Total Requests
                      </p>
                      <p className="text-2xl font-semibold">
                        {(apiKeyUsage.requestCount ?? 0).toLocaleString()}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">
                        Errors
                      </p>
                      <p className="text-2xl font-semibold text-destructive">
                        {(apiKeyUsage.errorCount ?? 0).toLocaleString()}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">
                        Last Used
                      </p>
                      <p className="text-sm font-medium">
                        {apiKeyUsage.lastUsedAt
                          ? new Date(apiKeyUsage.lastUsedAt).toLocaleString()
                          : "Never"}
                      </p>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-center py-8 text-muted-foreground">
                    No usage data available
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
