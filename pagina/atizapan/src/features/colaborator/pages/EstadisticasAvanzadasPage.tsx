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
import { promotionApiService } from '../promociones/services/api';
import { ApiCollaborator, ApiPromotion } from '../promociones/types';

interface CollaboratorStats {
  totalPromotions: number;
  activePromotions: number;
  totalRedemptions: number;
  monthlyRedemptions: number;
  totalRevenue: number;
  monthlyRevenue: number;
  averageRating: number;
  totalViews: number;
  conversionRate: number;
  topPromotion: string;
  peakHours: string[];
  topCategories: string[];
}

interface MonthlyData {
  month: string;
  promotions: number;
  redemptions: number;
  revenue: number;
}

export default function EstadisticasAvanzadasPage() {
  const { data: session } = useSession();
  const [stats, setStats] = useState<CollaboratorStats | null>(null);
  const [monthlyData, setMonthlyData] = useState<MonthlyData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [collaborator, setCollaborator] = useState<ApiCollaborator | null>(null);
  const [promotions, setPromotions] = useState<ApiPromotion[]>([]);
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
      const sessionData = session as any;
      const cognitoUsername = sessionData.cognitoUsername || sessionData.sub || sessionData.user?.id || sessionData.user?.sub;

      if (cognitoUsername) {
        console.log('🔄 Loading collaborator stats...');

        // Cargar datos del colaborador
        const collaboratorData = await promotionApiService.getCollaboratorByCognitoId(cognitoUsername);
        setCollaborator(collaboratorData);

        // Cargar promociones del colaborador
        const promotionsData = await promotionApiService.getPromotions(cognitoUsername);
        setPromotions(promotionsData);

        // Calcular estadísticas basadas en datos reales
        const calculatedStats: CollaboratorStats = {
          totalPromotions: promotionsData.length,
          activePromotions: promotionsData.filter(p => p.promotionState === 'activa').length,
          totalRedemptions: promotionsData.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0),
          monthlyRedemptions: Math.floor(promotionsData.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0) * 0.3), // Simulación
          totalRevenue: promotionsData.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0) * 75, // Simulación de precio promedio
          monthlyRevenue: promotionsData.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0) * 75 * 0.3, // Simulación
          averageRating: 4.7, // Simulación - el servidor no tiene ratings aún
          totalViews: promotionsData.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0) * 15, // Simulación
          conversionRate: promotionsData.length > 0 ? ((promotionsData.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0) / promotionsData.reduce((sum, p) => sum + (p.totalStock || 0), 0)) * 100) : 0,
          topPromotion: promotionsData.length > 0 ? promotionsData[0].title : "Sin promociones",
          peakHours: ["12:00-14:00", "19:00-21:00"], // Simulación
          topCategories: collaboratorData.categories?.map(c => c.name) || ["COMIDA"]
        };

        // Datos mensuales simulados basados en las promociones reales
        const mockMonthlyData: MonthlyData[] = [
          { month: "Ene", promotions: Math.floor(promotionsData.length * 0.15), redemptions: calculatedStats.monthlyRedemptions * 0.8, revenue: calculatedStats.monthlyRevenue * 0.8 },
          { month: "Feb", promotions: Math.floor(promotionsData.length * 0.12), redemptions: calculatedStats.monthlyRedemptions * 0.9, revenue: calculatedStats.monthlyRevenue * 0.9 },
          { month: "Mar", promotions: Math.floor(promotionsData.length * 0.18), redemptions: calculatedStats.monthlyRedemptions * 1.2, revenue: calculatedStats.monthlyRevenue * 1.2 },
          { month: "Abr", promotions: Math.floor(promotionsData.length * 0.20), redemptions: calculatedStats.monthlyRedemptions * 1.4, revenue: calculatedStats.monthlyRevenue * 1.4 },
          { month: "May", promotions: Math.floor(promotionsData.length * 0.15), redemptions: calculatedStats.monthlyRedemptions * 0.95, revenue: calculatedStats.monthlyRevenue * 0.95 },
          { month: "Jun", promotions: Math.floor(promotionsData.length * 0.20), redemptions: calculatedStats.monthlyRedemptions, revenue: calculatedStats.monthlyRevenue }
        ];

        setStats(calculatedStats);
        setMonthlyData(mockMonthlyData);
        console.log('✅ Stats loaded for:', collaboratorData.businessName);
      }
    } catch (err) {
      console.error('❌ Error loading stats:', err);
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
          {collaborator ? `${collaborator.businessName} - Análisis detallado de rendimiento` : "Análisis detallado de rendimiento"}
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
              {monthlyData.map((data, index) => (
                <div key={index} className="flex items-center justify-between">
                  <span className="text-sm font-medium text-gray-600 w-12">{data.month}</span>
                  <div className="flex-1 mx-4">
                    <div className="bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-[#008D96] h-2 rounded-full"
                        style={{ width: `${(data.redemptions / 70) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                  <span className="text-sm text-gray-900 w-16 text-right">{data.redemptions} canjes</span>
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
                <p className="text-sm text-gray-600">{stats?.topPromotion}</p>
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
              <p className="text-2xl font-bold text-[#008D96] mb-1">+23%</p>
              <p className="text-sm text-gray-600">vs. mes anterior</p>
            </div>

            {/* Revenue Growth */}
            <div className="text-center">
              <div className="bg-green-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <CurrencyDollarIcon className="h-8 w-8 text-green-600" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Crecimiento de Ingresos</h4>
              <p className="text-2xl font-bold text-green-600 mb-1">+15%</p>
              <p className="text-sm text-gray-600">vs. mes anterior</p>
            </div>

            {/* Customer Engagement */}
            <div className="text-center">
              <div className="bg-blue-100 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
                <UsersIcon className="h-8 w-8 text-blue-600" />
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Engagement de Clientes</h4>
              <p className="text-2xl font-bold text-blue-600 mb-1">87%</p>
              <p className="text-sm text-gray-600">satisfacción promedio</p>
            </div>
          </div>
        </div>

        {/* Recent Activity */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Actividad Reciente</h3>
          <div className="space-y-3">
            {[
              { action: "Nueva promoción creada", detail: "Descuento 30% en Hamburguesas", time: "Hace 2 horas" },
              { action: "Promoción canjeada", detail: "Mesa para 4 personas - Restaurante Luna", time: "Hace 4 horas" },
              { action: "Reseña recibida", detail: "5 estrellas - 'Excelente servicio'", time: "Hace 6 horas" },
              { action: "Promoción finalizada", detail: "Combo familiar terminó", time: "Hace 1 día" }
            ].map((activity, index) => (
              <div key={index} className="flex items-center space-x-4 p-3 bg-gray-50 rounded-lg">
                <div className="bg-[#008D96] rounded-full w-2 h-2"></div>
                <div className="flex-1">
                  <p className="font-medium text-gray-900">{activity.action}</p>
                  <p className="text-sm text-gray-600">{activity.detail}</p>
                </div>
                <span className="text-xs text-gray-500">{activity.time}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}