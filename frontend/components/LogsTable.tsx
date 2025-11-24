'use client';

import React from 'react';
import { AlertTriangle, Info, XCircle, Filter } from 'lucide-react';

interface Log {
  id: string;
  timestamp: string;
  level: 'info' | 'warning' | 'error';
  service: string;
  message: string;
  isAnomaly?: boolean;
}

const logs: Log[] = [
  { id: '1', timestamp: '10:23:45', level: 'error', service: 'api-gateway', message: 'Connection timeout to payment service' },
  { id: '2', timestamp: '10:23:42', level: 'warning', service: 'payment-svc', message: 'High response time detected', isAnomaly: true },
  { id: '3', timestamp: '10:23:38', level: 'info', service: 'auth-svc', message: 'User authentication successful' },
  { id: '4', timestamp: '10:23:35', level: 'error', service: 'database', message: 'Query execution failed: timeout' },
  { id: '5', timestamp: '10:23:30', level: 'info', service: 'api-gateway', message: 'Request processed successfully' },
  { id: '6', timestamp: '10:23:28', level: 'warning', service: 'cache-svc', message: 'Cache miss rate above threshold', isAnomaly: true },
];

export function LogsTable() {
  const [filters, setFilters] = React.useState({
    level: 'all',
    service: 'all',
  });

  const getLevelIcon = (level: string) => {
    switch (level) {
      case 'error': return <XCircle className="w-4 h-4 text-destructive" />;
      case 'warning': return <AlertTriangle className="w-4 h-4 text-yellow-500" />;
      case 'info': return <Info className="w-4 h-4 text-blue-500" />;
      default: return null;
    }
  };

  return (
    <div className="bg-card border border-border rounded-2xl p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="text-xl mb-1">Live Logs Stream</h3>
          <p className="text-sm text-muted-foreground">Real-time log ingestion with anomaly detection</p>
        </div>
        <button className="flex items-center gap-2 px-3 py-2 bg-secondary rounded-lg text-sm hover:bg-secondary/80 transition-colors">
          <Filter className="w-4 h-4" />
          Filters
        </button>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border text-left">
              <th className="pb-3 text-sm text-muted-foreground font-medium">Timestamp</th>
              <th className="pb-3 text-sm text-muted-foreground font-medium">Level</th>
              <th className="pb-3 text-sm text-muted-foreground font-medium">Service</th>
              <th className="pb-3 text-sm text-muted-foreground font-medium">Message</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr 
                key={log.id} 
                className={`border-b border-border/50 hover:bg-secondary/30 transition-colors ${
                  log.isAnomaly ? 'bg-yellow-500/10' : ''
                }`}
              >
                <td className="py-3 text-sm text-muted-foreground">{log.timestamp}</td>
                <td className="py-3">
                  <div className="flex items-center gap-2">
                    {getLevelIcon(log.level)}
                    <span className="text-sm capitalize">{log.level}</span>
                  </div>
                </td>
                <td className="py-3">
                  <span className="text-sm bg-secondary px-2 py-1 rounded">{log.service}</span>
                </td>
                <td className="py-3 text-sm">
                  {log.message}
                  {log.isAnomaly && (
                    <span className="ml-2 text-xs bg-yellow-500/20 text-yellow-500 px-2 py-0.5 rounded">
                      New pattern
                    </span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

