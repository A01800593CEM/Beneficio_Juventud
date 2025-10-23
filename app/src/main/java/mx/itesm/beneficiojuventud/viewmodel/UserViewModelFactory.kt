package mx.itesm.beneficiojuventud.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.itesm.beneficiojuventud.model.RoomDB.LocalDatabase
import mx.itesm.beneficiojuventud.model.SavedCouponRepository
import mx.itesm.beneficiojuventud.model.history.HistoryService

/**
 * Factory for creating UserViewModel with proper dependency injection.
 * Provides Room database DAOs to the repository and HistoryService for persistence.
 */
class UserViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val database = LocalDatabase.getDatabase(context)
            val repository = SavedCouponRepository(
                promotionDao = database.promotionDao(),
                bookingDao = database.bookingDao()
            )
            val historyService = HistoryService(historyDao = database.historyDao())
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository, historyService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
