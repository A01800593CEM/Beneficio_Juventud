// src/app/components/comercios/ComercioModal.tsx
"use client";

import { useState, useEffect } from "react";
import { XMarkIcon } from "@heroicons/react/24/outline";

interface Comercio {
  id?: string;
  name: string;
  categoria: string;
  telefono: string;
  email: string;
  direccion: string;
  descripcion: string;
  promocionesActivas: number;
  promedioMes: number;
}

interface ComercioModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (comercio: Comercio) => void;
  comercio?: Comercio;
  mode: 'create' | 'edit';
}

const categorias = [
  "Entretenimiento",
  "Alimentación", 
  "Restaurantes",
  "Deportes",
  "Moda",
  "Departamental",
  "Bienestar",
  "Electrónicos",
  "Salud",
  "Servicios",
  "Educación",
  "Automotriz",
  "Hogar"
];

export default function ComercioModal({ isOpen, onClose, onSave, comercio, mode }: ComercioModalProps) {
  const [formData, setFormData] = useState<Comercio>({
    name: "",
    categoria: "",
    telefono: "",
    email: "",
    direccion: "",
    descripcion: "",
    promocionesActivas: 0,
    promedioMes: 0,
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (comercio && mode === 'edit') {
      setFormData(comercio);
    } else {
      setFormData({
        name: "",
        categoria: "",
        telefono: "",
        email: "",
        direccion: "",
        descripcion: "",
        promocionesActivas: 0,
        promedioMes: 0,
      });
    }
    setErrors({});
  }, [comercio, mode, isOpen]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = "El nombre del comercio es obligatorio";
    }

    if (!formData.categoria) {
      newErrors.categoria = "La categoría es obligatoria";
    }

    if (!formData.email.trim()) {
      newErrors.email = "El email es obligatorio";
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "El email no es válido";
    }

    if (!formData.telefono.trim()) {
      newErrors.telefono = "El teléfono es obligatorio";
    } else if (!/^\d{10}$/.test(formData.telefono.replace(/\D/g, ""))) {
      newErrors.telefono = "El teléfono debe tener 10 dígitos";
    }

    if (!formData.direccion.trim()) {
      newErrors.direccion = "La dirección es obligatoria";
    }

    if (!formData.descripcion.trim()) {
      newErrors.descripcion = "La descripción es obligatoria";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulación de API
      onSave(formData);
      onClose();
    } catch (error) {
      console.error("Error guardando comercio:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof Comercio, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: "" }));
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
    <div className="fixed inset-0 z-[1000] flex items-center justify-center p-4 sm:p-0">
  {/* Overlay: vidrio esmerilado */}
  <div
    className="fixed inset-0 bg-white/5 backdrop-blur-sm backdrop-saturate-150 transition-opacity duration-300"
    onClick={onClose}
    aria-hidden="true"
  />

        {/* Modal */}
        <div
      role="dialog"
      aria-modal="true"
      className="relative z-[1001] w-full sm:max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl"
    >
      <div className="bg-white px-6 pt-6">
        <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-medium text-[#4B4C7E]">
                {mode === 'create' ? 'Nuevo Comercio' : 'Editar Comercio'}
              </h3>
              <button
                onClick={onClose}
                className="text-[#969696] hover:text-[#4B4C7E] transition-colors"
              >
                <XMarkIcon className="h-6 w-6" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Nombre del Comercio */}
                <div className="md:col-span-2">
                  <label htmlFor="name" className="block text-sm font-medium text-[#969696] mb-1">
                    Nombre del Comercio *
                  </label>
                  <input
                    id="name"
                    type="text"
                    value={formData.name}
                    onChange={(e) => handleInputChange('name', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                      errors.name ? 'border-red-500' : 'border-gray-200'
                    }`}
                    placeholder="Ej: Six Flags México"
                  />
                  {errors.name && (
                    <p className="mt-1 text-sm text-red-500">{errors.name}</p>
                  )}
                </div>

                {/* Categoría */}
                <div>
                  <label htmlFor="categoria" className="block text-sm font-medium text-[#969696] mb-1">
                    Categoría *
                  </label>
                  <select
                    id="categoria"
                    value={formData.categoria}
                    onChange={(e) => handleInputChange('categoria', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                      errors.categoria ? 'border-red-500' : 'border-gray-200'
                    }`}
                  >
                    <option value="">Selecciona una categoría</option>
                    {categorias.map(cat => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                  {errors.categoria && (
                    <p className="mt-1 text-sm text-red-500">{errors.categoria}</p>
                  )}
                </div>

                {/* Teléfono */}
                <div>
                  <label htmlFor="telefono" className="block text-sm font-medium text-[#969696] mb-1">
                    Teléfono *
                  </label>
                  <input
                    id="telefono"
                    type="tel"
                    value={formData.telefono}
                    onChange={(e) => handleInputChange('telefono', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                      errors.telefono ? 'border-red-500' : 'border-gray-200'
                    }`}
                    placeholder="5555555555"
                  />
                  {errors.telefono && (
                    <p className="mt-1 text-sm text-red-500">{errors.telefono}</p>
                  )}
                </div>

                {/* Email */}
                <div className="md:col-span-2">
                  <label htmlFor="email" className="block text-sm font-medium text-[#969696] mb-1">
                    Correo Electrónico *
                  </label>
                  <input
                    id="email"
                    type="email"
                    value={formData.email}
                    onChange={(e) => handleInputChange('email', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                      errors.email ? 'border-red-500' : 'border-gray-200'
                    }`}
                    placeholder="contacto@comercio.com"
                  />
                  {errors.email && (
                    <p className="mt-1 text-sm text-red-500">{errors.email}</p>
                  )}
                </div>

                {/* Dirección */}
                <div className="md:col-span-2">
                  <label htmlFor="direccion" className="block text-sm font-medium text-[#969696] mb-1">
                    Dirección *
                  </label>
                  <input
                    id="direccion"
                    type="text"
                    value={formData.direccion}
                    onChange={(e) => handleInputChange('direccion', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                      errors.direccion ? 'border-red-500' : 'border-gray-200'
                    }`}
                    placeholder="Av. Revolución 1234, Col. Centro"
                  />
                  {errors.direccion && (
                    <p className="mt-1 text-sm text-red-500">{errors.direccion}</p>
                  )}
                </div>

                {/* Descripción */}
                <div className="md:col-span-2">
                  <label htmlFor="descripcion" className="block text-sm font-medium text-[#969696] mb-1">
                    Descripción *
                  </label>
                  <textarea
                    id="descripcion"
                    rows={3}
                    value={formData.descripcion}
                    onChange={(e) => handleInputChange('descripcion', e.target.value)}
                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors resize-none ${
                      errors.descripcion ? 'border-red-500' : 'border-gray-200'
                    }`}
                    placeholder="Descripción del comercio y sus servicios..."
                  />
                  {errors.descripcion && (
                    <p className="mt-1 text-sm text-red-500">{errors.descripcion}</p>
                  )}
                </div>
              </div>

              {/* Información adicional en modo edición */}
              {mode === 'edit' && comercio && (
                <div className="bg-gray-50 p-4 rounded-lg mt-6">
                  <h4 className="text-sm font-medium text-[#4B4C7E] mb-3">Estadísticas</h4>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-xs text-[#969696]">Promociones Activas</span>
                      <p className="text-sm font-medium text-[#4B4C7E]">{comercio.promocionesActivas}</p>
                    </div>
                    <div>
                      <span className="text-xs text-[#969696]">Promedio por Mes</span>
                      <p className="text-sm font-medium text-[#008D96]">{comercio.promedioMes}</p>
                    </div>
                  </div>
                </div>
              )}
            </form>
          </div>

          {/* Footer */}
          <div className="bg-gray-50 px-6 py-4 sm:flex sm:flex-row-reverse">
            <button
              type="submit"
              onClick={handleSubmit}
              disabled={loading}
              className={`w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm transition-colors ${
                loading 
                  ? 'bg-gray-400 cursor-not-allowed' 
                  : 'bg-[#008D96] hover:bg-[#00565B] focus:ring-[#008D96]'
              }`}
            >
              {loading ? (
                <div className="flex items-center">
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Guardando...
                </div>
              ) : (
                mode === 'create' ? 'Crear Comercio' : 'Guardar Cambios'
              )}
            </button>
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-[#969696] hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#008D96] sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm transition-colors"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}