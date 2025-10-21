package mx.itesm.beneficiojuventud.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.itesm.beneficiojuventud.model.RoomDB.LocalDatabase
import mx.itesm.beneficiojuventud.model.SavedCouponRepository

class UserViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val database = LocalDatabase.getDatabase(context)
            val repository = SavedCouponRepository(database.promotionDao())
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
