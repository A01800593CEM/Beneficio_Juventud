"use client";

import { useState } from "react";

// Simple bar chart component without external dependencies
interface BarChartData {
  label: string;
  value: number;
  color?: string;
}

interface SimpleBarChartProps {
  data: BarChartData[];
  title: string;
  className?: string;
}

export function SimpleBarChart({ data, title, className = "" }: SimpleBarChartProps) {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  const maxValue = Math.max(...data.map(item => item.value));
  const chartHeight = 200;

  return (
    <div className={`bg-white rounded-xl shadow-sm border border-gray-200 p-6 ${className}`}>
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>

      <div className="relative" style={{ height: chartHeight + 40 }}>
        <div className="flex items-end justify-between h-full space-x-2">
          {data.map((item, index) => {
            const height = maxValue > 0 ? (item.value / maxValue) * chartHeight : 0;
            const isHovered = hoveredIndex === index;

            return (
              <div
                key={index}
                className="flex-1 flex flex-col items-center relative"
                onMouseEnter={() => setHoveredIndex(index)}
                onMouseLeave={() => setHoveredIndex(null)}
              >
                {/* Tooltip */}
                {isHovered && (
                  <div className="absolute bottom-full mb-2 bg-gray-800 text-white px-2 py-1 rounded text-xs whitespace-nowrap z-10">
                    {item.value} canjeos
                  </div>
                )}

                {/* Bar */}
                <div
                  className={`w-full transition-all duration-200 rounded-t ${
                    item.color || "bg-[#008D96]"
                  } ${isHovered ? "opacity-80" : ""}`}
                  style={{ height: `${height}px` }}
                />

                {/* Label */}
                <div className="mt-2 text-xs text-gray-600 text-center">
                  {item.label}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// Simple donut chart component
interface DonutChartData {
  label: string;
  value: number;
  color: string;
}

interface SimpleDonutChartProps {
  data: DonutChartData[];
  title: string;
  centerValue?: string;
  centerLabel?: string;
  className?: string;
}

export function SimpleDonutChart({
  data,
  title,
  centerValue,
  centerLabel,
  className = ""
}: SimpleDonutChartProps) {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  const total = data.reduce((sum, item) => sum + item.value, 0);
  const size = 160;
  const strokeWidth = 30;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;

  let currentOffset = 0;

  return (
    <div className={`bg-white rounded-xl shadow-sm border border-gray-200 p-6 ${className}`}>
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>

      <div className="flex flex-col lg:flex-row items-center space-y-4 lg:space-y-0 lg:space-x-6">
        {/* Chart */}
        <div className="relative">
          <svg width={size} height={size} className="transform -rotate-90">
            {data.map((item, index) => {
              const percentage = total > 0 ? item.value / total : 0;
              const strokeDasharray = percentage * circumference;
              const strokeDashoffset = currentOffset;

              currentOffset -= strokeDasharray;

              const isHovered = hoveredIndex === index;

              return (
                <circle
                  key={index}
                  cx={size / 2}
                  cy={size / 2}
                  r={radius}
                  fill="transparent"
                  stroke={item.color}
                  strokeWidth={strokeWidth}
                  strokeDasharray={`${strokeDasharray} ${circumference}`}
                  strokeDashoffset={strokeDashoffset}
                  className={`transition-all duration-200 cursor-pointer ${
                    isHovered ? "opacity-80" : ""
                  }`}
                  onMouseEnter={() => setHoveredIndex(index)}
                  onMouseLeave={() => setHoveredIndex(null)}
                />
              );
            })}
          </svg>

          {/* Center content */}
          {(centerValue || centerLabel) && (
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              {centerValue && (
                <div className="text-2xl font-bold text-gray-900">{centerValue}</div>
              )}
              {centerLabel && (
                <div className="text-sm text-gray-600">{centerLabel}</div>
              )}
            </div>
          )}
        </div>

        {/* Legend */}
        <div className="space-y-2">
          {data.map((item, index) => {
            const percentage = total > 0 ? ((item.value / total) * 100).toFixed(1) : 0;
            const isHovered = hoveredIndex === index;

            return (
              <div
                key={index}
                className={`flex items-center space-x-3 p-2 rounded transition-colors cursor-pointer ${
                  isHovered ? "bg-gray-50" : ""
                }`}
                onMouseEnter={() => setHoveredIndex(index)}
                onMouseLeave={() => setHoveredIndex(null)}
              >
                <div
                  className="w-3 h-3 rounded-full"
                  style={{ backgroundColor: item.color }}
                />
                <div className="flex-1">
                  <div className="text-sm font-medium text-gray-900">{item.label}</div>
                  <div className="text-xs text-gray-600">
                    {item.value} ({percentage}%)
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}