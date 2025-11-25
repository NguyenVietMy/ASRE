# OpsPilot - Backend Implementation Plan

## Overview

This document outlines the backend implementation plan for the Metrics Explorer and Logs Viewer features in OpsPilot, aligned with the existing system architecture.

## Architecture

### Data Storage

- **PostgreSQL (RDS)**: Users, incidents, rules, projects, API keys
- **TimescaleDB**: Time-series metrics storage with hypertables
- **OpenSearch**: Full-text log indexing and search
- **Supabase pgvector**: Embedding store for log pattern clustering
- **Redis**: Rate limits, caching, session management
- **S3**: Runbook markdown files, data exports

### Data Flow

```
Client SDK → Ingestion API → RabbitMQ → Ingestion Worker → TimescaleDB/OpenSearch
                                                              ↓
Frontend Dashboard ← API Gateway ← App Backend ← PostgreSQL
                                                              ↓
Alert Engine → Rule Evaluation → Incident Creation → PostgreSQL
                                                              ↓
AI Microservice ← Incident Context (metrics+logs) ← App Backend
```

### Observability Stack

- **Prometheus Exporters**: Export metrics from TimescaleDB, OpenSearch, and system components
- **Grafana Dashboards**: Visualize platform metrics, ingestion health, worker performance
- **CloudWatch**: AWS infrastructure metrics and alarms

## Metrics Explorer Backend

### Database Schema

#### TimescaleDB Metrics Hypertable

```sql
CREATE TABLE metrics (
  id BIGSERIAL,
  project_id UUID NOT NULL REFERENCES projects(id),
  timestamp TIMESTAMPTZ NOT NULL,
  metric_name VARCHAR(100) NOT NULL,
  metric_value DOUBLE PRECISION NOT NULL,
  service_name VARCHAR(100),
  tags JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Convert to hypertable for time-series optimization
SELECT create_hypertable('metrics', 'timestamp');

-- Indexes for efficient queries
CREATE INDEX idx_metrics_project_time_name ON metrics(project_id, timestamp DESC, metric_name);
CREATE INDEX idx_metrics_service ON metrics(project_id, service_name, timestamp DESC);
CREATE INDEX idx_metrics_tags ON metrics USING gin(tags);
```

#### Metric Types

- **Latency Metrics**: `latency_ms_p50`, `latency_ms_p95`, `latency_ms_p99`
- **Error Metrics**: `request_count`, `throughput`, `concurrency`, `error_rate`
- **System Metrics**: `cpu_usage`, `memory_usage`, `disk_io`, `db_connections`

### API Endpoints

#### 1. Query Metrics (with multi-tenancy)

```
GET /api/metrics/query
Headers:
  - Authorization: Bearer <token>
  - X-Project-ID: <project_id> (or from API key)
Query Parameters:
  - metric_name: string (required)
  - start_time: ISO 8601 timestamp
  - end_time: ISO 8601 timestamp
  - service_name: string (optional)
  - aggregation: string (avg, sum, min, max, p95, p99)
  - time_bucket: string (1m, 5m, 1h, 1d)
```

#### 2. Query Multiple Metrics (Overlay)

```
POST /api/metrics/query-multiple
Body: {
  metrics: [
    { name: "latency_ms_p95", service: "api" },
    { name: "error_rate", service: "api" }
  ],
  start_time: "2024-01-01T00:00:00Z",
  end_time: "2024-01-01T23:59:59Z",
  time_bucket: "1m"
}
```

#### 3. Anomaly Detection (via AI Microservice)

```
GET /api/metrics/anomalies
Query Parameters:
  - metric_name: string
  - start_time: ISO 8601 timestamp
  - end_time: ISO 8601 timestamp
  - threshold: number (sigma deviation, default: 3)

Note: Delegates to AI Microservice /anomaly endpoint
```

#### 4. Latency Histogram

```
GET /api/metrics/histogram
Query Parameters:
  - metric_name: string (latency_ms_p95, etc.)
  - start_time: ISO 8601 timestamp
  - end_time: ISO 8601 timestamp
  - bins: number (default: 20)
```

### Implementation Details

1. **Time-bucketed Aggregation**: Use TimescaleDB's `time_bucket()` function for efficient time-series queries
2. **Anomaly Detection**: AI Microservice uses z-score and IsolationForest algorithms
3. **Caching**: Cache frequently accessed metric queries (Redis) with TTL based on time range
4. **Multi-tenancy**: All queries filtered by `project_id` for data isolation

## Logs Viewer Backend

### Database Schema

#### OpenSearch Log Index

```json
{
  "mappings": {
    "properties": {
      "project_id": { "type": "keyword" },
      "timestamp": { "type": "date" },
      "level": { "type": "keyword" },
      "message": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "service_name": { "type": "keyword" },
      "context": { "type": "object" },
      "trace_id": { "type": "keyword" },
      "correlation_id": { "type": "keyword" }
    }
  }
}
```

#### PostgreSQL Log Metadata (for fast lookups)

```sql
CREATE TABLE log_metadata (
  id BIGSERIAL PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES projects(id),
  log_id VARCHAR(100) NOT NULL, -- Reference to OpenSearch document ID
  timestamp TIMESTAMPTZ NOT NULL,
  level VARCHAR(10) NOT NULL,
  service_name VARCHAR(100),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_log_metadata_project_time ON log_metadata(project_id, timestamp DESC);
CREATE INDEX idx_log_metadata_level ON log_metadata(project_id, level, timestamp DESC);
```

### API Endpoints

#### 1. Query Logs (with full-text search)

```
GET /api/logs/query
Headers:
  - Authorization: Bearer <token>
  - X-Project-ID: <project_id>
Query Parameters:
  - start_time: ISO 8601 timestamp
  - end_time: ISO 8601 timestamp
  - level: string (INFO, WARN, ERROR, DEBUG)
  - service_name: string
  - search: string (full-text search in OpenSearch)
  - trace_id: string (correlation ID)
  - limit: number (default: 100)
  - offset: number (default: 0)
```

#### 2. Log Context

```
GET /api/logs/{log_id}/context
Returns: 10 logs before and 10 logs after the specified log
Uses: OpenSearch scroll API for efficient pagination
```

#### 3. Log Volume Sparkline

```
GET /api/logs/volume
Query Parameters:
  - start_time: ISO 8601 timestamp
  - end_time: ISO 8601 timestamp
  - level: string (optional, filter by level)
  - time_bucket: string (1m, 5m, 1h)

Uses: OpenSearch date_histogram aggregation
```

#### 4. Error Spike Chart

```
GET /api/logs/error-spikes
Query Parameters:
  - start_time: ISO 8601 timestamp
  - end_time: ISO 8601 timestamp
  - time_bucket: string (default: 1m)

Filters: level = ERROR, aggregates by time bucket
```

### Implementation Details

1. **Full-text Search**: Use OpenSearch's native text search capabilities
2. **Pagination**: Use OpenSearch scroll API for large result sets
3. **Log Aggregation**: Use OpenSearch date_histogram aggregation for sparkline performance
4. **Multi-tenancy**: All queries filtered by `project_id` in OpenSearch query

## Ingestion Endpoints

### Metrics Ingestion (with API Key Auth)

```
POST /api/ingest/metrics
Headers:
  - X-API-Key: <api_key>
Body: {
  metrics: [
    {
      name: "latency_ms_p95",
      value: 125.5,
      service: "api",
      timestamp: "2024-01-01T12:00:00Z",
      tags: { "endpoint": "/users", "method": "GET" }
    }
  ]
}

Flow:
1. Validate API key → extract project_id
2. Enqueue to RabbitMQ (async processing)
3. Return 202 Accepted
4. Ingestion Worker processes batch → writes to TimescaleDB
```

### Logs Ingestion (with API Key Auth)

```
POST /api/ingest/logs
Headers:
  - X-API-Key: <api_key>
Body: {
  logs: [
    {
      level: "ERROR",
      message: "Database connection failed",
      service: "api",
      timestamp: "2024-01-01T12:00:00Z",
      context: { "error_code": "DB_001", "stack_trace": "..." },
      trace_id: "abc123" (optional)
    }
  ]
}

Flow:
1. Validate API key → extract project_id
2. Enqueue to RabbitMQ (async processing)
3. Return 202 Accepted
4. Ingestion Worker processes batch → writes to OpenSearch + PostgreSQL metadata
```

## Async Processing Layer

### RabbitMQ Queues

- **metrics.ingest**: Metrics ingestion queue
- **logs.ingest**: Logs ingestion queue
- **alerts.evaluate**: Alert rule evaluation queue

### Ingestion Worker

- Consumes from `metrics.ingest` and `logs.ingest` queues
- Batches writes for efficiency (1000 records per batch)
- Handles backpressure and retries
- Exposes Prometheus metrics for monitoring

### Alert Engine

- Periodically evaluates alert rules (every 1 minute)
- Queries TimescaleDB for metric thresholds
- Creates incidents in PostgreSQL when rules trigger
- Sends notifications (email/Slack/webhook)

## AI Microservice Integration

### Anomaly Detection

```
POST /ai/anomaly
Body: {
  metric_name: "latency_ms_p95",
  data_points: [{ timestamp: "...", value: 125.5 }, ...],
  method: "z-score" | "isolation_forest"
}

Returns: {
  anomalies: [
    { timestamp: "...", value: 250.0, deviation: 4.2 }
  ]
}
```

### Log Pattern Clustering

```
POST /ai/cluster
Body: {
  logs: [{ message: "...", level: "ERROR" }, ...]
}

Returns: {
  clusters: [
    { pattern: "Database connection failed", count: 150, logs: [...] }
  ]
}
```

## Prometheus & Grafana Integration

### Prometheus Exporters

1. **TimescaleDB Exporter**: Exports query performance, ingestion rates
2. **OpenSearch Exporter**: Exports search latency, index sizes
3. **Ingestion Worker Exporter**: Exports queue depth, processing rates, errors
4. **Alert Engine Exporter**: Exports rule evaluation latency, incident creation rate
5. **API Gateway Exporter**: Exports request rates, latency, error rates

### Grafana Dashboards

1. **Platform Health Dashboard**:

   - Ingestion API latency and throughput
   - Worker queue depths and processing rates
   - Database connection pool usage
   - Error rates by component

2. **Ingestion Performance Dashboard**:

   - Metrics ingestion rate (per project)
   - Logs ingestion rate (per project)
   - Batch write performance
   - Queue backlog metrics

3. **Query Performance Dashboard**:

   - Metric query latency (p50, p95, p99)
   - Log search latency
   - Cache hit rates
   - Database query times

4. **Alert Engine Dashboard**:
   - Rule evaluation frequency
   - Incident creation rate
   - Notification delivery success rate

## Technology Stack

- **API Framework**: Spring Boot (Java) - following existing DDD architecture
- **Database**: PostgreSQL (RDS) + TimescaleDB extension
- **Search Engine**: OpenSearch (AWS managed)
- **Message Queue**: RabbitMQ
- **Caching**: Redis
- **AI Service**: FastAPI (Python) - separate microservice
- **Vector Store**: Supabase pgvector
- **Object Storage**: S3
- **Observability**: Prometheus + Grafana
- **Infrastructure**: AWS EKS, ALB, RDS

## Implementation Phases

### Phase 1: Core Infrastructure & Ingestion

1. Set up TimescaleDB hypertables for metrics
2. Set up OpenSearch index for logs
3. Implement API key authentication middleware
4. Create RabbitMQ queues and ingestion worker
5. Implement basic ingestion endpoints with async processing
6. Set up Prometheus exporters for core components

### Phase 2: Metrics Explorer

1. Implement metric query APIs with multi-tenancy
2. Add time-bucketed aggregation using TimescaleDB functions
3. Integrate with AI Microservice for anomaly detection
4. Add histogram endpoint
5. Implement caching layer (Redis)
6. Set up Grafana dashboards for metrics query performance

### Phase 3: Logs Viewer

1. Implement log query APIs with OpenSearch integration
2. Add full-text search capability
3. Implement log context API using OpenSearch scroll
4. Add log volume aggregation endpoints
5. Integrate with AI Microservice for log pattern clustering
6. Set up Grafana dashboards for log search performance

### Phase 4: Alert Engine & Integration

1. Implement alert rule evaluation engine
2. Create incident management APIs
3. Integrate notification channels (email/Slack/webhook)
4. Add Prometheus metrics for alert engine
5. Create Grafana dashboard for alert performance

### Phase 5: Optimization & Scale

1. Optimize database indexes and query patterns
2. Implement query result caching strategies
3. Add rate limiting and throttling per API key
4. Implement data retention policies
5. Add batch ingestion optimizations
6. Scale workers horizontally based on queue depth

## Future Enhancements

- **Real-time Streaming**: WebSocket support for live metric/log updates
- **Advanced Anomaly Detection**: ML models for adaptive thresholds
- **Log Pattern Intelligence**: LLM-based pattern detection and summarization
- **Trace Correlation**: Distributed tracing integration (OpenTelemetry)
- **Custom Dashboards**: User-configurable Grafana dashboards
- **Data Export**: Scheduled exports to S3 for archival/analysis
