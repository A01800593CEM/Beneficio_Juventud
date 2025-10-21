// ============================================================================
// COMPONENT: BasicInfoSection - Información básica del formulario
// ============================================================================

import { PromotionFormData } from '../../types';

interface BasicInfoSectionProps {
  formData: PromotionFormData;
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
}

export default function BasicInfoSection({ formData, onChange }: BasicInfoSectionProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-5">
      <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
        <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        Información Básica
      </h3>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Título de la Promoción *
          </label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={onChange}
            required
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
            placeholder="Ej: 2x1 en Pizzas Familiares"
          />
        </div>

        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Descripción *
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={onChange}
            required
            rows={3}
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all resize-none"
            placeholder="Describe los detalles de tu promoción..."
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Tipo de Promoción
          </label>
          <select
            name="type"
            value={formData.type}
            onChange={onChange}
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
          >
            <option value="descuento">💸 Descuento</option>
            <option value="multicompra">🛒 Multicompra</option>
            <option value="regalo">🎁 Regalo</option>
            <option value="otro">✨ Otro</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Código Promocional
          </label>
          <input
            type="text"
            name="code"
            value={formData.code}
            onChange={onChange}
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
            placeholder="PIZZA2X1"
          />
        </div>
      </div>
    </div>
  );
}