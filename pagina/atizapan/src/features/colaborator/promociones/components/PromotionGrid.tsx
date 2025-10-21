// ============================================================================
// COMPONENT: PromotionGrid - Grid de promociones
// ============================================================================

import { ApiPromotion } from '../types';
import PromotionCard from './PromotionCard';

interface PromotionGridProps {
  promotions: ApiPromotion[];
  loading: boolean;
  onEdit: (promotion: ApiPromotion) => void;
  onDelete: (promotion: ApiPromotion) => void;
  onCreate: () => void;
}

export default function PromotionGrid({ promotions, loading, onEdit, onDelete, onCreate }: PromotionGridProps) {
  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
        <div className="text-[#969696] mt-2">Cargando...</div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-100">
        <h2 className="text-lg font-medium text-[#4B4C7E]">Mis Promociones</h2>
      </div>

      {promotions.length > 0 ? (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 p-6">
          {promotions.map((promotion) => (
            <PromotionCard
              key={promotion.promotionId}
              promotion={promotion}
              onEdit={onEdit}
              onDelete={onDelete}
              loading={loading}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-12">
          <div className="text-[#969696] text-lg mb-4">No hay promociones</div>
          <button
            onClick={onCreate}
            className="bg-[#008D96] text-white px-6 py-3 rounded-lg hover:bg-[#008D96]/90 transition-colors"
          >
            Crear Primera Promoción
          </button>
        </div>
      )}
    </div>
  );
}