package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos

class PromoViewModel : ViewModel() {

    private val model = RemoteServicePromos

    private val _promoState = MutableStateFlow(Promotions())
    val promoState: StateFlow<Promotions> = _promoState

    private val _promoListState = MutableStateFlow<List<Promotions>>(emptyList())
    val promoListState: StateFlow<List<Promotions>> = _promoListState

    // Helper para filtrar solo promociones activas
    private fun List<Promotions>.onlyActive(): List<Promotions> {
        return this.filter { it.promotionState == PromotionState.activa }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CRUD/Queries base
    // ─────────────────────────────────────────────────────────────────────────────

    /** Obtiene TODAS las promociones (para colaboradores) */
    suspend fun getAllPromotions() {
        _promoListState.value = model.getAllPromotions()
    }

    /** Obtiene SOLO promociones activas (para usuarios) */
    suspend fun getActivePromotions() {
        _promoListState.value = model.getAllPromotions().onlyActive()
    }

    suspend fun getPromotionById(id: Int) {
        _promoState.value = model.getPromotionById(id)
    }

    /** Obtiene promociones por categoría - TODAS (para colaboradores) */
    suspend fun getPromotionByCategory(category: String) {
        _promoListState.value = model.getPromotionByCategory(category)
    }

    /** Obtiene promociones por categoría - SOLO activas (para usuarios) */
    suspend fun getActivePromotionsByCategory(category: String) {
        _promoListState.value = model.getPromotionByCategory(category).onlyActive()
    }

    suspend fun createPromotion(promo: Promotions) {
        _promoState.value = model.createPromotion(promo)
    }

    suspend fun updatePromotion(id: Int, update: Promotions) {
        _promoState.value = model.updatePromotion(id, update)
    }

    suspend fun deletePromotion(id: Int) {
        model.deletePromotion(id)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Recomendado para ti (intercalado por categorías favoritas)
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Obtiene promociones ACTIVAS por cada categoría dada y las entrelaza (round-robin)
     * 1 de cada lista, repitiendo hasta agotar todas. Evita duplicados por promotionId.
     */
    suspend fun getRecommendedInterleaved(categories: List<String>) {
        if (categories.isEmpty()) {
            // Fallback si el usuario no tiene categorías - solo activas
            _promoListState.value = model.getAllPromotions().onlyActive()
            return
        }

        // Cargar en paralelo todas las listas por categoría (solo activas)
        val lists: List<List<Promotions>> = coroutineScope {
            categories.map { cat ->
                async {
                    runCatching { model.getPromotionByCategory(cat).onlyActive() }
                        .getOrElse { emptyList() }
                }
            }.map { it.await() }
        }

        _promoListState.value = interleaveRoundRobinDistinct(lists)
    }

    /** Intercala 1-a-1 manteniendo orden relativo dentro de cada lista y sin duplicados. */
    private fun interleaveRoundRobinDistinct(lists: List<List<Promotions>>): List<Promotions> {
        if (lists.isEmpty()) return emptyList()
        val queues = lists.map { it.toMutableList() }
        val seen = LinkedHashSet<Int>() // promotionId para deduplicar
        val out = ArrayList<Promotions>()

        var anyLeft: Boolean
        do {
            anyLeft = false
            for (q in queues) {
                if (q.isNotEmpty()) {
                    anyLeft = true
                    val next = q.removeAt(0)
                    val id = next.promotionId
                    if (id == null || seen.add(id)) {
                        out.add(next)
                    }
                }
            }
        } while (anyLeft)

        return out
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Favoritos locales (client-side)
    // ─────────────────────────────────────────────────────────────────────────────
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    fun isFavorite(promoId: Int): Boolean = _favoriteIds.value.contains(promoId)

    fun toggleFavorite(promoId: Int) = viewModelScope.launch {
        _favoriteIds.update { set ->
            if (set.contains(promoId)) set - promoId else set + promoId
        }
    }
}
