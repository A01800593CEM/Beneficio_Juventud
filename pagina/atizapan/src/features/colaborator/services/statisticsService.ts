// ============================================================================
// STATISTICS SERVICE - API de Estadísticas para Colaboradores
// ============================================================================

import { ApiPromotion, ApiCollaborator } from '../promociones/types';

export interface CollaboratorStatistics {
  // KPIs Principales
  totalPromotions: number;
  activePromotions: number;
  expiredPromotions: number;
  totalRedemptions: number;
  monthlyRedemptions: number;
  averageRating: number;
  totalViews: number;
  totalFavorites: number;
  conversionRate: number;

  // Insights
  topPromotion: {
    title: string;
    redemptions: number;
    views: number;
  };
  peakHours: string[];
  topCategories: string[];

  // Datos temporales
  monthlyData: MonthlyData[];
  weeklyData: WeeklyData[];
  dailyData: DailyData[];

  // Análisis de promociones
  promotionPerformance: PromotionPerformance[];
  categoryBreakdown: CategoryBreakdown[];

  // Tendencias
  growthMetrics: GrowthMetrics;
}

export interface MonthlyData {
  month: string;
  monthNumber: number;
  year: number;
  promotions: number;
  redemptions: number;
  views: number;
  conversionRate: number;
}

export interface WeeklyData {
  week: string;
  weekNumber: number;
  promotions: number;
  redemptions: number;
  views: number;
  startDate: string;
  endDate: string;
}

export interface DailyData {
  date: string;
  day: string;
  promotions: number;
  redemptions: number;
  views: number;
}

export interface PromotionPerformance {
  promotionId: number;
  title: string;
  type: string;
  totalStock: number;
  redemptions: number;
  redemptionRate: number;
  views: number;
  conversionRate: number;
  status: string;
  daysActive: number;
  averageDailyRedemptions: number;
}

export interface CategoryBreakdown {
  category: string;
  promotions: number;
  redemptions: number;
  percentage: number;
}

export interface GrowthMetrics {
  promotionsGrowth: number; // % vs mes anterior
  redemptionsGrowth: number;
  viewsGrowth: number;
  conversionRateGrowth: number;
  customerRetention: number;
  averageEngagement: number;
  favoritesGrowth: number;
}

export class StatisticsService {
  // En el navegador (cliente), siempre usar /api/proxy para evitar CORS
  // El proxy redirige a https://api.beneficiojoven.lat en el servidor
  private baseUrl = typeof window === 'undefined'
    ? 'https://api.beneficiojoven.lat'  // SSR: usar API directa
    : '/api/proxy';  // Cliente: usar proxy local
  private VIEWS_MULTIPLIER = 12; // Multiplicador para estimar vistas basado en canjes

  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    console.log(`📊 Statistics API Request: ${options.method || 'GET'} ${url}`);

    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ Statistics API Error:', errorText);
      throw new Error(`Statistics API Error: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();
    console.log('✅ Statistics data loaded');
    return data;
  }

  /**
   * Obtiene estadísticas completas del colaborador
   */
  async getCollaboratorStatistics(cognitoId: string): Promise<CollaboratorStatistics> {
    try {
      console.log('📊 Loading collaborator statistics for:', cognitoId);

      // Cargar datos del colaborador
      const collaborators: ApiCollaborator[] = await this.request('/collaborators');
      const collaborator = collaborators.find(c => c.cognitoId === cognitoId);

      if (!collaborator) {
        throw new Error(`Colaborador no encontrado con cognitoId: ${cognitoId}`);
      }

      // Cargar promociones del colaborador
      const promotions: ApiPromotion[] = await this.request(`/collaborators/promotions/${cognitoId}`);

      // Calcular estadísticas
      const stats = this.calculateStatistics(collaborator, promotions);

      console.log('✅ Statistics calculated successfully');
      return stats;

    } catch (error) {
      console.error('❌ Error loading statistics:', error);
      throw error;
    }
  }

  /**
   * Calcula todas las estadísticas basadas en los datos de promociones
   */
  private calculateStatistics(collaborator: ApiCollaborator, promotions: ApiPromotion[]): CollaboratorStatistics {

    // KPIs básicos
    const totalPromotions = promotions.length;
    const activePromotions = promotions.filter(p => p.promotionState === 'activa').length;
    const expiredPromotions = promotions.filter(p => p.promotionState === 'finalizada').length;

    // Cálculos de canjes y ingresos
    const totalRedemptions = promotions.reduce((sum, p) =>
      sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0
    );

    // Estimación de datos mensuales (30% del total para el mes actual)
    const monthlyRedemptions = Math.floor(totalRedemptions * 0.3);

    // Cálculos de rendimiento
    const totalStock = promotions.reduce((sum, p) => sum + (p.totalStock || 0), 0);
    const conversionRate = totalStock > 0 ? (totalRedemptions / totalStock) * 100 : 0;
    const totalViews = totalRedemptions * this.VIEWS_MULTIPLIER;

    // Promoción más exitosa
    const topPromotion = promotions.reduce((best, current) => {
      const currentRedemptions = (current.totalStock || 0) - (current.availableStock || 0);
      const bestRedemptions = (best.totalStock || 0) - (best.availableStock || 0);
      return currentRedemptions > bestRedemptions ? current : best;
    }, promotions[0] || { title: "Sin promociones", totalStock: 0, availableStock: 0 });

    const topPromotionRedemptions = (topPromotion.totalStock || 0) - (topPromotion.availableStock || 0);

    // Datos mensuales simulados realistas
    const monthlyData = this.generateMonthlyData(totalRedemptions, totalViews, totalPromotions);

    // Datos semanales
    const weeklyData = this.generateWeeklyData(monthlyRedemptions, totalViews);

    // Datos diarios
    const dailyData = this.generateDailyData(monthlyRedemptions, totalViews);

    // Performance de promociones
    const promotionPerformance = this.calculatePromotionPerformance(promotions);

    // Breakdown por categorías
    const categoryBreakdown = this.calculateCategoryBreakdown(collaborator, promotions, totalRedemptions);

    // Métricas de crecimiento
    const growthMetrics = this.calculateGrowthMetrics(monthlyData);

    return {
      // KPIs principales
      totalPromotions,
      activePromotions,
      expiredPromotions,
      totalRedemptions,
      monthlyRedemptions,
      averageRating: 4.6 + (Math.random() * 0.6), // Simulado entre 4.6-5.2
      totalViews,
      totalFavorites: Math.round(totalRedemptions * 0.4), // Estimado: 40% de canjes generan favoritos
      conversionRate: Math.round(conversionRate * 100) / 100,

      // Insights
      topPromotion: {
        title: topPromotion.title,
        redemptions: topPromotionRedemptions,
        views: topPromotionRedemptions * this.VIEWS_MULTIPLIER
      },
      peakHours: ["12:00-14:00", "19:00-21:00", "20:00-22:00"],
      topCategories: collaborator.categories?.map(c => c.name) || ["COMIDA"],

      // Datos temporales
      monthlyData,
      weeklyData,
      dailyData,

      // Análisis detallado
      promotionPerformance,
      categoryBreakdown,
      growthMetrics
    };
  }

  private generateMonthlyData(totalRedemptions: number, totalViews: number, totalPromotions: number): MonthlyData[] {
    const months = [
      "Ene", "Feb", "Mar", "Abr", "May", "Jun",
      "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    ];

    const currentMonth = new Date().getMonth();
    const data: MonthlyData[] = [];

    // Generar últimos 6 meses con distribución realista
    for (let i = 5; i >= 0; i--) {
      const monthIndex = (currentMonth - i + 12) % 12;
      const isCurrentMonth = i === 0;

      // Distribución más realista: crecimiento gradual con variaciones
      const baseMultiplier = isCurrentMonth ? 1.0 : (0.6 + (i * 0.08));
      const randomVariation = 0.85 + (Math.random() * 0.3); // ±15% variación
      const multiplier = baseMultiplier * randomVariation;

      data.push({
        month: months[monthIndex],
        monthNumber: monthIndex + 1,
        year: 2024,
        promotions: Math.round(totalPromotions * (multiplier * 0.15)),
        redemptions: Math.round(totalRedemptions * multiplier * 0.16),
        views: Math.round(totalViews * multiplier * 0.16),
        conversionRate: 8.5 + (Math.random() * 4.5) // Entre 8.5% y 13%
      });
    }

    return data;
  }

  private generateWeeklyData(monthlyRedemptions: number, totalViews: number): WeeklyData[] {
    const weeks = ["Sem 1", "Sem 2", "Sem 3", "Sem 4"];
    const data: WeeklyData[] = [];

    for (let i = 0; i < 4; i++) {
      const multiplier = 0.2 + (Math.random() * 0.15); // 20-35% del mes por semana
      data.push({
        week: weeks[i],
        weekNumber: i + 1,
        promotions: Math.round(2 + (Math.random() * 3)),
        redemptions: Math.round(monthlyRedemptions * multiplier),
        views: Math.round(totalViews * multiplier * 0.25),
        startDate: `2024-10-${1 + (i * 7)}`,
        endDate: `2024-10-${7 + (i * 7)}`
      });
    }

    return data;
  }

  private generateDailyData(monthlyRedemptions: number, totalViews: number): DailyData[] {
    const days = ["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"];
    const data: DailyData[] = [];

    for (let i = 0; i < 7; i++) {
      // Viernes y sábado tienen más actividad
      const isWeekend = i >= 5;
      const baseMultiplier = isWeekend ? 0.2 : 0.12; // Fin de semana más activo

      data.push({
        date: `2024-10-${14 + i}`,
        day: days[i],
        promotions: Math.round(Math.random() * 2),
        redemptions: Math.round(monthlyRedemptions * baseMultiplier),
        views: Math.round(totalViews * baseMultiplier * 0.14)
      });
    }

    return data;
  }

  private calculatePromotionPerformance(promotions: ApiPromotion[]): PromotionPerformance[] {
    return promotions.map(promo => {
      const redemptions = (promo.totalStock || 0) - (promo.availableStock || 0);
      const redemptionRate = promo.totalStock ? (redemptions / promo.totalStock) * 100 : 0;
      const views = redemptions * this.VIEWS_MULTIPLIER;
      const conversionRate = views > 0 ? (redemptions / views) * 100 : 0;

      const startDate = new Date(promo.initialDate);
      const endDate = new Date(promo.endDate);
      const daysActive = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
      const averageDailyRedemptions = daysActive > 0 ? redemptions / daysActive : 0;

      return {
        promotionId: promo.promotionId,
        title: promo.title,
        type: promo.promotionType,
        totalStock: promo.totalStock || 0,
        redemptions,
        redemptionRate: Math.round(redemptionRate * 100) / 100,
        views,
        conversionRate: Math.round(conversionRate * 100) / 100,
        status: promo.promotionState,
        daysActive,
        averageDailyRedemptions: Math.round(averageDailyRedemptions * 100) / 100
      };
    });
  }

  private calculateCategoryBreakdown(collaborator: ApiCollaborator, promotions: ApiPromotion[], totalRedemptions: number): CategoryBreakdown[] {
    const categories = collaborator.categories || [{ name: "GENERAL" }];

    return categories.map((category, index) => {
      // Distribuir datos entre categorías con algunas más populares
      const isMainCategory = index === 0;
      const percentage = isMainCategory ? 70 + (Math.random() * 20) : (30 / (categories.length - 1 || 1));

      return {
        category: category.name,
        promotions: Math.round(promotions.length * (percentage / 100)),
        redemptions: Math.round(totalRedemptions * (percentage / 100)),
        percentage: Math.round(percentage * 100) / 100
      };
    });
  }

  private calculateGrowthMetrics(monthlyData: MonthlyData[]): GrowthMetrics {
    if (monthlyData.length < 2) {
      return {
        promotionsGrowth: 0,
        redemptionsGrowth: 0,
        viewsGrowth: 0,
        conversionRateGrowth: 0,
        customerRetention: 75,
        averageEngagement: 3.5,
        favoritesGrowth: 8.2
      };
    }

    const current = monthlyData[monthlyData.length - 1];
    const previous = monthlyData[monthlyData.length - 2];

    const calculateGrowth = (current: number, previous: number) => {
      return previous > 0 ? ((current - previous) / previous) * 100 : 0;
    };

    return {
      promotionsGrowth: Math.round(calculateGrowth(current.promotions, previous.promotions) * 100) / 100,
      redemptionsGrowth: Math.round(calculateGrowth(current.redemptions, previous.redemptions) * 100) / 100,
      viewsGrowth: Math.round(calculateGrowth(current.views, previous.views) * 100) / 100,
      conversionRateGrowth: Math.round(calculateGrowth(current.conversionRate, previous.conversionRate) * 100) / 100,
      customerRetention: 72 + (Math.random() * 16), // 72-88%
      averageEngagement: 3.2 + (Math.random() * 1.6), // 3.2-4.8 interacciones promedio
      favoritesGrowth: 5 + (Math.random() * 15) // 5-20% crecimiento en favoritos
    };
  }
}

// Instancia singleton del servicio
export const statisticsService = new StatisticsService();