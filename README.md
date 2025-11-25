Overview

This document outlines the backend implementation plan for OpsPilot’s Metrics Explorer and Logs Viewer.
It integrates the corrected architecture, improved database semantics, refined ingestion pipeline, and best practices for scalable observability systems.

1. Architecture
   1.1 Data Storage (Final Layout)

Primary Postgres (Supabase or RDS, choose ONE):

Users, projects, services

Incidents, incident events, incident notes

Alert rules

Refresh tokens

Runbook metadata

Vector embeddings (pgvector)

Rate limit buckets

AI run history

TimescaleDB (dedicated cluster):

Time-series metrics

Raw metric samples

HDR histograms (optional)

Anomaly detection results

OpenSearch (AWS Managed):

Full-text logs

Log indexing for search_after pagination

Aggregations for log volume analysis

Redis:

Caching for metric queries

Rate limits

Alert state (“for duration” tracking)

S3:

Runbook markdown content

Large data exports

1.2 Data Flow
Client SDK
→ Ingestion API
→ RabbitMQ
→ Ingestion Worker
→ TimescaleDB (metrics)
→ OpenSearch (logs)
↓
Backend API ← API Gateway ← Frontend Dashboard
↓
Alert Engine → Rule Evaluation → PostgreSQL (incidents)
↓
AI Microservice ← Incident Context (metrics+logs) ← Backend API
1.3 Observability of the Platform Itself

Prometheus exporters (custom): ingestion worker, alert engine, API gateway

Grafana dashboards: ingestion health, query performance, DB latency

CloudWatch: AWS infra metrics

Grafana is for internal debugging only — the OpsPilot UI is custom.

2. Metrics Explorer Backend
   2.1 Metrics Schema (corrected)
   Store raw samples (recommended)
   CREATE TABLE metrics (
   id BIGSERIAL,
   project_id UUID NOT NULL REFERENCES projects(id),
   service_id UUID NOT NULL REFERENCES services(id),
   timestamp TIMESTAMPTZ NOT NULL,
   metric_name VARCHAR(100) NOT NULL,
   value DOUBLE PRECISION NOT NULL,
   tags JSONB,
   PRIMARY KEY (timestamp, id)
   );
   SELECT create_hypertable('metrics', 'timestamp');

Indexes
CREATE INDEX idx_metrics_project_time ON metrics(project_id, timestamp DESC);
CREATE INDEX idx_metrics_service ON metrics(service_id, timestamp DESC);
CREATE INDEX idx_metrics_name ON metrics(project_id, metric_name, timestamp DESC);
CREATE INDEX idx_metrics_tags ON metrics USING GIN(tags);
Why raw samples?

Accurate p50/p95/p99 via percentile_cont

Compatible with ML anomaly detection

Matches industry practice (Datadog, Prometheus, New Relic)

2.2 Supported Metric Types

Latency

Request count

Error rate

Throughput

CPU/memory usage

Disk I/O

DB connections

Custom user-defined metrics

2.3 API Endpoints (corrected + optimized)

1. Query Metrics
   GET /api/metrics/query
   Headers:
   Authorization: Bearer <token>
   X-Project-ID: <project_id>

Params:
metric: string (required)
stat: avg | min | max | p50 | p95 | p99
rollup: 1m | 5m | 1h | 1d
start_time: ISO timestamp
end_time: ISO timestamp
service_id: uuid

2. Query Multiple (overlay)
   POST /api/metrics/query-multiple
   {
   "queries": [
   { "metric": "latency_ms", "stat": "p99", "service_id": "..." },
   { "metric": "error_rate", "stat": "avg", "service_id": "..." }
   ],
   "start_time": "...",
   "end_time": "...",
   "rollup": "1m",
   "align": true
   }

3. Histogram (for latency distribution)
   GET /api/metrics/histogram
   Params:
   metric: latency_ms
   bins: 20
   start_time:
   end_time:

4. Anomaly Detection (internal-only gateway)
   GET /api/metrics/anomalies
   Params:
   metric
   start_time
   end_time
   method: zscore | isolation_forest

Backend fetches raw data → sends to AI service.

2.4 Implementation Notes

Use time_bucket() for aggregation

Use Redis cache (TTL 1–5 min) for costly queries

Multi-tenant enforcement at DB + API layer

Align timestamps in overlay queries for UI smoothness

3. Logs Viewer Backend
   3.1 OpenSearch Schema (correct)
   {
   "mappings": {
   "properties": {
   "project_id": { "type": "keyword" },
   "service_id": { "type": "keyword" },
   "timestamp": { "type": "date" },
   "ingested_at": { "type": "date" },
   "level": { "type": "keyword" },
   "message": {
   "type": "text",
   "analyzer": "standard",
   "fields": { "keyword": { "type": "keyword" } }
   },
   "trace_id": { "type": "keyword" },
   "context": { "type": "object" }
   }
   }
   }

Why include ingested_at?

Ordering correctness even when logs are delayed.

3.2 PostgreSQL log metadata (fixed)
CREATE TABLE log_metadata (
id BIGSERIAL PRIMARY KEY,
project_id UUID REFERENCES projects(id),
service_id UUID REFERENCES services(id),
log_id VARCHAR(200) NOT NULL,
timestamp TIMESTAMPTZ NOT NULL,
ingested_at TIMESTAMPTZ DEFAULT NOW(),
level VARCHAR(10),
created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_logs_project_time ON log_metadata(project_id, timestamp DESC, ingested_at DESC);

3.3 API Endpoints

1. Query Logs
   GET /api/logs/query
   Params:
   start_time
   end_time
   level
   service_id
   search (full-text)
   trace_id
   sort: asc|desc
   limit
   search_after: [timestamp, log_id]

2. Log Context (before/after)
   GET /api/logs/{id}/context
   Returns 10 before + 10 after
   Uses search_after, not scroll API

3. Log Volume Sparkline
   GET /api/logs/volume
   Params:
   start_time
   end_time
   level?
   rollup=1m

4. Error Spike Chart
   GET /api/logs/error-spikes
   Params:
   start_time
   end_time
   rollup=1m
   level=ERROR only

3.4 Implementation Notes

Use OpenSearch date_histogram for bucketed volumes

search_after is required for real-time infinite scroll

Add log sampling (INFO=5%, DEBUG=1%, ERROR=unsampled)

4. Ingestion Pipeline
   4.1 Metrics Ingestion
   POST /api/ingest/metrics
   Headers: X-API-Key
   Body:
   {
   "metrics": [
   {
   "name": "latency_ms",
   "value": 125.5,
   "service_id": "...",
   "timestamp": "...",
   "tags": { "endpoint": "/users" }
   }
   ]
   }

Flow:

Validate API key

Extract project_id

Enqueue to RabbitMQ

Return 202

Worker batches writes (1000 records or 1s)

Write to Timescale

4.2 Logs Ingestion
POST /api/ingest/logs
{
"logs": [
{
"level": "ERROR",
"message": "DB failed",
"service_id": "...",
"timestamp": "...",
"trace_id": "abc123",
"context": { "stack": "..." }
}
]
}

Flow:

Validate API key

Enqueue

Worker writes to OpenSearch + log_metadata

4.3 RabbitMQ Queues

metrics.ingest

logs.ingest

metrics.dlq

logs.dlq

alerts.evaluate

5. Alert Engine
   5.1 Rule Evaluation Logic

Every minute:

Fetch all active alert rules

Query metrics from TimescaleDB

Evaluate condition (e.g., p95 > threshold)

Track first_trigger_time in Redis

If sustained for duration_minutes → create incident

Avoid duplicate incidents using unique constraint

5.2 Incident Deduplication

Redis key:

alert:{project}:{rule_id}:state = firing/not_firing

Prevents alert storms.

6. AI Microservice Integration
   6.1 Anomaly Detection

Backend → AI:

POST /internal/ai/anomaly
{
"metric": "...",
"data": [
{ "timestamp": "...", "value": 125 }
],
"method": "zscore"
}

Frontend never calls AI directly.

6.2 Log Pattern Clustering
POST /internal/ai/log-cluster
{
"logs": [...]
}

Stores embeddings in pgvector.

7. Prometheus & Grafana (Internal Only)
   Custom Exporters

Ingestion worker

Alert engine

API gateway

Query service

RDS/Timescale connection pool

Dashboards

Platform health

Query performance

Ingestion performance

Alert engine metrics

8. Technology Stack

Spring Boot (DDD)

TimescaleDB

Postgres (Supabase or RDS)

OpenSearch (AWS)

RabbitMQ

Redis

FastAPI (AI)

S3

Prometheus + Grafana

AWS (EKS, ALB, RDS)

9. Implementation Phases (Corrected)
   Phase 1: Infrastructure & Auth

Stand up Postgres + Timescale + OpenSearch

Implement API key auth + JWT auth + rate limits

RabbitMQ queues

Ingestion worker v1

Redis cluster

Prometheus exporters

Phase 2: Metrics Explorer

Query API

Overlay queries with alignment

Histogram support

Anomaly detection integration

Redis caching

Grafana dashboard for internal validation

Phase 3: Logs Viewer

Log ingestion

search_after pagination

log context

log volume aggregation

clustering integration

sampling

Phase 4: Alert Engine

Rule evaluation

“for duration” logic

incident creation

notifications

alert deduplication

Phase 5: Optimization

Query optimizer + materialized views

Data retention policies

Worker autoscaling

S3 archival

Batch writes

Multi-tenant limits

10. Future Enhancements

WebSocket real-time streams

Adaptive anomaly detection

LLM-based log summarization

OpenTelemetry traces

User-defined dashboards

Scheduled exports
