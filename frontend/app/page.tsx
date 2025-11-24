import { Navigation } from "@/components/Navigation";
import { Footer } from "@/components/Footer";
import { Button } from "@/components/Button";
import { FeatureCard } from "@/components/FeatureCard";
import { DashboardMockup } from "@/components/DashboardMockup";
import { MetricsChart } from "@/components/MetricsChart";
import { LogsTable } from "@/components/LogsTable";
import { AISummaryCard } from "@/components/AISummaryCard";
import { AlertRuleCard } from "@/components/AlertRuleCard";
import {
  Activity,
  Bell,
  Sparkles,
  Code,
  ArrowRight,
  Container,
  Boxes,
  Database,
  GitBranch,
  Workflow,
} from "lucide-react";

export default function Home() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <Navigation />

      {/* Hero Section */}
      <section className="relative pt-32 pb-20 px-6 overflow-hidden">
        {/* Background effects */}
        <div className="absolute inset-0 grid-background opacity-20"></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-primary/10 rounded-full blur-3xl"></div>

        <div className="max-w-7xl mx-auto relative z-10">
          <div className="text-center max-w-4xl mx-auto mb-16">
            <h1 className="text-5xl md:text-6xl lg:text-7xl mb-6 bg-gradient-to-r from-white to-white/60 bg-clip-text text-transparent leading-tight">
              AI-Assisted Incident & Performance Platform
            </h1>
            <p className="text-xl md:text-2xl text-muted-foreground mb-8 leading-relaxed">
              Ingest logs and metrics. Detect anomalies. Resolve incidents
              faster with an AI copilot.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button variant="primary">
                Get Started
                <ArrowRight className="w-5 h-5" />
              </Button>
              <Button variant="secondary">View Demo</Button>
            </div>
          </div>

          {/* Hero Dashboard Mockup */}
          <div className="max-w-6xl mx-auto">
            <DashboardMockup />
          </div>
        </div>
      </section>

      {/* Trusted By Section */}
      <section className="py-12 px-6 border-y border-border bg-card/30">
        <div className="max-w-7xl mx-auto">
          <p className="text-center text-sm text-muted-foreground mb-8">
            Trusted by engineering teams at
          </p>
          <div className="flex flex-wrap justify-center items-center gap-12 opacity-40">
            <span className="text-2xl">TechCorp</span>
            <span className="text-2xl">DataFlow</span>
            <span className="text-2xl">CloudScale</span>
            <span className="text-2xl">DevOps Inc</span>
            <span className="text-2xl">SystemPro</span>
          </div>
        </div>
      </section>

      {/* Feature Grid */}
      <section id="features" className="py-20 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-4xl md:text-5xl mb-4">
              Everything you need to manage incidents
            </h2>
            <p className="text-xl text-muted-foreground">
              Built for modern engineering teams who ship fast and need
              reliability
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <FeatureCard
              icon={Activity}
              title="Real-time Telemetry Ingestion"
              description="Stream logs, metrics, and traces from any source. Built for scale."
            />
            <FeatureCard
              icon={Bell}
              title="Intelligent Alert Engine"
              description="Custom rules, smart triggers, and automatic incident creation."
            />
            <FeatureCard
              icon={Sparkles}
              title="AI Incident Copilot"
              description="Root cause analysis, summaries, and suggested actions powered by AI."
            />
            <FeatureCard
              icon={Code}
              title="Developer-First Architecture"
              description="Clean APIs, multi-tenant support, and comprehensive SDKs."
            />
          </div>
        </div>
      </section>

      {/* Deep Feature Section - AI Copilot */}
      <section className="py-20 px-6 bg-card/30">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-4xl md:text-5xl mb-6">
                AI that understands your system
              </h2>
              <div className="space-y-6 text-lg text-muted-foreground">
                <p>
                  OpsPilot's AI Copilot analyzes your entire system to provide
                  actionable insights during incidents.
                </p>
                <div className="space-y-4">
                  <div className="flex items-start gap-3">
                    <div className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center mt-1 flex-shrink-0">
                      <div className="w-2 h-2 rounded-full bg-primary"></div>
                    </div>
                    <div>
                      <h3 className="text-xl text-foreground mb-1">
                        Root Cause Analysis
                      </h3>
                      <p>
                        Automatically identify the source of issues by
                        correlating logs, metrics, and traces.
                      </p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <div className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center mt-1 flex-shrink-0">
                      <div className="w-2 h-2 rounded-full bg-primary"></div>
                    </div>
                    <div>
                      <h3 className="text-xl text-foreground mb-1">
                        Investigative Steps
                      </h3>
                      <p>
                        Get suggested debugging steps based on similar past
                        incidents.
                      </p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <div className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center mt-1 flex-shrink-0">
                      <div className="w-2 h-2 rounded-full bg-primary"></div>
                    </div>
                    <div>
                      <h3 className="text-xl text-foreground mb-1">
                        Anomaly Detection
                      </h3>
                      <p>
                        Machine learning models detect patterns and anomalies in
                        real-time.
                      </p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <div className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center mt-1 flex-shrink-0">
                      <div className="w-2 h-2 rounded-full bg-primary"></div>
                    </div>
                    <div>
                      <h3 className="text-xl text-foreground mb-1">
                        Log Pattern Clustering
                      </h3>
                      <p>
                        Identify new error patterns and surface them before they
                        become critical.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div>
              <AISummaryCard />
            </div>
          </div>
        </div>
      </section>

      {/* Metrics & Logs Section */}
      <section className="py-20 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-4xl md:text-5xl mb-4">
              See everything in one place
            </h2>
            <p className="text-xl text-muted-foreground">
              Unified metrics, logs, and incidents — no context switching.
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <MetricsChart />
            <LogsTable />
          </div>
        </div>
      </section>

      {/* Alert Rule Builder Section */}
      <section className="py-20 px-6 bg-card/30">
        <div className="max-w-4xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-4xl md:text-5xl mb-4">
              Powerful alerting, simple setup
            </h2>
            <p className="text-xl text-muted-foreground">
              Create custom alert rules in seconds with our intuitive builder
            </p>
          </div>
          <AlertRuleCard />
        </div>
      </section>

      {/* DevOps Stack Section */}
      <section className="py-20 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl mb-4">
              Built for real engineering teams
            </h2>
            <p className="text-xl text-muted-foreground">
              Integrates seamlessly with your existing infrastructure
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-8">
            {[
              { icon: Container, name: "Docker" },
              { icon: Boxes, name: "Terraform" },
              { icon: Database, name: "AWS" },
              { icon: GitBranch, name: "GitHub" },
              { icon: Workflow, name: "Kubernetes" },
              { icon: Activity, name: "Kafka" },
            ].map((item, index) => (
              <div
                key={index}
                className="flex flex-col items-center gap-3 p-6 bg-card border border-border rounded-xl hover:border-primary/50 transition-all"
              >
                <item.icon className="w-12 h-12 text-muted-foreground" />
                <span className="text-sm text-muted-foreground">
                  {item.name}
                </span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Banner */}
      <section className="py-20 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="relative overflow-hidden rounded-3xl p-12 text-center">
            {/* Background */}
            <div className="absolute inset-0 gradient-dark"></div>
            <div className="absolute inset-0 grid-background opacity-10"></div>
            <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-primary/20 rounded-full blur-3xl"></div>

            {/* Content */}
            <div className="relative z-10">
              <h2 className="text-4xl md:text-5xl mb-6">
                Catch issues before your users do
              </h2>
              <p className="text-xl text-muted-foreground mb-8">
                Join hundreds of engineering teams using OpsPilot to ship with
                confidence
              </p>
              <Button variant="primary">
                Start Using OpsPilot
                <ArrowRight className="w-5 h-5" />
              </Button>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
