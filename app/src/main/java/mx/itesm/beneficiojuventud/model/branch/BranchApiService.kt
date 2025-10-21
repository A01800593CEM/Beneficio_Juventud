package mx.itesm.beneficiojuventud.model.branch

import mx.itesm.beneficiojuventud.model.Branch
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para gestión de sucursales
 */
interface BranchApiService {

    /**
     * Obtiene todas las sucursales
     */
    @GET("branch")
    suspend fun getAllBranches(): Response<List<Branch>>

    /**
     * Obtiene una sucursal por ID
     */
    @GET("branch/{id}")
    suspend fun getBranchById(@Path("id") id: Int): Response<Branch>

    /**
     * Obtiene todas las sucursales de un colaborador
     */
    @GET("branch/collaborator/{collaboratorId}")
    suspend fun getBranchesByCollaborator(
        @Path("collaboratorId") collaboratorId: String
    ): Response<List<Branch>>

    /**
     * Crea una nueva sucursal
     */
    @POST("branch")
    suspend fun createBranch(
        @Body request: CreateBranchRequest
    ): Response<Branch>

    /**
     * Actualiza una sucursal existente
     */
    @PATCH("branch/{id}")
    suspend fun updateBranch(
        @Path("id") id: Int,
        @Body update: UpdateBranchRequest
    ): Response<Branch>

    /**
     * Elimina una sucursal
     */
    @DELETE("branch/{id}")
    suspend fun deleteBranch(@Path("id") id: Int): Response<Unit>
}
