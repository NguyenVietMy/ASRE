"use client";

import React from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { Activity } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";

export default function DashboardPage() {
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

      {/* Main Content */}
      <main className="pt-[112px]">
        {/* Dashboard Content */}
        <div className="p-6">
          <div className="max-w-7xl mx-auto">
            {/* Welcome Section */}
            <div className="mb-8">
              <h2 className="text-3xl font-semibold mb-2">
                Welcome back, {user.email.split("@")[0]}
              </h2>
              <p className="text-muted-foreground">
                Here's an overview of your system performance
              </p>
            </div>

            {/* User Info Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {[
                {
                  title: "User ID",
                  value: user.id.substring(0, 8) + "...",
                  change: "Your account",
                },
                { title: "Email", value: user.email, change: "Verified" },
                { title: "Role", value: user.role, change: "Account type" },
                { title: "Status", value: "Active", change: "Logged in" },
              ].map((stat, index) => (
                <div
                  key={index}
                  className="bg-card border border-border rounded-xl p-6 hover:border-primary/50 transition-all"
                >
                  <p className="text-sm text-muted-foreground mb-2">
                    {stat.title}
                  </p>
                  <p className="text-2xl font-semibold mb-1">{stat.value}</p>
                  <p className="text-xs text-muted-foreground">{stat.change}</p>
                </div>
              ))}
            </div>

            {/* Main Content Area */}
            <div className="bg-card border border-border rounded-xl p-8">
              <div className="text-center py-12">
                <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center mx-auto mb-4">
                  <Activity className="w-8 h-8 text-primary" />
                </div>
                <h3 className="text-xl font-semibold mb-2">
                  Dashboard Content
                </h3>
                <p className="text-muted-foreground max-w-md mx-auto">
                  Your dashboard content will appear here. Add metrics, charts,
                  and other widgets as needed.
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
