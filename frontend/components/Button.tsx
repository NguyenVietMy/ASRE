"use client";

import React from "react";
import { LucideIcon } from "lucide-react";

interface ButtonProps {
  children: React.ReactNode;
  variant?: "primary" | "secondary" | "icon";
  icon?: LucideIcon;
  onClick?: () => void;
  className?: string;
  type?: "button" | "submit" | "reset";
  disabled?: boolean;
}

export function Button({
  children,
  variant = "primary",
  icon: Icon,
  onClick,
  className = "",
  type = "button",
  disabled = false,
}: ButtonProps) {
  const baseClasses =
    "px-6 py-3 rounded-lg transition-all duration-200 cursor-pointer inline-flex items-center gap-2 justify-center disabled:opacity-50 disabled:cursor-not-allowed";

  const variantClasses = {
    primary:
      "bg-primary text-primary-foreground hover:shadow-[0_0_30px_rgba(91,120,255,0.5)] hover:scale-105",
    secondary:
      "border border-primary/50 text-foreground hover:bg-primary/10 hover:border-primary backdrop-blur-sm",
    icon: "w-8 h-8 p-0 bg-secondary hover:bg-primary/20 rounded-lg",
  };

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`${baseClasses} ${variantClasses[variant]} ${className}`}
    >
      {Icon && <Icon className={variant === "icon" ? "w-4 h-4" : "w-5 h-5"} />}
      {variant !== "icon" && children}
    </button>
  );
}
