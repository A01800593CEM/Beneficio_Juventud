// ============================================================================
// COMPONENT: PromotionHeader - Header con acciones
// ============================================================================

import { ApiCollaborator } from '../types';

interface PromotionHeaderProps {
  collaborator: ApiCollaborator | null;
  authLoading: boolean;
  loading: boolean;
  onCreateWithAI: () => void;
  onCreateManual: () => void;
}

export default function PromotionHeader({
  collaborator,
  authLoading,
  loading,
  onCreateWithAI,
  onCreateManual
}: PromotionHeaderProps) {
  return (
    <div className="flex items-center gap-4">
      {/* Mostrar email del colaborador autenticado */}
      {authLoading ? (
        <div className="text-sm text-[#969696]">Verificando autenticación...</div>
      ) : collaborator ? (
        <div className="text-sm bg-[#008D96]/10 text-[#008D96] px-3 py-1 rounded-full border border-[#008D96]/20">
          👤 {collaborator.email}
        </div>
      ) : (
        <div className="text-sm bg-red-100 text-red-800 px-3 py-1 rounded-full border border-red-200">
          ❌ No autenticado
        </div>
      )}

      <div className="flex gap-3">
        <button
          onClick={onCreateWithAI}
          disabled={loading}
          className="bg-[#4B4C7E] text-white px-4 py-2 rounded-lg hover:bg-[#4B4C7E]/90 disabled:opacity-50 flex items-center gap-2 transition-colors"
        >
          🤖 Crear con IA
        </button>
        <button
          onClick={onCreateManual}
          disabled={loading}
          className="bg-[#008D96] text-white px-4 py-2 rounded-lg hover:bg-[#008D96]/90 disabled:opacity-50 transition-colors"
        >
          ➕ Manual
        </button>
      </div>
    </div>
  );
}