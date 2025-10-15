"use client";

import { useState, useRef, useCallback, useEffect } from "react";
import Image from "next/image";
import {
  XMarkIcon,
  PhotoIcon,
  EyeIcon
} from "@heroicons/react/24/outline";
import { Promotion } from "./PromotionCard";

interface PromotionFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (promotion: Omit<Promotion, "id" | "totalViews" | "totalRedemptions" | "conversionRate">) => void;
  promotion?: Promotion;
  mode: "create" | "edit";
}

interface FormData {
  title: string;
  description: string;
  discount: string;
  discountType: "percentage" | "fixed";
  category: string;
  startDate: string;
  endDate: string;
  terms: string;
  image?: string;
}

const categories = [
  "Comida",
  "Bebida",
  "Combo",
  "Descuento",
  "Especial",
  "Temporada",
  "Happy Hour"
];

// Helper function to convert ISO date to input date format (YYYY-MM-DD)
const formatDateForInput = (isoDate: string): string => {
  if (!isoDate) return "";
  try {
    const date = new Date(isoDate);
    return date.toISOString().split('T')[0];
  } catch {
    return "";
  }
};

// Helper function to convert input date to ISO format
const formatDateToISO = (inputDate: string): string => {
  if (!inputDate) return "";
  return new Date(inputDate).toISOString();
};

export default function PromotionForm({
  isOpen,
  onClose,
  onSave,
  promotion,
  mode
}: PromotionFormProps) {
  const [formData, setFormData] = useState<FormData>({
    title: "",
    description: "",
    discount: "",
    discountType: "percentage",
    category: "",
    startDate: "",
    endDate: "",
    terms: "",
    image: ""
  });

  const [errors, setErrors] = useState<Partial<FormData>>({});
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [showPreview, setShowPreview] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Update form data when modal opens or promotion changes
  useEffect(() => {
    if (isOpen) {
      if (promotion && mode === "edit") {
        console.log('🔄 Loading promotion data for editing:', promotion);

        const updatedFormData: FormData = {
          title: promotion.title || "",
          description: promotion.description || "",
          discount: promotion.discount?.toString() || "",
          discountType: promotion.discountType || "percentage",
          category: promotion.category || "",
          startDate: formatDateForInput(promotion.startDate || ""),
          endDate: formatDateForInput(promotion.endDate || ""),
          terms: promotion.terms || "",
          image: promotion.image || ""
        };

        setFormData(updatedFormData);
        setPreviewImage(promotion.image || null);
        setErrors({});

        console.log('✅ Form data updated for editing:', updatedFormData);
      } else if (mode === "create") {
        // Reset form for create mode
        console.log('🆕 Resetting form for create mode');
        setFormData({
          title: "",
          description: "",
          discount: "",
          discountType: "percentage",
          category: "",
          startDate: "",
          endDate: "",
          terms: "",
          image: ""
        });
        setPreviewImage(null);
        setErrors({});
      }
    }
  }, [isOpen, promotion, mode]);

  const validateForm = (): boolean => {
    const newErrors: Partial<FormData> = {};

    if (!formData.title.trim()) {
      newErrors.title = "El título es obligatorio";
    } else if (formData.title.length < 5) {
      newErrors.title = "El título debe tener al menos 5 caracteres";
    }

    if (!formData.description.trim()) {
      newErrors.description = "La descripción es obligatoria";
    } else if (formData.description.length < 20) {
      newErrors.description = "La descripción debe tener al menos 20 caracteres";
    }

    if (!formData.discount.trim()) {
      newErrors.discount = "El descuento es obligatorio";
    } else {
      const discountValue = parseInt(formData.discount);
      if (isNaN(discountValue) || discountValue <= 0) {
        newErrors.discount = "El descuento debe ser un número mayor a 0";
      } else if (formData.discountType === "percentage" && discountValue > 100) {
        newErrors.discount = "El porcentaje no puede ser mayor a 100";
      }
    }

    if (!formData.category) {
      newErrors.category = "La categoría es obligatoria";
    }

    if (!formData.startDate) {
      newErrors.startDate = "La fecha de inicio es obligatoria";
    }

    if (!formData.endDate) {
      newErrors.endDate = "La fecha de fin es obligatoria";
    } else if (formData.startDate && formData.endDate <= formData.startDate) {
      newErrors.endDate = "La fecha de fin debe ser posterior a la fecha de inicio";
    }

    if (!formData.terms.trim()) {
      newErrors.terms = "Los términos y condiciones son obligatorios";
    } else if (formData.terms.length < 10) {
      newErrors.terms = "Los términos deben tener al menos 10 caracteres";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: keyof FormData, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));

    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  const handleImageUpload = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith("image/")) {
      alert("Por favor selecciona un archivo de imagen válido");
      return;
    }

    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      alert("La imagen no debe superar los 5MB");
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const result = e.target?.result as string;
      setPreviewImage(result);
      handleInputChange("image", result);
    };
    reader.readAsDataURL(file);
  }, []);

  const handleRemoveImage = () => {
    setPreviewImage(null);
    handleInputChange("image", "");
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const promotionData = {
      title: formData.title.trim(),
      description: formData.description.trim(),
      discount: parseInt(formData.discount),
      discountType: formData.discountType,
      category: formData.category,
      startDate: formatDateToISO(formData.startDate),
      endDate: formatDateToISO(formData.endDate),
      status: "active" as const, // Set as active instead of scheduled
      terms: formData.terms.trim(),
      image: formData.image
    };

    console.log('💾 Saving promotion data:', promotionData);
    onSave(promotionData);
    onClose();
  };

  const getDiscountPreview = () => {
    if (!formData.discount) return "";

    const discount = parseInt(formData.discount);
    if (isNaN(discount)) return "";

    return formData.discountType === "percentage"
      ? `${discount}% OFF`
      : `$${discount} OFF`;
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
  <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">

    {/* Backdrop: dale z-40 */}
    <div
      className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity z-40"
      onClick={onClose}
    />


        {/* Modal */}
        <div
      role="dialog"
      aria-modal="true"
      className="relative z-50 inline-block w-full max-w-4xl my-8 overflow-hidden text-left align-middle transition-all transform bg-white shadow-xl rounded-2xl"
    > {/* Header */}
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            <h2 className="text-xl font-semibold text-gray-900">
              {mode === "create" ? "Nueva Promoción" : "Editar Promoción"}
            </h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <XMarkIcon className="h-6 w-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="flex flex-col lg:flex-row">
              {/* Form Section */}
              <div className="flex-1 px-6 py-4 space-y-6">
                {/* Title */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Título de la Promoción *
                  </label>
                  <input
                    type="text"
                    value={formData.title}
                    onChange={(e) => handleInputChange("title", e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent ${
                      errors.title ? "border-red-500" : "border-gray-300"
                    }`}
                    placeholder="Ej: 50% OFF en Pizzas Familiares"
                  />
                  {errors.title && (
                    <p className="mt-1 text-sm text-red-600">{errors.title}</p>
                  )}
                </div>

                {/* Description */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Descripción *
                  </label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => handleInputChange("description", e.target.value)}
                    rows={3}
                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent ${
                      errors.description ? "border-red-500" : "border-gray-300"
                    }`}
                    placeholder="Describe los detalles de tu promoción..."
                  />
                  {errors.description && (
                    <p className="mt-1 text-sm text-red-600">{errors.description}</p>
                  )}
                </div>

                {/* Discount */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Descuento *
                    </label>
                    <input
                      type="number"
                      value={formData.discount}
                      onChange={(e) => handleInputChange("discount", e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent ${
                        errors.discount ? "border-red-500" : "border-gray-300"
                      }`}
                      placeholder="50"
                      min="1"
                      max={formData.discountType === "percentage" ? "100" : undefined}
                    />
                    {errors.discount && (
                      <p className="mt-1 text-sm text-red-600">{errors.discount}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Tipo de Descuento
                    </label>
                    <select
                      value={formData.discountType}
                      onChange={(e) => handleInputChange("discountType", e.target.value as "percentage" | "fixed")}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                    >
                      <option value="percentage">Porcentaje (%)</option>
                      <option value="fixed">Cantidad Fija ($)</option>
                    </select>
                  </div>
                </div>

                {/* Category */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Categoría *
                  </label>
                  <select
                    value={formData.category}
                    onChange={(e) => handleInputChange("category", e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent ${
                      errors.category ? "border-red-500" : "border-gray-300"
                    }`}
                  >
                    <option value="">Selecciona una categoría</option>
                    {categories.map((cat) => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                  {errors.category && (
                    <p className="mt-1 text-sm text-red-600">{errors.category}</p>
                  )}
                </div>

                {/* Date Range */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Fecha de Inicio *
                    </label>
                    <input
                      type="date"
                      value={formData.startDate}
                      onChange={(e) => handleInputChange("startDate", e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent ${
                        errors.startDate ? "border-red-500" : "border-gray-300"
                      }`}
                    />
                    {errors.startDate && (
                      <p className="mt-1 text-sm text-red-600">{errors.startDate}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Fecha de Fin *
                    </label>
                    <input
                      type="date"
                      value={formData.endDate}
                      onChange={(e) => handleInputChange("endDate", e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent ${
                        errors.endDate ? "border-red-500" : "border-gray-300"
                      }`}
                    />
                    {errors.endDate && (
                      <p className="mt-1 text-sm text-red-600">{errors.endDate}</p>
                    )}
                  </div>
                </div>

                {/* Image Upload */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Imagen de la Promoción
                  </label>
                  <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md">
                    {previewImage ? (
                      <div className="relative">
                        <Image
                          src={previewImage}
                          alt="Preview"
                          width={200}
                          height={100}
                          className="rounded-md object-cover"
                        />
                        <button
                          type="button"
                          onClick={handleRemoveImage}
                          className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
                        >
                          <XMarkIcon className="h-4 w-4" />
                        </button>
                      </div>
                    ) : (
                      <div className="space-y-1 text-center">
                        <PhotoIcon className="mx-auto h-12 w-12 text-gray-400" />
                        <div className="flex text-sm text-gray-600">
                          <label className="relative cursor-pointer bg-white rounded-md font-medium text-[#008D96] hover:text-[#007580] focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-[#008D96]">
                            <span>Subir una imagen</span>
                            <input
                              ref={fileInputRef}
                              type="file"
                              className="sr-only"
                              accept="image/*"
                              onChange={handleImageUpload}
                            />
                          </label>
                          <p className="pl-1">o arrastra y suelta</p>
                        </div>
                        <p className="text-xs text-gray-500">PNG, JPG hasta 5MB</p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Terms and Conditions */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Términos y Condiciones *
                  </label>
                  <textarea
                    value={formData.terms}
                    onChange={(e) => handleInputChange("terms", e.target.value)}
                    rows={3}
                    className={`w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent ${
                      errors.terms ? "border-red-500" : "border-gray-300"
                    }`}
                    placeholder="Términos, restricciones y condiciones de la promoción..."
                  />
                  {errors.terms && (
                    <p className="mt-1 text-sm text-red-600">{errors.terms}</p>
                  )}
                </div>
              </div>

              {/* Preview Section */}
              <div className="lg:w-80 bg-gray-50 border-l border-gray-200">
                <div className="p-6">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-medium text-gray-900">Vista Previa</h3>
                    <button
                      type="button"
                      onClick={() => setShowPreview(!showPreview)}
                      className="lg:hidden p-2 text-gray-500 hover:text-gray-700"
                    >
                      <EyeIcon className="h-5 w-5" />
                    </button>
                  </div>

                  <div className={`${showPreview ? "block" : "hidden lg:block"}`}>
                    {/* Preview Card */}
                    <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                      {/* Header with image and discount */}
                      <div className="relative h-24 bg-gradient-to-r from-[#008D96] to-[#00C0CC]">
                        {previewImage ? (
                          <Image
                            src={previewImage}
                            alt="Preview"
                            fill
                            className="object-cover"
                          />
                        ) : (
                          <div className="flex items-center justify-center h-full">
                            <div className="text-center text-white">
                              <span className="text-lg font-bold">
                                {getDiscountPreview() || "Descuento"}
                              </span>
                            </div>
                          </div>
                        )}

                        <div className="absolute top-2 right-2">
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            Programada
                          </span>
                        </div>
                      </div>

                      {/* Content */}
                      <div className="p-3">
                        <h4 className="font-semibold text-gray-900 text-sm mb-1 line-clamp-1">
                          {formData.title || "Título de la promoción"}
                        </h4>
                        <p className="text-xs text-gray-600 line-clamp-2 mb-2">
                          {formData.description || "Descripción de la promoción"}
                        </p>

                        {formData.startDate && formData.endDate && (
                          <p className="text-xs text-gray-500 mb-2">
                            {new Date(formData.startDate).toLocaleDateString("es-ES")} - {new Date(formData.endDate).toLocaleDateString("es-ES")}
                          </p>
                        )}

                        <div className="flex items-center justify-between pt-2 border-t border-gray-100">
                          <span className="text-xs text-gray-500 px-2 py-1 bg-gray-100 rounded">
                            {formData.category || "Categoría"}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Footer */}
            <div className="flex justify-end space-x-3 px-6 py-4 border-t border-gray-200">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#007580] transition-colors"
              >
                {mode === "create" ? "Crear Promoción" : "Guardar Cambios"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}