"use client";

import React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/Button";
import { X } from "lucide-react";

const createAlertRuleSchema = z.object({
  name: z
    .string()
    .min(1, "Name is required")
    .min(3, "Name must be at least 3 characters"),
  description: z.string().max(500).optional(),
  serviceId: z.string().optional(),
  metricName: z.string().min(1, "Metric name is required"),
  condition: z.object({
    operator: z.enum(["GT", "LT", "EQ", "GTE", "LTE"]),
    threshold: z.number().min(0),
    duration: z.string().min(1, "Duration is required"),
  }),
  severity: z.enum(["LOW", "MEDIUM", "HIGH", "CRITICAL"]),
  enabled: z.boolean().default(true),
});

type CreateAlertRuleFormData = z.infer<typeof createAlertRuleSchema>;

interface CreateAlertRuleDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: CreateAlertRuleFormData) => Promise<void>;
  services: Array<{ id: string; name: string }>;
}

export function CreateAlertRuleDialog({
  open,
  onClose,
  onSubmit,
  services,
}: CreateAlertRuleDialogProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<CreateAlertRuleFormData>({
    resolver: zodResolver(createAlertRuleSchema),
    defaultValues: {
      enabled: true,
      severity: "MEDIUM",
      condition: {
        operator: "GT",
        threshold: 0,
        duration: "5m",
      },
    },
  });

  const handleFormSubmit = async (data: CreateAlertRuleFormData) => {
    try {
      await onSubmit(data);
      reset();
    } catch (error) {
      // Error already handled in parent
    }
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
      />
      <div className="relative z-10 bg-[#0A0A0A] border border-border rounded-lg p-6 w-full max-w-2xl shadow-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold">Create Alert Rule</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-secondary rounded-lg transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="name" className="block text-sm font-medium mb-2">
                Rule Name *
              </label>
              <input
                id="name"
                type="text"
                {...register("name")}
                className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-destructive">
                  {errors.name.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="severity"
                className="block text-sm font-medium mb-2"
              >
                Severity *
              </label>
              <select
                id="severity"
                {...register("severity")}
                className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
          </div>

          <div>
            <label
              htmlFor="description"
              className="block text-sm font-medium mb-2"
            >
              Description
            </label>
            <textarea
              id="description"
              {...register("description")}
              rows={2}
              className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all resize-none"
            />
          </div>

          <div>
            <label
              htmlFor="serviceId"
              className="block text-sm font-medium mb-2"
            >
              Service (Optional)
            </label>
            <select
              id="serviceId"
              {...register("serviceId")}
              className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
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
            <label
              htmlFor="metricName"
              className="block text-sm font-medium mb-2"
            >
              Metric Name *
            </label>
            <input
              id="metricName"
              type="text"
              {...register("metricName")}
              placeholder="e.g., http_server_duration"
              className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
            />
            {errors.metricName && (
              <p className="mt-1 text-sm text-destructive">
                {errors.metricName.message}
              </p>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label
                htmlFor="operator"
                className="block text-sm font-medium mb-2"
              >
                Operator *
              </label>
              <select
                id="operator"
                {...register("condition.operator")}
                className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
              >
                <option value="GT">Greater Than</option>
                <option value="GTE">Greater Than or Equal</option>
                <option value="LT">Less Than</option>
                <option value="LTE">Less Than or Equal</option>
                <option value="EQ">Equal</option>
              </select>
            </div>

            <div>
              <label
                htmlFor="threshold"
                className="block text-sm font-medium mb-2"
              >
                Threshold *
              </label>
              <input
                id="threshold"
                type="number"
                step="0.01"
                {...register("condition.threshold", { valueAsNumber: true })}
                className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
              />
              {errors.condition?.threshold && (
                <p className="mt-1 text-sm text-destructive">
                  {errors.condition.threshold.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="duration"
                className="block text-sm font-medium mb-2"
              >
                Duration *
              </label>
              <input
                id="duration"
                type="text"
                {...register("condition.duration")}
                placeholder="e.g., 5m, 1h"
                className="w-full bg-secondary border border-border rounded-lg px-4 py-3 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all"
              />
              {errors.condition?.duration && (
                <p className="mt-1 text-sm text-destructive">
                  {errors.condition.duration.message}
                </p>
              )}
            </div>
          </div>

          <div className="flex items-center gap-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={onClose}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={isSubmitting}
              className="flex-1"
            >
              {isSubmitting ? "Creating..." : "Create Rule"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
