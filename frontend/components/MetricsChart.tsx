'use client';

import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface MetricsChartProps {
  showTimeSelector?: boolean;
}

export function MetricsChart({ showTimeSelector = true }: MetricsChartProps) {
  const [timeRange, setTimeRange] = React.useState('24h');

  const data = [
    { time: '00:00', latency: 120, errors: 2 },
    { time: '04:00', latency: 98, errors: 1 },
    { time: '08:00', latency: 156, errors: 5 },
    { time: '12:00', latency: 245, errors: 12 },
    { time: '16:00', latency: 289, errors: 18 },
    { time: '20:00', latency: 198, errors: 8 },
    { time: '23:59', latency: 156, errors: 4 },
  ];

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="glass rounded-lg p-3 border border-border">
          <p className="text-sm text-muted-foreground mb-1">{payload[0].payload.time}</p>
          <p className="text-sm">
            <span className="text-primary">●</span> Latency: {payload[0].value}ms
          </p>
          <p className="text-sm">
            <span className="text-destructive">●</span> Errors: {payload[1].value}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-card border border-border rounded-2xl p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="text-xl mb-1">System Metrics</h3>
          <p className="text-sm text-muted-foreground">p95 latency and error rate</p>
        </div>
        {showTimeSelector && (
          <div className="flex gap-2">
            {['1h', '24h', '7d'].map((range) => (
              <button
                key={range}
                onClick={() => setTimeRange(range)}
                className={`px-3 py-1 rounded-lg text-sm transition-colors ${
                  timeRange === range
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-secondary text-muted-foreground hover:text-foreground'
                }`}
              >
                {range}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Chart */}
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="#2A2A2C" />
          <XAxis 
            dataKey="time" 
            stroke="#A1A1AA"
            style={{ fontSize: '12px' }}
          />
          <YAxis 
            stroke="#A1A1AA"
            style={{ fontSize: '12px' }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Line 
            type="monotone" 
            dataKey="latency" 
            stroke="#5B78FF" 
            strokeWidth={2}
            dot={{ fill: '#5B78FF', r: 4 }}
            activeDot={{ r: 6 }}
          />
          <Line 
            type="monotone" 
            dataKey="errors" 
            stroke="#EF4444" 
            strokeWidth={2}
            dot={{ fill: '#EF4444', r: 4 }}
            activeDot={{ r: 6 }}
          />
        </LineChart>
      </ResponsiveContainer>

      {/* Legend */}
      <div className="flex gap-6 mt-4">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-primary"></div>
          <span className="text-sm text-muted-foreground">p95 Latency (ms)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-destructive"></div>
          <span className="text-sm text-muted-foreground">Error Rate</span>
        </div>
      </div>
    </div>
  );
}

