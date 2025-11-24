import React from 'react';
import { AlertCircle, CheckCircle, Clock } from 'lucide-react';

interface Incident {
  id: string;
  time: string;
  status: 'open' | 'investigating' | 'resolved';
  severity: 'critical' | 'high' | 'medium';
  title: string;
  service: string;
}

const incidents: Incident[] = [
  { id: '1', time: '2 min ago', status: 'open', severity: 'critical', title: 'Payment API timeout spike', service: 'payment-svc' },
  { id: '2', time: '15 min ago', status: 'investigating', severity: 'high', title: 'Database connection pool exhausted', service: 'postgres' },
  { id: '3', time: '1 hour ago', status: 'resolved', severity: 'medium', title: 'Cache miss rate elevated', service: 'redis' },
  { id: '4', time: '3 hours ago', status: 'resolved', severity: 'high', title: 'API Gateway latency increase', service: 'api-gateway' },
];

export function IncidentTimeline() {
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'open': return <AlertCircle className="w-5 h-5 text-destructive" />;
      case 'investigating': return <Clock className="w-5 h-5 text-yellow-500" />;
      case 'resolved': return <CheckCircle className="w-5 h-5 text-green-500" />;
      default: return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'open': return 'bg-destructive/20 text-destructive border-destructive/50';
      case 'investigating': return 'bg-yellow-500/20 text-yellow-500 border-yellow-500/50';
      case 'resolved': return 'bg-green-500/20 text-green-500 border-green-500/50';
      default: return '';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'critical': return 'text-destructive';
      case 'high': return 'text-orange-500';
      case 'medium': return 'text-yellow-500';
      default: return 'text-muted-foreground';
    }
  };

  return (
    <div className="bg-card border border-border rounded-2xl p-6">
      <div className="mb-6">
        <h3 className="text-xl mb-1">Incident Timeline</h3>
        <p className="text-sm text-muted-foreground">Recent incidents and their resolution status</p>
      </div>

      <div className="space-y-4">
        {incidents.map((incident, index) => (
          <div key={incident.id} className="relative">
            {/* Timeline line */}
            {index !== incidents.length - 1 && (
              <div className="absolute left-6 top-12 bottom-0 w-0.5 bg-border"></div>
            )}

            {/* Incident card */}
            <div className="flex gap-4">
              <div className="flex-shrink-0 w-12 h-12 rounded-xl bg-secondary flex items-center justify-center relative z-10">
                {getStatusIcon(incident.status)}
              </div>
              
              <div className="flex-1 pb-4">
                <div className="flex items-start justify-between gap-4 mb-2">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <h4 className="text-base">{incident.title}</h4>
                      <span className={`text-xs px-2 py-0.5 rounded border ${getStatusColor(incident.status)} capitalize`}>
                        {incident.status}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 text-sm">
                      <span className="text-muted-foreground">{incident.time}</span>
                      <span className="text-muted-foreground">•</span>
                      <span className="bg-secondary px-2 py-0.5 rounded text-muted-foreground">{incident.service}</span>
                      <span className="text-muted-foreground">•</span>
                      <span className={`capitalize ${getSeverityColor(incident.severity)}`}>
                        {incident.severity}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

