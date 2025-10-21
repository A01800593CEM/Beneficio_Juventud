package mx.itesm.beneficiojuventud.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.*
import java.lang.reflect.Type

data class Branch(
    val branchId: Int? = null,
    val collaboratorId: String? = null,
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val zipCode: String? = null,
    @JsonAdapter(LocationAdapter::class)
    val location: String? = null,
    val jsonSchedule: Any? = null,
    val state: BranchState? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

enum class BranchState {
    @SerializedName("activa") ACTIVE,
    @SerializedName("inactiva") INACTIVE
}

/**
 * Adaptador personalizado para manejar el campo location que puede venir como:
 * - String: "(longitude,latitude)"
 * - Objeto: {"x": longitude, "y": latitude}
 */
class LocationAdapter : JsonSerializer<String>, JsonDeserializer<String> {
    override fun serialize(src: String?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): String? {
        if (json == null || json.isJsonNull) return null

        return when {
            json.isJsonPrimitive -> json.asString
            json.isJsonObject -> {
                val obj = json.asJsonObject
                // PostgreSQL Point retorna como {x: lon, y: lat}
                val x = obj.get("x")?.asDouble
                val y = obj.get("y")?.asDouble
                if (x != null && y != null) {
                    "($x,$y)"
                } else {
                    null
                }
            }
            else -> null
        }
    }
}

