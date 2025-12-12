"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { Project, getProject } from "@/lib/api/projects";

interface ProjectContextType {
  project: Project | null;
  loading: boolean;
  setProject: (project: Project | null) => void;
  refreshProject: () => Promise<void>;
}

const ProjectContext = createContext<ProjectContextType | undefined>(undefined);

export function ProjectProvider({ children }: { children: React.ReactNode }) {
  const [project, setProjectState] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const pathname = usePathname();

  // Extract project ID from URL
  const extractProjectIdFromUrl = (path: string): string | null => {
    // Match patterns like /dashboard/projects/[id] or /dashboard/projects/[id]/...
    const match = path.match(/\/dashboard\/projects\/([^/]+)/);
    return match ? match[1] : null;
  };

  // Load project from URL
  useEffect(() => {
    const projectId = extractProjectIdFromUrl(pathname || "");

    if (projectId) {
      // Only load if it's a different project
      if (project?.id !== projectId) {
        loadProject(projectId);
      } else {
        setLoading(false);
      }
    } else {
      // No project in URL, clear context
      if (project) {
        setProjectState(null);
      }
      setLoading(false);
    }
  }, [pathname]);

  const loadProject = async (projectId: string) => {
    try {
      setLoading(true);
      const projectData = await getProject(projectId);
      setProjectState(projectData);
    } catch (error) {
      setProjectState(null);
      // Optionally redirect to projects list on error
      // router.push("/dashboard/projects");
    } finally {
      setLoading(false);
    }
  };

  const setProject = (newProject: Project | null) => {
    setProjectState(newProject);

    // If setting a project, navigate to its URL
    if (newProject && newProject.id) {
      router.push(`/dashboard/projects/${newProject.id}`);
    } else {
      router.push("/dashboard/projects");
    }
  };

  const refreshProject = async () => {
    if (project?.id) {
      await loadProject(project.id);
    }
  };

  return (
    <ProjectContext.Provider
      value={{ project, loading, setProject, refreshProject }}
    >
      {children}
    </ProjectContext.Provider>
  );
}

export function useProject() {
  const context = useContext(ProjectContext);
  if (context === undefined) {
    throw new Error("useProject must be used within a ProjectProvider");
  }
  return context;
}
