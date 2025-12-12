"use client";

import React, { useEffect, useState, useMemo } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  queryLogs,
  type LogEntry,
  type LogQueryResponse,
} from "@/lib/api/logs";
import { getServices } from "@/lib/api/services";
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  flexRender,
  type ColumnDef,
  type SortingState,
} from "@tanstack/react-table";
import { Button } from "@/components/Button";
import {
  Loader2,
  Search,
  Calendar,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import toast from "react-hot-toast";

export default function LogsPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, loading: projectLoading } = useProject();
  const router = useRouter();

  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [services, setServices] = useState<Array<{ id: string; name: string }>>(
    []
  );
  const [nextToken, setNextToken] = useState<string | undefined>();
  const [sorting, setSorting] = useState<SortingState>([]);
  const [searchText, setSearchText] = useState("");
  const [selectedService, setSelectedService] = useState<string>("");
  const [selectedLevel, setSelectedLevel] = useState<string>("");
  const [timeRange, setTimeRange] = useState<{ start: Date; end: Date }>({
    start: new Date(Date.now() - 1 * 60 * 60 * 1000), // 1 hour ago
    end: new Date(),
  });

  const columns = useMemo<ColumnDef<LogEntry>[]>(
    () => [
      {
        accessorKey: "timestamp",
        header: "Timestamp",
        cell: (info) => (
          <span className="text-xs font-mono">
            {new Date(info.getValue() as string).toLocaleString()}
          </span>
        ),
      },
      {
        accessorKey: "level",
        header: "Level",
        cell: (info) => {
          const level = info.getValue() as string;
          const colors = {
            ERROR: "text-destructive",
            WARN: "text-yellow-500",
            INFO: "text-primary",
            DEBUG: "text-muted-foreground",
          };
          return (
            <span
              className={`text-xs font-medium ${
                colors[level as keyof typeof colors] || ""
              }`}
            >
              {level}
            </span>
          );
        },
      },
      {
        accessorKey: "message",
        header: "Message",
        cell: (info) => (
          <span className="text-sm font-mono max-w-md truncate block">
            {info.getValue() as string}
          </span>
        ),
      },
      {
        accessorKey: "serviceId",
        header: "Service",
        cell: (info) => {
          const serviceId = info.getValue() as string | undefined;
          if (!serviceId)
            return <span className="text-muted-foreground">-</span>;
          const service = services.find((s) => s.id === serviceId);
          return <span className="text-xs">{service?.name || serviceId}</span>;
        },
      },
      {
        accessorKey: "traceId",
        header: "Trace ID",
        cell: (info) => {
          const traceId = info.getValue() as string | undefined;
          if (!traceId) return <span className="text-muted-foreground">-</span>;
          return (
            <span className="text-xs font-mono text-primary cursor-pointer hover:underline">
              {traceId.substring(0, 8)}...
            </span>
          );
        },
      },
    ],
    [services]
  );

  const table = useReactTable({
    data: logs,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    state: {
      sorting,
    },
    onSortingChange: setSorting,
  });

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (project && user) {
      loadServices();
      loadLogs();
    }
  }, [project, user]);

  const loadServices = async () => {
    if (!project) return;
    try {
      const data = await getServices(project.id);
      setServices(data.map((s) => ({ id: s.id, name: s.name })));
    } catch (error) {
      // Silently fail - services filter is optional
    }
  };

  const loadLogs = async (paginationToken?: string) => {
    if (!project) return;
    try {
      setLoading(true);
      const response: LogQueryResponse = await queryLogs(project.id, {
        startTime: timeRange.start.toISOString(),
        endTime: timeRange.end.toISOString(),
        searchText: searchText || undefined,
        serviceId: selectedService || undefined,
        level: (selectedLevel as any) || undefined,
        limit: 50,
        paginationToken,
      });
      setLogs(response.logs);
      setNextToken(response.nextToken);
    } catch (error: any) {
      toast.error(error.message || "Failed to load logs");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    loadLogs();
  };

  const handleTimeRangeChange = (hours: number) => {
    const end = new Date();
    const start = new Date(end.getTime() - hours * 60 * 60 * 1000);
    setTimeRange({ start, end });
  };

  useEffect(() => {
    if (project) {
      loadLogs();
    }
  }, [timeRange]);

  if (authLoading || projectLoading) {
    return (
      <div className="min-h-screen bg-background text-foreground">
        <AppNavigation />
        <main className="pt-[112px] flex items-center justify-center min-h-[calc(100vh-112px)]">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </main>
      </div>
    );
  }

  if (!user || !project) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          <div className="mb-8">
            <h1 className="text-3xl font-semibold mb-2">Logs Viewer</h1>
            <p className="text-muted-foreground">
              Search and analyze logs from {project.name}
            </p>
          </div>

          {/* Filters */}
          <div className="bg-[#0A0A0A] border border-border rounded-lg p-6 mb-6">
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Search
                  </label>
                  <div className="flex gap-2">
                    <div className="relative flex-1">
                      <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                      <input
                        type="text"
                        value={searchText}
                        onChange={(e) => setSearchText(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                        placeholder="Search logs..."
                        className="w-full pl-9 pr-4 py-2 bg-secondary border border-border rounded-lg text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                      />
                    </div>
                    <Button
                      variant="primary"
                      icon={Search}
                      onClick={handleSearch}
                      disabled={loading}
                    >
                      Search
                    </Button>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Service
                  </label>
                  <select
                    value={selectedService}
                    onChange={(e) => setSelectedService(e.target.value)}
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                  >
                    <option value="">All Services</option>
                    {services.map((service) => (
                      <option key={service.id} value={service.id}>
                        {service.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">
                    Level
                  </label>
                  <select
                    value={selectedLevel}
                    onChange={(e) => setSelectedLevel(e.target.value)}
                    className="w-full bg-secondary border border-border rounded-lg px-4 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
                  >
                    <option value="">All Levels</option>
                    <option value="ERROR">ERROR</option>
                    <option value="WARN">WARN</option>
                    <option value="INFO">INFO</option>
                    <option value="DEBUG">DEBUG</option>
                  </select>
                </div>
              </div>

              {/* Time Range */}
              <div>
                <label className="block text-sm font-medium mb-2">
                  Time Range
                </label>
                <div className="flex items-center gap-2 flex-wrap">
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(1)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 1 hour
                  </button>
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(6)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 6 hours
                  </button>
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(12)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 12 hours
                  </button>
                  <button
                    type="button"
                    onClick={() => handleTimeRangeChange(24)}
                    className="px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-secondary transition-colors"
                  >
                    Last 24 hours
                  </button>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground ml-4">
                    <Calendar className="w-4 h-4" />
                    <span>
                      {timeRange.start.toLocaleString()} -{" "}
                      {timeRange.end.toLocaleString()}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Table */}
          <div className="bg-[#0A0A0A] border border-border rounded-lg overflow-hidden">
            {loading ? (
              <div className="flex items-center justify-center py-12">
                <Loader2 className="w-8 h-8 animate-spin text-primary" />
              </div>
            ) : logs.length === 0 ? (
              <div className="flex items-center justify-center py-12 text-muted-foreground">
                No logs found
              </div>
            ) : (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-secondary border-b border-border">
                      {table.getHeaderGroups().map((headerGroup) => (
                        <tr key={headerGroup.id}>
                          {headerGroup.headers.map((header) => (
                            <th
                              key={header.id}
                              className="px-4 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider"
                            >
                              {header.isPlaceholder ? null : (
                                <div
                                  className={
                                    header.column.getCanSort()
                                      ? "cursor-pointer select-none flex items-center gap-2"
                                      : ""
                                  }
                                  onClick={header.column.getToggleSortingHandler()}
                                >
                                  {flexRender(
                                    header.column.columnDef.header,
                                    header.getContext()
                                  )}
                                  {{
                                    asc: " ↑",
                                    desc: " ↓",
                                  }[header.column.getIsSorted() as string] ??
                                    null}
                                </div>
                              )}
                            </th>
                          ))}
                        </tr>
                      ))}
                    </thead>
                    <tbody className="divide-y divide-border">
                      {table.getRowModel().rows.map((row) => (
                        <tr
                          key={row.id}
                          className="hover:bg-secondary/50 transition-colors"
                        >
                          {row.getVisibleCells().map((cell) => (
                            <td key={cell.id} className="px-4 py-3">
                              {flexRender(
                                cell.column.columnDef.cell,
                                cell.getContext()
                              )}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Pagination */}
                {nextToken && (
                  <div className="flex items-center justify-between px-4 py-3 border-t border-border">
                    <p className="text-sm text-muted-foreground">
                      Showing {logs.length} logs
                    </p>
                    <Button
                      variant="secondary"
                      onClick={() => loadLogs(nextToken)}
                      disabled={loading}
                      icon={ChevronRight}
                    >
                      Load More
                    </Button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
