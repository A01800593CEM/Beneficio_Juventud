// src/components/dashboard/charts/BarChart.tsx
"use client";

import { BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer, Legend } from 'recharts';

interface BarChartData {
  date: string;
  promediopordia: number;
  cuponesusados: number;
}

interface CustomBarChartProps {
  data: BarChartData[];
  title: string;
}

export function CustomBarChart({ data, title }: CustomBarChartProps) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 px-6 pt-6">
      <h3 className="text-xl font-regular text-[#015463] mb-6">{title}</h3>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} margin={{ top: 20, right: 30, left: -20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis 
              dataKey="date" 
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <YAxis 
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <Legend 
              wrapperStyle={{ 
                paddingTop: '20px',
                fontSize: '12px'
              }}
            />
            <Bar 
              dataKey="promediopordia" 
              fill="#00C0CC" 
              name="Promedio por día"
              radius={[2, 2, 0, 0]}
              maxBarSize={30}
            />
            <Bar 
              dataKey="cuponesusados" 
              fill="#4B4C7E" 
              name="Cupones usados"
              radius={[2, 2, 0, 0]}
              maxBarSize={30}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
