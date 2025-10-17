// ============================================================================
// COMPONENT: PromotionCard - Tarjeta individual de promoción
// ============================================================================

import { PromotionCardProps } from '../types';

export default function PromotionCard({ promotion, onEdit, onDelete, loading }: PromotionCardProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('es-ES');
  };

  return (
    <div className="bg-gray-50 rounded-lg p-4 border border-gray-200 hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-3">
        <h3 className="font-medium text-[#015463] text-lg">{promotion.title}</h3>
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
          promotion.promotionState === 'activa' ? 'bg-[#008D96]/10 text-[#008D96]' :
          promotion.promotionState === 'inactiva' ? 'bg-yellow-100 text-yellow-800' :
          'bg-red-100 text-red-800'
        }`}>
          {promotion.promotionState}
        </span>
      </div>

      <p className="text-[#969696] text-sm mb-4 line-clamp-2">{promotion.description}</p>

      <div className="text-xs text-[#969696] mb-4 space-y-1">
        <div className="flex justify-between">
          <span>Tipo:</span>
          <span className="text-[#015463]">{promotion.promotionType}</span>
        </div>
        <div className="flex justify-between">
          <span>Stock:</span>
          <span className="text-[#015463]">{promotion.availableStock}/{promotion.totalStock}</span>
        </div>
        <div className="flex justify-between">
          <span>Vigencia:</span>
          <span className="text-[#015463]">{formatDate(promotion.initialDate)} - {formatDate(promotion.endDate)}</span>
        </div>
      </div>

      <div className="flex gap-2">
        <button
          onClick={() => onEdit(promotion)}
          disabled={loading}
          className="flex-1 bg-[#4B4C7E]/10 text-[#4B4C7E] px-3 py-2 rounded text-sm hover:bg-[#4B4C7E]/20 disabled:opacity-50 transition-colors"
        >
          ✏️ Editar
        </button>
        <button
          onClick={() => onDelete(promotion)}
          disabled={loading}
          className="flex-1 bg-red-50 text-red-600 px-3 py-2 rounded text-sm hover:bg-red-100 disabled:opacity-50 transition-colors"
        >
          🗑️ Eliminar
        </button>
      </div>
    </div>
  );
}