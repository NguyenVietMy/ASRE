import React from 'react';
import { Github, Twitter, Linkedin } from 'lucide-react';

export function Footer() {
  return (
    <footer className="border-t border-border bg-background">
      <div className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 rounded-lg gradient-primary flex items-center justify-center">
                <span className="text-white">OP</span>
              </div>
              <span className="text-xl">OpsPilot</span>
            </div>
            <p className="text-sm text-muted-foreground">
              AI-Assisted Incident & Performance Platform for modern engineering teams.
            </p>
          </div>

          {/* Product */}
          <div>
            <h4 className="mb-4">Product</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Dashboard</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Incidents</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Metrics</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Logs</a></li>
            </ul>
          </div>

          {/* Docs */}
          <div>
            <h4 className="mb-4">Docs</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">API Reference</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Deploying</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Guides</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Integrations</a></li>
            </ul>
          </div>

          {/* Company */}
          <div>
            <h4 className="mb-4">Company</h4>
            <ul className="space-y-2">
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">About</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">GitHub</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Contact</a></li>
              <li><a href="#" className="text-sm text-muted-foreground hover:text-foreground transition-colors">Careers</a></li>
            </ul>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="flex flex-col md:flex-row justify-between items-center pt-8 border-t border-border gap-4">
          <p className="text-sm text-muted-foreground">
            © 2024 OpsPilot. All rights reserved.
          </p>
          <div className="flex gap-4">
            <a href="#" className="text-muted-foreground hover:text-foreground transition-colors">
              <Github className="w-5 h-5" />
            </a>
            <a href="#" className="text-muted-foreground hover:text-foreground transition-colors">
              <Twitter className="w-5 h-5" />
            </a>
            <a href="#" className="text-muted-foreground hover:text-foreground transition-colors">
              <Linkedin className="w-5 h-5" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}

