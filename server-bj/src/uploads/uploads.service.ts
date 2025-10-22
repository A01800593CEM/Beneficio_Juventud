import { Injectable } from '@nestjs/common';
import FormData from 'form-data';

@Injectable()
export class UploadsService {
  private webhookUrl = 'https://primary-production-0858b.up.railway.app/webhook/d4e2e473-8dcf-4cca-aea2-ba25ff544450';

  async uploadViaWebhook(file: {
    buffer: Buffer;
    originalname: string;
    mimetype: string;
    size: number;
  }): Promise<string> {
    try {
      // Crear FormData para enviar el archivo al webhook
      const formData = new FormData();
      formData.append('file', file.buffer, {
        filename: file.originalname,
        contentType: file.mimetype,
      });

      const response = await fetch(this.webhookUrl, {
        method: 'POST',
        body: formData as any,
        headers: formData.getHeaders(),
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(`Webhook error: ${response.status} - ${errorData}`);
      }

      const result = await response.json();

      // Extraer URL de la respuesta (puede venir como imageUrl, url, uri, etc.)
      const imageUrl = result.imageUrl || result.url || result.uri;

      if (!imageUrl) {
        throw new Error('Webhook did not return a valid image URL');
      }

      return imageUrl;
    } catch (error) {
      console.error('Webhook upload error:', error);
      throw new Error(`Failed to upload file via webhook: ${error.message}`);
    }
  }
}
