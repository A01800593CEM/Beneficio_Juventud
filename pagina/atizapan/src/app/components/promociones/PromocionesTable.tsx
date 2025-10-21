// src/app/components/promociones/PromocionesTable.tsx
"use client";

import { PencilIcon, TrashIcon, PlayIcon, PauseIcon, EyeIcon } from "@heroicons/react/24/outline";
import { useState } from "react";

interface Promocion {
  id: string;
  titulo: string;
  comercio: string;
  descuento: number;
  tipoDescuento: "porcentaje" | "fijo";
  fechaInicio: string;
  fechaFin: string;
  estado: "activa" | "pausada" | "programada" | "expirada";
  cuponesGenerados: number;
  cuponesUsados: number;
  categoria: string;
  descripcion: string;
  codigoPromo: string;
  limiteUsos: number;
  condiciones: string;
}

interface PromocionesTableProps {
  promociones: Promocion[];
  onEdit: (promocionId: string) => void;
  onDelete: (promocionId: string) => void;
  onToggleEstado: (promocionId: string) => void;
}

export default function PromocionesTable({ 
  promociones, 
  onEdit, 
  onDelete, 
  onToggleEstado 
}: PromocionesTableProps) {
  const [expandedRow, setExpandedRow] = useState<string | null>(null);

  const getEstadoColor = (estado: string) => {
    const colors: Record<string, string> = {
      "activa": "bg-green-100 text-green-800",
      "pausada": "bg-yellow-100 text-yellow-800",
      "programada": "bg-blue-100 text-blue-800",
      "expirada": "bg-red-100 text-red-800",
    };
    return colors[estado] || "bg-gray-100 text-gray-800";
  };

  const formatDescuento = (descuento: number, tipo: string) => {
    return tipo === "porcentaje" ? `${descuento}%` : `$${descuento}`;
  };

  const formatFecha = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const calcularEfectividad = (usados: number, generados: number) => {
    if (generados === 0) return 0;
    return Math.round((usados / generados) * 100);
  };

  const toggleExpanded = (id: string) => {
    setExpandedRow(expandedRow === id ? null : id);
  };

  if (promociones.length === 0) {
    return (
      <div className="p-12 text-center">
        <svg className="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
        </svg>
        <p className="text-xl font-medium text-gray-900 mb-2">No se encontraron promociones</p>
        <p className="text-gray-500">Intenta con diferentes criterios de búsqueda o crea una nueva promoción</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-full">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-3 sm:px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              Promoción
            </th>
            <th className="hidden md:table-cell px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              Comercio
            </th>
            <th className="px-3 sm:px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              Descuento
            </th>
            <th className="hidden lg:table-cell px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              Vigencia
            </th>
            <th className="px-3 sm:px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              Estado
            </th>
            <th className="hidden xl:table-cell px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              Efectividad
            </th>
            <th className="px-3 sm:px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              Acciones
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {promociones.map((promocion) => (
            <>
              <tr key={promocion.id} className="hover:bg-gray-50 transition-colors duration-150">
                <td className="px-3 sm:px-6 py-4">
                  <div>
                    <div className="text-sm font-medium text-[#015463]">
                      {promocion.titulo}
                    </div>
                    <div className="text-xs text-[#969696] mt-1">
                      {promocion.codigoPromo}
                    </div>
                    {/* En móvil, mostrar comercio */}
                    <div className="md:hidden text-xs text-[#969696] mt-1">
                      {promocion.comercio}
                    </div>
                  </div>
                </td>
                <td className="hidden md:table-cell px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-[#969696]">
                    {promocion.comercio}
                  </div>
                  <div className="text-xs text-[#969696]">
                    {promocion.categoria}
                  </div>
                </td>
                <td className="px-3 sm:px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-[#008D96]">
                    {formatDescuento(promocion.descuento, promocion.tipoDescuento)}
                  </div>
                </td>
                <td className="hidden lg:table-cell px-6 py-4 whitespace-nowrap">
                  <div className="text-xs text-[#969696]">
                    <div>{formatFecha(promocion.fechaInicio)}</div>
                    <div>{formatFecha(promocion.fechaFin)}</div>
                  </div>
                </td>
                <td className="px-3 sm:px-6 py-4 whitespace-nowrap">
                  <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getEstadoColor(promocion.estado)}`}>
                    {promocion.estado}
                  </span>
                  {/* En móvil, mostrar efectividad */}
                  <div className="xl:hidden text-xs text-[#969696] mt-1">
                    {promocion.cuponesGenerados > 0 && (
                      <span>{calcularEfectividad(promocion.cuponesUsados, promocion.cuponesGenerados)}% efectividad</span>
                    )}
                  </div>
                </td>
                <td className="hidden xl:table-cell px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-[#4B4C7E]">
                    {promocion.cuponesUsados}/{promocion.cuponesGenerados}
                  </div>
                  {promocion.cuponesGenerados > 0 && (
                    <div className="text-xs text-[#969696]">
                      {calcularEfectividad(promocion.cuponesUsados, promocion.cuponesGenerados)}% efectividad
                    </div>
                  )}
                </td>
                <td className="px-3 sm:px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div className="flex space-x-1 sm:space-x-2">
                    <button
                      onClick={() => toggleExpanded(promocion.id)}
                      className="text-[#969696] hover:text-[#4B4C7E] transition-colors duration-200 p-1"
                      title="Ver detalles"
                    >
                      <EyeIcon className="h-4 w-4" />
                    </button>
                    {(promocion.estado === 'activa' || promocion.estado === 'pausada') && (
                      <button
                        onClick={() => onToggleEstado(promocion.id)}
                        className={`transition-colors duration-200 p-1 ${
                          promocion.estado === 'activa' 
                            ? 'text-yellow-600 hover:text-yellow-700' 
                            : 'text-green-600 hover:text-green-700'
                        }`}
                        title={promocion.estado === 'activa' ? 'Pausar' : 'Activar'}
                      >
                        {promocion.estado === 'activa' ? 
                          <PauseIcon className="h-4 w-4" /> : 
                          <PlayIcon className="h-4 w-4" />
                        }
                      </button>
                    )}
                    <button
                      onClick={() => onEdit(promocion.id)}
                      className="text-[#008D96] hover:text-[#00565B] transition-colors duration-200 p-1"
                      title="Editar promoción"
                    >
                      <PencilIcon className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => onDelete(promocion.id)}
                      className="text-red-500 hover:text-red-700 transition-colors duration-200 p-1"
                      title="Eliminar promoción"
                    >
                      <TrashIcon className="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
              
              {/* Fila expandida con detalles */}
              {expandedRow === promocion.id && (
                <tr className="bg-gray-50">
                  <td colSpan={7} className="px-6 py-4">
                    <div className="space-y-3">
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        <div>
                          <h4 className="text-sm font-medium text-[#4B4C7E] mb-2">Descripción</h4>
                          <p className="text-sm text-[#969696]">{promocion.descripcion}</p>
                        </div>
                        <div>
                          <h4 className="text-sm font-medium text-[#4B4C7E] mb-2">Condiciones</h4>
                          <p className="text-sm text-[#969696]">{promocion.condiciones}</p>
                        </div>
                        <div>
                          <h4 className="text-sm font-medium text-[#4B4C7E] mb-2">Estadísticas</h4>
                          <div className="text-sm text-[#969696] space-y-1">
                            <div>Límite de usos: {promocion.limiteUsos}</div>
                            <div>Cupones generados: {promocion.cuponesGenerados}</div>
                            <div>Cupones usados: {promocion.cuponesUsados}</div>
                            <div>Vigencia: {formatFecha(promocion.fechaInicio)} - {formatFecha(promocion.fechaFin)}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </td>
                </tr>
              )}
            </>
          ))}
        </tbody>
      </table>
    </div>
  );
}