package mx.itesm.beneficiojuventud.model

import android.util.Log
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository {

    private val TAG = "AuthRepository"

    /**
     * Registrar un nuevo usuario
     */
    suspend fun signUp(
        email: String,
        password: String,
        nombreCompleto: String,
        telefono: String? = null
    ): Result<String> = suspendCancellableCoroutine { continuation ->

        // Construye lista de atributos CORRECTA (no Map)
        val attributes = mutableListOf(
            AuthUserAttribute(AuthUserAttributeKey.email(), email),
            AuthUserAttribute(AuthUserAttributeKey.name(), nombreCompleto)
        )
        telefono?.let {
            attributes.add(AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), it))
        }

        val options = AuthSignUpOptions.builder()
            .userAttributes(attributes)
            .build()

        Amplify.Auth.signUp(
            email,
            password,
            options,
            { result ->
                try {
                    Log.i(
                        TAG,
                        "Sign up exitoso. isSignUpComplete=${result.isSignUpComplete}, nextStep=${result.nextStep.signUpStep}"
                    )
                    // En algunas versiones: result.userId (v2) / otras: result.user?.userId
                    val userId = result.userId ?: result.userId ?: email
                    if (!continuation.isCancelled) {
                        continuation.resume(Result.success(userId))
                    }
                } catch (t: Throwable) {
                    if (!continuation.isCancelled) {
                        continuation.resume(Result.failure(t))
                    }
                }
            },
            { error ->
                Log.e(TAG, "Error en sign up", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }

    /**
     * Confirmar el código de verificación
     */
    suspend fun confirmSignUp(
        email: String,
        code: String
    ): Result<Boolean> = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.confirmSignUp(
            email,
            code,
            { result ->
                Log.i(TAG, "Confirmación exitosa. isSignUpComplete=${result.isSignUpComplete}")
                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(result.isSignUpComplete))
                }
            },
            { error ->
                Log.e(TAG, "Error en confirmación", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }

    /**
     * Iniciar sesión
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<Boolean> = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.signIn(
            email,
            password,
            { result ->
                Log.i(TAG, "Sign in exitoso. isSignedIn=${result.isSignedIn}")
                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(result.isSignedIn))
                }
            },
            { error ->
                Log.e(TAG, "Error en sign in", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }

    /**
     * Cerrar sesión
     */
    suspend fun signOut(): Result<Unit> = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.signOut(
            {
            }
        )
    }

    /**
     * Resetear contraseña (inicia el flujo y envía código)
     */
    suspend fun resetPassword(email: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->

            Amplify.Auth.resetPassword(
                email,
                { result ->
                    Log.i(TAG, "Reset password iniciado. nextStep=${result.nextStep}")
                    if (!continuation.isCancelled) {
                        continuation.resume(Result.success(Unit))
                    }
                },
                { error ->
                    Log.e(TAG, "Error en reset password", error)
                    if (!continuation.isCancelled) {
                        continuation.resume(Result.failure(error))
                    }
                }
            )
        }

    /**
     * Confirmar nueva contraseña con el código recibido
     */
    /**
     * Verificar si hay usuario logueado
     */
    suspend fun isUserSignedIn(): Boolean = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.fetchAuthSession(
            { session ->
                if (!continuation.isCancelled) {
                    continuation.resume(session.isSignedIn)
                }
            },
            { error ->
                Log.e(TAG, "Error verificando sesión", error)
                if (!continuation.isCancelled) {
                    continuation.resume(false)
                }
            }
        )
    }
}