"use client";

import React, { useState, useRef, useEffect } from "react";
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
      <div className="max-w-[1920px] mx-auto px-6 -mt-1 relative">
        <div className="flex items-center gap-1 overflow-x-auto relative">
          {/* Sliding hover background */}
          <div
            id="sliding-bg"
            className="absolute top-0 bg-secondary rounded transition-all duration-300 ease-out pointer-events-none opacity-0"
            style={{
              width: "0px",
              left: "0px",
              top: "0.2rem",
              bottom: "0.35rem",
            }}
          />
          {navItems.map((item, index) => {
            // More precise active state: exact match or starts with href + "/" but exclude parent routes when on child routes
            let isActive = false;
            if (pathname === item.href || pathname === item.href + "/") {
              isActive = true;
            } else if (pathname?.startsWith(item.href + "/")) {
              // Only match if this is the most specific route
              // For /dashboard, don't match if we're on a more specific route like /dashboard/metrics
              if (item.href === "/dashboard") {
                // Check if pathname matches any other nav item (more specific)
                const isMoreSpecificRoute = navItems
                  .filter((other) => other.href !== item.href)
                  .some((other) => pathname?.startsWith(other.href));
                isActive = !isMoreSpecificRoute;
              } else {
                // For other routes, allow sub-routes
                isActive = true;
              }
            }
            const linkRef = useRef<HTMLAnchorElement>(null);

            return (
              <div
                key={item.href}
                className="relative"
                style={{ paddingBottom: "0.1rem" }}
              >
                <Link
                  ref={linkRef}
                  href={item.href}
                  className="relative z-10"
                  onMouseEnter={(e) => {
                    const bg = document.getElementById("sliding-bg");
                    if (bg && linkRef.current) {
                      const rect = linkRef.current.getBoundingClientRect();
                      const container =
                        linkRef.current.parentElement?.parentElement;
                      if (container) {
                        const containerRect = container.getBoundingClientRect();
                        bg.style.width = `${rect.width}px`;
                        bg.style.left = `${rect.left - containerRect.left}px`;
                        bg.style.opacity = "1";
                      }
                    }
                  }}
                  onMouseLeave={() => {
                    const bg = document.getElementById("sliding-bg");
                    if (bg) {
                      bg.style.opacity = "0";
                    }
                  }}
                >
                  <span
                    className={`
                    block px-4 pt-3 pb-3 text-sm font-medium transition-colors whitespace-nowrap rounded
                    ${
                      isActive
                        ? "text-foreground"
                        : "text-muted-foreground hover:text-foreground"
                    }
                  `}
                    style={
                      !isActive
                        ? {
                            marginBottom: "0.1rem",
                          }
                        : {
                            marginBottom: "0.1rem",
                          }
                    }
                  >
                    {item.name}
                  </span>
                </Link>
                {isActive && (
                  <span
                    className="absolute bg-white z-30"
                    style={{
                      bottom: "0",
                      left: "0",
                      right: "0",
                      height: "2px",
                    }}
                  ></span>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </nav>
  );
}
