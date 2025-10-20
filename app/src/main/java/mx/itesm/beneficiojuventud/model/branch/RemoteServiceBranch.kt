package mx.itesm.beneficiojuventud.model.branch

import com.google.gson.GsonBuilder
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServiceBranch {

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val branchApiService by lazy { retrofit.create(BranchApiService::class.java) }

    suspend fun getAllBranches(): List<Branch> {
        val response = branchApiService.getAllBranches()
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: emptyList()
    }

    suspend fun getBranchById(id: Int): Branch {
        val response = branchApiService.getBranchById(id)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al obtener la sucursal $id")
    }

    suspend fun getBranchesByCollaborator(collaboratorId: String): List<Branch> {
        val response = branchApiService.getBranchesByCollaborator(collaboratorId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: emptyList()
    }

    suspend fun createBranch(request: CreateBranchRequest): Branch {
        val response = branchApiService.createBranch(request)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string().orEmpty()
            throw Exception("Error ${response.code()}: $errorBody")
        }
        return response.body() ?: throw Exception("Respuesta vacía al crear la sucursal")
    }

    suspend fun updateBranch(id: Int, update: UpdateBranchRequest): Branch {
        val response = branchApiService.updateBranch(id, update)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al actualizar la sucursal $id")
    }

    suspend fun deleteBranch(id: Int) {
        val response = branchApiService.deleteBranch(id)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }
}
