"use client";

import React, { useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import {
  Search,
  LayoutGrid,
  List,
  ListChecks,
  Plus,
  ChevronDown,
  ExternalLink,
  GitBranch,
  AlertCircle,
  MoreVertical,
  Globe,
  Github,
  Calendar,
} from "lucide-react";

export default function DashboardPage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [viewMode, setViewMode] = useState<"grid" | "list" | "detailed">(
    "grid"
  );

  React.useEffect(() => {
    if (!loading && !user) {
      router.push("/signin");
    }
  }, [user, loading, router]);

  if (loading) {
    return (
      <div className="min-h-screen bg-black text-foreground flex items-center justify-center">
        <div className="text-muted-foreground">Loading...</div>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  // Mock usage data
  const usageMetrics = [
    { label: "Edge Requests", value: "3.2K", limit: "1M", percentage: 0.32 },
    {
      label: "Image Optimization - Transformations",
      value: "3",
      limit: "5K",
      percentage: 0.06,
    },
    { label: "ISR Reads", value: "530", limit: "1M", percentage: 0.053 },
    {
      label: "Fast Origin Transfer",
      value: "3.13 MB",
      limit: "10 GB",
      percentage: 0.031,
    },
    {
      label: "Fast Data Transfer",
      value: "12.43 MB",
      limit: "100 GB",
      percentage: 0.012,
    },
    {
      label: "Image Optimization - Cache Writes",
      value: "12",
      limit: "100K",
      percentage: 0.012,
    },
    {
      label: "Edge Request CPU Duration",
      value: "0s",
      limit: "1h",
      percentage: 0,
    },
    {
      label: "Microfrontends Routing",
      value: "0",
      limit: "50K",
      percentage: 0,
    },
    { label: "ISR Writes", value: "0", limit: "200K", percentage: 0 },
    {
      label: "Function Invocations",
      value: "0",
      limit: "1M",
      percentage: 0,
    },
  ];

  // Mock projects data
  const projects = [
    {
      name: "outreachly",
      domain: "outreachly-liart.vercel.app",
      gitRepo: "NguyenVietMy/Outreachly",
      lastCommit: "remove redundant Md files",
      commitDate: "Oct 29",
      branch: "main",
      hasWarning: true,
    },
    {
      name: "app",
      domain: "www.outreach-ly.com",
      gitRepo: null,
      lastCommit: null,
      commitDate: "Oct 29",
      branch: null,
      needsGitConnection: true,
    },
  ];

  return (
    <div className="min-h-screen bg-black text-foreground">
      <AppNavigation />

      {/* Main Content */}
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto pl-[9.5%] pr-[9.5%] py-8">
          {/* Top Bar: Search, View Modes, Add New */}
          <div className="flex items-center gap-4 mb-8">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <input
                type="text"
                placeholder="Search Projects..."
                className="w-full pl-9 pr-4 py-2 bg-background border border-border rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
              />
            </div>

            <div className="flex items-center gap-2 flex-shrink-0">
              {/* View Mode Toggle */}
              <div className="flex items-center border border-border rounded-lg overflow-hidden">
                <button
                  onClick={() => setViewMode("list")}
                  className={`p-2 ${
                    viewMode === "list"
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:text-foreground"
                  } transition-colors`}
                >
                  <List className="w-4 h-4" />
                </button>
                <button
                  onClick={() => setViewMode("grid")}
                  className={`p-2 border-l border-border ${
                    viewMode === "grid"
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:text-foreground"
                  } transition-colors`}
                >
                  <LayoutGrid className="w-4 h-4" />
                </button>
                <button
                  onClick={() => setViewMode("detailed")}
                  className={`p-2 border-l border-border ${
                    viewMode === "detailed"
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:text-foreground"
                  } transition-colors`}
                >
                  <ListChecks className="w-4 h-4" />
                </button>
              </div>

              {/* Add New Button */}
              <button className="flex items-center gap-2 px-4 py-2 bg-foreground text-background rounded-lg hover:bg-foreground/90 transition-colors text-sm font-medium">
                Add New...
                <ChevronDown className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Main Grid Layout */}
          <div className="flex flex-col lg:flex-row gap-6">
            {/* Left Column: Usage and Alerts (~30% width) */}
            <div className="flex-[0.3024] flex flex-col">
              {/* Usage Heading */}
              <div className="mb-4">
                <h2 className="text-lg font-semibold">Usage</h2>
              </div>

              {/* Usage Section */}
              <div className="bg-[#0A0A0A] border border-border rounded-lg p-6 flex-[2.95] flex flex-col">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <p className="text-xs text-muted-foreground">
                      Last 30 days
                    </p>
                  </div>
                  <button className="px-3 py-1.5 text-xs font-medium bg-primary/10 text-primary rounded-md hover:bg-primary/20 transition-colors">
                    Upgrade
                  </button>
                </div>

                <div className="space-y-4 overflow-y-auto flex-1">
                  {usageMetrics.map((metric, index) => (
                    <div key={index} className="space-y-1.5">
                      <div className="flex items-center justify-between text-xs">
                        <span className="text-muted-foreground truncate pr-2">
                          {metric.label}
                        </span>
                        <span className="text-foreground font-medium whitespace-nowrap">
                          {metric.value} / {metric.limit}
                        </span>
                      </div>
                      <div className="w-full h-1.5 bg-secondary rounded-full overflow-hidden">
                        <div
                          className="h-full bg-primary rounded-full transition-all"
                          style={{
                            width: `${Math.min(metric.percentage * 100, 100)}%`,
                          }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Alerts Section */}
              <div className="mt-6 bg-[#0A0A0A] border border-border rounded-lg p-6">
                <h3 className="text-sm font-semibold mb-4">Alerts</h3>
                <div className="bg-secondary/50 border border-border rounded-lg p-4 space-y-3">
                  <div>
                    <h4 className="text-sm font-medium mb-1">
                      Get alerted for anomalies
                    </h4>
                    <p className="text-xs text-muted-foreground">
                      Automatically monitor your projects for anomalies and get
                      notified.
                    </p>
                  </div>
                  <button className="w-full px-4 py-2 text-xs font-medium bg-primary/10 text-primary rounded-md hover:bg-primary/20 transition-colors">
                    Upgrade to Observability Plus
                  </button>
                </div>
              </div>
            </div>

            {/* Right Column: Projects (~70% width) */}
            <div className="flex-[0.6976] flex flex-col">
              <div className="mb-4">
                <h2 className="text-lg font-semibold">Projects</h2>
              </div>

              {viewMode === "grid" ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {projects.map((project, index) => (
                    <div
                      key={index}
                      className="bg-[#0A0A0A] border border-border rounded-lg p-6 hover:border-primary/50 transition-all group"
                    >
                      <div className="flex items-start justify-between mb-4">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <h3 className="text-base font-semibold">
                              {project.name}
                            </h3>
                            {project.hasWarning && (
                              <AlertCircle className="w-4 h-4 text-yellow-500" />
                            )}
                          </div>
                          <div className="flex items-center gap-2 text-sm text-muted-foreground mb-1">
                            <Globe className="w-3.5 h-3.5" />
                            <span className="truncate">{project.domain}</span>
                            <ExternalLink className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity" />
                          </div>
                        </div>
                        <button className="p-1.5 rounded-md hover:bg-secondary transition-colors opacity-0 group-hover:opacity-100">
                          <MoreVertical className="w-4 h-4 text-muted-foreground" />
                        </button>
                      </div>

                      {project.needsGitConnection ? (
                        <div className="space-y-3">
                          <div className="flex items-center gap-2 text-sm text-muted-foreground">
                            <Github className="w-4 h-4" />
                            <span>Connect Git Repository</span>
                          </div>
                          <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <Calendar className="w-3.5 h-3.5" />
                            <span>{project.commitDate}</span>
                          </div>
                        </div>
                      ) : (
                        <div className="space-y-3">
                          <div className="flex items-center gap-2 text-sm">
                            <Github className="w-4 h-4 text-muted-foreground" />
                            <span className="text-muted-foreground truncate">
                              {project.gitRepo}
                            </span>
                          </div>
                          <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <GitBranch className="w-3.5 h-3.5" />
                            <span className="truncate">
                              {project.lastCommit}
                            </span>
                          </div>
                          <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            <Calendar className="w-3.5 h-3.5" />
                            <span>
                              {project.commitDate} on {project.branch}
                            </span>
                          </div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="space-y-3">
                  {projects.map((project, index) => (
                    <div
                      key={index}
                      className="bg-[#0A0A0A] border border-border rounded-lg p-4 hover:border-primary/50 transition-all group"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4 flex-1">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <h3 className="text-base font-semibold">
                                {project.name}
                              </h3>
                              {project.hasWarning && (
                                <AlertCircle className="w-4 h-4 text-yellow-500" />
                              )}
                            </div>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground">
                              <div className="flex items-center gap-1.5">
                                <Globe className="w-3.5 h-3.5" />
                                <span>{project.domain}</span>
                              </div>
                              {project.gitRepo && (
                                <div className="flex items-center gap-1.5">
                                  <Github className="w-3.5 h-3.5" />
                                  <span>{project.gitRepo}</span>
                                </div>
                              )}
                              {project.lastCommit && (
                                <div className="flex items-center gap-1.5">
                                  <GitBranch className="w-3.5 h-3.5" />
                                  <span className="truncate max-w-xs">
                                    {project.lastCommit}
                                  </span>
                                </div>
                              )}
                              <div className="flex items-center gap-1.5">
                                <Calendar className="w-3.5 h-3.5" />
                                <span>
                                  {project.commitDate}
                                  {project.branch && ` on ${project.branch}`}
                                </span>
                              </div>
                            </div>
                          </div>
                        </div>
                        <button className="p-1.5 rounded-md hover:bg-secondary transition-colors opacity-0 group-hover:opacity-100 ml-4">
                          <MoreVertical className="w-4 h-4 text-muted-foreground" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
