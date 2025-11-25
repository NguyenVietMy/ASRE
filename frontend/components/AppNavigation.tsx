"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { Bell, User, Search } from "lucide-react";

const navItems = [
  { name: "Dashboard", href: "/dashboard" },
  { name: "Metrics", href: "/dashboard/metrics" },
  { name: "Logs", href: "/dashboard/logs" },
  { name: "Incidents", href: "/dashboard/incidents" },
  { name: "Projects", href: "/dashboard/projects" },
  { name: "Rules", href: "/dashboard/rules" },
  { name: "Settings", href: "/dashboard/settings" },
];

export function AppNavigation() {
  const pathname = usePathname();
  const { user } = useAuth();

  return (
    <nav
      className="fixed top-0 left-0 right-0 z-50 border-b border-border"
      style={{ backgroundColor: "#0A0A0A" }}
    >
      {/* Top Bar */}
      <div>
        <div className="max-w-[1920px] mx-auto px-6 h-16 flex items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg gradient-primary flex items-center justify-center">
              <span className="text-white text-sm font-semibold">OP</span>
            </div>
            <span className="text-base font-medium text-foreground">
              OpsPilot
            </span>
          </div>

          {/* Right Side Actions */}
          <div className="flex items-center gap-3">
            {/* Search Bar */}
            <div className="flex items-center gap-2">
              <div className="relative flex items-center">
                <Search className="absolute left-3 w-4 h-4 text-muted-foreground" />
                <input
                  type="text"
                  placeholder="Find..."
                  className="pl-9 pr-10 py-2 w-53 bg-background border border-border rounded-[0.4rem] text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
                />
                <button className="absolute right-2 px-1.5 py-1 border border-border rounded-[0.3rem] text-xs text-foreground hover:bg-secondary transition-colors">
                  F
                </button>
              </div>
            </div>

            {/* Feedback Button */}
            <button className="px-3.5 py-2 bg-background border border-border rounded-[0.4rem] text-sm text-foreground hover:bg-secondary transition-colors">
              Feedback
            </button>

            {/* Notifications Bell */}
            <button className="relative p-2 border-2 border-primary rounded-full hover:bg-secondary transition-colors">
              <Bell className="w-5 h-5 text-white" />
              <span className="absolute top-1 right-1 w-2 h-2 bg-destructive rounded-full"></span>
            </button>

            {/* User Avatar */}
            <div className="flex items-center gap-2 p-2 rounded-lg hover:bg-secondary transition-colors cursor-pointer">
              <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center">
                <User className="w-4 h-4 text-primary" />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="max-w-[1920px] mx-auto px-6 -mt-1">
        <div className="flex items-center gap-1 overflow-x-auto">
          {navItems.map((item) => {
            const isActive =
              pathname === item.href || pathname?.startsWith(item.href + "/");
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`
                  relative px-4 py-2 text-sm font-medium transition-colors whitespace-nowrap rounded
                  ${
                    isActive
                      ? "text-foreground"
                      : "text-muted-foreground hover:text-foreground hover:bg-secondary"
                  }
                `}
              >
                {item.name}
                {isActive && (
                  <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-foreground"></span>
                )}
              </Link>
            );
          })}
        </div>
      </div>
    </nav>
  );
}
