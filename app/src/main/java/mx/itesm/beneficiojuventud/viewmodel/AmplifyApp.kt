package mx.itesm.beneficiojuventud.viewmodel

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify

class AmplifyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("AmplifyApp", "Iniciando configuración de Amplify...")

        try {
            // 1. Agregar el plugin
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Log.d("AmplifyApp", "Plugin agregado")

            // 2. Configurar Amplify (lee automáticamente amplifyconfiguration.json de res/raw)
            Amplify.configure(applicationContext)
            Log.d("AmplifyApp", "✅ Amplify configurado exitosamente")

            // 3. Verificar configuración
            verifyConfiguration()

        } catch (error: AmplifyException) {
            Log.e("AmplifyApp", "❌ Error Amplify: ${error.message}", error)
        }
    }

    private fun verifyConfiguration() {
        try {
            Amplify.Auth.fetchAuthSession(
                { session ->
                    Log.i("AmplifyApp", "✅ Auth verificado. Autenticado: ${session.isSignedIn}")
                },
                { error ->
                    Log.w("AmplifyApp", "⚠️ Verificación (esperado si no hay sesión): ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e("AmplifyApp", "❌ Error verificando: ${e.message}")
        }
    }
}