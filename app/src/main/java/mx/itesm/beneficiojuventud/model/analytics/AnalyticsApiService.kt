package mx.itesm.beneficiojuventud.model.analytics

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnalyticsApiService {

    @GET("analytics/collaborator/{collaboratorId}")
    suspend fun getCollaboratorDashboard(
        @Path("collaboratorId") collaboratorId: String,
        @Query("timeRange") timeRange: String
    ): Response<AnalyticsDashboard>

    @GET("analytics/collaborator/{collaboratorId}/promotions")
    suspend fun getPromotionAnalytics(
        @Path("collaboratorId") collaboratorId: String
    ): Response<PromotionAnalyticsResponse>
}