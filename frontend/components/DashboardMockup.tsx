import React from 'react';
import { MetricsChart } from './MetricsChart';
import { IncidentTimeline } from './IncidentTimeline';
import { AISummaryCard } from './AISummaryCard';

export function DashboardMockup() {
  return (
    <div className="relative">
      {/* Glow effect */}
      <div className="absolute inset-0 bg-gradient-to-br from-primary/20 to-accent/20 blur-3xl rounded-3xl transform scale-105 -z-10"></div>
      
      {/* Main dashboard container */}
      <div className="bg-background/80 backdrop-blur-xl border border-border rounded-2xl p-6 shadow-2xl">
        {/* Dashboard header */}
        <div className="flex items-center justify-between mb-6 pb-4 border-b border-border">
          <div>
            <h3 className="text-lg mb-1">Production Environment</h3>
            <p className="text-sm text-muted-foreground">Last updated: 2 seconds ago</p>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
            <span className="text-sm text-muted-foreground">All systems operational</span>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left column - Metrics */}
          <div className="lg:col-span-2">
            <MetricsChart showTimeSelector={false} />
          </div>

          {/* Right column - AI Summary */}
          <div>
            <AISummaryCard />
          </div>

          {/* Bottom - Incidents */}
          <div className="lg:col-span-3">
            <IncidentTimeline />
          </div>
        </div>
      </div>
    </div>
  );
}

