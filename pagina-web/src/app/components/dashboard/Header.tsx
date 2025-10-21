// src/components/dashboard/Header.tsx
"use client";

import { Bars3Icon } from "@heroicons/react/24/outline";

interface HeaderProps {
  title: string;
  subtitle?: string;
  actions?: React.ReactNode;
  onMenuClick: () => void;
}

export default function Header({ title, subtitle, actions, onMenuClick }: HeaderProps) {
  return (
    <header className="pt-12">
      <div className="px-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            {/* Mobile menu button */}
            <button
              onClick={onMenuClick}
              className="lg:hidden p-2 rounded-md hover:bg-gray-100 mr-3"
            >
              <Bars3Icon className="h-6 w-6 text-gray-500" />
            </button>
            
            <div>
              <h1 className="text-2xl font-regular text-[#4B4C7E]">{title}</h1>
              {subtitle && (
                <p className="text-sm text-[#969696] mt-1">{subtitle}</p>
              )}
            </div>
          </div>
          
          {actions && (
            <div className="flex items-center space-x-3">
              {actions}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}