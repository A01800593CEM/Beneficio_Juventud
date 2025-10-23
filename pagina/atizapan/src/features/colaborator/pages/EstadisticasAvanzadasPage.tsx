'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import KPICard from '@/features/admin/components/KPICard';
import {
  ChartBarIcon,
  ArrowTrendingUpIcon,
  UsersIcon,
  TicketIcon,
  StarIcon
} from '@heroicons/react/24/outline';
import { statisticsService, CollaboratorStatistics } from '../services/statisticsService';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar, AreaChart, Area } from 'recharts';

// Analytics interfaces for API integration
interface ApiAnalytics {
  metadata: {
    generatedAt: string;
    collaboratorId: string;
    collaboratorName: string;
    timeRange: string;
    period: {
      startDate: string;
      endDate: string;
    };
  };
  summary: {
    totalPromotions: number;
    activePromotions: number;
    totalBookings: number;
    redeemedCoupons: number;
    totalFavorites: number;
    conversionRate: string;
  };
  charts: {
    redemptionTrends: {
      type: string;
      title: string;
      description: string;
      entries: Array<{x: string; y: number}>;
      xAxisLabel: string;
      yAxisLabel: string;
      minY: number;
      maxY: number;
    };
    bookingTrends: {
      type: string;
      title: string;
      description: string;
      entries: Array<{x: string; y: number}>;
      xAxisLabel: string;
      yAxisLabel: string;
      minY: number;
      maxY: number;
    };
    topRedeemedCoupons: {
      type: string;
      title: string;
      description: string;
      entries: Array<{label: string; value: number; promotionId?: number}>;
      xAxisLabel: string;
      yAxisLabel: string;
    };
    redemptionTrendsByPromotion: {
      type: string;
      title: string;
      description: string;
      series: Array<{
        seriesId: string;
        seriesLabel: string;
        entries: Array<{x: string; y: number}>;
      }>;
      xAxisLabel: string;
      yAxisLabel: string;
    };
  };
  promotionStats: Array<{
    promotionId: number;
    title: string;
    type: string;
    status: string;
    stockRemaining: number;
    totalStock: number;
    stockUtilization: string;
  }>;
}

const CHART_COLORS = [
  '#008D96', // Primary Teal
  '#6366f1', // Indigo
  '#ec4899', // Pink
  '#f59e0b', // Amber
  '#10b981', // Emerald
  '#ef4444', // Red
  '#8b5cf6', // Violet
  '#06b6d4'  // Cyan
];

export default function EstadisticasAvanzadasPage() {
  const { data: session } = useSession();
  const [stats, setStats] = useState<CollaboratorStatistics | null>(null);
  const [apiAnalytics, setApiAnalytics] = useState<ApiAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedPeriod, setSelectedPeriod] = useState<'week' | 'month' | 'year'>('month');
  const [useRealData, setUseRealData] = useState(false);

  useEffect(() => {
    loadCollaboratorAndStats();
  }, [session, selectedPeriod, useRealData]);

  const loadCollaboratorAndStats = async () => {
    if (!session) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const sessionData = session as { cognitoUsername?: string; sub?: string; user?: { id?: string; sub?: string } };
      const cognitoUsername = sessionData.cognitoUsername || sessionData.sub || sessionData.user?.id || sessionData.user?.sub;

      if (cognitoUsername) {
        console.log('🔄 Loading collaborator statistics...');

        if (useRealData) {
          // Load real analytics from backend API
          const response = await fetch(`/api/proxy/analytics/collaborator/${cognitoUsername}?timeRange=${selectedPeriod}`);
          if (response.ok) {
            const analyticsData = await response.json();
            setApiAnalytics(analyticsData);
            console.log('✅ Real analytics loaded successfully');
          } else {
            throw new Error('Failed to load real analytics data');
          }
        } else {
          // Load simulated data using the statistics service
          const statisticsData = await statisticsService.getCollaboratorStatistics(cognitoUsername);
          setStats(statisticsData);
          console.log('✅ Simulated statistics loaded successfully');
        }
      }
    } catch (err) {
      console.error('❌ Error loading statistics:', err);
      setError('Error al cargar las estadísticas');
    } finally {
      setLoading(false);
    }
  };

  // Transform API data for charts
  const transformApiDataForCharts = (apiData: ApiAnalytics) => {
    return {
      redemptionTrends: apiData.charts.redemptionTrends.entries.map(entry => ({
        date: new Date(entry.x).toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' }),
        value: entry.y
      })),
      bookingTrends: apiData.charts.bookingTrends.entries.map(entry => ({
        date: new Date(entry.x).toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' }),
        value: entry.y
      })),
      topCoupons: apiData.charts.topRedeemedCoupons.entries.map(entry => ({
        name: entry.label.length > 15 ? entry.label.substring(0, 15) + '...' : entry.label,
        value: entry.value
      })),
      multiSeries: apiData.charts.redemptionTrendsByPromotion.series.reduce((acc, series) => {
        series.entries.forEach((entry, index) => {
          if (!acc[index]) {
            acc[index] = {
              date: new Date(entry.x).toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' })
            };
          }
          acc[index][series.seriesLabel] = entry.y;
        });
        return acc;
      }, [] as any[])
    };
  };

  if (loading) {
    return (
      <div>
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Estadísticas Avanzadas</h1>
        </div>
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
          <span className="ml-3 text-gray-600">Cargando estadísticas...</span>
        </div>
      </div>
    );
  }

  const chartData = useRealData && apiAnalytics ? transformApiDataForCharts(apiAnalytics) : null;

  const kpiData = useRealData && apiAnalytics ? [
    {
      title: "Promociones Totales",
      value: apiAnalytics.summary.totalPromotions,
      delta: 12,
      icon: <TicketIcon className="h-4 w-4" />
    },
    {
      title: "Promociones Activas",
      value: apiAnalytics.summary.activePromotions,
      delta: 5,
      icon: <TicketIcon className="h-4 w-4" />
    },
    {
      title: "Canjes Totales",
      value: apiAnalytics.summary.redeemedCoupons,
      delta: 8,
      icon: <ArrowTrendingUpIcon className="h-4 w-4" />
    },
    {
      title: "Reservas Totales",
      value: apiAnalytics.summary.totalBookings,
      delta: 15,
      icon: <UsersIcon className="h-4 w-4" />
    },
    {
      title: "Tasa de Conversión",
      value: apiAnalytics.summary.conversionRate,
      delta: 2.3,
      icon: <ChartBarIcon className="h-4 w-4" />
    }
  ] : stats ? [
    {
      title: "Promociones Totales",
      value: stats.totalPromotions,
      delta: 12,
      icon: <TicketIcon className="h-4 w-4" />
    },
    {
      title: "Promociones Activas",
      value: stats.activePromotions,
      delta: 5,
      icon: <TicketIcon className="h-4 w-4" />
    },
    {
      title: "Canjes Este Mes",
      value: stats.monthlyRedemptions,
      delta: 8,
      icon: <ArrowTrendingUpIcon className="h-4 w-4" />
    },
    {
      title: "Total de Vistas",
      value: stats.totalViews.toLocaleString(),
      delta: 22,
      icon: <UsersIcon className="h-4 w-4" />
    },
    {
      title: "Tasa de Conversión",
      value: `${stats.conversionRate}%`,
      delta: 2.3,
      icon: <ChartBarIcon className="h-4 w-4" />
    }
  ] : [];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Estadísticas Avanzadas</h1>
        <p className="text-gray-600">
          Análisis detallado de rendimiento - {useRealData ? 'Datos del Backend API' : 'Datos Simulados'}
        </p>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}
      <div className="space-y-6 px-16">
        {/* Data Source and Period Selector */}
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">Panel de Estadísticas</h2>
          <div className="flex items-center gap-4">
            {/* Data Source Toggle */}
            <div className="flex items-center">
              <span className="text-sm text-gray-600 mr-2">Datos:</span>
              <button
                onClick={() => setUseRealData(!useRealData)}
                className={`px-3 py-1 rounded-md text-xs font-medium transition-colors ${
                  useRealData
                    ? 'bg-green-100 text-green-800'
                    : 'bg-blue-100 text-blue-800'
                }`}
              >
                {useRealData ? 'API Real' : 'Simulados'}
              </button>
            </div>

            {/* Period Selector */}
            <div className="flex bg-gray-100 rounded-lg p-1">
              {(['week', 'month', 'year'] as const).map((period) => (
                <button
                  key={period}
                  onClick={() => setSelectedPeriod(period)}
                  className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                    selectedPeriod === period
                      ? 'bg-white text-[#008D96] shadow-sm'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  {period === 'week' ? 'Semana' : period === 'month' ? 'Mes' : 'Año'}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* KPIs */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
          {kpiData.map((kpi, index) => (
            <KPICard
              key={index}
              title={kpi.title}
              value={kpi.value}
              delta={kpi.delta}
              icon={kpi.icon}
            />
          ))}
        </div>

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Redemption Trends Chart */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              {useRealData ? 'Tendencias de Canjes (API Real)' : 'Rendimiento Mensual'}
            </h3>
            {useRealData && chartData ? (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData.redemptionTrends}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="value" stroke="#008D96" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            ) : stats ? (
              <div className="space-y-4">
                {stats.monthlyData.map((data, index) => (
                  <div key={index} className="flex items-center justify-between">
                    <span className="text-sm font-medium text-gray-600 w-12">{data.month}</span>
                    <div className="flex-1 mx-4">
                      <div className="bg-gray-200 rounded-full h-2">
                        <div
                          className="bg-[#008D96] h-2 rounded-full"
                          style={{ width: `${Math.min((data.redemptions / Math.max(...stats.monthlyData.map(d => d.redemptions))) * 100, 100)}%` }}
                        ></div>
                      </div>
                    </div>
                    <span className="text-sm text-gray-900 w-20 text-right">{data.redemptions} canjes</span>
                    <span className="text-xs text-gray-500 w-16 text-right">{data.views} vistas</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex items-center justify-center h-64 text-gray-500">
                No hay datos disponibles
              </div>
            )}
          </div>

          {/* Top Redeemed Coupons Chart */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              {useRealData ? 'Top Cupones Canjeados (API Real)' : 'Insights Clave'}
            </h3>
            {useRealData && chartData ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={chartData.topCoupons}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="value" fill="#008D96" />
                </BarChart>
              </ResponsiveContainer>
            ) : stats ? (
              <div className="space-y-4">
                <div className="border-l-4 border-[#008D96] pl-4">
                  <h4 className="font-medium text-gray-900">Promoción Más Exitosa</h4>
                  <p className="text-sm text-gray-600">{stats?.topPromotion.title}</p>
                  <p className="text-xs text-gray-500">{stats?.topPromotion.redemptions} canjes totales</p>
                </div>

                <div className="border-l-4 border-blue-500 pl-4">
                  <h4 className="font-medium text-gray-900">Horarios Pico</h4>
                  <p className="text-sm text-gray-600">{stats?.peakHours.join(", ")}</p>
                </div>

                <div className="border-l-4 border-green-500 pl-4">
                  <h4 className="font-medium text-gray-900">Categorías Top</h4>
                  <p className="text-sm text-gray-600">{stats?.topCategories.join(", ")}</p>
                </div>

                <div className="border-l-4 border-yellow-500 pl-4">
                  <h4 className="font-medium text-gray-900">Total de Vistas</h4>
                  <p className="text-sm text-gray-600">{stats?.totalViews.toLocaleString()} visualizaciones</p>
                </div>

                <div className="border-l-4 border-purple-500 pl-4">
                  <h4 className="font-medium text-gray-900">Crecimiento de Canjes</h4>
                  <p className="text-sm text-gray-600">
                    {(stats?.growthMetrics?.redemptionsGrowth ?? 0) > 0 ? '+' : ''}{(stats?.growthMetrics?.redemptionsGrowth ?? 0).toFixed(1)}% vs mes anterior
                  </p>
                </div>
              </div>
            ) : (
              <div className="flex items-center justify-center h-64 text-gray-500">
                No hay datos disponibles
              </div>
            )}
          </div>
        </div>

        {/* Multi-Series Line Chart (Real Data Only) */}
        {useRealData && chartData && apiAnalytics && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Tendencias de Canjes por Promoción
            </h3>
            <ResponsiveContainer width="100%" height={400}>
              <LineChart data={chartData.multiSeries}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                {apiAnalytics.charts.redemptionTrendsByPromotion.series.map((series, index) => (
                  <Line
                    key={series.seriesId}
                    type="monotone"
                    dataKey={series.seriesLabel}
                    stroke={CHART_COLORS[index % CHART_COLORS.length]}
                    strokeWidth={2}
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Booking Trends Chart (Real Data Only) */}
        {useRealData && chartData && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Tendencias de Rdijifseservas
            </h3>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={chartData.bookingTrends}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Area type="monotone" dataKey="value" stroke="#6366f1" fill="#6366f1" fillOpacity={0.3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Detailed Analytics */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-6">Análisis Detallado</h3>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Redemption Trends */}
            <div className="text-center">
              <div className="bg-[#008D96] bg-opacity-10 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <ArrowTrendingUpIcon className="h-8 w-8 text-[#008D96]" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Tendencia de Canjes</h4>
              <p className="text-2xl font-bold text-[#008D96] mb-1">
                {(stats?.growthMetrics?.redemptionsGrowth ?? 0) > 0 ? '+' : ''}{(stats?.growthMetrics?.redemptionsGrowth ?? 0).toFixed(1)}%
              </p>
              <p className="text-sm text-gray-600">vs. mes anterior</p>
            </div>

            {/* Views Growth */}
            <div className="text-center">
              <div className="bg-green-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <UsersIcon className="h-8 w-8 text-green-600" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Crecimiento de Visualizaciones</h4>
              <p className="text-2xl font-bold text-green-600 mb-1">
                {(stats?.growthMetrics?.viewsGrowth ?? 0) > 0 ? '+' : ''}{(stats?.growthMetrics?.viewsGrowth ?? 0).toFixed(1)}%
              </p>
              <p className="text-sm text-gray-600">vs. mes anterior</p>
            </div>

            {/* Customer Engagement */}
            <div className="text-center">
              <div className="bg-blue-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <UsersIcon className="h-8 w-8 text-blue-600" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Retención de Clientes</h4>
              <p className="text-2xl font-bold text-blue-600 mb-1">{(stats?.growthMetrics?.customerRetention ?? 0).toFixed(0)}%</p>
              <p className="text-sm text-gray-600">tasa de retención</p>
            </div>
          </div>
        </div>

        {/* Promotion Performance */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            {useRealData ? 'Rendimiento de Promociones (API Real)' : 'Rendimiento de Promociones'}
          </h3>
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-4 font-medium text-gray-900">Promoción</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-900">Tipo</th>
                  <th className="text-right py-3 px-4 font-medium text-gray-900">
                    {useRealData ? 'Stock Restante' : 'Canjes'}
                  </th>
                  <th className="text-right py-3 px-4 font-medium text-gray-900">
                    {useRealData ? 'Utilización' : 'Tasa de Canje'}
                  </th>
                  {!useRealData && (
                    <th className="text-right py-3 px-4 font-medium text-gray-900">Vistas</th>
                  )}
                  <th className="text-center py-3 px-4 font-medium text-gray-900">Estado</th>
                </tr>
              </thead>
              <tbody>
                {useRealData && apiAnalytics ? (
                  apiAnalytics.promotionStats.slice(0, 5).map((promo, index) => (
                    <tr key={index} className="border-b border-gray-100">
                      <td className="py-3 px-4">
                        <div>
                          <p className="font-medium text-gray-900 truncate max-w-48">{promo.title}</p>
                          <p className="text-sm text-gray-500">ID: {promo.promotionId}</p>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                          {promo.type}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <p className="font-medium text-gray-900">{promo.stockRemaining}</p>
                        <p className="text-sm text-gray-500">de {promo.totalStock}</p>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <p className="font-medium text-gray-900">{promo.stockUtilization}%</p>
                      </td>
                      <td className="py-3 px-4 text-center">
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                          promo.status === 'activa'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}>
                          {promo.status}
                        </span>
                      </td>
                    </tr>
                  ))
                ) : (
                  stats?.promotionPerformance.slice(0, 5).map((promo, index) => (
                    <tr key={index} className="border-b border-gray-100">
                      <td className="py-3 px-4">
                        <div>
                          <p className="font-medium text-gray-900 truncate max-w-48">{promo.title}</p>
                          <p className="text-sm text-gray-500">{promo.daysActive} días activa</p>
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                          {promo.type}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <p className="font-medium text-gray-900">{promo.redemptions}</p>
                        <p className="text-sm text-gray-500">de {promo.totalStock}</p>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <p className="font-medium text-gray-900">{promo.redemptionRate.toFixed(1)}%</p>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <p className="font-medium text-gray-900">{promo.views.toLocaleString()}</p>
                      </td>
                      <td className="py-3 px-4 text-center">
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                          promo.status === 'activa'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}>
                          {promo.status}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Category Breakdown */}
        {!useRealData && stats && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Breakdown por Categorías</h3>
            <div className="space-y-4">
              {stats.categoryBreakdown.map((category, index) => (
                <div key={index} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div className="flex-1">
                    <h4 className="font-medium text-gray-900">{category.category}</h4>
                    <p className="text-sm text-gray-600">{category.promotions} promociones</p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-gray-900">{category.percentage.toFixed(1)}%</p>
                    <p className="text-sm text-gray-600">{category.redemptions} canjes</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* API Data Info Card */}
        {useRealData && apiAnalytics && (
          <div className="bg-gradient-to-r from-green-50 to-blue-50 border border-green-200 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-green-800 mb-4">Información de Datos API</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-green-700">
                  <span className="font-medium">Colaborador:</span> {apiAnalytics.metadata.collaboratorName}
                </p>
                <p className="text-sm text-green-700">
                  <span className="font-medium">Período:</span> {apiAnalytics.metadata.timeRange}
                </p>
                <p className="text-sm text-green-700">
                  <span className="font-medium">Generado:</span> {new Date(apiAnalytics.metadata.generatedAt).toLocaleString('es-ES')}
                </p>
              </div>
              <div>
                <p className="text-sm text-green-700">
                  <span className="font-medium">Desde:</span> {new Date(apiAnalytics.metadata.period.startDate).toLocaleDateString('es-ES')}
                </p>
                <p className="text-sm text-green-700">
                  <span className="font-medium">Hasta:</span> {new Date(apiAnalytics.metadata.period.endDate).toLocaleDateString('es-ES')}
                </p>
                <p className="text-sm text-green-700">
                  <span className="font-medium">Fuente:</span> API Analytics Backend
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}