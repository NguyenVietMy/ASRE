# Functional Requirements Plan – OpsPilot / AutoSRE

## 1. Accounts, Authentication & Projects

### 1.1 User Accounts (MVP)

- Users can register, log in, and log out.
- Users can reset passwords via email.
- System stores:
  - User ID, email, name (optional), creation time, and per-project roles.

### 1.2 Projects & Multi-Tenancy (MVP)

- Users can create multiple projects.
- Each project has:
  - Project ID, name, creation timestamp.
- Telemetry, incidents, and logs must be isolated per project.

### 1.3 Project Membership & Roles (V2)

- Project owners can invite members by email.
- Roles: owner, editor, viewer.
- Permissions vary by role (owner can manage members; viewer can only view data).

---

## 2. API Keys & Access Control

### 2.1 API Key Management (MVP)

- Each project gets one or more API keys.
- Keys can be regenerated, disabled, and partially masked in UI.
- Ingestion APIs require API key authentication.

### 2.2 Rate Limiting & Quotas (V2)

- Rate limits per API key (requests per second/minute).
- Requests exceeding limits return an error and record the violation.

---

## 3. Telemetry Ingestion

### 3.1 Metrics Ingestion (MVP)

- Endpoint accepts batches of metrics with:
  - Timestamp, metric name, value, service ID, optional tags.
- System validates format and timestamp range.
- System stores metric data for querying.

### 3.2 Log Ingestion (MVP)

- Endpoint accepts batches of logs with:
  - Timestamp, level, message, optional JSON payload, service ID.
- Logs must be filterable by:
  - Time, level, service, search text, and JSON fields.

### 3.3 Traces / Correlation IDs (V2)

- Metrics and logs can include a trace or correlation ID.
- Querying by correlation ID returns all related telemetry.

---

## 4. Metrics Querying & Visualization

### 4.1 Metrics Query API (MVP)

- Query by project, service, metric name, time range.
- Supports aggregations (avg, p95, p99, sum, count).
- Supports grouping (minute/hour).

### 4.2 Metrics Dashboards (MVP)

- UI supports selecting metrics, services, time ranges.
- Shows time-series charts with hover values.
- Saved dashboard views available in V2.

---

## 5. Log Search & Exploration

### 5.1 Log Query API (MVP)

- Query logs by:
  - Time range, level, service, text search, JSON-field filters.

### 5.2 Log Viewer UI (MVP)

- Paginated logs table.
- Expandable log entries showing full JSON/stack trace.
- “Show logs around this timestamp” feature.

---

## 6. Alert Rules & Incident Management

### 6.1 Alert Rule Definition (MVP)

- Users can define rules with:
  - Metric, threshold, comparator, evaluation window, service scope.
- Rules specify notification channels.

### 6.2 Alert Evaluation & Incident Creation (MVP)

- System periodically evaluates rules.
- When rule triggers:
  - Creates new incident or updates an existing open one.

### 6.3 Incident Timeline (MVP)

- Each incident keeps:
  - Metric snapshots, logs, comments, status changes.
- Timeline visible in UI.

### 6.4 Notifications (MVP)

- Emails sent on incident creation/resolution.
- Slack/webhook support in V2.

### 6.5 Incident Lifecycle (MVP)

- Users can change status, add comments.
- Incident list filterable by status, time, service.

---

## 7. AI / ML Features

### 7.1 AI Incident Summary (MVP)

- “Generate AI Summary” produces:
  - Likely root cause, next steps.
- Uses:
  - Nearby metrics, logs, runbook content.
- Users can regenerate summaries.

### 7.2 Knowledge Base & RAG (V2)

- Support runbooks, past incidents, error explanations.
- AI uses RAG to retrieve similar incidents/runbooks during summary.

### 7.3 Anomaly Detection (V4)

- System detects anomalies in metrics based on deviation from baseline.
- Anomalies shown in charts and linked to incidents.

### 7.4 Log Pattern Intelligence (V4)

- System clusters logs into templates.
- Shows top patterns and “new error patterns.”
- Marks never-seen-before stack traces.

---

## 8. Web UI / Dashboard

### 8.1 Project & Service Overview (MVP)

- Shows services derived from telemetry.
- Displays:
  - Recent incidents, current error rate, p95 latency.

### 8.2 Incidents List & Detail (MVP)

- Filter by time, service, status.
- Incident page shows:
  - Timeline, AI summary, metadata.

### 8.3 Settings UI (V2)

- API key management.
- Alert rules configuration.
- Notification channels.
- Project member management.

---

## 9. Integrations & Extensibility

### 9.1 Notifications (MVP/V2)

- MVP: Email.
- V2: Slack / webhook notifications.

### 9.2 Incoming Webhooks (V3)

- External events can be ingested (deployments, feature flags).
- Timelines display these external events.

---

## 10. Platform Observability

### 10.1 Health Status (V3)

- Status page showing ingestion health, worker health, AI service health.

### 10.2 Platform Metrics (V3)

- System exposes its own metrics.
- Alerts on ingestion failures, worker failures, AI latency.

---

## 11. Security & Compliance

### 11.1 Access Control (MVP)

- API key required for ingestion.
- Auth required for dashboard & management.
- Users can only access projects they belong to.

### 11.2 Data Retention (V2)

- Configurable log & metric retention periods per project.
- Expired data must not be queryable.
