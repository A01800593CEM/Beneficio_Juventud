"use client";

import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  ResponsiveContainer,
  Legend,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  Tooltip
} from 'recharts';
import { MonthlyData, WeeklyData, CategoryBreakdown } from '../../services/statisticsService';

interface MonthlyPerformanceChartProps {
  data: MonthlyData[];
  title: string;
}

export function MonthlyPerformanceChart({ data, title }: MonthlyPerformanceChartProps) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis
              dataKey="month"
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                fontSize: '12px'
              }}
              formatter={(value: number, name: string) => {
                if (name === 'revenue') return [`$${value.toLocaleString()}`, 'Ingresos'];
                if (name === 'redemptions') return [value, 'Canjes'];
                if (name === 'promotions') return [value, 'Promociones'];
                return [value, name];
              }}
            />
            <Legend
              wrapperStyle={{
                paddingTop: '20px',
                fontSize: '12px'
              }}
            />
            <Bar
              dataKey="redemptions"
              fill="#008D96"
              name="Canjes"
              radius={[2, 2, 0, 0]}
              maxBarSize={40}
            />
            <Bar
              dataKey="revenue"
              fill="#00C0CC"
              name="Ingresos ($)"
              radius={[2, 2, 0, 0]}
              maxBarSize={40}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

interface RevenueGrowthChartProps {
  data: MonthlyData[];
  title: string;
}

export function RevenueGrowthChart({ data, title }: RevenueGrowthChartProps) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis
              dataKey="month"
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
              tickFormatter={(value) => `$${(value / 1000).toFixed(0)}K`}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                fontSize: '12px'
              }}
              formatter={(value: number) => [`$${value.toLocaleString()}`, 'Ingresos']}
            />
            <Line
              type="monotone"
              dataKey="revenue"
              stroke="#008D96"
              strokeWidth={3}
              dot={{ fill: '#008D96', strokeWidth: 2, r: 4 }}
              activeDot={{ r: 6, stroke: '#008D96', strokeWidth: 2, fill: '#fff' }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

interface ConversionRateChartProps {
  data: MonthlyData[];
  title: string;
}

export function ConversionRateChart({ data, title }: ConversionRateChartProps) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis
              dataKey="month"
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
              tickFormatter={(value) => `${value}%`}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                fontSize: '12px'
              }}
              formatter={(value: number) => [`${value.toFixed(1)}%`, 'Tasa de Conversión']}
            />
            <Line
              type="monotone"
              dataKey="conversionRate"
              stroke="#10B981"
              strokeWidth={3}
              dot={{ fill: '#10B981', strokeWidth: 2, r: 4 }}
              activeDot={{ r: 6, stroke: '#10B981', strokeWidth: 2, fill: '#fff' }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

interface CategoryBreakdownChartProps {
  data: CategoryBreakdown[];
  title: string;
}

const COLORS = ['#008D96', '#00C0CC', '#4B4C7E', '#10B981', '#F59E0B', '#EF4444'];

export function CategoryBreakdownChart({ data, title }: CategoryBreakdownChartProps) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data as any[]}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={120}
              paddingAngle={5}
              dataKey="revenue"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                backgroundColor: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                fontSize: '12px'
              }}
              formatter={(value: number, name: string, props?: { payload?: { category?: string; percentage?: number } }) => [
                `$${value.toLocaleString()}`,
                `${props?.payload?.category || name} (${props?.payload?.percentage?.toFixed(1) || '0'}%)`
              ]}
            />
            <Legend
              verticalAlign="bottom"
              height={36}
              formatter={(value, entry) => (entry.payload as { category?: string } | undefined)?.category || value}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

interface WeeklyTrendsChartProps {
  data: WeeklyData[];
  title: string;
}

export function WeeklyTrendsChart({ data, title }: WeeklyTrendsChartProps) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis
              dataKey="week"
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 12, fill: '#969696' }}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#fff',
                border: '1px solid #e5e7eb',
                borderRadius: '8px',
                fontSize: '12px'
              }}
              formatter={(value: number, name: string) => {
                if (name === 'revenue') return [`$${value.toLocaleString()}`, 'Ingresos'];
                if (name === 'redemptions') return [value, 'Canjes'];
                return [value, name];
              }}
            />
            <Legend
              wrapperStyle={{
                paddingTop: '20px',
                fontSize: '12px'
              }}
            />
            <Bar
              dataKey="redemptions"
              fill="#008D96"
              name="Canjes"
              radius={[2, 2, 0, 0]}
              maxBarSize={50}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}