'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import KPICard from '@/features/admin/components/KPICard';
import {
  ChartBarIcon,
  ArrowTrendingUpIcon,
  CurrencyDollarIcon,
  TicketIcon,
  StarIcon,
  EyeIcon
} from '@heroicons/react/24/outline';
import { statisticsService, CollaboratorStatistics } from '../services/statisticsService';
import {
  MonthlyPerformanceChart,
  RevenueGrowthChart,
  ConversionRateChart,
  CategoryBreakdownChart,
  WeeklyTrendsChart
} from '../components/charts/StatisticsCharts';

export default function EstadisticasEnhancedPage() {
  const { data: session } = useSession();
  const [stats, setStats] = useState<CollaboratorStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedPeriod, setSelectedPeriod] = useState<'week' | 'month' | 'year'>('month');

  useEffect(() => {
    loadStatistics();
  }, [session, selectedPeriod]);

  const loadStatistics = async () => {
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
        console.log('🔄 Loading enhanced statistics...');
        const statisticsData = await statisticsService.getCollaboratorStatistics(cognitoUsername);
        setStats(statisticsData);
        console.log('✅ Enhanced statistics loaded successfully');
      }
    } catch (err) {
      console.error('❌ Error loading enhanced statistics:', err);
      setError('Error al cargar las estadísticas');
    } finally {
      setLoading(false);
    }
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

  const kpiData = stats ? [
    {
      title: "Promociones Totales",
      value: stats.totalPromotions,
      delta: stats.growthMetrics.promotionsGrowth,
      icon: <TicketIcon className="h-4 w-4" />
    },
    {
      title: "Canjes Este Mes",
      value: stats.monthlyRedemptions,
      delta: stats.growthMetrics.redemptionsGrowth,
      icon: <ArrowTrendingUpIcon className="h-4 w-4" />
    },
    {
      title: "Ingresos Mensuales",
      value: `$${stats.monthlyRevenue.toLocaleString()}`,
      delta: stats.growthMetrics.revenueGrowth,
      icon: <CurrencyDollarIcon className="h-4 w-4" />
    },
    {
      title: "Tasa de Conversión",
      value: `${stats.conversionRate.toFixed(1)}%`,
      delta: stats.growthMetrics.conversionRateGrowth,
      icon: <ChartBarIcon className="h-4 w-4" />
    },
    {
      title: "Rating Promedio",
      value: stats.averageRating.toFixed(1),
      delta: 0.2,
      icon: <StarIcon className="h-4 w-4" />
    },
    {
      title: "Total de Vistas",
      value: stats.totalViews.toLocaleString(),
      delta: stats.growthMetrics.viewsGrowth,
      icon: <EyeIcon className="h-4 w-4" />
    }
  ] : [];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Estadísticas Avanzadas</h1>
        <p className="text-gray-600">
          Dashboard completo con datos en tiempo real desde el servidor
        </p>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      <div className="space-y-6">
        {/* Period Selector */}
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">Dashboard de Rendimiento</h2>
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

        {/* KPIs */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-6">
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

        {/* Main Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {stats && (
            <>
              <MonthlyPerformanceChart
                data={stats.monthlyData}
                title="Rendimiento Mensual"
              />
              <RevenueGrowthChart
                data={stats.monthlyData}
                title="Crecimiento de Ingresos"
              />
            </>
          )}
        </div>

        {/* Secondary Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {stats && (
            <>
              <ConversionRateChart
                data={stats.monthlyData}
                title="Tasa de Conversión"
              />
              {selectedPeriod === 'month' && (
                <WeeklyTrendsChart
                  data={stats.weeklyData}
                  title="Tendencias Semanales"
                />
              )}
            </>
          )}
        </div>

        {/* Category Analysis */}
        {stats && stats.categoryBreakdown.length > 1 && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <CategoryBreakdownChart
              data={stats.categoryBreakdown}
              title="Distribución por Categorías"
            />

            {/* Growth Metrics Summary */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-6">Métricas de Crecimiento</h3>
              <div className="space-y-4">
                <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium text-gray-700">Retención de Clientes</span>
                  <span className="text-lg font-bold text-[#008D96]">
                    {stats.growthMetrics.customerRetention.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium text-gray-700">Valor Promedio de Orden</span>
                  <span className="text-lg font-bold text-green-600">
                    ${stats.growthMetrics.averageOrderValue.toFixed(0)}
                  </span>
                </div>
                <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium text-gray-700">Valor de Vida del Cliente</span>
                  <span className="text-lg font-bold text-blue-600">
                    ${stats.growthMetrics.customerLifetimeValue.toFixed(0)}
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Detailed Performance Table */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">Rendimiento Detallado de Promociones</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Promoción
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tipo
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Stock Total
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Canjes
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tasa de Canje
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ingresos Est.
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Días Activa
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estado
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {stats?.promotionPerformance.map((promo, index) => (
                  <tr key={index} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900 max-w-48 truncate">
                        {promo.title}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {promo.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                      {promo.totalStock}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium text-gray-900">
                      {promo.redemptions}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                      {promo.redemptionRate.toFixed(1)}%
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                      ${promo.revenue.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                      {promo.daysActive}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        promo.status === 'activa'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}>
                        {promo.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Quick Insights */}
        <div className="bg-gradient-to-r from-[#008D96] to-[#00C0CC] rounded-lg shadow p-6 text-white">
          <h3 className="text-lg font-semibold mb-4">Insights Rápidos</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white bg-opacity-20 rounded-lg p-4">
              <h4 className="font-medium mb-2">Promoción Top</h4>
              <p className="text-sm opacity-90">{stats?.topPromotion.title}</p>
              <p className="text-xs opacity-75">{stats?.topPromotion.redemptions} canjes</p>
            </div>
            <div className="bg-white bg-opacity-20 rounded-lg p-4">
              <h4 className="font-medium mb-2">Horarios Pico</h4>
              <p className="text-sm opacity-90">{stats?.peakHours.slice(0, 2).join(', ')}</p>
            </div>
            <div className="bg-white bg-opacity-20 rounded-lg p-4">
              <h4 className="font-medium mb-2">Categoría Principal</h4>
              <p className="text-sm opacity-90">{stats?.topCategories[0]}</p>
            </div>
            <div className="bg-white bg-opacity-20 rounded-lg p-4">
              <h4 className="font-medium mb-2">Total Visualizaciones</h4>
              <p className="text-sm opacity-90">{stats?.totalViews.toLocaleString()}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}