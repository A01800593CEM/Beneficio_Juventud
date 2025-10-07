package mx.itesm.beneficiojuventud.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.coroutines.resume

object ImageStorageManager {
    private const val TAG = "ImageStorageManager"

    suspend fun uploadProfileImage(context: Context, imageUri: Uri, userId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                Log.d(TAG, "Iniciando subida de imagen de perfil para usuario: $userId")

                // Crear archivo temporal
                tempFile = createTempFileFromUri(context, imageUri)
                if (tempFile == null) {
                    Log.e(TAG, "Error al crear archivo temporal")
                    return@withContext Result.failure(Exception("Error al procesar la imagen"))
                }

                // Usar directamente el userId como path - Amplify maneja automáticamente el access level
                val path = "profile-images/$userId.jpg"

                // Subir archivo de manera simple
                val uploadResult = suspendCancellableCoroutine { continuation ->
                    try {
                        Amplify.Storage.uploadFile(
                            path,
                            tempFile,
                            { result ->
                                Log.d(TAG, "Imagen subida exitosamente: ${result.key}")
                                continuation.resume(Result.success(result.key))
                            },
                            { error ->
                                Log.e(TAG, "Error en uploadFile: ${error.message}", error)
                                continuation.resume(Result.failure(Exception("Error subiendo archivo: ${error.message}")))
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Excepción en uploadFile: ${e.message}", e)
                        continuation.resume(Result.failure(e))
                    }
                }

                uploadResult

            } catch (e: Exception) {
                Log.e(TAG, "Error subiendo imagen: ${e.message}", e)
                Result.failure(e)
            } finally {
                // Limpiar archivo temporal
                tempFile?.let {
                    try {
                        if (it.exists()) {
                            it.delete()
                            Log.d(TAG, "Archivo temporal eliminado")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error eliminando archivo temporal: ${e.message}")
                    }
                }
            }
        }
    }

    suspend fun getProfileImageUrl(imageKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Obteniendo URL para imagen: $imageKey")

                suspendCancellableCoroutine { continuation ->
                    try {
                        Amplify.Storage.getUrl(
                            imageKey,
                            { result ->
                                Log.d(TAG, "URL obtenida exitosamente")
                                continuation.resume(Result.success(result.url.toString()))
                            },
                            { error ->
                                Log.e(TAG, "Error obteniendo URL de imagen: ${error.message}", error)
                                continuation.resume(Result.failure(Exception("Error obteniendo URL: ${error.message}")))
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Excepción en getUrl: ${e.message}", e)
                        continuation.resume(Result.failure(e))
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo URL de imagen: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteProfileImage(imageKey: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Eliminando imagen: $imageKey")

                suspendCancellableCoroutine { continuation ->
                    try {
                        Amplify.Storage.remove(
                            imageKey,
                            { result ->
                                Log.d(TAG, "Imagen eliminada exitosamente")
                                continuation.resume(Result.success(Unit))
                            },
                            { error ->
                                Log.e(TAG, "Error eliminando imagen: ${error.message}", error)
                                continuation.resume(Result.failure(Exception("Error eliminando imagen: ${error.message}")))
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Excepción en remove: ${e.message}", e)
                        continuation.resume(Result.failure(e))
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando imagen: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            Log.d(TAG, "Creando archivo temporal desde URI: $uri")

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
                Log.d(TAG, "Archivo temporal creado: ${tempFile.absolutePath}, tamaño: ${tempFile.length()} bytes")
                tempFile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando archivo temporal: ${e.message}", e)
            null
        }
    }
}