package mx.itesm.beneficiojuventud.utils

import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AmplifyUserHelper {
    suspend fun getCurrentUserId(): String? {
        return suspendCancellableCoroutine { continuation ->
            Amplify.Auth.getCurrentUser(
                { result ->
                    continuation.resume(result.userId)
                },
                { error ->
                    continuation.resume(null)
                }
            )
        }
    }
}