"use client";

import React from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { ChevronDown, Sparkles, ArrowRight, Calendar } from "lucide-react";
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

// Mock data for graphs
const edgeRequestsData = [
  { time: "12h ago", value: 0 },
  { time: "10h ago", value: 5 },
  { time: "8h ago", value: 10 },
  { time: "6h ago", value: 15 },
  { time: "4h ago", value: 20 },
  { time: "2h ago", value: 25 },
  { time: "1h ago", value: 30 },
  { time: "30m ago", value: 35 },
  { time: "15m ago", value: 40 },
  { time: "5m ago", value: 70 },
];

const fastDataTransferData = [
  { time: "12h ago", value: 0 },
  { time: "10h ago", value: 50 },
  { time: "8h ago", value: 100 },
  { time: "6h ago", value: 150 },
  { time: "4h ago", value: 200 },
  { time: "2h ago", value: 250 },
  { time: "1h ago", value: 300 },
  { time: "30m ago", value: 350 },
  { time: "15m ago", value: 400 },
  { time: "5m ago", value: 505 },
];

interface MetricCardProps {
  title: string;
  value: string;
  subtitle?: string;
  children: React.ReactNode;
}

function MetricCard({ title, value, subtitle, children }: MetricCardProps) {
  return (
    <div className="bg-card border border-border rounded-lg p-6 hover:border-primary/50 transition-all">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-medium text-foreground">{title}</h3>
        <button className="text-muted-foreground hover:text-foreground transition-colors">
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>
      <div className="mb-4">
        <p className="text-2xl font-semibold text-foreground">{value}</p>
        {subtitle && (
          <p className="text-sm text-muted-foreground mt-1">{subtitle}</p>
        )}
      </div>
      <div className="h-[120px]">{children}</div>
    </div>
  );
}

export default function MetricsPage() {
  const { user, loading } = useAuth();
  const router = useRouter();

  React.useEffect(() => {
    if (!loading && !user) {
      router.push("/signin");
    }
  }, [user, loading, router]);

  if (loading) {
    return (
      <div className="min-h-screen bg-background text-foreground flex items-center justify-center">
        <div className="text-muted-foreground">Loading...</div>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />

      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-6">
          {/* Header */}
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-semibold">Observability</h1>
            <div className="flex items-center gap-3">
              <button className="flex items-center gap-2 px-3 py-2 border border-border rounded text-sm text-foreground hover:bg-secondary transition-colors">
                Production
                <ChevronDown className="w-4 h-4" />
              </button>
              <button className="flex items-center gap-2 px-3 py-2 border border-border rounded text-sm text-foreground hover:bg-secondary transition-colors">
                <Calendar className="w-4 h-4" />
                Last 12 hours
                <ChevronDown className="w-4 h-4" />
              </button>
              <button className="p-2 text-muted-foreground hover:text-foreground transition-colors">
                <span className="text-xl">⋯</span>
              </button>
            </div>
          </div>

          {/* Upgrade Banner */}
          <div className="mb-6 p-4 bg-card border border-border rounded-lg flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Sparkles className="w-5 h-5 text-primary" />
              <p className="text-sm text-foreground">
                Unlock anomaly alerts, custom queries, 30-day retention, and
                more with Pro and Observability Plus.
              </p>
            </div>
            <button className="px-4 py-2 bg-primary text-primary-foreground rounded text-sm font-medium hover:opacity-90 transition-opacity">
              Upgrade to Pro
            </button>
          </div>

          {/* Metrics Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            {/* Edge Requests */}
            <MetricCard title="Edge Requests" value="Invocations 70">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={edgeRequestsData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2A2A2C" />
                  <XAxis
                    dataKey="time"
                    stroke="#A1A1AA"
                    fontSize={10}
                    tick={{ fill: "#A1A1AA" }}
                  />
                  <YAxis
                    stroke="#A1A1AA"
                    fontSize={10}
                    tick={{ fill: "#A1A1AA" }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#1A1B1E",
                      border: "1px solid #2A2A2C",
                      borderRadius: "0.5rem",
                    }}
                    labelStyle={{ color: "#FFFFFF" }}
                  />
                  <Line
                    type="monotone"
                    dataKey="value"
                    stroke="#5B78FF"
                    strokeWidth={2}
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </MetricCard>

            {/* Fast Data Transfer */}
            <MetricCard title="Fast Data Transfer" value="Total 505 KB">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={fastDataTransferData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#2A2A2C" />
                  <XAxis
                    dataKey="time"
                    stroke="#A1A1AA"
                    fontSize={10}
                    tick={{ fill: "#A1A1AA" }}
                  />
                  <YAxis
                    stroke="#A1A1AA"
                    fontSize={10}
                    tick={{ fill: "#A1A1AA" }}
                    label={{ value: "KB", position: "insideLeft", fill: "#A1A1AA" }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#1A1B1E",
                      border: "1px solid #2A2A2C",
                      borderRadius: "0.5rem",
                    }}
                    labelStyle={{ color: "#FFFFFF" }}
                    formatter={(value: number) => [`${value} KB`, "Value"]}
                  />
                  <Bar dataKey="value" fill="#5B78FF" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </MetricCard>

            {/* Vercel Functions */}
            <MetricCard
              title="Vercel Functions"
              value="Invocations 0"
              subtitle="No invocations in this time range"
            >
              <div className="flex items-center justify-center h-full text-muted-foreground text-sm">
                No data available
              </div>
            </MetricCard>

            {/* Middleware Invocations */}
            <MetricCard
              title="Middleware Invocations"
              value="Invocations 0"
              subtitle="No invocations in this time range"
            >
              <div className="flex items-center justify-center h-full text-muted-foreground text-sm">
                No data available
              </div>
            </MetricCard>
          </div>

          {/* Bottom Section */}
          <div className="space-y-4">
            <div className="flex items-center gap-4">
              <input
                type="text"
                placeholder="Search..."
                className="flex-1 px-4 py-2 bg-background border border-border rounded text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
              />
              <button className="flex items-center gap-2 px-4 py-2 border border-border rounded text-sm text-foreground hover:bg-secondary transition-colors">
                Project
                <ChevronDown className="w-4 h-4" />
              </button>
              <button className="flex items-center gap-2 px-4 py-2 border border-border rounded text-sm text-foreground hover:bg-secondary transition-colors">
                Requests
                <ChevronDown className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

