// ============================================================================
// IMAGE SERVICE - Manejo de imágenes
// ============================================================================

export class ImageService {
  // Función para subir imagen a S3
  static async uploadImageToS3(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/upload', {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || 'Error subiendo imagen');
    }

    const result = await response.json();
    return result.imageUrl;
  }

  // Generar imagen con IA usando webhook de n8n
  static async generateImageWithAI(title: string, description: string): Promise<string> {
    console.log('🤖 Generando imagen con IA para:', title);

    // Preparar el texto para el webhook
    const promptText = `${title}. ${description}`;

    // Llamar al proxy local que se conecta al webhook de n8n
    const response = await fetch('/api/generate-image', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        text: promptText
      }),
    });

    if (!response.ok) {
      throw new Error(`Error del webhook de IA: ${response.status} ${response.statusText}`);
    }

    const result = await response.json();
    console.log('🤖 Respuesta del webhook:', result);

    // Asumir que el webhook devuelve la URL de S3 en el campo imageUrl o uri
    const imageUrl = result.imageUrl || result.uri || result.url;

    if (!imageUrl) {
      throw new Error('El webhook no devolvió una URL de imagen válida');
    }

    console.log('✅ Imagen generada exitosamente con IA:', imageUrl);
    return imageUrl;
  }

  // Validar si un archivo es una imagen
  static isValidImageFile(file: File): boolean {
    return file.type.startsWith('image/');
  }

  // Validar tamaño máximo de archivo (en MB)
  static validateFileSize(file: File, maxSizeMB: number = 5): boolean {
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    return file.size <= maxSizeBytes;
  }
}