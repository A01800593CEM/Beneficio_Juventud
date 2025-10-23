'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import KPICard from '@/features/admin/components/KPICard';
import {
  ChartBarIcon,
  ArrowTrendingUpIcon,
  CurrencyDollarIcon,
  UsersIcon,
  TicketIcon,
  StarIcon
} from '@heroicons/react/24/outline';
import { statisticsService, CollaboratorStatistics } from '../services/statisticsService';


export default function EstadisticasAvanzadasPage() {
  const { data: session } = useSession();
  const [stats, setStats] = useState<CollaboratorStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedPeriod, setSelectedPeriod] = useState<'week' | 'month' | 'year'>('month');

  useEffect(() => {
    loadCollaboratorAndStats();
  }, [session, selectedPeriod]);

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

        // Cargar estadísticas usando el nuevo servicio
        const statisticsData = await statisticsService.getCollaboratorStatistics(cognitoUsername);
        setStats(statisticsData);

        console.log('✅ Statistics loaded successfully');
      }
    } catch (err) {
      console.error('❌ Error loading statistics:', err);
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
      delta: 12,
      icon: <TicketIcon className="h-4 w-4" />
    },
    {
      title: "Canjes Este Mes",
      value: stats.monthlyRedemptions,
      delta: 8,
      icon: <ArrowTrendingUpIcon className="h-4 w-4" />
    },
    {
      title: "Ingresos Mensuales",
      value: `$${stats.monthlyRevenue.toLocaleString()}`,
      delta: 15,
      icon: <CurrencyDollarIcon className="h-4 w-4" />
    },
    {
      title: "Tasa de Conversión",
      value: `${stats.conversionRate}%`,
      delta: 2.3,
      icon: <ChartBarIcon className="h-4 w-4" />
    },
    {
      title: "Rating Promedio",
      value: stats.averageRating,
      delta: 0.2,
      icon: <StarIcon className="h-4 w-4" />
    }
  ] : [];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Estadísticas Avanzadas</h1>
        <p className="text-gray-600">
          Análisis detallado de rendimiento basado en datos del servidor
        </p>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}
      <div className="space-y-6 px-16">
        {/* Period Selector */}
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-900">Panel de Estadísticas</h2>
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
          {/* Monthly Performance Chart */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Rendimiento Mensual</h3>
            <div className="space-y-4">
              {stats?.monthlyData.map((data, index) => (
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
                  <span className="text-xs text-gray-500 w-16 text-right">${data.revenue.toLocaleString()}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Top Insights */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Insights Clave</h3>
            <div className="space-y-4">
              <div className="border-l-4 border-[#008D96] pl-4">
                <h4 className="font-medium text-gray-900">Promoción Más Exitosa</h4>
                <p className="text-sm text-gray-600">{stats?.topPromotion.title}</p>
                <p className="text-xs text-gray-500">{stats?.topPromotion.redemptions} canjes • ${stats?.topPromotion.revenue.toLocaleString()}</p>
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
                <h4 className="font-medium text-gray-900">Crecimiento de Ingresos</h4>
                <p className="text-sm text-gray-600">
                  {(stats?.growthMetrics?.revenueGrowth ?? 0) > 0 ? '+' : ''}{(stats?.growthMetrics?.revenueGrowth ?? 0).toFixed(1)}% vs mes anterior
                </p>
              </div>
            </div>
          </div>
        </div>

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

            {/* Revenue Growth */}
            <div className="text-center">
              <div className="bg-green-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <CurrencyDollarIcon className="h-8 w-8 text-green-600" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Crecimiento de Ingresos</h4>
              <p className="text-2xl font-bold text-green-600 mb-1">
                {(stats?.growthMetrics?.revenueGrowth ?? 0) > 0 ? '+' : ''}{(stats?.growthMetrics?.revenueGrowth ?? 0).toFixed(1)}%
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
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Rendimiento de Promociones</h3>
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-4 font-medium text-gray-900">Promoción</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-900">Tipo</th>
                  <th className="text-right py-3 px-4 font-medium text-gray-900">Canjes</th>
                  <th className="text-right py-3 px-4 font-medium text-gray-900">Tasa de Canje</th>
                  <th className="text-right py-3 px-4 font-medium text-gray-900">Ingresos</th>
                  <th className="text-center py-3 px-4 font-medium text-gray-900">Estado</th>
                </tr>
              </thead>
              <tbody>
                {stats?.promotionPerformance.slice(0, 5).map((promo, index) => (
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
                      <p className="font-medium text-gray-900">${promo.revenue.toLocaleString()}</p>
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
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Category Breakdown */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Breakdown por Categorías</h3>
          <div className="space-y-4">
            {stats?.categoryBreakdown.map((category, index) => (
              <div key={index} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div className="flex-1">
                  <h4 className="font-medium text-gray-900">{category.category}</h4>
                  <p className="text-sm text-gray-600">{category.promotions} promociones</p>
                </div>
                <div className="text-right">
                  <p className="font-medium text-gray-900">{category.percentage.toFixed(1)}%</p>
                  <p className="text-sm text-gray-600">${category.revenue.toLocaleString()}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}