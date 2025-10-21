// src/app/admin/comercios/page.tsx
"use client";

import { useState } from "react";
import DashboardLayout from "../../../features/admin/components/DashboardLayout";
import { NewCommerceButton, ExportButton, RefreshButton } from "../../../features/admin/components/ActionButtons";
import SearchInput from "../../../features/admin/components/SearchInput";
import ComerciosGrid from "../../../features/admin/components/ComerciosGrid";
import ComercioModal from "../../../features/admin/components/ComercioModal";
import Pagination from "../../../features/admin/components/Pagination";
import { usePagination } from "../../../shared/hooks/usePagination";

// Datos de ejemplo para los comercios
const mockComercios = [
  {
    id: "1",
    name: "Six flags",
    promocionesActivas: 3,
    promedioMes: 236,
    categoria: "Entretenimiento",
    telefono: "5555555555",
    email: "contacto@sixflags.com",
    direccion: "Carretera Picacho-Ajusco Km 1.5",
    descripcion: "Parque de diversiones con las mejores montañas rusas"
  },
  {
    id: "2", 
    name: "La Europea",
    promocionesActivas: 5,
    promedioMes: 450,
    categoria: "Alimentación",
    telefono: "5555555556",
    email: "info@laeuropea.com",
    direccion: "Av. Revolución 1234",
    descripcion: "Tienda de vinos y licores premium"
  },
  {
    id: "3",
    name: "Cinépolis VIP",
    promocionesActivas: 2,
    promedioMes: 320,
    categoria: "Entretenimiento",
    telefono: "5555555557",
    email: "vip@cinepolis.com",
    direccion: "Centro Comercial Santa Fe",
    descripcion: "Experiencia cinematográfica premium"
  },
  {
    id: "4",
    name: "Restaurante Pujol",
    promocionesActivas: 1,
    promedioMes: 180,
    categoria: "Restaurantes",
    telefono: "5555555558",
    email: "reservas@pujol.com.mx",
    direccion: "Tennyson 133, Polanco",
    descripcion: "Alta cocina mexicana contemporánea"
  },
  {
    id: "5",
    name: "Nike Store",
    promocionesActivas: 4,
    promedioMes: 520,
    categoria: "Deportes",
    telefono: "5555555559",
    email: "mexico@nike.com",
    direccion: "Av. Presidente Masaryk 393",
    descripcion: "Ropa y calzado deportivo de alta calidad"
  },
  {
    id: "6",
    name: "Starbucks Coffee",
    promocionesActivas: 6,
    promedioMes: 680,
    categoria: "Alimentación",
    telefono: "5555555560",
    email: "mexico@starbucks.com",
    direccion: "Múltiples ubicaciones",
    descripcion: "Café de especialidad y bebidas artesanales"
  },
  {
    id: "7",
    name: "Zara México",
    promocionesActivas: 3,
    promedioMes: 390,
    categoria: "Moda",
    telefono: "5555555561",
    email: "atencion@zara.mx",
    direccion: "Av. Santa Fe 482",
    descripcion: "Moda y tendencias internacionales"
  },
  {
    id: "8",
    name: "Palacio de Hierro",
    promocionesActivas: 8,
    promedioMes: 750,
    categoria: "Departamental",
    telefono: "5555555562",
    email: "info@elpalaciodehierro.com",
    direccion: "Av. Moliere 222",
    descripcion: "Centro comercial de lujo y alta gama"
  },
  {
    id: "9",
    name: "GamePlanet",
    promocionesActivas: 2,
    promedioMes: 280,
    categoria: "Entretenimiento",
    telefono: "5555555563",
    email: "soporte@gameplanet.com",
    direccion: "Centro Comercial Perisur",
    descripcion: "Videojuegos y entretenimiento digital"
  },
  {
    id: "10",
    name: "Spa Relax",
    promocionesActivas: 1,
    promedioMes: 95,
    categoria: "Bienestar",
    telefono: "5555555564",
    email: "reservas@sparelax.mx",
    direccion: "Zona Rosa, CDMX",
    descripcion: "Tratamientos de relajación y bienestar"
  },
  {
    id: "11",
    name: "Elektra",
    promocionesActivas: 7,
    promedioMes: 620,
    categoria: "Electrónicos",
    telefono: "5555555565",
    email: "atencion@elektra.com.mx",
    direccion: "Av. Insurgentes Sur 1234",
    descripcion: "Electrodomésticos y productos electrónicos"
  },
  {
    id: "12",
    name: "Farmacia Guadalajara",
    promocionesActivas: 4,
    promedioMes: 340,
    categoria: "Salud",
    telefono: "5555555566",
    email: "info@farmaciasguadalajara.com",
    direccion: "Múltiples sucursales",
    descripcion: "Farmacia y productos de salud"
  }
];

type Comercio = typeof mockComercios[0];

export default function ComerciosPage() {
  const [comercios, setComercios] = useState<Comercio[]>(mockComercios);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [selectedComercio, setSelectedComercio] = useState<Comercio | undefined>();

  // Filtrar comercios basado en búsqueda
  const filteredComercios = comercios.filter(comercio => 
    comercio.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    comercio.categoria.toLowerCase().includes(searchTerm.toLowerCase()) ||
    comercio.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Usar hook de paginación - 8 comercios por página para mejor visualización en grid
  const pagination = usePagination({
    data: filteredComercios,
    itemsPerPage: 8
  });

  const handleNewComercio = () => {
    setModalMode('create');
    setSelectedComercio(undefined);
    setModalOpen(true);
  };

  const handleEditComercio = (comercioId: string) => {
    const comercio = comercios.find(c => c.id === comercioId);
    if (comercio) {
      setModalMode('edit');
      setSelectedComercio(comercio);
      setModalOpen(true);
    }
  };

  const handleDeleteComercio = (comercioId: string) => {
    if (window.confirm('¿Estás seguro de que quieres eliminar este comercio?')) {
      setComercios(prev => prev.filter(comercio => comercio.id !== comercioId));
      console.log("Comercio eliminado:", comercioId);
    }
  };

  const handleSaveComercio = (comercioData: Omit<Comercio, 'id'> & { id?: string }) => {
    if (modalMode === 'create') {
      const newComercio: Comercio = {
        ...comercioData,
        id: String(Date.now()), // ID temporal
        promocionesActivas: 0,
        promedioMes: 0
      };
      setComercios(prev => [...prev, newComercio]);
      console.log("Comercio creado:", newComercio);
    } else if (modalMode === 'edit' && comercioData.id) {
      setComercios(prev => prev.map(comercio => 
        comercio.id === comercioData.id ? { ...comercio, ...comercioData } : comercio
      ));
      console.log("Comercio actualizado:", comercioData);
    }
  };

  const handleExport = () => {
    console.log("Exportar comercios");
    const csvContent = "data:text/csv;charset=utf-8," 
      + "ID,Nombre,Categoría,Email,Teléfono,Promociones Activas,Promedio Mes\n"
      + filteredComercios.map(comercio => 
          `${comercio.id},${comercio.name},${comercio.categoria},${comercio.email},${comercio.telefono},${comercio.promocionesActivas},${comercio.promedioMes}`
        ).join("\n");
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "comercios.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleRefresh = async () => {
    setLoading(true);
    try {
      // Aquí harías la llamada a la API para refrescar datos
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulación
      console.log("Datos actualizados");
    } catch (error) {
      console.error("Error actualizando datos:", error);
    } finally {
      setLoading(false);
    }
  };

  const actions = (
    <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-3 sm:space-y-0 sm:space-x-3">
      <SearchInput
        placeholder="Buscar por nombre, categoría, email..."
        value={searchTerm}
        onChange={setSearchTerm}
        className="w-full sm:w-80"
      />
      <div className="flex items-center space-x-3">
        <RefreshButton onClick={handleRefresh} loading={loading} />
        <ExportButton onClick={handleExport} />
        <NewCommerceButton onClick={handleNewComercio} />
      </div>
    </div>
  );

  return (
    <>
      <DashboardLayout 
        title="Gestión de Comercios" 
        subtitle="Administra la información de cada comercio"
        actions={actions}
      >
        <div className="space-y-6">
          <ComerciosGrid 
            comercios={pagination.currentData}
            onEdit={handleEditComercio}
            onDelete={handleDeleteComercio}
          />
          
          {pagination.totalPages > 1 && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-100">
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
          )}
        </div>
      </DashboardLayout>

      <ComercioModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSave={handleSaveComercio}
        comercio={selectedComercio}
        mode={modalMode}
      />
    </>
  );
}