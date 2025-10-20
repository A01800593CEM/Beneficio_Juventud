package mx.itesm.beneficiojuventud.model.analytics

import com.google.gson.GsonBuilder
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServiceAnalytics {

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val analyticsApiService by lazy { retrofit.create(AnalyticsApiService::class.java) }

    /**
     * Get collaborator analytics dashboard
     * @param collaboratorId The collaborator's cognito ID
     * @param timeRange One of: "week", "month", "year"
     */
    suspend fun getCollaboratorDashboard(
        collaboratorId: String,
        timeRange: String
    ): AnalyticsDashboard {
        val response = analyticsApiService.getCollaboratorDashboard(collaboratorId, timeRange)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Empty response getting analytics dashboard")
    }

    /**
     * Get detailed promotion analytics for a collaborator
     * @param collaboratorId The collaborator's cognito ID
     */
    suspend fun getPromotionAnalytics(collaboratorId: String): PromotionAnalyticsResponse {
        val response = analyticsApiService.getPromotionAnalytics(collaboratorId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Empty response getting promotion analytics")
    }
}