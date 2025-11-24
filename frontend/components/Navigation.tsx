'use client';

import React from 'react';
import { Menu, X } from 'lucide-react';
import { Button } from './Button';

export function Navigation() {
  const [mobileMenuOpen, setMobileMenuOpen] = React.useState(false);

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 glass border-b border-border/50">
      <div className="max-w-7xl mx-auto px-6 py-4">
        <div className="flex items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg gradient-primary flex items-center justify-center">
              <span className="text-white">OP</span>
            </div>
            <span className="text-xl">OpsPilot</span>
          </div>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-8">
            <a href="#features" className="text-muted-foreground hover:text-foreground transition-colors">Features</a>
            <a href="#docs" className="text-muted-foreground hover:text-foreground transition-colors">Docs</a>
            <a href="#pricing" className="text-muted-foreground hover:text-foreground transition-colors">Pricing</a>
          </div>

          {/* CTA Buttons */}
          <div className="hidden md:flex items-center gap-4">
            <a href="/signin" className="text-muted-foreground hover:text-foreground transition-colors">
              Sign In
            </a>
            <Button variant="primary">Get Started</Button>
          </div>

          {/* Mobile Menu Button */}
          <button 
            className="md:hidden text-foreground"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden mt-4 pb-4 flex flex-col gap-4">
            <a href="#features" className="text-muted-foreground hover:text-foreground transition-colors">Features</a>
            <a href="#docs" className="text-muted-foreground hover:text-foreground transition-colors">Docs</a>
            <a href="#pricing" className="text-muted-foreground hover:text-foreground transition-colors">Pricing</a>
            <div className="flex flex-col gap-2 pt-4 border-t border-border">
              <a href="/signin" className="text-left text-muted-foreground hover:text-foreground transition-colors">
                Sign In
              </a>
              <Button variant="primary">Get Started</Button>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
}

