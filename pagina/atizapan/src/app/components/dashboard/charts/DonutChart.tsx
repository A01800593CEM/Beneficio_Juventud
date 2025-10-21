
// src/components/dashboard/charts/DonutChart.tsx
"use client";

import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';

interface DonutChartProps {
  title: string;
  centerValue: string;
  centerLabel?: string;
  data?: Array<{ name: string; value: number; color: string }>;
  legend?: Array<{ label: string; color: string }>;
  stats?: Array<{ label: string; value: string }>;
}

export function DonutChart({ 
  title, 
  centerValue, 
  centerLabel,
  data = [],
  legend = [],
  stats = []
}: DonutChartProps) {
  // Datos por defecto si no se proporcionan
  const defaultData = [
    { name: 'Used', value: 75, color: '#008D96' },
    { name: 'Remaining', value: 25, color: '#E5E7EB' }
  ];

  const chartData = data.length > 0 ? data : defaultData;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 px-6">
      <h3 className="text-xl  pt-6 font-regular text-[#015463] ">{title}</h3>
      
      <div className="flex items-center justify-between">
        {/* Stats Column */}
        {stats.length > 0 && (
          <div className="flex-1 ">
            {stats.map((stat, index) => (
              <div key={index} className="flex items-center text-sm space-x-2 py-1">
                <div className={`text-[${data[index].color}] text-lg font-semibold`}>{stat.value} </div>

                <div className="text-[#969696]">{stat.label}</div>
              </div>
            ))}
          </div>
        )}

        {/* Legend Column */}
        {legend.length > 0 && (
          <div className="flex-1 space-y-2">
            {legend.map((item, index) => (
              <div key={index} className="flex items-center text-sm">
                <div 
                  className="w-3 h-3 rounded-full mr-2"
                  style={{ backgroundColor: item.color }}
                />
                <span className="text-[#969696]">{item.label}</span>
              </div>
            ))}
          </div>
        )}

        {/* Chart */}
        <div className="flex-1 flex justify-center mb-2">
          <div className="relative w-56 h-56">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={chartData}
                  cx="50%"
                  cy="50%"
                  innerRadius={95}
                  outerRadius={110}
                  startAngle={90}
                  endAngle={450}
                  dataKey="value"
                >
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            
            {/* Center Text */}
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <div className="text-lg font-normal text-[#4B4C7E]">{centerValue}</div>
              {centerLabel && (
                <div className="text-xs text-[#969696] text-center">{centerLabel}</div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}