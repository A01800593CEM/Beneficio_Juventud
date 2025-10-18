package mx.itesm.beneficiojuventud.viewmodel

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.amplifyframework.core.Amplify

class AmplifyApp : Application() {
    companion object {
        private const val TAG = "AmplifyApp"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Iniciando configuración de Amplify...")

        try {
            configureAmplify()
        } catch (error: AmplifyException) {
            Log.e(TAG, "❌ Error Amplify: ${error.message}", error)
            Log.e(TAG, "Causa raíz: ${error.cause?.message}")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Error inesperado: ${error.message}", error)
        }
    }

    private fun configureAmplify() {
        try {
            // 1. Agregar plugins
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Log.d(TAG, "Plugins Auth y Storage agregados exitosamente")

            // 2. Configurar Amplify (lee automáticamente amplifyconfiguration.json de res/raw)
            Amplify.configure(applicationContext)
            Log.d(TAG, "✅ Amplify configurado exitosamente")

            // 3. Verificar configuración
            verifyConfiguration()

        } catch (error: AmplifyException) {
            Log.e(TAG, "❌ Error configurando Amplify: ${error.message}", error)
            throw error
        }
    }

    private fun verifyConfiguration() {
        try {
            Amplify.Auth.fetchAuthSession(
                { session ->
                    Log.i(TAG, "✅ Auth verificado exitosamente")
                    Log.i(TAG, "Usuario autenticado: ${session.isSignedIn}")

                    // Solo mostrar información básica de la sesión
                    if (session.isSignedIn) {
                        Log.i(TAG, "Sesión activa detectada")
                    } else {
                        Log.i(TAG, "No hay sesión activa")
                    }
                },
                { error ->
                    Log.w(TAG, "⚠️ Verificación Auth (normal si no hay sesión activa): ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando configuración: ${e.message}", e)
        }
    }
}