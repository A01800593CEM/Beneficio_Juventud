// src/app/components/comercios/ComerciosGrid.tsx
"use client";

import { PencilIcon, TrashIcon, CheckCircleIcon } from "@heroicons/react/24/outline";

interface Comercio {
  id: string;
  name: string;
  promocionesActivas: number;
  promedioMes: number;
  categoria: string;
  telefono: string;
  email: string;
  direccion: string;
  descripcion: string;
}

interface ComerciosGridProps {
  comercios: Comercio[];
  onEdit: (comercioId: string) => void;
  onDelete: (comercioId: string) => void;
}

export default function ComerciosGrid({ comercios, onEdit, onDelete }: ComerciosGridProps) {
  const getCategoriaColor = (categoria: string) => {
    const colors: Record<string, string> = {
      "Entretenimiento": "bg-purple-100 text-purple-800",
      "Alimentación": "bg-green-100 text-green-800",
      "Restaurantes": "bg-orange-100 text-orange-800",
      "Deportes": "bg-blue-100 text-blue-800",
      "Moda": "bg-pink-100 text-pink-800",
      "Departamental": "bg-indigo-100 text-indigo-800",
      "Bienestar": "bg-teal-100 text-teal-800",
      "Electrónicos": "bg-gray-100 text-gray-800",
      "Salud": "bg-red-100 text-red-800",
    };
    return colors[categoria] || "bg-gray-100 text-gray-800";
  };

  if (comercios.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12">
        <div className="text-center">
          <svg className="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
          <p className="text-xl font-medium text-gray-900 mb-2">No se encontraron comercios</p>
          <p className="text-gray-500">Intenta con diferentes criterios de búsqueda o agrega un nuevo comercio</p>
        </div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
      {comercios.map((comercio) => (
        <div
          key={comercio.id}
          className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 hover:shadow-md transition-all duration-200 group"
        >
          {/* Header con nombre y verificación */}
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-[#015463] mb-1 group-hover:text-[#008D96] transition-colors">
                {comercio.name}
              </h3>
              <div className="flex items-center text-sm text-[#969696]">
                <CheckCircleIcon className="h-4 w-4 text-[#008D96] mr-1" />
                Verificado
              </div>
            </div>
            <div className="flex space-x-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <button
                onClick={() => onEdit(comercio.id)}
                className="p-1 text-[#008D96] hover:text-[#00565B] transition-colors"
                title="Editar comercio"
              >
                <PencilIcon className="h-4 w-4" />
              </button>
              <button
                onClick={() => onDelete(comercio.id)}
                className="p-1 text-red-500 hover:text-red-700 transition-colors"
                title="Eliminar comercio"
              >
                <TrashIcon className="h-4 w-4" />
              </button>
            </div>
          </div>

          {/* Estadísticas */}
          <div className="space-y-3 mb-2">
            <div className="flex justify-between items-center">
              <span className="text-sm text-[#969696]">Promociones Activas:</span>
              <span className="text-sm font-medium text-[#4B4C7E]">{comercio.promocionesActivas}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-[#969696]">Promedio por mes:</span>
              <span className="text-sm font-medium text-[#4B4C7E]">{comercio.promedioMes}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-[#969696]">Categoría:</span>
              <span className={`text-xs px-2 py-1 rounded-full font-medium ${getCategoriaColor(comercio.categoria)}`}>
                {comercio.categoria}
              </span>
            </div>
          </div>

          {/* Información de contacto */}
          <div className="border-t border-gray-100 pt-4 space-y-2">
            <div className="text-xs text-[#969696]">
              <strong>Email:</strong> {comercio.email}
            </div>
            <div className="text-xs text-[#969696]">
              <strong>Teléfono:</strong> {comercio.telefono}
            </div>
            <div className="text-xs text-[#969696]">
              <strong>Dirección:</strong> {comercio.direccion}
            </div>
          </div>

          {/* Descripción */}
          {comercio.descripcion && (
            <div className="mt-2 pt-2 border-t border-gray-100">
              <p className="text-xs text-[#969696] line-clamp-2">
                {comercio.descripcion}
              </p>
            </div>
          )}

          {/* Indicador de rendimiento */}
          <div className="mt-4 pt-3 border-t border-gray-100">
            <div className="flex items-center justify-between">
              <span className="text-xs text-[#969696]">Rendimiento</span>
              <div className="flex items-center">
                <div className={`h-2 w-2 rounded-full mr-2 ${
                  comercio.promocionesActivas >= 5 ? 'bg-green-500' :
                  comercio.promocionesActivas >= 3 ? 'bg-yellow-500' : 'bg-red-500'
                }`} />
                <span className="text-xs font-medium text-[#4B4C7E]">
                  {comercio.promocionesActivas >= 5 ? 'Excelente' :
                   comercio.promocionesActivas >= 3 ? 'Bueno' : 'Regular'}
                </span>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}