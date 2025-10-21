// ============================================================================
// HOOK: usePromotions - Manejo de promociones
// ============================================================================

import { useState, useEffect, useCallback } from 'react';
import { promotionApiService } from '../services/api';
import { ApiPromotion, ApiCollaborator, CreatePromotionData } from '../types';

export const usePromotions = (collaborator: ApiCollaborator | null) => {
  const [promotions, setPromotions] = useState<ApiPromotion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Cargar promociones cuando el colaborador esté disponible
  useEffect(() => {
    if (collaborator?.cognitoId) {
      loadPromotions();
    }
  }, [collaborator]);

  const loadPromotions = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('🔄 Loading promotions...');

      if (!collaborator?.cognitoId) {
        console.log('❌ No collaborator cognitoId available');
        setPromotions([]);
        return;
      }

      const data = await promotionApiService.getPromotions(collaborator.cognitoId);
      setPromotions(data);
      console.log(`✅ Loaded ${data.length} promotions for cognitoId: ${collaborator.cognitoId}`);
    } catch (err) {
      console.error('❌ Error loading promotions:', err);
      setError('Error al cargar las promociones');
    } finally {
      setLoading(false);
    }
  }, [collaborator?.cognitoId]);

  const createPromotion = useCallback(async (data: CreatePromotionData): Promise<ApiPromotion> => {
    console.log('➕ Creating promotion:', data);
    const newPromotion = await promotionApiService.createPromotion(data);
    await loadPromotions(); // Recargar lista
    return newPromotion;
  }, [loadPromotions]);

  const updatePromotion = useCallback(async (id: number, data: Partial<CreatePromotionData>): Promise<ApiPromotion> => {
    console.log('✏️ Updating promotion:', id, data);
    const updatedPromotion = await promotionApiService.updatePromotion(id, data);
    await loadPromotions(); // Recargar lista
    return updatedPromotion;
  }, [loadPromotions]);

  const deletePromotion = useCallback(async (id: number): Promise<void> => {
    console.log('🗑️ Deleting promotion:', id);
    await promotionApiService.deletePromotion(id);
    await loadPromotions(); // Recargar lista
  }, [loadPromotions]);

  // Estadísticas calculadas
  const stats = {
    total: promotions.length,
    active: promotions.filter(p => p.promotionState === 'activa').length,
    totalStock: promotions.reduce((sum, p) => sum + (p.totalStock || 0), 0),
    usedStock: promotions.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0),
  };

  return {
    promotions,
    loading,
    error,
    stats,
    loadPromotions,
    createPromotion,
    updatePromotion,
    deletePromotion,
    setError,
  };
};