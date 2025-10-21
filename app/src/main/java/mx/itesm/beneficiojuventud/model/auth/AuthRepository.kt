package mx.itesm.beneficiojuventud.model.auth

import android.util.Log
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthRepository {

    private val TAG = "AuthRepository"

    /**
     * Registrar un nuevo usuario siguiendo la documentación oficial de Amplify
     */
    suspend fun signUp(
        email: String,
        password: String,
        telefono: String? = null
    ): Result<AuthSignUpResult> = suspendCancellableCoroutine { continuation ->

        val attributes = mutableListOf(
            AuthUserAttribute(AuthUserAttributeKey.email(), email)
        )

        telefono?.let { phone ->
            val formattedPhone = if (!phone.startsWith("+")) "+52$phone" else phone
            attributes.add(AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), formattedPhone))
        }

        val options = AuthSignUpOptions.builder()
            .userAttributes(attributes)
            .build()

        Amplify.Auth.signUp(
            email,
            password,
            options,
            { result ->
                Log.i(TAG, "✅ Sign up successful")
                Log.i(TAG, "Sign up complete: ${result.isSignUpComplete}")
                Log.i(TAG, "User ID: ${result.userId}")
                Log.i(TAG, "Next step: ${result.nextStep?.signUpStep}")

                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(result))
                }
            },
            { error ->
                Log.e(TAG, "❌ Sign up failed: ${error.message}", error)
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
     * Iniciar sesión siguiendo la documentación oficial de Amplify
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthSignInResult> = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.signIn(
            email,
            password,
            { result ->
                Log.i(TAG, "✅ Sign in successful")
                Log.i(TAG, "Sign in complete: ${result.isSignedIn}")
                Log.i(TAG, "Next step: ${result.nextStep?.signInStep}")

                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(result))
                }
            },
            { error ->
                Log.e(TAG, "❌ Sign in failed: ${error.message}", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }

    // en AuthRepository
    suspend fun resendSignUpCode(email: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            try {
                Amplify.Auth.resendSignUpCode(
                    email,
                    { /* onSuccess */ continuation.resume(Result.success(Unit)) },
                    { error -> continuation.resume(Result.failure(error)) }
                )
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }


    /**
     * Cerrar sesión con manejo completo de resultados AWS Cognito
     */
    suspend fun signOut(globalSignOut: Boolean = true): Result<Unit> =
        suspendCancellableCoroutine { continuation ->

            val options = AuthSignOutOptions.builder()
                .globalSignOut(globalSignOut)
                .build()

            Amplify.Auth.signOut(options) { signOutResult ->
                when (signOutResult) {
                    is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                        Log.i(TAG, "✅ Sign out completado exitosamente")
                        if (!continuation.isCancelled) {
                            continuation.resume(Result.success(Unit))
                        }
                    }

                    is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                        Log.w(TAG, "⚠️ Sign out parcial completado")

                        // Manejar errores específicos del sign out parcial
                        signOutResult.hostedUIError?.let { error ->
                            Log.e(TAG, "Error HostedUI", error.exception)
                        }
                        signOutResult.globalSignOutError?.let { error ->
                            Log.e(TAG, "Error GlobalSignOut", error.exception)
                        }
                        signOutResult.revokeTokenError?.let { error ->
                            Log.e(TAG, "Error RevokeToken", error.exception)
                        }

                        // El usuario sigue deslogueado del dispositivo, así que es exitoso
                        if (!continuation.isCancelled) {
                            continuation.resume(Result.success(Unit))
                        }
                    }

                    is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                        Log.e(TAG, "❌ Sign out falló completamente", signOutResult.exception)
                        if (!continuation.isCancelled) {
                            continuation.resume(Result.failure(signOutResult.exception))
                        }
                    }
                }
            }
        }

    /**
     * Resetear contraseña (inicia el flujo y envía código)
     */
    suspend fun resetPassword(email: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->

            Amplify.Auth.resetPassword(
                email,
                { result ->
                    Log.i(TAG, "✅ Password reset OK: $result")
                    if (!continuation.isCancelled) {
                        continuation.resume(Result.success(Unit))
                    }
                },
                { error ->
                    Log.e(TAG, "❌ Password reset failed", error)
                    if (!continuation.isCancelled) {
                        continuation.resume(Result.failure(error))
                    }
                }
            )
        }

    /**
     * Confirmar nueva contraseña con el código recibido
     */
    suspend fun confirmResetPassword(
        email: String,
        newPassword: String,
        confirmationCode: String
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.confirmResetPassword(
            email,
            newPassword,
            confirmationCode,
            {
                Log.i(TAG, "✅ New password confirmed")
                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(Unit))
                }
            },
            { error ->
                Log.e(TAG, "❌ Failed to confirm password reset", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }

    /**
     * Actualizar contraseña del usuario autenticado
     */
    suspend fun updatePassword(
        existingPassword: String,
        newPassword: String
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.updatePassword(
            existingPassword,
            newPassword,
            {
                Log.i(TAG, "✅ Updated password successfully")
                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(Unit))
                }
            },
            { error ->
                Log.e(TAG, "❌ Password update failed", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }

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

    /**
     * Obtener información del usuario actual siguiendo la documentación oficial
     */
    suspend fun getCurrentUser(): Result<AuthUser?> = suspendCancellableCoroutine { continuation ->

        Amplify.Auth.getCurrentUser(
            { user ->
                Log.i(TAG, "✅ Current user retrieved")
                Log.i(TAG, "Username: ${user.username}")
                Log.i(TAG, "User ID: ${user.userId}")

                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(user))
                }
            },
            { error ->
                Log.e(TAG, "❌ Failed to get current user: ${error.message}", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }

    /**
     * Obtener atributos del usuario actual
     */
    suspend fun fetchUserAttributes(): Result<List<AuthUserAttribute>> =
        suspendCancellableCoroutine { continuation ->

            Amplify.Auth.fetchUserAttributes(
                { attributes ->
                    Log.i(TAG, "✅ User attributes retrieved: ${attributes.size} attributes")
                    attributes.forEach { attribute ->
                        Log.i(TAG, "${attribute.key.keyString}: ${attribute.value}")
                    }

                    if (!continuation.isCancelled) {
                        continuation.resume(Result.success(attributes))
                    }
                },
                { error ->
                    Log.e(TAG, "❌ Failed to fetch user attributes: ${error.message}", error)
                    if (!continuation.isCancelled) {
                        continuation.resume(Result.failure(error))
                    }
                }
            )
        }

    /**
     * Eliminar la cuenta del usuario actual de Cognito
     */
    suspend fun deleteUser(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Amplify.Auth.deleteUser(
            {
                Log.i(TAG, "✅ User account deleted successfully")
                if (!continuation.isCancelled) {
                    continuation.resume(Result.success(Unit))
                }
            },
            { error ->
                Log.e(TAG, "❌ Failed to delete user account: ${error.message}", error)
                if (!continuation.isCancelled) {
                    continuation.resume(Result.failure(error))
                }
            }
        )
    }
}