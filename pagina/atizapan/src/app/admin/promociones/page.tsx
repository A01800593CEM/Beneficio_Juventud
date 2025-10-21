// src/app/admin/promociones/page.tsx
"use client";

import { useState, useEffect, useCallback } from "react";
import DashboardLayout from "../../../features/admin/components/DashboardLayout";
import { ActionButton, ExportButton, RefreshButton } from "../../../features/admin/components/ActionButtons";
import SearchInput from "../../../features/admin/components/SearchInput";
import PromocionesTable from "../../../features/admin/components/PromocionesTable";
import PromocionModal from "../../../features/admin/components/PromocionModal";
import Pagination from "../../../features/admin/components/Pagination";
import { usePagination } from "../../../shared/hooks/usePagination";
import { TagIcon } from "@heroicons/react/24/outline";
import { apiService } from "@/lib/api";

// Datos de ejemplo para las promociones
const mockPromociones = [
  {
    id: "1",
    titulo: "Descuento Six Flags Verano",
    comercio: "Six flags",
    descuento: 25,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2025-06-01",
    fechaFin: "2025-08-31",
    estado: "activa" as const,
    cuponesGenerados: 150,
    cuponesUsados: 89,
    categoria: "Entretenimiento",
    descripcion: "Descuento especial para temporada de verano en Six Flags México",
    codigoPromo: "VERANO25",
    limiteUsos: 200,
    condiciones: "Válido de lunes a viernes, no incluye días festivos"
  },
  {
    id: "2",
    titulo: "2x1 en Cines VIP",
    comercio: "Cinépolis VIP",
    descuento: 50,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2025-01-01",
    fechaFin: "2025-12-31",
    estado: "activa" as const,
    cuponesGenerados: 300,
    cuponesUsados: 245,
    categoria: "Entretenimiento",
    descripcion: "Promoción 2x1 en boletos de cine VIP",
    codigoPromo: "CINE2X1",
    limiteUsos: 500,
    condiciones: "Válido para funciones de lunes a miércoles"
  },
  {
    id: "3",
    titulo: "Descuento Restaurante Pujol",
    comercio: "Restaurante Pujol",
    descuento: 500,
    tipoDescuento: "fijo" as const,
    fechaInicio: "2025-01-15",
    fechaFin: "2025-03-15",
    estado: "pausada" as const,
    cuponesGenerados: 50,
    cuponesUsados: 12,
    categoria: "Restaurantes",
    descripcion: "$500 de descuento en cena para dos personas",
    codigoPromo: "PUJOL500",
    limiteUsos: 100,
    condiciones: "Válido solo para cenas, reserva con 48hrs de anticipación"
  },
  {
    id: "4",
    titulo: "Black Friday Nike",
    comercio: "Nike Store",
    descuento: 40,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2024-11-24",
    fechaFin: "2024-11-30",
    estado: "expirada" as const,
    cuponesGenerados: 500,
    cuponesUsados: 487,
    categoria: "Deportes",
    descripcion: "Descuento especial Black Friday en toda la tienda",
    codigoPromo: "NIKE40BF",
    limiteUsos: 500,
    condiciones: "No aplica con otras promociones, válido en tienda física"
  },
  {
    id: "5",
    titulo: "Café Gratis Starbucks",
    comercio: "Starbucks Coffee",
    descuento: 100,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2025-02-01",
    fechaFin: "2025-02-28",
    estado: "programada" as const,
    cuponesGenerados: 0,
    cuponesUsados: 0,
    categoria: "Alimentación",
    descripcion: "Café grande gratis en tu cumpleaños",
    codigoPromo: "BIRTHDAY",
    limiteUsos: 1000,
    condiciones: "Válido solo el día de tu cumpleaños, presentar identificación"
  },
  {
    id: "6",
    titulo: "30% Zara Nueva Colección",
    comercio: "Zara México",
    descuento: 30,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2025-01-10",
    fechaFin: "2025-01-31",
    estado: "activa" as const,
    cuponesGenerados: 200,
    cuponesUsados: 156,
    categoria: "Moda",
    descripcion: "Descuento en nueva colección primavera-verano",
    codigoPromo: "ZARA30SS",
    limiteUsos: 300,
    condiciones: "Aplica solo en artículos de nueva colección"
  },
  {
    id: "7",
    titulo: "MSI Gaming Week",
    comercio: "Elektra",
    descuento: 15,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2025-01-20",
    fechaFin: "2025-01-27",
    estado: "activa" as const,
    cuponesGenerados: 75,
    cuponesUsados: 23,
    categoria: "Electrónicos",
    descripcion: "Descuento especial en laptops gaming MSI",
    codigoPromo: "MSIGAME15",
    limiteUsos: 100,
    condiciones: "Válido solo en laptops MSI, mínimo de compra $15,000"
  },
  {
    id: "8",
    titulo: "Spa Day Relax",
    comercio: "Spa Relax",
    descuento: 1200,
    tipoDescuento: "fijo" as const,
    fechaInicio: "2025-01-01",
    fechaFin: "2025-06-30",
    estado: "activa" as const,
    cuponesGenerados: 30,
    cuponesUsados: 8,
    categoria: "Bienestar",
    descripcion: "$1,200 de descuento en paquete spa completo",
    codigoPromo: "RELAX1200",
    limiteUsos: 50,
    condiciones: "Válido de lunes a viernes, previa cita"
  },
  {
    id: "9",
    titulo: "Descuento Medicamentos",
    comercio: "Farmacia Guadalajara",
    descuento: 20,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2025-01-01",
    fechaFin: "2025-12-31",
    estado: "activa" as const,
    cuponesGenerados: 400,
    cuponesUsados: 267,
    categoria: "Salud",
    descripcion: "Descuento permanente en medicamentos genéricos",
    codigoPromo: "SALUD20",
    limiteUsos: 1000,
    condiciones: "Válido solo en medicamentos genéricos, presentar receta"
  },
  {
    id: "10",
    titulo: "Weekend Palacio VIP",
    comercio: "Palacio de Hierro",
    descuento: 25,
    tipoDescuento: "porcentaje" as const,
    fechaInicio: "2025-01-25",
    fechaFin: "2025-01-26",
    estado: "programada" as const,
    cuponesGenerados: 0,
    cuponesUsados: 0,
    categoria: "Departamental",
    descripcion: "Weekend VIP con descuentos exclusivos",
    codigoPromo: "VIPWEEK25",
    limiteUsos: 200,
    condiciones: "Solo para clientes VIP, compra mínima $3,000"
  }
];

type Promocion = {
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
};

export default function PromocionesPage() {
  const [promociones, setPromociones] = useState<Promocion[]>(mockPromociones);
  const [searchTerm, setSearchTerm] = useState("");
  const [filtroEstado, setFiltroEstado] = useState<string>("todas");
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [selectedPromocion, setSelectedPromocion] = useState<Promocion | undefined>();
  const [error, setError] = useState<string | null>(null);

  // Convert API promotion to admin format
  const convertApiPromotionForAdmin = (apiPromotion: any): Promocion => {
    let discount = 0;
    if (apiPromotion.promotionString) {
      const percentMatch = apiPromotion.promotionString.match(/(\d+)%/);
      const offMatch = apiPromotion.promotionString.match(/(\d+)% OFF/);
      if (percentMatch || offMatch) {
        discount = parseInt(percentMatch?.[1] || offMatch?.[1] || '0');
      } else if (apiPromotion.promotionString.includes('2x1')) {
        discount = 50;
      }
    }

    return {
      id: apiPromotion.promotionId?.toString() || '',
      titulo: apiPromotion.title || '',
      comercio: `Negocio ${apiPromotion.collaboratorId || 'Local'}`,
      descuento: discount,
      tipoDescuento: 'porcentaje' as const,
      fechaInicio: apiPromotion.initialDate || '',
      fechaFin: apiPromotion.endDate || '',
      estado: apiPromotion.promotionState === 'activa' ? 'activa' as const : 'pausada' as const,
      cuponesGenerados: apiPromotion.totalStock || 0,
      cuponesUsados: (apiPromotion.totalStock || 0) - (apiPromotion.availableStock || 0),
      categoria: apiPromotion.categories?.[0]?.name || 'General',
      descripcion: apiPromotion.description || '',
      codigoPromo: apiPromotion.promotionString || '',
      limiteUsos: apiPromotion.totalStock || 0,
      condiciones: `Límite por usuario: ${apiPromotion.limitPerUser || 1}. Límite diario: ${apiPromotion.dailyLimitPerUser || 1}`
    };
  };

  // Load promotions from API
  const loadPromotions = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      console.log('🔄 Loading promotions for admin...');
      const apiPromotions = await apiService.getPromotions();
      console.log('📦 Raw API promotions for admin:', apiPromotions);

      const convertedPromotions = apiPromotions.map(convertApiPromotionForAdmin);
      setPromociones(convertedPromotions);
      console.log('✅ Loaded promotions for admin:', convertedPromotions.length);

    } catch (error) {
      console.error('❌ Error loading promotions:', error);
      setError('Error al cargar las promociones. Mostrando datos de ejemplo.');
      // Keep mock data as fallback
      setPromociones(mockPromociones);
    } finally {
      setLoading(false);
    }
  }, []);

  // Load promotions on component mount
  useEffect(() => {
    loadPromotions();
  }, [loadPromotions]);

  // Filtrar promociones basado en búsqueda y estado
  const filteredPromociones = promociones.filter(promocion => {
    const matchesSearch = 
      promocion.titulo.toLowerCase().includes(searchTerm.toLowerCase()) ||
      promocion.comercio.toLowerCase().includes(searchTerm.toLowerCase()) ||
      promocion.categoria.toLowerCase().includes(searchTerm.toLowerCase()) ||
      promocion.codigoPromo.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesEstado = filtroEstado === "todas" || promocion.estado === filtroEstado;
    
    return matchesSearch && matchesEstado;
  });

  // Usar hook de paginación
  const pagination = usePagination({
    data: filteredPromociones,
    itemsPerPage: 10
  });

  const handleNewPromocion = () => {
    setModalMode('create');
    setSelectedPromocion(undefined);
    setModalOpen(true);
  };

  const handleEditPromocion = (promocionId: string) => {
    const promocion = promociones.find(p => p.id === promocionId);
    if (promocion) {
      setModalMode('edit');
      setSelectedPromocion(promocion);
      setModalOpen(true);
    }
  };

  const handleDeletePromocion = (promocionId: string) => {
    if (window.confirm('¿Estás seguro de que quieres eliminar esta promoción?')) {
      setPromociones(prev => prev.filter(promocion => promocion.id !== promocionId));
      console.log("Promoción eliminada:", promocionId);
    }
  };

  const handleToggleEstado = (promocionId: string) => {
    setPromociones(prev => prev.map(promocion => {
      if (promocion.id === promocionId) {
        const newEstado = promocion.estado === 'activa' ? 'pausada' : 'activa';
        return { ...promocion, estado: newEstado };
      }
      return promocion;
    }));
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleSavePromocion = (promocionData: any) => {
    if (modalMode === 'create') {
      const newPromocion: Promocion = {
        ...promocionData,
        id: String(Date.now()), // ID temporal
        cuponesGenerados: promocionData.cuponesGenerados ?? 0,
        cuponesUsados: promocionData.cuponesUsados ?? 0,
        estado: promocionData.estado ?? 'programada'
      };
      setPromociones(prev => [...prev, newPromocion]);
      console.log("Promoción creada:", newPromocion);
    } else if (modalMode === 'edit' && promocionData.id) {
      setPromociones(prev => prev.map(promocion => 
        promocion.id === promocionData.id ? { 
          ...promocion, 
          ...promocionData, 
          id: promocionData.id,
          cuponesGenerados: promocionData.cuponesGenerados ?? promocion.cuponesGenerados,
          cuponesUsados: promocionData.cuponesUsados ?? promocion.cuponesUsados
        } : promocion
      ));
      console.log("Promoción actualizada:", promocionData);
    }
  };

  const handleExport = () => {
    console.log("Exportar promociones");
    const csvContent = "data:text/csv;charset=utf-8," 
      + "ID,Título,Comercio,Descuento,Tipo,Código,Estado,Cupones Generados,Cupones Usados,Fecha Inicio,Fecha Fin\n"
      + filteredPromociones.map(promo => 
          `${promo.id},${promo.titulo},${promo.comercio},${promo.descuento},${promo.tipoDescuento},${promo.codigoPromo},${promo.estado},${promo.cuponesGenerados},${promo.cuponesUsados},${promo.fechaInicio},${promo.fechaFin}`
        ).join("\n");
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "promociones.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleRefresh = async () => {
    setLoading(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      console.log("Datos actualizados");
    } catch (error) {
      console.error("Error actualizando datos:", error);
    } finally {
      setLoading(false);
    }
  };

  const actions = (
    <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-3 sm:space-y-0 sm:space-x-3">
      <div className="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-3">
        <SearchInput
          placeholder="Buscar por título, comercio, código..."
          value={searchTerm}
          onChange={setSearchTerm}
          className="w-full sm:w-80"
        />
        <select
          value={filtroEstado}
          onChange={(e) => setFiltroEstado(e.target.value)}
          className="px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] text-sm"
        >
          <option value="todas">Todos los estados</option>
          <option value="activa">Activas</option>
          <option value="pausada">Pausadas</option>
          <option value="programada">Programadas</option>
          <option value="expirada">Expiradas</option>
        </select>
      </div>
      <div className="flex items-center space-x-3">
        <RefreshButton onClick={handleRefresh} loading={loading} />
        <ExportButton onClick={handleExport} />
        <ActionButton
          variant="primary"
          icon={<TagIcon className="h-4 w-4" />}
          onClick={handleNewPromocion}
        >
          Nueva Promoción
        </ActionButton>
      </div>
    </div>
  );

  return (
    <>
      <DashboardLayout 
        title="Gestión de Promociones" 
        subtitle="Administra promociones y cupones de descuento"
        actions={actions}
      >
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <PromocionesTable 
            promociones={pagination.currentData}
            onEdit={handleEditPromocion}
            onDelete={handleDeletePromocion}
            onToggleEstado={handleToggleEstado}
          />
          
          <Pagination
            currentPage={pagination.currentPage}
            totalPages={pagination.totalPages}
            totalItems={pagination.totalItems}
            startIndex={pagination.startIndex}
            endIndex={pagination.endIndex}
            onPageChange={pagination.goToPage}
            hasNextPage={pagination.hasNextPage}
            hasPreviousPage={pagination.hasPreviousPage}
          />
        </div>
      </DashboardLayout>

      <PromocionModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSave={handleSavePromocion}
        promocion={selectedPromocion}
        mode={modalMode}
      />
    </>
  );
}