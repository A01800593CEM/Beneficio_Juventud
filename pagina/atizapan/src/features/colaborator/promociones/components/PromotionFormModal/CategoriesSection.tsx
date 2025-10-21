// ============================================================================
// COMPONENT: CategoriesSection - Categorías y configuraciones adicionales
// ============================================================================

import { PromotionFormData } from '../../types';

interface CategoriesSectionProps {
  formData: PromotionFormData;
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  onCategoryChange: (category: string) => void;
}

const availableCategories = [
  { id: 'COMIDA', name: 'Comida', icon: '🍕' },
  { id: 'BEBIDAS', name: 'Bebidas', icon: '🥤' },
  { id: 'ENTRETENIMIENTO', name: 'Entretenimiento', icon: '🎮' },
  { id: 'DEPORTES', name: 'Deportes', icon: '⚽' },
  { id: 'SALUD', name: 'Salud', icon: '🏥' },
  { id: 'BELLEZA', name: 'Belleza', icon: '💄' },
  { id: 'TECNOLOGIA', name: 'Tecnología', icon: '📱' },
  { id: 'ROPA', name: 'Ropa', icon: '👕' },
  { id: 'HOGAR', name: 'Hogar', icon: '🏠' },
  { id: 'EDUCACION', name: 'Educación', icon: '📚' },
];

export default function CategoriesSection({ formData, onChange, onCategoryChange }: CategoriesSectionProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-5">
      <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
        <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.99 1.99 0 013 12V7a4 4 0 014-4z" />
        </svg>
        Categorías y Configuración
      </h3>

      {/* Categorías */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-[#015463] mb-3">
          Categorías de la Promoción
        </label>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3">
          {availableCategories.map((category) => (
            <label
              key={category.id}
              className={`flex items-center p-3 rounded-lg border-2 cursor-pointer transition-all ${
                formData.categories.includes(category.id)
                  ? 'border-[#008D96] bg-[#008D96]/5 text-[#008D96]'
                  : 'border-gray-200 bg-gray-50 text-gray-700 hover:border-[#008D96]/50'
              }`}
            >
              <input
                type="checkbox"
                checked={formData.categories.includes(category.id)}
                onChange={() => onCategoryChange(category.id)}
                className="sr-only"
              />
              <span className="text-lg mr-2">{category.icon}</span>
              <span className="text-sm font-medium">{category.name}</span>
            </label>
          ))}
        </div>
        <p className="text-xs text-gray-500 mt-2">Selecciona las categorías que mejor describan tu promoción</p>
      </div>

      {/* Configuraciones adicionales */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-[#015463] mb-2">
            Tema de la Promoción
          </label>
          <select
            name="promotionTheme"
            value={formData.promotionTheme}
            onChange={onChange}
            className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
          >
            <option value="light">🌞 Tema Claro</option>
            <option value="dark">🌙 Tema Oscuro</option>
          </select>
        </div>

        <div>
          <label className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg border border-gray-200">
            <input
              type="checkbox"
              name="isBookable"
              checked={formData.isBookable}
              onChange={onChange}
              className="rounded border-gray-300 text-[#008D96] focus:ring-[#008D96] focus:ring-offset-0"
            />
            <div>
              <span className="text-sm font-medium text-[#015463]">📅 Permitir Reservas</span>
              <p className="text-xs text-gray-500">Los usuarios pueden reservar esta promoción con anticipación</p>
            </div>
          </label>
        </div>
      </div>
    </div>
  );
}