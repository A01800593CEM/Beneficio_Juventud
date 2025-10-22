package mx.itesm.beneficiojuventud.model.analytics

import android.util.Log
import com.google.gson.GsonBuilder
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServiceAnalytics {

    private const val TAG = "RemoteServiceAnalytics"

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
        Log.d(TAG, "Fetching dashboard for collaboratorId: $collaboratorId, timeRange: $timeRange")
        Log.d(TAG, "Request URL: ${Constants.BASE_URL}analytics/collaborator/$collaboratorId?timeRange=$timeRange")

        val response = analyticsApiService.getCollaboratorDashboard(collaboratorId, timeRange)

        Log.d(TAG, "Response code: ${response.code()}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string().orEmpty()
            Log.e(TAG, "Error response: $errorBody")
            throw Exception("Error ${response.code()}: $errorBody")
        }

        val body = response.body()
        if (body == null) {
            Log.e(TAG, "Response body is null")
            throw Exception("Empty response getting analytics dashboard")
        }

        Log.d(TAG, "Successfully fetched dashboard")
        Log.d(TAG, "Charts in response: ${body.charts.keys}")
        return body
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