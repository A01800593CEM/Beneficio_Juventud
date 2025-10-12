package mx.itesm.beneficiojuventud.model

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser

object TestRemote {

    fun probarLlamada() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = RemoteServiceUser.getUserById("813bc520-5071-70c6-02f6-e73ca6e4beb6")
                Log.d("RetrofitTest", "✅ Usuario obtenido: $user")
            } catch (e: Exception) {
                Log.e("RetrofitTest", "❌ Error: ${e.message}")

            }
        }
    }

    fun probarPromoService() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val promo = RemoteServicePromos.getPromotionById(1)
                Log.d("TEST_PROMO", "Promoción obtenida: $promo")
            } catch (e: Exception) {
                Log.e("TEST_PROMO", "Error: ${e.message}")
            }
        }
    }
}
