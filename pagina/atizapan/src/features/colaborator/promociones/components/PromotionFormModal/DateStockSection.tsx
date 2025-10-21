// ============================================================================
// COMPONENT: DateStockSection - Fechas y disponibilidad
// ============================================================================

import { PromotionFormData } from '../../types';

interface DateStockSectionProps {
  formData: PromotionFormData;
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
}

export default function DateStockSection({ formData, onChange }: DateStockSectionProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-5">
      <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
        <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        Fechas y Disponibilidad
      </h3>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Fecha de Inicio *
          </label>
          <input
            type="date"
            name="startDate"
            value={formData.startDate}
            onChange={onChange}
            required
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Fecha de Fin *
          </label>
          <input
            type="date"
            name="endDate"
            value={formData.endDate}
            onChange={onChange}
            required
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Stock Total
          </label>
          <input
            type="number"
            name="stock"
            value={formData.stock}
            onChange={onChange}
            min="1"
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
            placeholder="100"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Límite por Usuario
          </label>
          <input
            type="number"
            name="limitPerUser"
            value={formData.limitPerUser}
            onChange={onChange}
            min="1"
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
            placeholder="1"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Límite Diario
          </label>
          <input
            type="number"
            name="dailyLimit"
            value={formData.dailyLimit}
            onChange={onChange}
            min="1"
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
            placeholder="1"
          />
        </div>
      </div>

      {/* Info boxes */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <h4 className="text-sm font-medium text-blue-800 mb-1">💡 Stock Total</h4>
          <p className="text-xs text-blue-600">Número total de cupones disponibles para esta promoción</p>
        </div>
        <div className="bg-green-50 border border-green-200 rounded-lg p-3">
          <h4 className="text-sm font-medium text-green-800 mb-1">👤 Límite por Usuario</h4>
          <p className="text-xs text-green-600">Máximo de cupones que puede obtener cada usuario</p>
        </div>
        <div className="bg-orange-50 border border-orange-200 rounded-lg p-3">
          <h4 className="text-sm font-medium text-orange-800 mb-1">📅 Límite Diario</h4>
          <p className="text-xs text-orange-600">Máximo de cupones que puede obtener por día</p>
        </div>
      </div>
    </div>
  );
}