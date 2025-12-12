import React from "react";

interface SkeletonProps {
  className?: string;
  variant?: "text" | "rectangular" | "circular";
  width?: string | number;
  height?: string | number;
}

export function Skeleton({
  className = "",
  variant = "rectangular",
  width,
  height,
}: SkeletonProps) {
  const baseClasses = "animate-pulse bg-secondary rounded";

  const variantClasses = {
    text: "h-4",
    rectangular: "",
    circular: "rounded-full",
  };

  const style: React.CSSProperties = {};
  if (width) style.width = typeof width === "number" ? `${width}px` : width;
  if (height)
    style.height = typeof height === "number" ? `${height}px` : height;

  return (
    <div
      className={`${baseClasses} ${variantClasses[variant]} ${className}`}
      style={style}
    />
  );
}

export function SkeletonCard() {
  return (
    <div className="bg-[#0A0A0A] border border-border rounded-lg p-6">
      <Skeleton variant="text" width="60%" className="mb-4" />
      <Skeleton variant="text" width="40%" className="mb-2" />
      <Skeleton variant="text" width="80%" />
    </div>
  );
}

export function SkeletonTable() {
  return (
    <div className="bg-[#0A0A0A] border border-border rounded-lg overflow-hidden">
      <div className="p-4 border-b border-border">
        <Skeleton variant="text" width="20%" className="mb-2" />
        <Skeleton variant="text" width="40%" />
      </div>
      <div className="p-4 space-y-3">
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="flex items-center gap-4">
            <Skeleton variant="text" width="15%" />
            <Skeleton variant="text" width="20%" />
            <Skeleton variant="text" width="40%" />
            <Skeleton variant="text" width="15%" />
            <Skeleton variant="text" width="10%" />
          </div>
        ))}
      </div>
    </div>
  );
}
