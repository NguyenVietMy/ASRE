import React from 'react';
import { Sparkles, CheckCircle, ExternalLink } from 'lucide-react';

export function AISummaryCard() {
  const suggestedActions = [
    'Check payment service health endpoints',
    'Review recent deployment changes',
    'Verify database connection pool settings',
    'Scale payment service replicas',
  ];

  const references = [
    { title: 'Payment Service Runbook', url: '#' },
    { title: 'Similar incident from 2024-10-15', url: '#' },
  ];

  return (
    <div className="glass rounded-2xl p-6 border border-primary/30 glow-primary">
      {/* Header */}
      <div className="flex items-center gap-2 mb-4">
        <div className="w-8 h-8 rounded-lg gradient-primary flex items-center justify-center">
          <Sparkles className="w-4 h-4 text-white" />
        </div>
        <h3 className="text-xl">AI Incident Summary</h3>
      </div>

      {/* Root Cause */}
      <div className="mb-6">
        <h4 className="text-sm text-primary mb-2">Root Cause Analysis</h4>
        <p className="text-sm text-muted-foreground leading-relaxed">
          Payment service experiencing cascading timeouts due to database connection pool exhaustion. 
          Traffic spike at 10:20 AM exceeded configured connection limits, causing new requests to queue. 
          This pattern matches previous incident from October when similar load conditions occurred.
        </p>
      </div>

      {/* Metrics Snapshot */}
      <div className="grid grid-cols-3 gap-4 mb-6 p-4 bg-background/50 rounded-xl">
        <div>
          <p className="text-xs text-muted-foreground mb-1">Error Rate</p>
          <p className="text-lg text-destructive">+342%</p>
        </div>
        <div>
          <p className="text-xs text-muted-foreground mb-1">Latency (p95)</p>
          <p className="text-lg text-orange-500">2.8s</p>
        </div>
        <div>
          <p className="text-xs text-muted-foreground mb-1">Affected Users</p>
          <p className="text-lg text-yellow-500">~1.2k</p>
        </div>
      </div>

      {/* Suggested Actions */}
      <div className="mb-6">
        <h4 className="text-sm text-primary mb-3">Suggested Actions</h4>
        <div className="space-y-2">
          {suggestedActions.map((action, index) => (
            <div key={index} className="flex items-start gap-2">
              <CheckCircle className="w-4 h-4 text-muted-foreground mt-0.5 flex-shrink-0" />
              <span className="text-sm text-muted-foreground">{action}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Reference Links */}
      <div>
        <h4 className="text-sm text-primary mb-3">References</h4>
        <div className="space-y-2">
          {references.map((ref, index) => (
            <a
              key={index}
              href={ref.url}
              className="flex items-center gap-2 text-sm text-muted-foreground hover:text-primary transition-colors"
            >
              <ExternalLink className="w-4 h-4" />
              {ref.title}
            </a>
          ))}
        </div>
      </div>
    </div>
  );
}

