"use client";

import { useState, useEffect } from "react";
import { useSession } from 'next-auth/react';
import {
  ArrowUpIcon,
  ArrowDownIcon,
  CalendarIcon,
  DocumentArrowDownIcon
} from "@heroicons/react/24/outline";
import { SimpleBarChart, SimpleDonutChart } from "../components/SimpleChart";
import { promotionApiService } from '../promociones/services/api';
import { ApiCollaborator, ApiPromotion } from '../promociones/types';

// Mock data for statistics
const mockStats = {
  currentMonth: {
    couponsRedeemed: 670,
    conversionRate: 8.2,
    totalViews: 8150,
    activePromotions: 5
  },
  previousMonth: {
    couponsRedeemed: 545,
    conversionRate: 7.8,
    totalViews: 7420,
    activePromotions: 4
  },
  dailyRedemptions: [
    { label: "Lun", value: 95 },
    { label: "Mar", value: 110 },
    { label: "Mié", value: 78 },
    { label: "Jue", value: 134 },
    { label: "Vie", value: 156 },
    { label: "Sáb", value: 189 },
    { label: "Dom", value: 142 }
  ],
  topPromotions: [
    { label: "50% OFF Pizzas", value: 245, color: "#008D96" },
    { label: "Happy Hour 2x1", value: 189, color: "#00C0CC" },
    { label: "Pasta + Bebida", value: 156, color: "#015463" },
    { label: "Descuento Estudiantes", value: 80, color: "#4B4C7E" }
  ]
};

export default function EstadisticasPage() {
  const { data: session } = useSession();
  const [timeRange, setTimeRange] = useState("month");
  const [stats, setStats] = useState(mockStats);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [collaborator, setCollaborator] = useState<ApiCollaborator | null>(null);

  // Cargar datos del colaborador y generar estadísticas
  useEffect(() => {
    loadCollaboratorStats();
  }, [session, timeRange]);

  const loadCollaboratorStats = async () => {
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

        // Calcular estadísticas reales
        const totalRedemptions = promotionsData.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0);
        const activePromotions = promotionsData.filter(p => p.promotionState === 'activa').length;
        const totalViews = totalRedemptions * 15; // Simulación: 15 vistas por canje
        const conversionRate = totalViews > 0 ? (totalRedemptions / totalViews) * 100 : 0;

        // Generar datos simulados para gráficos basados en datos reales
        const dailyRedemptions = [
          { label: "Lun", value: Math.floor(totalRedemptions * 0.12) },
          { label: "Mar", value: Math.floor(totalRedemptions * 0.15) },
          { label: "Mié", value: Math.floor(totalRedemptions * 0.10) },
          { label: "Jue", value: Math.floor(totalRedemptions * 0.18) },
          { label: "Vie", value: Math.floor(totalRedemptions * 0.20) },
          { label: "Sáb", value: Math.floor(totalRedemptions * 0.25) },
          { label: "Dom", value: Math.floor(totalRedemptions * 0.15) }
        ];

        const topPromotions = promotionsData
          .map((promo, index) => ({
            label: promo.title.length > 20 ? promo.title.substring(0, 20) + '...' : promo.title,
            value: (promo.totalStock || 0) - (promo.availableStock || 0),
            color: ['#008D96', '#00C0CC', '#015463', '#4B4C7E'][index % 4]
          }))
          .filter(p => p.value > 0)
          .sort((a, b) => b.value - a.value)
          .slice(0, 4);

        const calculatedStats = {
          currentMonth: {
            couponsRedeemed: totalRedemptions,
            conversionRate: Number(conversionRate.toFixed(1)),
            totalViews: totalViews,
            activePromotions: activePromotions
          },
          previousMonth: {
            couponsRedeemed: Math.floor(totalRedemptions * 0.85), // Simulación: 15% menos el mes pasado
            conversionRate: Number((conversionRate * 0.9).toFixed(1)),
            totalViews: Math.floor(totalViews * 0.9),
            activePromotions: Math.max(1, activePromotions - 1)
          },
          dailyRedemptions: dailyRedemptions,
          topPromotions: topPromotions.length > 0 ? topPromotions : mockStats.topPromotions
        };

        setStats(calculatedStats);
        console.log('✅ Stats calculated for:', collaboratorData.businessName);
      }
    } catch (err) {
      console.error('❌ Error loading stats:', err);
      setError('Error al cargar las estadísticas');
      // Usar datos mock en caso de error
      setStats(mockStats);
    } finally {
      setLoading(false);
    }
  };

  // Calculate changes from previous period
  const couponsChange = stats.previousMonth.couponsRedeemed > 0
    ? ((stats.currentMonth.couponsRedeemed - stats.previousMonth.couponsRedeemed) / stats.previousMonth.couponsRedeemed) * 100
    : 0;
  const conversionChange = stats.currentMonth.conversionRate - stats.previousMonth.conversionRate;
  const viewsChange = stats.previousMonth.totalViews > 0
    ? ((stats.currentMonth.totalViews - stats.previousMonth.totalViews) / stats.previousMonth.totalViews) * 100
    : 0;

  const handleExport = () => {
    console.log("Exporting statistics...");
    // TODO: Implement export functionality
  };

  const actions = (
    <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-3 sm:space-y-0 sm:space-x-4">
      {/* Time Range Selector */}
      <div className="relative">
        <select
          value={timeRange}
          onChange={(e) => setTimeRange(e.target.value)}
          className="pl-4 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent appearance-none bg-white"
        >
          <option value="week">Esta Semana</option>
          <option value="month">Este Mes</option>
          <option value="quarter">Este Trimestre</option>
        </select>
        <CalendarIcon className="absolute right-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400 pointer-events-none" />
      </div>

      {/* Export Button */}
      <button
        onClick={handleExport}
        className="inline-flex items-center justify-center px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-medium text-gray-700"
      >
        <DocumentArrowDownIcon className="h-5 w-5 mr-2" />
        Exportar
      </button>
    </div>
  );

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
          <div className="text-[#969696] mt-2">Cargando estadísticas...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-8 space-y-4 sm:space-y-0">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Estadísticas y Análisis</h1>
          <p className="text-gray-600">
            {collaborator ? `${collaborator.businessName} - Analiza el rendimiento de tus promociones` : "Analiza el rendimiento de tus promociones"}
          </p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {actions}
      </div>
        {/* KPI Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {/* Coupons Redeemed */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Cupones Canjeados</p>
                <p className="text-3xl font-bold text-gray-900">
                  {mockStats.currentMonth.couponsRedeemed.toLocaleString()}
                </p>
              </div>
              <div className={`flex items-center ${couponsChange >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {couponsChange >= 0 ? (
                  <ArrowUpIcon className="h-4 w-4 mr-1" />
                ) : (
                  <ArrowDownIcon className="h-4 w-4 mr-1" />
                )}
                <span className="text-sm font-medium">
                  {Math.abs(couponsChange).toFixed(1)}%
                </span>
              </div>
            </div>
            <p className="text-xs text-gray-500 mt-2">vs mes anterior</p>
          </div>

          {/* Conversion Rate */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Tasa de Conversión</p>
                <p className="text-3xl font-bold text-gray-900">
                  {mockStats.currentMonth.conversionRate.toFixed(1)}%
                </p>
              </div>
              <div className={`flex items-center ${conversionChange >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {conversionChange >= 0 ? (
                  <ArrowUpIcon className="h-4 w-4 mr-1" />
                ) : (
                  <ArrowDownIcon className="h-4 w-4 mr-1" />
                )}
                <span className="text-sm font-medium">
                  {Math.abs(conversionChange).toFixed(1)}pp
                </span>
              </div>
            </div>
            <p className="text-xs text-gray-500 mt-2">vs mes anterior</p>
          </div>

          {/* Total Views */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Total Vistas</p>
                <p className="text-3xl font-bold text-gray-900">
                  {mockStats.currentMonth.totalViews.toLocaleString()}
                </p>
              </div>
              <div className={`flex items-center ${viewsChange >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {viewsChange >= 0 ? (
                  <ArrowUpIcon className="h-4 w-4 mr-1" />
                ) : (
                  <ArrowDownIcon className="h-4 w-4 mr-1" />
                )}
                <span className="text-sm font-medium">
                  {Math.abs(viewsChange).toFixed(1)}%
                </span>
              </div>
            </div>
            <p className="text-xs text-gray-500 mt-2">vs mes anterior</p>
          </div>

          {/* Active Promotions */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">Promociones Activas</p>
                <p className="text-3xl font-bold text-gray-900">
                  {mockStats.currentMonth.activePromotions}
                </p>
              </div>
              <div className="flex items-center text-blue-600">
                <span className="text-sm font-medium">
                  +{mockStats.currentMonth.activePromotions - mockStats.previousMonth.activePromotions}
                </span>
              </div>
            </div>
            <p className="text-xs text-gray-500 mt-2">vs mes anterior</p>
          </div>
        </div>

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Daily Redemptions Bar Chart */}
          <SimpleBarChart
            data={mockStats.dailyRedemptions}
            title="Cupones Canjeados por Día"
            className="lg:col-span-1"
          />

          {/* Top Promotions Donut Chart */}
          <SimpleDonutChart
            data={mockStats.topPromotions}
            title="Promociones Más Canjeadas"
            centerValue="670"
            centerLabel="Total"
            className="lg:col-span-1"
          />
        </div>

        {/* Detailed Analytics Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Rendimiento por Promoción
            </h3>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Promoción
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Vistas
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Canjeos
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Conversión
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estado
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                <tr>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    50% OFF en Pizzas Familiares
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    2,456
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    245
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    10.0%
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      Activa
                    </span>
                  </td>
                </tr>
                <tr>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    Happy Hour - 2x1 en Cervezas
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    3,211
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    189
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    5.9%
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      Activa
                    </span>
                  </td>
                </tr>
                <tr>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    Pasta + Bebida por $199
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    1,834
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    156
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    8.5%
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      Activa
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
    </div>
  );
}