'use client';

import React from 'react';
import { Save, PlayCircle } from 'lucide-react';
import { Button } from './Button';

export function AlertRuleCard() {
  const [metric, setMetric] = React.useState('latency_p95');
  const [operator, setOperator] = React.useState('>');
  const [value, setValue] = React.useState('500');
  const [duration, setDuration] = React.useState('5');

  return (
    <div className="bg-card border border-border rounded-2xl p-6">
      <div className="mb-6">
        <h3 className="text-xl mb-1">Alert Rule Builder</h3>
        <p className="text-sm text-muted-foreground">Create custom alerting rules for your services</p>
      </div>

      {/* Rule Builder */}
      <div className="bg-background/50 rounded-xl p-6 mb-6">
        <div className="flex flex-wrap items-center gap-3 text-base">
          <span className="text-muted-foreground">IF</span>
          
          <select 
            value={metric}
            onChange={(e) => setMetric(e.target.value)}
            className="bg-secondary border border-border rounded-lg px-3 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="latency_p95">latency_p95</option>
            <option value="error_rate">error_rate</option>
            <option value="cpu_usage">cpu_usage</option>
            <option value="memory_usage">memory_usage</option>
          </select>

          <select 
            value={operator}
            onChange={(e) => setOperator(e.target.value)}
            className="bg-secondary border border-border rounded-lg px-3 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value=">">{'>'}</option>
            <option value="<">{'<'}</option>
            <option value=">=">{'>='}</option>
            <option value="<=">{'<='}</option>
            <option value="==">{'=='}</option>
          </select>

          <input
            type="text"
            value={value}
            onChange={(e) => setValue(e.target.value)}
            className="bg-secondary border border-border rounded-lg px-3 py-2 w-24 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            placeholder="Value"
          />

          <span className="text-muted-foreground">FOR</span>

          <input
            type="text"
            value={duration}
            onChange={(e) => setDuration(e.target.value)}
            className="bg-secondary border border-border rounded-lg px-3 py-2 w-20 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            placeholder="5"
          />

          <span className="text-muted-foreground">minutes</span>

          <span className="text-muted-foreground">→</span>

          <span className="text-primary">trigger incident</span>
        </div>
      </div>

      {/* Additional Settings */}
      <div className="space-y-4 mb-6">
        <div>
          <label className="text-sm text-muted-foreground mb-2 block">Service (optional)</label>
          <input
            type="text"
            placeholder="payment-svc, api-gateway, ..."
            className="w-full bg-secondary border border-border rounded-lg px-3 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        
        <div>
          <label className="text-sm text-muted-foreground mb-2 block">Alert Severity</label>
          <select className="w-full bg-secondary border border-border rounded-lg px-3 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary">
            <option value="critical">Critical</option>
            <option value="high">High</option>
            <option value="medium">Medium</option>
            <option value="low">Low</option>
          </select>
        </div>
      </div>

      {/* Actions */}
      <div className="flex gap-3">
        <Button variant="primary" icon={Save}>Save Rule</Button>
        <Button variant="secondary" icon={PlayCircle}>Test Rule</Button>
      </div>
    </div>
  );
}

