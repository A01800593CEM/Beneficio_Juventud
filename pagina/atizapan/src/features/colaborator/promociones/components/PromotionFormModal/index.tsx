// ============================================================================
// COMPONENT: PromotionFormModal - Modal principal del formulario
// ============================================================================

import { useState } from 'react';
import { PromotionFormModalProps, PromotionFormData } from '../../types';
import BasicInfoSection from './BasicInfoSection';
import DateStockSection from './DateStockSection';
import CategoriesSection from './CategoriesSection';
import ImageUploadSection from './ImageUploadSection';

export default function PromotionFormModal({ promotion, onSave, onCancel }: PromotionFormModalProps) {
  // Detectar si es una promoción existente (tiene promotionId) o datos de IA
  const isExistingPromotion = promotion && 'promotionId' in promotion;

  const [formData, setFormData] = useState<PromotionFormData>({
    title: isExistingPromotion ? promotion.title : (promotion?.title || ''),
    description: isExistingPromotion ? promotion.description : (promotion?.description || ''),
    imageUrl: isExistingPromotion ? promotion.imageUrl || '' : (promotion?.imageUrl || ''),
    startDate: isExistingPromotion
      ? (promotion.initialDate ? promotion.initialDate.split('T')[0] : '')
      : (promotion?.startDate || ''),
    endDate: isExistingPromotion
      ? (promotion.endDate ? promotion.endDate.split('T')[0] : '')
      : (promotion?.endDate || ''),
    type: isExistingPromotion ? promotion.promotionType : (promotion?.type || 'descuento'),
    code: isExistingPromotion ? promotion.promotionString || '' : (promotion?.code || ''),
    stock: isExistingPromotion
      ? (promotion.totalStock?.toString() || '100')
      : (promotion?.stock || '100'),
    limitPerUser: isExistingPromotion
      ? (promotion.limitPerUser?.toString() || '1')
      : (promotion?.limitPerUser || '1'),
    dailyLimit: isExistingPromotion
      ? (promotion.dailyLimitPerUser?.toString() || '1')
      : (promotion?.dailyLimit || '1'),
    categories: isExistingPromotion ? (promotion.categories || ['COMIDA']) : (promotion?.categories || ['COMIDA']),
    promotionTheme: isExistingPromotion ? (promotion.promotionTheme || 'light') : (promotion?.promotionTheme || 'light'),
    isBookable: isExistingPromotion ? (promotion.is_bookable || false) : (promotion?.isBookable || false),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('📋 Form submitted:', formData);
    onSave(formData);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;

    if (type === 'checkbox') {
      const checkbox = e.target as HTMLInputElement;
      setFormData(prev => ({
        ...prev,
        [name]: checkbox.checked
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const handleCategoryChange = (category: string) => {
    setFormData(prev => ({
      ...prev,
      categories: prev.categories.includes(category)
        ? prev.categories.filter((c: string) => c !== category)
        : [...prev.categories, category]
    }));
  };

  const handleImageChange = (imageUrl: string) => {
    setFormData(prev => ({
      ...prev,
      imageUrl
    }));
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-start justify-center p-4 z-50 overflow-y-auto">
      <div className="bg-white rounded-xl w-full max-w-6xl my-8 shadow-2xl border border-gray-100">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </div>
            <div>
              <h2 className="text-xl font-semibold text-[#015463]">
                {isExistingPromotion ? 'Editar Promoción' : promotion ? 'Promoción Generada por IA' : 'Nueva Promoción'}
              </h2>
              {!isExistingPromotion && promotion && (
                <p className="text-sm text-[#008D96]">💡 Revisa y edita los campos antes de publicar</p>
              )}
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
          <div className="grid grid-cols-12 gap-6">
            {/* Columna Principal - 8/12 */}
            <div className="col-span-12 lg:col-span-8 space-y-6">
              <BasicInfoSection formData={formData} onChange={handleChange} />
              <DateStockSection formData={formData} onChange={handleChange} />
              <CategoriesSection
                formData={formData}
                onChange={handleChange}
                onCategoryChange={handleCategoryChange}
              />
            </div>

            {/* Columna Lateral - 4/12 */}
            <div className="col-span-12 lg:col-span-4 space-y-6">
              <ImageUploadSection
                imageUrl={formData.imageUrl}
                title={formData.title}
                description={formData.description}
                onImageChange={handleImageChange}
              />

              {/* Botones de acción */}
              <div className="bg-white border border-gray-200 rounded-lg p-5">
                <div className="flex flex-col gap-3">
                  <button
                    type="submit"
                    className="w-full bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white py-3 px-4 rounded-lg hover:opacity-90 transition-all font-medium"
                  >
                    {isExistingPromotion ? '💾 Guardar Cambios' : '🚀 Publicar Promoción'}
                  </button>
                  <button
                    type="button"
                    onClick={onCancel}
                    className="w-full bg-gray-100 text-gray-700 py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors font-medium"
                  >
                    ❌ Cancelar
                  </button>
                </div>

                {/* Preview info */}
                <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                  <h4 className="text-sm font-medium text-gray-700 mb-2">📋 Resumen:</h4>
                  <ul className="text-xs text-gray-600 space-y-1">
                    <li>• Título: {formData.title || 'Sin título'}</li>
                    <li>• Tipo: {formData.type}</li>
                    <li>• Stock: {formData.stock} cupones</li>
                    <li>• Categorías: {formData.categories.length} seleccionadas</li>
                    <li>• Imagen: {formData.imageUrl ? 'Configurada' : 'Sin imagen'}</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}