// src/app/components/users/UserModal.tsx
"use client";

import { useState, useEffect } from "react";
import { XMarkIcon } from "@heroicons/react/24/outline";

interface User {
  id?: string;
  name: string;
  email: string;
  phone: string;
  cuponesUsados?: number;
  totalAhorrado?: number;
}

interface UserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (user: User) => void;
  user?: User;
  mode: 'create' | 'edit';
}

export default function UserModal({ isOpen, onClose, onSave, user, mode }: UserModalProps) {
  const [formData, setFormData] = useState<User>({
    name: "",
    email: "",
    phone: "",
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (user && mode === 'edit') {
      setFormData(user);
    } else {
      setFormData({
        name: "",
        email: "",
        phone: "",
      });
    }
    setErrors({});
  }, [user, mode, isOpen]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = "El nombre es obligatorio";
    }

    if (!formData.email.trim()) {
      newErrors.email = "El email es obligatorio";
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "El email no es válido";
    }

    if (!formData.phone.trim()) {
      newErrors.phone = "El teléfono es obligatorio";
    } else if (!/^\d{10}$/.test(formData.phone.replace(/\D/g, ""))) {
      newErrors.phone = "El teléfono debe tener 10 dígitos";
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
      console.error("Error guardando usuario:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof User, value: string) => {
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

    {/* Modal encima del overlay */}
    <div
      role="dialog"
      aria-modal="true"
      className="relative z-[1001] w-full sm:max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl"
    >
      {/* Header + Body */}
      <div className="bg-white px-6 pt-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-medium text-[#4B4C7E]">
            {mode === 'create' ? 'Nuevo Usuario' : 'Editar Usuario'}
          </h3>
          <button
            onClick={onClose}
            className="text-[#969696] hover:text-[#4B4C7E] transition-colors"
          >
            <XMarkIcon className="h-6 w-6" />
          </button>
        </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Nombre */}
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-[#969696] mb-1">
                  Nombre Completo *
                </label>
                <input
                  id="name"
                  type="text"
                  value={formData.name}
                  onChange={(e) => handleInputChange('name', e.target.value)}
                  className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                    errors.name ? 'border-red-500' : 'border-gray-200'
                  }`}
                  placeholder="Ingresa el nombre completo"
                />
                {errors.name && (
                  <p className="mt-1 text-sm text-red-500">{errors.name}</p>
                )}
              </div>

              {/* Email */}
              <div>
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
                  placeholder="usuario@email.com"
                />
                {errors.email && (
                  <p className="mt-1 text-sm text-red-500">{errors.email}</p>
                )}
              </div>

              {/* Teléfono */}
              <div>
                <label htmlFor="phone" className="block text-sm font-medium text-[#969696] mb-1">
                  Teléfono *
                </label>
                <input
                  id="phone"
                  type="tel"
                  value={formData.phone}
                  onChange={(e) => handleInputChange('phone', e.target.value)}
                  className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] transition-colors ${
                    errors.phone ? 'border-red-500' : 'border-gray-200'
                  }`}
                  placeholder="5555555555"
                />
                {errors.phone && (
                  <p className="mt-1 text-sm text-red-500">{errors.phone}</p>
                )}
              </div>

              {/* Información adicional en modo edición */}
              {mode === 'edit' && user && (
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h4 className="text-sm font-medium text-[#4B4C7E] mb-2">Información Adicional</h4>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-xs text-[#969696]">Cupones Usados</span>
                      <p className="text-sm font-medium text-[#4B4C7E]">{user.cuponesUsados || 0}</p>
                    </div>
                    <div>
                      <span className="text-xs text-[#969696]">Total Ahorrado</span>
                      <p className="text-sm font-medium text-[#008D96]">${user.totalAhorrado || 0}</p>
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
                mode === 'create' ? 'Crear Usuario' : 'Guardar Cambios'
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