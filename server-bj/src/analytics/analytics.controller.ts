import { Controller, Get, Param, Query } from '@nestjs/common';
import { AnalyticsService } from './analytics.service';

/**
 * Controller for analytics endpoints serving collaborators.
 * Provides optimized responses for Vico (Android).
 * @route /analytics
 */
@Controller('analytics')
export class AnalyticsController {
  constructor(private readonly analyticsService: AnalyticsService) {}

  /**
   * Get comprehensive analytics for a specific collaborator.
   * Optimized for Vico visualization library (Android).
   * @route GET /analytics/collaborator/:collaboratorId?timeRange=month
   * @param collaboratorId - Cognito ID of the collaborator
   * @param timeRange - 'week', 'month', 'year' (default: 'month')
   * @returns Collaborator dashboard data optimized for Vico
   */
  @Get('collaborator/:collaboratorId')
  async getCollaboratorDashboard(
    @Param('collaboratorId') collaboratorId: string,
    @Query('timeRange') timeRange: string = 'month',
  ) {
    return this.analyticsService.getCollaboratorDashboard(
      collaboratorId,
      timeRange,
    );
  }

  /**
   * Get promotion-specific analytics for a collaborator.
   * @route GET /analytics/collaborator/:collaboratorId/promotions
   * @param collaboratorId - Cognito ID of the collaborator
   * @returns Promotion performance data
   */
  @Get('collaborator/:collaboratorId/promotions')
  async getPromotionAnalytics(@Param('collaboratorId') collaboratorId: string) {
    return this.analyticsService.getPromotionAnalytics(collaboratorId);
  }

  /**
   * Get comprehensive platform-wide analytics for administrators.
   * Optimized for Recharts visualization library (Web).
   * @route GET /analytics/admin/dashboard?timeRange=month
   * @param timeRange - 'week', 'month', 'year' (default: 'month')
   * @returns Admin dashboard data optimized for Recharts
   */
  @Get('admin/dashboard')
  async getAdminDashboard(@Query('timeRange') timeRange: string = 'month') {
    return this.analyticsService.getAdminDashboard(timeRange);
  }
}