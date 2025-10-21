// ============================================================================
// COMPONENT: AIPromotionModal - Modal para generar promoción con IA
// ============================================================================

import { useState } from 'react';
import { AIPromotionModalProps } from '../types';

export default function AIPromotionModal({ onGenerate, onCancel }: AIPromotionModalProps) {
  const [idea, setIdea] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!idea.trim()) return;

    setLoading(true);
    try {
      await onGenerate(idea);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl w-full max-w-md shadow-2xl border border-gray-100">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] rounded-lg flex items-center justify-center">
              <span className="text-white text-lg">🤖</span>
            </div>
            <div>
              <h2 className="text-xl font-semibold text-[#015463]">Crear con IA</h2>
              <p className="text-sm text-[#008D96]">Describe tu idea y la IA creará la promoción</p>
            </div>
          </div>
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-gray-600 p-2 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <form onSubmit={handleSubmit} className="px-6 py-4">
          <div className="mb-6">
            <label className="block text-sm font-medium text-[#015463] mb-2">
              Describe tu idea de promoción
            </label>
            <textarea
              value={idea}
              onChange={(e) => setIdea(e.target.value)}
              placeholder="Ej: Una promoción de 2x1 en pizzas para este fin de semana..."
              className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all resize-none"
              rows={4}
              required
              disabled={loading}
            />
            <p className="text-xs text-gray-500 mt-2">
              💡 Sé específico: incluye el tipo de descuento, productos, fechas, etc.
            </p>
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={onCancel}
              disabled={loading}
              className="flex-1 px-4 py-3 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 disabled:opacity-50 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading || !idea.trim()}
              className="flex-1 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white px-4 py-3 rounded-lg hover:opacity-90 disabled:opacity-50 transition-all"
            >
              {loading ? (
                <div className="flex items-center justify-center">
                  <svg className="animate-spin -ml-1 mr-3 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Generando...
                </div>
              ) : (
                '🚀 Generar Promoción'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}