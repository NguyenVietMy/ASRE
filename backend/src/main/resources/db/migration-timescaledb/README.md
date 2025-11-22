# TimescaleDB Migrations

This directory contains migrations that require TimescaleDB extension.

These migrations are **NOT** automatically run by Flyway. They are kept separate from the main migrations directory (`db/migration`) so they won't execute on databases without TimescaleDB (like Supabase).

## Usage

### To run these migrations on a TimescaleDB instance:

1. **Option 1: Copy to main migration directory**

   ```bash
   cp V2__TimescaleDB_tables.sql ../migration/
   ```

   Then restart your application - Flyway will run it automatically.

2. **Option 2: Run manually**

   ```bash
   psql -h your-timescaledb-host -U postgres -d your_database -f V2__TimescaleDB_tables.sql
   ```

3. **Option 3: Configure Flyway to use this directory**
   Update `application.yaml`:
   ```yaml
   spring:
     flyway:
       locations: classpath:db/migration,classpath:db/migration-timescaledb
   ```
   (Only do this when connecting to a TimescaleDB instance)

## What's in V2?

- TimescaleDB extension creation
- `metrics` table (converted to hypertable)
- `log_entries` table
- `anomaly_detection_results` table
- Appropriate indexes for time-series queries

## Prerequisites

- TimescaleDB extension must be installed on your PostgreSQL instance
- V1\_\_Initial_schema.sql must be run first (creates the `services` table that these tables reference)
