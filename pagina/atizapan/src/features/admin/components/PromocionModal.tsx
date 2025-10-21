// src/app/components/promociones/PromocionModal.tsx
"use client";

import { useState, useEffect } from "react";
import { XMarkIcon, CalendarIcon, TagIcon } from "@heroicons/react/24/outline";

interface Promocion {
  id?: string;
  titulo: string;
  comercio: string;
  descuento: number;
  tipoDescuento: "porcentaje" | "fijo";
  fechaInicio: string;
  fechaFin: string;
  estado: "activa" | "pausada" | "programada" | "expirada";
  cuponesGenerados?: number;
  cuponesUsados?: number;
  categoria: string;
  descripcion: string;
  codigoPromo: string;
  limiteUsos: number;
  condiciones: string;
}

interface PromocionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (promocion: Promocion) => void;
  promocion?: Promocion;
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

const comerciosPorCategoria: Record<string, string[]> = {
  "Entretenimiento": ["Six flags", "Cinépolis VIP", "GamePlanet"],
  "Alimentación": ["Starbucks Coffee", "La Europea"],
  "Restaurantes": ["Restaurante Pujol", "Café de Tacuba", "Quintonil"],
  "Deportes": ["Nike Store", "Adidas", "Decathlon"],
  "Moda": ["Zara México", "H&M", "Liverpool"],
  "Departamental": ["Palacio de Hierro", "El Corte Inglés"],
  "Bienestar": ["Spa Relax", "Fitness First"],
  "Electrónicos": ["Elektra", "Best Buy"],
  "Salud": ["Farmacia Guadalajara", "Farmacias Similares"],
  "Servicios": ["Uber", "DiDi"],
  "Educación": ["UNAM", "Tec de Monterrey"],
  "Automotriz": ["AutoZone", "Nissan"],
  "Hogar": ["Home Depot", "Ikea"]
};

export default function PromocionModal({ isOpen, onClose, onSave, promocion, mode }: PromocionModalProps) {
  const [formData, setFormData] = useState<Promocion>({
    titulo: "",
    comercio: "",
    descuento: 0,
    tipoDescuento: "porcentaje",
    fechaInicio: "",
    fechaFin: "",
    estado: "programada",
    categoria: "",
    descripcion: "",
    codigoPromo: "",
    limiteUsos: 100,
    condiciones: "",
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [comerciosDisponibles, setComercios] = useState<string[]>([]);

  useEffect(() => {
    if (promocion && mode === 'edit') {
      setFormData(promocion);
    } else {
      setFormData({
        titulo: "",
        comercio: "",
        descuento: 0,
        tipoDescuento: "porcentaje",
        fechaInicio: "",
        fechaFin: "",
        estado: "programada",
        categoria: "",
        descripcion: "",
        codigoPromo: "",
        limiteUsos: 100,
        condiciones: "",
      });
    }
    setErrors({});
  }, [promocion, mode, isOpen]);

  useEffect(() => {
    if (formData.categoria) {
      setComercios(comerciosPorCategoria[formData.categoria] || []);
      if (!comerciosPorCategoria[formData.categoria]?.includes(formData.comercio)) {
        setFormData(prev => ({ ...prev, comercio: "" }));
      }
    } else {
      setComercios([]);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [formData.categoria]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.titulo.trim()) {
      newErrors.titulo = "El título es obligatorio";
    }

    if (!formData.categoria) {
      newErrors.categoria = "La categoría es obligatoria";
    }

    if (!formData.comercio) {
      newErrors.comercio = "El comercio es obligatorio";
    }

    if (!formData.descuento || formData.descuento <= 0) {
      newErrors.descuento = "El descuento debe ser mayor a 0";
    }

    if (formData.tipoDescuento === "porcentaje" && formData.descuento > 100) {
      newErrors.descuento = "El porcentaje no puede ser mayor a 100%";
    }

    if (!formData.fechaInicio) {
      newErrors.fechaInicio = "La fecha de inicio es obligatoria";
    }

    if (!formData.fechaFin) {
      newErrors.fechaFin = "La fecha de fin es obligatoria";
    }

    if (formData.fechaInicio && formData.fechaFin && formData.fechaInicio >= formData.fechaFin) {
      newErrors.fechaFin = "La fecha de fin debe ser posterior a la fecha de inicio";
    }

    if (!formData.codigoPromo.trim()) {
      newErrors.codigoPromo = "El código promocional es obligatorio";
    }

    if (!formData.descripcion.trim()) {
      newErrors.descripcion = "La descripción es obligatoria";
    }

    if (!formData.condiciones.trim()) {
      newErrors.condiciones = "Las condiciones son obligatorias";
    }

    if (!formData.limiteUsos || formData.limiteUsos <= 0) {
      newErrors.limiteUsos = "El límite de usos debe ser mayor a 0";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      onSave(formData);
      onClose();
    } catch (error) {
      console.error("Error guardando promoción:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof Promocion, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: "" }));
    }
  };

  const generateCodigoPromo = () => {
    const titulo = formData.titulo.replace(/\s+/g, '').toUpperCase();
    const descuento = formData.descuento;
    const tipo = formData.tipoDescuento === "porcentaje" ? "" : "MX";
    const codigo = `${titulo.substring(0, 6)}${descuento}${tipo}`;
    handleInputChange('codigoPromo', codigo);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <div
    className="fixed inset-0 bg-white/5  backdrop-blur-sm backdrop-saturate-150 transition-opacity duration-300"
    onClick={onClose}
    aria-hidden="true"
  />

        <div 
      role="dialog"
      aria-modal="true" className=" relative z-[1001] inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full">
          <div className="bg-white px-6 pt-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-medium text-[#4B4C7E]">
                {mode === 'create' ? 'Nueva Promoción' : 'Editar Promoción'}
              </h3>
              <button
                onClick={onClose}
                className="text-[#969696] hover:text-[#4B4C7E] transition-colors"
              >
                <XMarkIcon className="h-6 w-6" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-8">
              {/* Fila 1: Información Básica */}
              <div>
                <h4 className="text-sm font-medium text-[#4B4C7E] mb-4 flex items-center">
                  <TagIcon className="h-4 w-4 mr-2" />
                  Información Básica
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="md:col-span-2">
                    <label htmlFor="titulo" className="block text-sm font-medium text-[#969696] mb-1">
                      Título de la Promoción *
                    </label>
                    <input
                      id="titulo"
                      type="text"
                      value={formData.titulo}
                      onChange={(e) => handleInputChange('titulo', e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                        errors.titulo ? 'border-red-500' : 'border-gray-200'
                      }`}
                      placeholder="Ej: Descuento de Verano Six Flags"
                    />
                    {errors.titulo && <p className="mt-1 text-sm text-red-500">{errors.titulo}</p>}
                  </div>

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
                      <option value="">Selecciona categoría</option>
                      {categorias.map(cat => (
                        <option key={cat} value={cat}>{cat}</option>
                      ))}
                    </select>
                    {errors.categoria && <p className="mt-1 text-sm text-red-500">{errors.categoria}</p>}
                  </div>
                </div>
              </div>

              {/* Fila 2: Comercio y Descuento */}
              <div>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  <div>
                    <label htmlFor="comercio" className="block text-sm font-medium text-[#969696] mb-1">
                      Comercio *
                    </label>
                    <select
                      id="comercio"
                      value={formData.comercio}
                      onChange={(e) => handleInputChange('comercio', e.target.value)}
                      disabled={!formData.categoria}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                        errors.comercio ? 'border-red-500' : 'border-gray-200'
                      } ${!formData.categoria ? 'bg-gray-100' : ''}`}
                    >
                      <option value="">Selecciona comercio</option>
                      {comerciosDisponibles.map(comercio => (
                        <option key={comercio} value={comercio}>{comercio}</option>
                      ))}
                    </select>
                    {errors.comercio && <p className="mt-1 text-sm text-red-500">{errors.comercio}</p>}
                  </div>

                  <div>
                    <label htmlFor="tipoDescuento" className="block text-sm font-medium text-[#969696] mb-1">
                      Tipo *
                    </label>
                    <select
                      id="tipoDescuento"
                      value={formData.tipoDescuento}
                      onChange={(e) => handleInputChange('tipoDescuento', e.target.value as "porcentaje" | "fijo")}
                      className="w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96]"
                    >
                      <option value="porcentaje">Porcentaje (%)</option>
                      <option value="fijo">Monto Fijo ($)</option>
                    </select>
                  </div>

                  <div>
                    <label htmlFor="descuento" className="block text-sm font-medium text-[#969696] mb-1">
                      Descuento *
                    </label>
                    <div className="relative">
                      <input
                        id="descuento"
                        type="number"
                        min="0"
                        max={formData.tipoDescuento === "porcentaje" ? "100" : undefined}
                        value={formData.descuento}
                        onChange={(e) => handleInputChange('descuento', Number(e.target.value))}
                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                          errors.descuento ? 'border-red-500' : 'border-gray-200'
                        }`}
                        placeholder={formData.tipoDescuento === "porcentaje" ? "25" : "500"}
                      />
                      <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                        <span className="text-gray-500 text-sm">
                          {formData.tipoDescuento === "porcentaje" ? "%" : "$"}
                        </span>
                      </div>
                    </div>
                    {errors.descuento && <p className="mt-1 text-sm text-red-500">{errors.descuento}</p>}
                  </div>

                  <div>
                    <label htmlFor="limiteUsos" className="block text-sm font-medium text-[#969696] mb-1">
                      Límite Usos *
                    </label>
                    <input
                      id="limiteUsos"
                      type="number"
                      min="1"
                      value={formData.limiteUsos}
                      onChange={(e) => handleInputChange('limiteUsos', Number(e.target.value))}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                        errors.limiteUsos ? 'border-red-500' : 'border-gray-200'
                      }`}
                      placeholder="100"
                    />
                    {errors.limiteUsos && <p className="mt-1 text-sm text-red-500">{errors.limiteUsos}</p>}
                  </div>
                </div>
              </div>

              {/* Fila 3: Fechas y Código */}
              <div>
                <h4 className="text-sm font-medium text-[#4B4C7E] mb-4 flex items-center">
                  <CalendarIcon className="h-4 w-4 mr-2" />
                  Vigencia y Código
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  <div>
                    <label htmlFor="fechaInicio" className="block text-sm font-medium text-[#969696] mb-1">
                      Fecha Inicio *
                    </label>
                    <input
                      id="fechaInicio"
                      type="date"
                      value={formData.fechaInicio}
                      onChange={(e) => handleInputChange('fechaInicio', e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                        errors.fechaInicio ? 'border-red-500' : 'border-gray-200'
                      }`}
                    />
                    {errors.fechaInicio && <p className="mt-1 text-sm text-red-500">{errors.fechaInicio}</p>}
                  </div>

                  <div>
                    <label htmlFor="fechaFin" className="block text-sm font-medium text-[#969696] mb-1">
                      Fecha Fin *
                    </label>
                    <input
                      id="fechaFin"
                      type="date"
                      value={formData.fechaFin}
                      onChange={(e) => handleInputChange('fechaFin', e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                        errors.fechaFin ? 'border-red-500' : 'border-gray-200'
                      }`}
                    />
                    {errors.fechaFin && <p className="mt-1 text-sm text-red-500">{errors.fechaFin}</p>}
                  </div>

                  <div>
                    <label htmlFor="codigoPromo" className="block text-sm font-medium text-[#969696] mb-1">
                      Código Promo *
                    </label>
                    <input
                      id="codigoPromo"
                      type="text"
                      value={formData.codigoPromo}
                      onChange={(e) => handleInputChange('codigoPromo', e.target.value.toUpperCase())}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                        errors.codigoPromo ? 'border-red-500' : 'border-gray-200'
                      }`}
                      placeholder="VERANO25"
                    />
                    {errors.codigoPromo && <p className="mt-1 text-sm text-red-500">{errors.codigoPromo}</p>}
                  </div>

                  <div className="flex items-end">
                    <button
                      type="button"
                      onClick={generateCodigoPromo}
                      className="w-full px-4 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#00565B] transition-colors text-sm font-medium"
                    >
                      Generar Código
                    </button>
                  </div>
                </div>
              </div>

              {/* Fila 4: Descripción y Condiciones */}
              <div>
                <h4 className="text-sm font-medium text-[#4B4C7E] mb-4">Detalles de la Promoción</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label htmlFor="descripcion" className="block text-sm font-medium text-[#969696] mb-1">
                      Descripción *
                    </label>
                    <textarea
                      id="descripcion"
                      rows={4}
                      value={formData.descripcion}
                      onChange={(e) => handleInputChange('descripcion', e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors resize-none ${
                        errors.descripcion ? 'border-red-500' : 'border-gray-200'
                      }`}
                      placeholder="Describe la promoción y sus beneficios..."
                    />
                    {errors.descripcion && <p className="mt-1 text-sm text-red-500">{errors.descripcion}</p>}
                  </div>

                  <div>
                    <label htmlFor="condiciones" className="block text-sm font-medium text-[#969696] mb-1">
                      Términos y Condiciones *
                    </label>
                    <textarea
                      id="condiciones"
                      rows={4}
                      value={formData.condiciones}
                      onChange={(e) => handleInputChange('condiciones', e.target.value)}
                      className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors resize-none ${
                        errors.condiciones ? 'border-red-500' : 'border-gray-200'
                      }`}
                      placeholder="Especifica las condiciones de uso, restricciones, etc..."
                    />
                    {errors.condiciones && <p className="mt-1 text-sm text-red-500">{errors.condiciones}</p>}
                  </div>
                </div>
              </div>

              {/* Estadísticas en modo edición */}
              {mode === 'edit' && promocion && (
                <div className="bg-gradient-to-r from-blue-50 to-cyan-50 p-6 rounded-lg border border-blue-200">
                  <h4 className="text-sm font-medium text-[#4B4C7E] mb-4">📊 Estadísticas Actuales</h4>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                    <div className="text-center">
                      <span className="text-xs text-[#969696] block">Estado</span>
                      <p className="text-lg font-bold text-[#4B4C7E] mt-1">{promocion.estado}</p>
                    </div>
                    <div className="text-center">
                      <span className="text-xs text-[#969696] block">Cupones Generados</span>
                      <p className="text-lg font-bold text-[#4B4C7E] mt-1">{promocion.cuponesGenerados || 0}</p>
                    </div>
                    <div className="text-center">
                      <span className="text-xs text-[#969696] block">Cupones Usados</span>
                      <p className="text-lg font-bold text-[#008D96] mt-1">{promocion.cuponesUsados || 0}</p>
                    </div>
                    <div className="text-center">
                      <span className="text-xs text-[#969696] block">Efectividad</span>
                      <p className="text-lg font-bold text-[#008D96] mt-1">
                        {promocion.cuponesGenerados ? 
                          Math.round((promocion.cuponesUsados || 0) / promocion.cuponesGenerados * 100) : 0
                        }%
                      </p>
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
                mode === 'create' ? 'Crear Promoción' : 'Guardar Cambios'
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