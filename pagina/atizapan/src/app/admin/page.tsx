// src/app/admin/page.tsx
"use client";

import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect, useState, useCallback } from "react";
import DashboardLayout from "../../features/admin/components/DashboardLayout";
import KPICard from "../../features/admin/components/KPICard";
import AddCard from "../../features/admin/components/AddCard";
import { CustomBarChart } from "../../features/admin/components/charts/BarChart";
import { DonutChart } from "../../features/admin/components/charts/DonutChart";
import {
  UsersIcon,
  CurrencyDollarIcon,
  TicketIcon,
  UserGroupIcon
} from "@heroicons/react/24/outline";
import { apiService } from "@/lib/api";
import { AdminStats } from "@/types/user";
import { Promotion } from "@/types/promotion";

export default function AdminDashboard() {
  const { data: session, status } = useSession();
  const router = useRouter();
  const [adminStats, setAdminStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load admin data from API
  const loadAdminData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      console.log('🔄 Loading admin dashboard data...');

      // Load all data in parallel
      const [promotionsData, usersData, businessesData] = await Promise.all([
        apiService.getPromotions().catch(() => []),
        apiService.getUsers().catch(() => []),
        apiService.getBusinesses().catch(() => [])
      ]);

      // Try to load admin stats
      try {
        const stats = await apiService.getAdminStats();
        setAdminStats(stats);
      } catch {
        console.log('Admin stats not available, calculating from data');
        // Calculate stats from available data
        const calculatedStats: AdminStats = {
          totalUsers: usersData.length,
          totalBusinesses: businessesData.length,
          totalPromotions: promotionsData.length,
          totalRedemptions: promotionsData.reduce((sum: number, p: Promotion) =>
            sum + ((p.maxRedemptions || 0) - (p.currentRedemptions || 0)), 0
          ),
          monthlyGrowth: {
            users: Math.floor(Math.random() * 10) + 5, // Mock growth percentages
            businesses: Math.floor(Math.random() * 8) + 3,
            promotions: Math.floor(Math.random() * 15) + 10,
            redemptions: Math.floor(Math.random() * 20) + 15
          }
        };
        setAdminStats(calculatedStats);
      }

      console.log('✅ Admin data loaded:', {
        promotions: promotionsData.length,
        users: usersData.length,
        businesses: businessesData.length
      });

    } catch (error) {
      console.error('❌ Error loading admin data:', error);
      setError('Error al cargar los datos del dashboard');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (status === "loading") return;

    if (!session) {
      router.push("/login");
      return;
    }

    const profile = (session as { profile?: string }).profile;

    if (profile === "colaborator" || profile === "collaborator") {
      router.push("/colaborator");
      return;
    }

    if (!profile || (profile !== "admin" && profile !== "colaborator" && profile !== "collaborator")) {
      router.push("/usuario");
      return;
    }

    // Load admin data when authenticated
    loadAdminData();
  }, [session, status, router, loadAdminData]);

  if (status === "loading") {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!session) {
    return null;
  }

  // Generate KPI data from real API data
  const kpiData = adminStats ? [
    {
      title: "Usuarios Totales",
      value: adminStats.totalUsers,
      delta: adminStats.monthlyGrowth.users,
      icon: <UsersIcon className="h-4 w-4" />
    },
    {
      title: "Negocios Totales",
      value: adminStats.totalBusinesses,
      delta: adminStats.monthlyGrowth.businesses,
      icon: <CurrencyDollarIcon className="h-4 w-4" />
    },
    {
      title: "Promociones Activas",
      value: adminStats.totalPromotions,
      delta: adminStats.monthlyGrowth.promotions,
      icon: <TicketIcon className="h-4 w-4" />
    },
    {
      title: "Total Canjeos",
      value: adminStats.totalRedemptions,
      delta: adminStats.monthlyGrowth.redemptions,
      icon: <UserGroupIcon className="h-4 w-4" />
    }
  ] : [];

  // Datos para el gráfico de barras
  const chartData = [
    { date: "1 Jul", promediopordia: 380, cuponesusados: 460 },
    { date: "2 Jul", promediopordia: 320, cuponesusados: 380 },
    { date: "3 Jul", promediopordia: 360, cuponesusados: 440 },
    { date: "4 Jul", promediopordia: 290, cuponesusados: 340 },
    { date: "5 Jul", promediopordia: 370, cuponesusados: 410 },
    { date: "6 Jul", promediopordia: 250, cuponesusados: 320 },
    { date: "7 Jul", promediopordia: 540, cuponesusados: 580 },
    { date: "8 Jul", promediopordia: 380, cuponesusados: 520 },
    { date: "9 Jul", promediopordia: 320, cuponesusados: 450 },
    { date: "10 Jul", promediopordia: 360, cuponesusados: 320 }
  ];

  // Datos para el gráfico de rangos de edad
  const ageRangesLegend = [
    { label: "12-16 años", color: "#00C0CC" },
    { label: "17-20 años", color: "#008D96" },
    { label: "21-24 años", color: "#015463" },
    { label: "25-29 años", color: "#4B4C7E" }
  ];

  const ageRangesData = [
    { name: "12-16", value: 25, color: "#00C0CC" },
    { name: "17-20", value: 35, color: "#008D96" },
    { name: "21-24", value: 30, color: "#015463" },
    { name: "25-29", value: 10, color: "#4B4C7E" }
  ];

  // Datos para el gráfico de usuarios
  const usersStats = [
    { label: "usuarios totales", value: "5729" },
    { label: "nuevos usuarios", value: "213" },
    { label: "usuarios activos", value: "578" },
    { label: "menores de 22", value: "2200" }
  ];

  const usersData = [
    { name: "new_total", value: 4000, color: "#008D96" },
    { name: "new_user", value: 516, color: "#00C0CC" },
    { name: "active", value: 2133, color: "#015463" },
    { name: "under22", value: 3516, color: "#4B4C7E" },
  ];

  if (loading) {
    return (
      <DashboardLayout title="Dashboard">
        <div className="space-y-4 px-16">
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
            <span className="ml-3 text-gray-600">Cargando datos del dashboard...</span>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout title="Dashboard">
      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mx-16 mb-4">
          <div className="flex">
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Error</h3>
              <div className="mt-2 text-sm text-red-700">{error}</div>
            </div>
          </div>
        </div>
      )}

      <div className="space-y-4 px-16">
        {/* KPIs Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-5 gap-6">
          {kpiData.map((kpi, index) => (
            <KPICard
              key={index}
              title={kpi.title}
              value={kpi.value}
              delta={kpi.delta}
              icon={kpi.icon}
            />
          ))}
          <AddCard onClick={() => console.log("Agregar nueva tarjeta")} />
        </div>

        {/* Main Chart */}
        <div className="w-full">
          <CustomBarChart 
            data={chartData} 
            title="Cupones utilizados" 
          />
        </div>

        {/* Bottom Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <DonutChart
            title="Rangos de edad"
            centerValue="22 AÑOS"
            legend={ageRangesLegend}
            data={ageRangesData}
          />
          
          <DonutChart
            title="Usuarios"
            centerValue="213"
            stats={usersStats}
            data={usersData}
          />
        </div>
      </div>
    </DashboardLayout>
  );
}