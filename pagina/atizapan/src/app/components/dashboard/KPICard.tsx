// src/components/dashboard/KPICard.tsx
"use client";

import { ReactNode } from "react";

interface KPICardProps {
  title: string;
  value: string | number;
  delta?: number;
  icon?: ReactNode;
  prefix?: string;
  suffix?: string;
}

export default function KPICard({ 
  title, 
  value, 
  delta, 
  icon, 
  prefix = "", 
  suffix = "" 
}: KPICardProps) {
  const getDeltaColor = (delta: number) => {
    if (delta > 0) return "text-[#008D96]";
    if (delta < 0) return "text-red-500";
    return "text-[#969696]";
  };

  const getDeltaIcon = (delta: number) => {
    if (delta > 0) {
      return (
        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M3.293 9.707a1 1 0 010-1.414l6-6a1 1 0 011.414 0l6 6a1 1 0 01-1.414 1.414L10 4.414 4.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
        </svg>
      );
    }
    if (delta < 0) {
      return (
        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M16.707 10.293a1 1 0 010 1.414l-6 6a1 1 0 01-1.414 0l-6-6a1 1 0 111.414-1.414L10 15.586l5.293-5.293a1 1 0 011.414 0z" clipRule="evenodd" />
        </svg>
      );
    }
    return null;
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 px-8 pt-5 h-28 hover:shadow-md transition-shadow duration-200">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center text-sm text-[#969696] mb-2">
            {icon && <span className="mr-2">{icon}</span>}
            {title}
          </div>
          <div className="flex justify-between ">
          <div className="text-2xl font-regular text-[#015463]">
            {prefix}{typeof value === 'number' ? value.toLocaleString() : value}{suffix}
          </div>
          {delta !== undefined && (
            <div className={`flex items-center pt-6 text-xs font-medium ${getDeltaColor(delta)}`}>
              {getDeltaIcon(delta)}
              <span className="ml-1">
                {delta > 0 ? '+' : ''}{delta}%
              </span>
            </div>
          )}</div>
        </div>
      </div>
    </div>
  );
}