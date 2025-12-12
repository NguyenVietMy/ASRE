"use client";

import React, { useEffect, useState } from "react";
import { AppNavigation } from "@/components/AppNavigation";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { useProject } from "@/contexts/ProjectContext";
import {
  getProjects,
  createProject,
  deleteProject,
  type Project,
} from "@/lib/api/projects";
import { Button } from "@/components/Button";
import { Plus, Settings, Trash2, ExternalLink, Loader2 } from "lucide-react";
import toast from "react-hot-toast";
import { CreateProjectDialog } from "@/components/projects/CreateProjectDialog";

export default function ProjectsPage() {
  const { user, loading: authLoading } = useAuth();
  const { project, setProject } = useProject();
  const router = useRouter();
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/signin");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (user) {
      loadProjects();
    }
  }, [user]);

  const loadProjects = async () => {
    try {
      setLoading(true);
      const data = await getProjects();
      setProjects(data);
    } catch (error: any) {
      toast.error(error.message || "Failed to load projects");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateProject = async (projectData: {
    name: string;
    description?: string;
  }) => {
    try {
      const newProject = await createProject(projectData);
      toast.success("Project created successfully!");
      setCreateDialogOpen(false);

      // Check if the response includes an ID
      if (newProject?.id) {
        // Update projects list and navigate
        setProjects((prev) => [newProject, ...prev]);
        setProject(newProject);
      } else {
        // If ID is missing, reload projects and find the newly created one
        const updatedProjects = await getProjects();
        setProjects(updatedProjects);

        // Find the most recently created project with matching name
        const createdProject = updatedProjects.find(
          (p) => p.name === projectData.name
        );

        if (createdProject?.id) {
          setProject(createdProject);
        } else {
          // If still not found, just stay on projects list
          router.push("/dashboard/projects");
        }
      }
    } catch (error: any) {
      toast.error(error.message || "Failed to create project");
      throw error;
    }
  };

  const handleDeleteProject = async (
    projectId: string,
    e: React.MouseEvent
  ) => {
    e.stopPropagation();
    if (
      !confirm(
        "Are you sure you want to delete this project? This action cannot be undone."
      )
    ) {
      return;
    }

    try {
      await deleteProject(projectId);
      toast.success("Project deleted successfully");
      setProjects((prev) => prev.filter((p) => p.id !== projectId));
      // If deleted project was selected, clear selection
      if (project?.id === projectId) {
        setProject(null);
      }
    } catch (error: any) {
      toast.error(error.message || "Failed to delete project");
    }
  };

  const handleProjectClick = (project: Project) => {
    setProject(project);
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-background text-foreground">
        <AppNavigation />
        <main className="pt-[112px] flex items-center justify-center min-h-[calc(100vh-112px)]">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </main>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <AppNavigation />
      <main className="pt-[112px]">
        <div className="max-w-[1920px] mx-auto px-6 py-8">
          {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-3xl font-semibold mb-2">Projects</h1>
              <p className="text-muted-foreground">
                Manage your observability projects
              </p>
            </div>
            <Button
              variant="primary"
              icon={Plus}
              onClick={() => setCreateDialogOpen(true)}
            >
              Create Project
            </Button>
          </div>

          {/* Projects Grid */}
          {projects.length === 0 ? (
            <div className="bg-[#0A0A0A] border border-border rounded-lg p-12 text-center">
              <p className="text-muted-foreground mb-4">
                No projects yet. Create your first project to get started.
              </p>
              <Button
                variant="primary"
                icon={Plus}
                onClick={() => setCreateDialogOpen(true)}
              >
                Create Project
              </Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {projects.map((proj) => (
                <div
                  key={proj.id}
                  onClick={() => handleProjectClick(proj)}
                  className={`bg-[#0A0A0A] border rounded-lg p-6 cursor-pointer transition-all hover:border-primary/50 group ${
                    project?.id === proj.id
                      ? "border-primary bg-primary/5"
                      : "border-border"
                  }`}
                >
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold mb-1">
                        {proj.name}
                      </h3>
                      {proj.description && (
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {proj.description}
                        </p>
                      )}
                    </div>
                    <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          router.push(
                            `/dashboard/projects/${proj.id}/settings`
                          );
                        }}
                        className="p-2 hover:bg-secondary rounded-lg transition-colors"
                        title="Settings"
                      >
                        <Settings className="w-4 h-4 text-muted-foreground" />
                      </button>
                      <button
                        onClick={(e) => handleDeleteProject(proj.id, e)}
                        className="p-2 hover:bg-destructive/20 rounded-lg transition-colors"
                        title="Delete"
                      >
                        <Trash2 className="w-4 h-4 text-destructive" />
                      </button>
                    </div>
                  </div>
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>
                      Created {new Date(proj.createdAt).toLocaleDateString()}
                    </span>
                    <ExternalLink className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      <CreateProjectDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSubmit={handleCreateProject}
      />
    </div>
  );
}
