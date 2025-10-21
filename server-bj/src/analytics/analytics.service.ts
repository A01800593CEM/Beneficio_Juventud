import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Between } from 'typeorm';
import { Promotion } from '../promotions/entities/promotion.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { Redeemedcoupon } from '../redeemedcoupon/entities/redeemedcoupon.entity';
import { Collaborator } from '../collaborators/entities/collaborator.entity';
import { Favorite } from '../favorites/entities/favorite.entity';
import { User } from '../users/entities/user.entity';
import { PromotionState } from 'src/promotions/enums/promotion-state.enums';

export interface VicoChartEntry {
  x: number;
  y: number;
}

export interface VicoBarChartEntry {
  label: string;
  value: number;
  promotionId?: number;
}

export interface VicoMultiSeriesEntry {
  seriesId: string;
  seriesLabel: string;
  entries: VicoChartEntry[];
}

// Admin Dashboard Interfaces (Optimized for Recharts)
export interface RechartsDataPoint {
  name: string;
  value: number;
  [key: string]: any;
}

export interface RechartsTimeSeriesPoint {
  date: string;
  [key: string]: number | string;
}

export interface TopCollaboratorData {
  collaboratorId: string;
  businessName: string;
  totalPromotions: number;
  totalRedemptions: number;
  totalBookings: number;
  conversionRate: string;
}

export interface TopPromotionData {
  promotionId: number;
  title: string;
  collaboratorName: string;
  redemptionCount: number;
  bookingCount: number;
  conversionRate: string;
}

@Injectable()
export class AnalyticsService {
  constructor(
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,
    @InjectRepository(Booking)
    private bookingsRepository: Repository<Booking>,
    @InjectRepository(Redeemedcoupon)
    private redeemedcouponRepository: Repository<Redeemedcoupon>,
    @InjectRepository(Collaborator)
    private collaboratorsRepository: Repository<Collaborator>,
    @InjectRepository(Favorite)
    private favoritesRepository: Repository<Favorite>,
    @InjectRepository(User)
    private usersRepository: Repository<User>,
  ) {}

  /**
   * Get collaborator-specific dashboard optimized for Vico (Android).
   * Vico expects chart data as arrays of x,y coordinates for line charts.
   */
  async getCollaboratorDashboard(collaboratorId: string, timeRange: string) {
    // Verify collaborator exists
    const collaborator = await this.collaboratorsRepository.findOne({
      where: { cognitoId: collaboratorId },
    });

    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }

    const dateRange = this.getDateRange(timeRange);

    // Fetch collaborator-specific data in parallel
    const [
      redemptionTrends,
      bookingTrends,
      promotionStats,
      totalStats,
      topRedeemedCoupons,
      redemptionTrendsByPromotion,
    ] = await Promise.all([
      this.getCollaboratorRedemptionTrends(collaboratorId, dateRange),
      this.getCollaboratorBookingTrends(collaboratorId, dateRange),
      this.getCollaboratorPromotionStats(collaboratorId),
      this.getCollaboratorStatistics(collaboratorId, dateRange),
      this.getTopRedeemedCoupons(collaboratorId, dateRange),
      this.getRedemptionTrendsByPromotion(collaboratorId, dateRange),
    ]);

    return {
      metadata: {
        generatedAt: new Date().toISOString(),
        collaboratorId: collaboratorId,
        collaboratorName: collaborator.businessName,
        timeRange: timeRange,
        period: {
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
        },
      },
      summary: {
        totalPromotions: totalStats.totalPromotions,
        activePromotions: totalStats.activePromotions,
        totalBookings: totalStats.totalBookings,
        redeemedCoupons: totalStats.redeemedCoupons,
        totalFavorites: totalStats.totalFavorites,
        conversionRate: this.calculateConversionRate(
          totalStats.totalBookings,
          totalStats.redeemedCoupons,
        ),
      },
      charts: {
        // Redemption trends - Vico Line Chart
        // Format: Array of {x: dayIndex, y: count}
        redemptionTrends: {
          type: 'line',
          title: 'Daily Redemptions',
          description: 'Coupons redeemed per day',
          entries: redemptionTrends,
          xAxisLabel: 'Days',
          yAxisLabel: 'Redemptions',
          minY: 0,
          maxY: Math.max(...redemptionTrends.map((e) => e.y), 1),
        },
        // Booking trends - Vico Line Chart
        bookingTrends: {
          type: 'line',
          title: 'Daily Bookings',
          description: 'Coupons booked/reserved per day',
          entries: bookingTrends,
          xAxisLabel: 'Days',
          yAxisLabel: 'Bookings',
          minY: 0,
          maxY: Math.max(...bookingTrends.map((e) => e.y), 1),
        },
        // Promotion statistics summary
        promotionStats: {
          type: 'summary',
          title: 'Active Promotions',
          description: 'Detailed breakdown of promotions',
          data: promotionStats,
        },
        // Top 5 most redeemed coupons - Vico Bar Chart
        topRedeemedCoupons: {
          type: 'bar',
          title: 'Top 5 Most Redeemed Coupons',
          description: 'Most popular coupons by redemption count',
          entries: topRedeemedCoupons,
          xAxisLabel: 'Coupons',
          yAxisLabel: 'Redemptions',
        },
        // Multi-series line chart - Redemptions over time by promotion
        redemptionTrendsByPromotion: {
          type: 'multiline',
          title: 'Redemptions Over Time by Coupon',
          description: 'Track redemption trends for top 5 coupons',
          series: redemptionTrendsByPromotion,
          xAxisLabel: 'Days',
          yAxisLabel: 'Redemptions',
        },
      },
      insights: this.generateCollaboratorInsights(
        totalStats,
        redemptionTrends,
        promotionStats,
      ),
    };
  }

  /**
   * Get promotion-specific analytics for a collaborator.
   */
  async getPromotionAnalytics(collaboratorId: string) {
    // Verify collaborator exists
    const collaborator = await this.collaboratorsRepository.findOne({
      where: { cognitoId: collaboratorId },
    });

    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }

    // Get all promotions for collaborator
    const promotions = await this.promotionsRepository.find({
      where: { collaboratorId },
      relations: ['bookings', 'redeemedcoupon'],
    });

    const promotionAnalytics = await Promise.all(
      promotions.map(async (promo) => {
        const bookings = await this.bookingsRepository.count({
          where: { promotionId: promo.promotionId },
        });

        const redemptions = await this.redeemedcouponRepository.count({
          where: { promotionId: promo.promotionId },
        });

        return {
          promotionId: promo.promotionId,
          title: promo.title,
          description: promo.description,
          type: promo.promotionType,
          status: promo.promotionState,
          dateRange: {
            startDate: promo.initialDate,
            endDate: promo.endDate,
          },
          performance: {
            totalStock: promo.totalStock,
            availableStock: promo.availableStock,
            usedStock: promo.totalStock - promo.availableStock,
            redeemedCount: redemptions,
            bookingCount: bookings,
            conversionRate: this.calculateConversionRate(bookings, redemptions),
            redemptionPercentage: (
              ((promo.totalStock - promo.availableStock) / (promo.totalStock || 1)) *
              100
            ).toFixed(2),
          },
        };
      }),
    );

    return {
      metadata: {
        generatedAt: new Date().toISOString(),
        collaboratorId: collaboratorId,
        collaboratorName: collaborator.businessName,
      },
      summary: {
        totalPromotions: promotions.length,
        activePromotions: promotions.filter(
          (p) => p.promotionState === 'activa',
        ).length,
        totalRedemptions: promotionAnalytics.reduce(
          (sum, p) => sum + p.performance.redeemedCount,
          0,
        ),
        totalBookings: promotionAnalytics.reduce(
          (sum, p) => sum + p.performance.bookingCount,
          0,
        ),
      },
      promotions: promotionAnalytics,
    };
  }

  // =================== PRIVATE HELPER METHODS ===================

  /**
   * Calculate date range based on timeRange parameter
   */
  private getDateRange(timeRange: string) {
    const today = new Date();
    let startDate: Date;
    let endDate = new Date(today);

    // Reset time to midnight for consistency
    endDate.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    switch (timeRange.toLowerCase()) {
      case 'week':
        startDate = new Date(today);
        startDate.setDate(today.getDate() - 7);
        break;
      case 'year':
        startDate = new Date(today.getFullYear(), 0, 1);
        endDate = new Date(today.getFullYear(), 11, 31);
        break;
      case 'month':
      default:
        startDate = new Date(today);
        startDate.setDate(today.getDate() - 30);
        break;
    }

    return { startDate, endDate };
  }

  /**
   * Get array of days between two dates for Vico chart alignment
   */
  private getDaysBetween(startDate: Date, endDate: Date): Date[] {
    const days: Date[] = [];
    const current = new Date(startDate);

    while (current <= endDate) {
      days.push(new Date(current));
      current.setDate(current.getDate() + 1);
    }

    return days;
  }

  /**
   * Convert database trend data to Vico chart entries (x,y format)
   */
  private convertToVicoEntries(
    trendData: any[],
    days: Date[],
  ): VicoChartEntry[] {
    const dataMap = new Map(
      trendData.map((item) => [
        new Date(item.date).toISOString().split('T')[0],
        parseInt(item.count),
      ]),
    );

    return days.map((day, index) => ({
      x: index,
      y: dataMap.get(day.toISOString().split('T')[0]) || 0,
    }));
  }

  /**
   * Get redemption trends for a specific collaborator
   */
  private async getCollaboratorRedemptionTrends(
    collaboratorId: string,
    dateRange: any,
  ): Promise<VicoChartEntry[]> {
    const days = this.getDaysBetween(dateRange.startDate, dateRange.endDate);

    const redemptions = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .leftJoinAndSelect('redeem.promotion', 'promo')
      .where('promo.collaboratorId = :collaboratorId', { collaboratorId })
      .andWhere('redeem.usedDate >= :startDate', {
        startDate: dateRange.startDate,
      })
      .andWhere('redeem.usedDate <= :endDate', {
        endDate: dateRange.endDate,
      })
      .select('DATE(redeem.usedDate)', 'date')
      .addSelect('COUNT(redeem.usedId)', 'count')
      .groupBy('DATE(redeem.usedDate)')
      .orderBy('date', 'ASC')
      .getRawMany();

    return this.convertToVicoEntries(redemptions, days);
  }

  /**
   * Get booking trends for a specific collaborator
   */
  private async getCollaboratorBookingTrends(
    collaboratorId: string,
    dateRange: any,
  ): Promise<VicoChartEntry[]> {
    const days = this.getDaysBetween(dateRange.startDate, dateRange.endDate);

    const bookings = await this.bookingsRepository
      .createQueryBuilder('booking')
      .leftJoinAndSelect('booking.promotion', 'promo')
      .where('promo.collaboratorId = :collaboratorId', { collaboratorId })
      .andWhere('booking.bookingDate >= :startDate', {
        startDate: dateRange.startDate,
      })
      .andWhere('booking.bookingDate <= :endDate', {
        endDate: dateRange.endDate,
      })
      .select('DATE(booking.bookingDate)', 'date')
      .addSelect('COUNT(booking.bookingId)', 'count')
      .groupBy('DATE(booking.bookingDate)')
      .orderBy('date', 'ASC')
      .getRawMany();

    return this.convertToVicoEntries(bookings, days);
  }

  /**
   * Get summary statistics for promotions
   */
  private async getCollaboratorPromotionStats(collaboratorId: string) {
    const promotions = await this.promotionsRepository.find({
      where: { collaboratorId },
    });

    return promotions.map((p) => ({
      promotionId: p.promotionId,
      title: p.title,
      type: p.promotionType,
      status: p.promotionState,
      stockRemaining: p.availableStock,
      totalStock: p.totalStock,
      stockUtilization: (
        ((p.totalStock - p.availableStock) / p.totalStock) *
        100
      ).toFixed(2),
    }));
  }

  /**
   * Get total statistics for collaborator
   */
  private async getCollaboratorStatistics(
    collaboratorId: string,
    dateRange: any,
  ) {
    // Count total promotions
    const totalPromotions = await this.promotionsRepository.count({
      where: { collaboratorId },
    });

    // Count active promotions
    const activePromotions = await this.promotionsRepository.count({
      where: { collaboratorId, promotionState: PromotionState.ACTIVE },
    });

    // Count total bookings for collaborator's promotions
    const totalBookings = await this.bookingsRepository
      .createQueryBuilder('booking')
      .leftJoinAndSelect('booking.promotion', 'promo')
      .where('promo.collaboratorId = :collaboratorId', { collaboratorId })
      .getCount();

    // Count redeemed coupons in date range
    const redeemedCoupons = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .leftJoinAndSelect('redeem.promotion', 'promo')
      .where('promo.collaboratorId = :collaboratorId', { collaboratorId })
      .andWhere('redeem.usedDate >= :startDate', {
        startDate: dateRange.startDate,
      })
      .andWhere('redeem.usedDate <= :endDate', {
        endDate: dateRange.endDate,
      })
      .getCount();

    // Count total favorites for this collaborator
    const totalFavorites = await this.favoritesRepository.count({
      where: { collaboratorId },
    });

    return {
      totalPromotions,
      activePromotions,
      totalBookings,
      redeemedCoupons,
      totalFavorites,
    };
  }

  /**
   * Calculate conversion rate between bookings and redemptions
   */
  private calculateConversionRate(bookings: number, redemptions: number): string {
    if (bookings === 0) return '0%';
    return ((redemptions / bookings) * 100).toFixed(2) + '%';
  }

  /**
   * Get top 5 most redeemed coupons for a collaborator.
   * Returns data optimized for Vico bar chart.
   */
  private async getTopRedeemedCoupons(
    collaboratorId: string,
    dateRange: any,
  ): Promise<VicoBarChartEntry[]> {
    const topCoupons = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .leftJoinAndSelect('redeem.promotion', 'promo')
      .where('promo.collaboratorId = :collaboratorId', { collaboratorId })
      .andWhere('redeem.usedDate >= :startDate', {
        startDate: dateRange.startDate,
      })
      .andWhere('redeem.usedDate <= :endDate', {
        endDate: dateRange.endDate,
      })
      .select('promo.promotionId', 'promotionId')
      .addSelect('promo.title', 'title')
      .addSelect('COUNT(redeem.usedId)', 'redemptionCount')
      .groupBy('promo.promotionId')
      .addGroupBy('promo.title')
      .orderBy('"redemptionCount"', 'DESC')
      .limit(5)
      .getRawMany();

    return topCoupons.map((coupon) => ({
      label: coupon.title,
      value: parseInt(coupon.redemptionCount),
      promotionId: coupon.promotionId,
    }));
  }

  /**
   * Get redemption trends over time for top 5 promotions.
   * Returns multi-series data optimized for Vico multi-line chart.
   */
  private async getRedemptionTrendsByPromotion(
    collaboratorId: string,
    dateRange: any,
  ): Promise<VicoMultiSeriesEntry[]> {
    const days = this.getDaysBetween(dateRange.startDate, dateRange.endDate);

    // First, get top 5 promotions by redemption count
    const topPromotions = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .leftJoinAndSelect('redeem.promotion', 'promo')
      .where('promo.collaboratorId = :collaboratorId', { collaboratorId })
      .andWhere('redeem.usedDate >= :startDate', {
        startDate: dateRange.startDate,
      })
      .andWhere('redeem.usedDate <= :endDate', {
        endDate: dateRange.endDate,
      })
      .select('promo.promotionId', 'promotionId')
      .addSelect('promo.title', 'title')
      .addSelect('COUNT(redeem.usedId)', 'count')
      .groupBy('promo.promotionId')
      .addGroupBy('promo.title')
      .orderBy('count', 'DESC')
      .limit(5)
      .getRawMany();

    // For each top promotion, get daily redemption trends
    const seriesPromises = topPromotions.map(async (promo) => {
      const dailyRedemptions = await this.redeemedcouponRepository
        .createQueryBuilder('redeem')
        .where('redeem.promotionId = :promotionId', {
          promotionId: promo.promotionId,
        })
        .andWhere('redeem.usedDate >= :startDate', {
          startDate: dateRange.startDate,
        })
        .andWhere('redeem.usedDate <= :endDate', {
          endDate: dateRange.endDate,
        })
        .select('DATE(redeem.usedDate)', 'date')
        .addSelect('COUNT(redeem.usedId)', 'count')
        .groupBy('DATE(redeem.usedDate)')
        .orderBy('date', 'ASC')
        .getRawMany();

      const entries = this.convertToVicoEntries(dailyRedemptions, days);

      return {
        seriesId: `promo_${promo.promotionId}`,
        seriesLabel: promo.title,
        entries,
      };
    });

    return Promise.all(seriesPromises);
  }

  /**
   * Get comprehensive admin dashboard with platform-wide analytics.
   * Optimized for Recharts (Web).
   */
  async getAdminDashboard(timeRange: string = 'month') {
    const dateRange = this.getDateRange(timeRange);

    // Fetch all admin analytics in parallel
    const [
      platformStats,
      userGrowth,
      topCollaborators,
      topPromotions,
      categoryDistribution,
      redemptionTrends,
    ] = await Promise.all([
      this.getPlatformStatistics(dateRange),
      this.getUserGrowthTrends(dateRange),
      this.getTopCollaborators(dateRange),
      this.getTopPromotions(dateRange),
      this.getCategoryDistribution(),
      this.getPlatformRedemptionTrends(dateRange),
    ]);

    return {
      metadata: {
        generatedAt: new Date().toISOString(),
        timeRange: timeRange,
        period: {
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
        },
      },
      summary: {
        totalUsers: platformStats.totalUsers,
        totalCollaborators: platformStats.totalCollaborators,
        totalPromotions: platformStats.totalPromotions,
        activePromotions: platformStats.activePromotions,
        totalRedemptions: platformStats.totalRedemptions,
        totalBookings: platformStats.totalBookings,
        platformConversionRate: this.calculateConversionRate(
          platformStats.totalBookings,
          platformStats.totalRedemptions,
        ),
      },
      charts: {
        // User growth over time - Line Chart
        userGrowth: {
          type: 'line',
          title: 'User Growth',
          description: 'New user registrations over time',
          data: userGrowth,
        },
        // Top collaborators - Bar Chart
        topCollaborators: {
          type: 'bar',
          title: 'Top 10 Collaborators',
          description: 'Most active collaborators by redemptions',
          data: topCollaborators,
        },
        // Top promotions - Bar Chart
        topPromotions: {
          type: 'bar',
          title: 'Top 10 Promotions',
          description: 'Most popular promotions platform-wide',
          data: topPromotions,
        },
        // Category distribution - Pie Chart
        categoryDistribution: {
          type: 'pie',
          title: 'Promotion Categories',
          description: 'Distribution of promotions by category',
          data: categoryDistribution,
        },
        // Platform redemption trends - Area Chart
        redemptionTrends: {
          type: 'area',
          title: 'Platform Redemption Trends',
          description: 'Daily redemptions across the platform',
          data: redemptionTrends,
        },
      },
    };
  }

  /**
   * Get platform-wide statistics
   */
  private async getPlatformStatistics(dateRange: any) {
    const totalUsers = await this.usersRepository.count();
    const totalCollaborators = await this.collaboratorsRepository.count();
    const totalPromotions = await this.promotionsRepository.count();
    const activePromotions = await this.promotionsRepository.count({
      where: { promotionState: PromotionState.ACTIVE },
    });

    const totalRedemptions = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .where('redeem.usedDate >= :startDate', { startDate: dateRange.startDate })
      .andWhere('redeem.usedDate <= :endDate', { endDate: dateRange.endDate })
      .getCount();

    const totalBookings = await this.bookingsRepository
      .createQueryBuilder('booking')
      .where('booking.bookingDate >= :startDate', { startDate: dateRange.startDate })
      .andWhere('booking.bookingDate <= :endDate', { endDate: dateRange.endDate })
      .getCount();

    return {
      totalUsers,
      totalCollaborators,
      totalPromotions,
      activePromotions,
      totalRedemptions,
      totalBookings,
    };
  }

  /**
   * Get user growth trends for Recharts line chart
   */
  private async getUserGrowthTrends(dateRange: any): Promise<RechartsTimeSeriesPoint[]> {
    const days = this.getDaysBetween(dateRange.startDate, dateRange.endDate);

    const userRegistrations = await this.usersRepository
      .createQueryBuilder('user')
      .where('user.registrationDate >= :startDate', { startDate: dateRange.startDate })
      .andWhere('user.registrationDate <= :endDate', { endDate: dateRange.endDate })
      .select('DATE(user.registrationDate)', 'date')
      .addSelect('COUNT(user.id)', 'count')
      .groupBy('DATE(user.registrationDate)')
      .orderBy('date', 'ASC')
      .getRawMany();

    const dataMap = new Map(
      userRegistrations.map((item) => [
        new Date(item.date).toISOString().split('T')[0],
        parseInt(item.count),
      ]),
    );

    return days.map((day) => ({
      date: day.toISOString().split('T')[0],
      users: dataMap.get(day.toISOString().split('T')[0]) || 0,
    }));
  }

  /**
   * Get top 10 collaborators for Recharts bar chart
   */
  private async getTopCollaborators(dateRange: any): Promise<TopCollaboratorData[]> {
    const topCollabs = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .leftJoinAndSelect('redeem.promotion', 'promo')
      .leftJoinAndSelect('promo.collaborator', 'collab')
      .where('redeem.usedDate >= :startDate', { startDate: dateRange.startDate })
      .andWhere('redeem.usedDate <= :endDate', { endDate: dateRange.endDate })
      .select('collab.cognitoId', 'collaboratorId')
      .addSelect('collab.businessName', 'businessName')
      .addSelect('COUNT(DISTINCT promo.promotionId)', 'totalPromotions')
      .addSelect('COUNT(redeem.usedId)', 'totalRedemptions')
      .groupBy('collab.cognitoId')
      .addGroupBy('collab.businessName')
      .orderBy('COUNT(redeem.usedId)', 'DESC')
      .limit(10)
      .getRawMany();

    // Get booking counts for each collaborator
    const collabsWithBookings = await Promise.all(
      topCollabs.map(async (collab) => {
        const bookingCount = await this.bookingsRepository
          .createQueryBuilder('booking')
          .leftJoin('booking.promotion', 'promo')
          .where('promo.collaboratorId = :collaboratorId', {
            collaboratorId: collab.collaboratorId,
          })
          .andWhere('booking.bookingDate >= :startDate', { startDate: dateRange.startDate })
          .andWhere('booking.bookingDate <= :endDate', { endDate: dateRange.endDate })
          .getCount();

        return {
          collaboratorId: collab.collaboratorId,
          businessName: collab.businessName,
          totalPromotions: parseInt(collab.totalPromotions),
          totalRedemptions: parseInt(collab.totalRedemptions),
          totalBookings: bookingCount,
          conversionRate: this.calculateConversionRate(
            bookingCount,
            parseInt(collab.totalRedemptions),
          ),
        };
      }),
    );

    return collabsWithBookings;
  }

  /**
   * Get top 10 promotions for Recharts bar chart
   */
  private async getTopPromotions(dateRange: any): Promise<TopPromotionData[]> {
    const topPromos = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .leftJoinAndSelect('redeem.promotion', 'promo')
      .leftJoinAndSelect('promo.collaborator', 'collab')
      .where('redeem.usedDate >= :startDate', { startDate: dateRange.startDate })
      .andWhere('redeem.usedDate <= :endDate', { endDate: dateRange.endDate })
      .select('promo.promotionId', 'promotionId')
      .addSelect('promo.title', 'title')
      .addSelect('collab.businessName', 'collaboratorName')
      .addSelect('COUNT(redeem.usedId)', 'redemptionCount')
      .groupBy('promo.promotionId')
      .addGroupBy('promo.title')
      .addGroupBy('collab.businessName')
      .orderBy('COUNT(redeem.usedId)', 'DESC')
      .limit(10)
      .getRawMany();

    // Get booking counts for each promotion
    const promosWithBookings = await Promise.all(
      topPromos.map(async (promo) => {
        const bookingCount = await this.bookingsRepository.count({
          where: { promotionId: promo.promotionId },
        });

        return {
          promotionId: promo.promotionId,
          title: promo.title,
          collaboratorName: promo.collaboratorName,
          redemptionCount: parseInt(promo.redemptionCount),
          bookingCount,
          conversionRate: this.calculateConversionRate(
            bookingCount,
            parseInt(promo.redemptionCount),
          ),
        };
      }),
    );

    return promosWithBookings;
  }

  /**
   * Get category distribution for Recharts pie chart
   */
  private async getCategoryDistribution(): Promise<RechartsDataPoint[]> {
    const categoryStats = await this.promotionsRepository
      .createQueryBuilder('promo')
      .leftJoin('promo.categories', 'category')
      .select('category.name', 'name')
      .addSelect('COUNT(promo.promotionId)', 'value')
      .groupBy('category.name')
      .orderBy('value', 'DESC')
      .getRawMany();

    return categoryStats.map((cat) => ({
      name: cat.name || 'Sin categoría',
      value: parseInt(cat.value),
    }));
  }

  /**
   * Get platform-wide redemption trends for Recharts area chart
   */
  private async getPlatformRedemptionTrends(
    dateRange: any,
  ): Promise<RechartsTimeSeriesPoint[]> {
    const days = this.getDaysBetween(dateRange.startDate, dateRange.endDate);

    const redemptions = await this.redeemedcouponRepository
      .createQueryBuilder('redeem')
      .where('redeem.usedDate >= :startDate', { startDate: dateRange.startDate })
      .andWhere('redeem.usedDate <= :endDate', { endDate: dateRange.endDate })
      .select('DATE(redeem.usedDate)', 'date')
      .addSelect('COUNT(redeem.usedId)', 'count')
      .groupBy('DATE(redeem.usedDate)')
      .orderBy('date', 'ASC')
      .getRawMany();

    const dataMap = new Map(
      redemptions.map((item) => [
        new Date(item.date).toISOString().split('T')[0],
        parseInt(item.count),
      ]),
    );

    return days.map((day) => ({
      date: day.toISOString().split('T')[0],
      redemptions: dataMap.get(day.toISOString().split('T')[0]) || 0,
    }));
  }

  /**
   * Generate insights based on collaborator statistics
   */
  private generateCollaboratorInsights(
    totalStats: any,
    redemptionTrends: VicoChartEntry[],
    promotionStats: any[],
  ): any[] {
    const insights: any[] = [];

    // Check conversion rate
    const conversionRate = parseFloat(totalStats.conversionRate);
    if (conversionRate < 50) {
      insights.push({
        type: 'warning',
        title: 'Low Conversion Rate',
        message: `Your conversion rate is ${totalStats.conversionRate}. Consider reviewing your promotion terms.`,
        severity: 'medium',
      });
    }

    // Check for low redemptions
    if (totalStats.redeemedCoupons < 10) {
      insights.push({
        type: 'info',
        title: 'Limited Redemptions',
        message: 'Your promotions have had limited engagement. Try different promotion strategies.',
        severity: 'low',
      });
    }

    // Check stock utilization
    const lowStockPromos = promotionStats.filter(
      (p) => parseFloat(p.stockUtilization) < 20,
    );
    if (lowStockPromos.length > 0) {
      insights.push({
        type: 'info',
        title: 'Low Stock Utilization',
        message: `${lowStockPromos.length} promotion(s) have low stock utilization.`,
        severity: 'low',
      });
    }

    // Positive insight for high redemptions
    if (totalStats.redeemedCoupons > 50) {
      insights.push({
        type: 'success',
        title: 'Great Performance',
        message: `Excellent engagement with ${totalStats.redeemedCoupons} redemptions!`,
        severity: 'high',
      });
    }

    return insights;
  }
}