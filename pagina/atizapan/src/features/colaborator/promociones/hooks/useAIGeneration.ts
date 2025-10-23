// ============================================================================
// HOOK: useAIGeneration - Generación con IA
// ============================================================================

import { useState } from 'react';
import { promotionApiService } from '../services/api';

export const useAIGeneration = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const generatePromotion = async (idea: string): Promise<Record<string, unknown>> => {
    try {
      setLoading(true);
      setError(null);
      console.log('🤖 Generating promotion with AI for idea:', idea);

      // Llamar al webhook IA
      const aiResponse = await promotionApiService.generatePromotionWithAI(idea);
      console.log('🤖 AI generated promotion:', aiResponse);

      // Convertir respuesta IA a formato del formulario
      const promotionData = {
        title: aiResponse.title,
        description: aiResponse.description,
        imageUrl: 'https://example.com/ai-generated.jpg',
        startDate: aiResponse.initialDate ? aiResponse.initialDate.split('T')[0] : '',
        endDate: aiResponse.endDate ? aiResponse.endDate.split('T')[0] : '',
        type: aiResponse.promotionType || 'descuento',
        code: '',
        stock: aiResponse.totalStock?.toString() || '100',
        limitPerUser: aiResponse.limitPerUser?.toString() || '1',
        dailyLimit: aiResponse.dailyLimitPerUser?.toString() || '1',
      };

      console.log('🤖 AI promotion data for form:', promotionData);
      return promotionData;
    } catch (err) {
      console.error('❌ Error generating promotion with AI:', err);
      setError('Error al generar la promoción con IA');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    error,
    generatePromotion,
    setError,
  };
};