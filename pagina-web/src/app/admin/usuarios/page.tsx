// src/app/admin/usuarios/page.tsx
"use client";

import { useState } from "react";
import DashboardLayout from "../../../features/admin/components/DashboardLayout";
import { NewUserButton, ExportButton, RefreshButton } from "../../../features/admin/components/ActionButtons";
import SearchInput from "../../../features/admin/components/SearchInput";
import UsersTable from "../../../features/admin/components/UsersTable";
import UserModal from "../../../features/admin/components/UserModal";
import Pagination from "../../../features/admin/components/Pagination";
import { usePagination } from "../../../shared/hooks/usePagination";

// Datos de ejemplo ampliados para mostrar paginación
const mockUsers = [
  { id: "3927", name: "Juan Rodríguez", email: "JuanRo@gmail.com", phone: "5565378929", cuponesUsados: 9, totalAhorrado: 215 },
  { id: "3928", name: "María García", email: "maria.garcia@gmail.com", phone: "5565378930", cuponesUsados: 15, totalAhorrado: 450 },
  { id: "3929", name: "Carlos López", email: "carlos.lopez@gmail.com", phone: "5565378931", cuponesUsados: 7, totalAhorrado: 189 },
  { id: "3930", name: "Ana Martínez", email: "ana.martinez@gmail.com", phone: "5565378932", cuponesUsados: 12, totalAhorrado: 324 },
  { id: "3931", name: "Luis Hernández", email: "luis.hernandez@gmail.com", phone: "5565378933", cuponesUsados: 6, totalAhorrado: 156 },
  { id: "3932", name: "Sofia Vargas", email: "sofia.vargas@gmail.com", phone: "5565378934", cuponesUsados: 18, totalAhorrado: 567 },
  { id: "3933", name: "Pedro Ramírez", email: "pedro.ramirez@gmail.com", phone: "5565378935", cuponesUsados: 4, totalAhorrado: 89 },
  { id: "3934", name: "Laura Torres", email: "laura.torres@gmail.com", phone: "5565378936", cuponesUsados: 22, totalAhorrado: 689 },
  { id: "3935", name: "Diego Morales", email: "diego.morales@gmail.com", phone: "5565378937", cuponesUsados: 8, totalAhorrado: 234 },
  { id: "3936", name: "Carmen Jiménez", email: "carmen.jimenez@gmail.com", phone: "5565378938", cuponesUsados: 14, totalAhorrado: 398 },
  { id: "3937", name: "Roberto Silva", email: "roberto.silva@gmail.com", phone: "5565378939", cuponesUsados: 11, totalAhorrado: 287 },
  { id: "3938", name: "Patricia Ruiz", email: "patricia.ruiz@gmail.com", phone: "5565378940", cuponesUsados: 16, totalAhorrado: 445 },
  { id: "3939", name: "Fernando Castro", email: "fernando.castro@gmail.com", phone: "5565378941", cuponesUsados: 5, totalAhorrado: 123 },
  { id: "3940", name: "Gabriela Mendoza", email: "gabriela.mendoza@gmail.com", phone: "5565378942", cuponesUsados: 19, totalAhorrado: 578 },
  { id: "3941", name: "Alejandro Ortiz", email: "alejandro.ortiz@gmail.com", phone: "5565378943", cuponesUsados: 13, totalAhorrado: 356 },
  { id: "3942", name: "Valentina Cruz", email: "valentina.cruz@gmail.com", phone: "5565378944", cuponesUsados: 20, totalAhorrado: 612 },
  { id: "3943", name: "Miguel Flores", email: "miguel.flores@gmail.com", phone: "5565378945", cuponesUsados: 7, totalAhorrado: 178 },
  { id: "3944", name: "Isabella Romero", email: "isabella.romero@gmail.com", phone: "5565378946", cuponesUsados: 25, totalAhorrado: 734 },
  { id: "3945", name: "Ricardo Herrera", email: "ricardo.herrera@gmail.com", phone: "5565378947", cuponesUsados: 3, totalAhorrado: 67 },
  { id: "3946", name: "Natalia Peña", email: "natalia.pena@gmail.com", phone: "5565378948", cuponesUsados: 17, totalAhorrado: 489 }
];

type User = typeof mockUsers[0];

export default function UsuariosPage() {
  const [users, setUsers] = useState<User[]>(mockUsers);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [selectedUser, setSelectedUser] = useState<User | undefined>();

  // Filtrar usuarios basado en búsqueda
  const filteredUsers = users.filter(user => 
    user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.phone.includes(searchTerm)
  );

  // Usar hook de paginación
  const pagination = usePagination({
    data: filteredUsers,
    itemsPerPage: 10
  });

  const handleNewUser = () => {
    setModalMode('create');
    setSelectedUser(undefined);
    setModalOpen(true);
  };

  const handleEditUser = (userId: string) => {
    const user = users.find(u => u.id === userId);
    if (user) {
      setModalMode('edit');
      setSelectedUser(user);
      setModalOpen(true);
    }
  };

  const handleDeleteUser = (userId: string) => {
    if (window.confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
      setUsers(prev => prev.filter(user => user.id !== userId));
      console.log("Usuario eliminado:", userId);
    }
  };

  const handleSaveUser = (userData: { id?: string; name: string; email: string; phone: string; cuponesUsados?: number; totalAhorrado?: number }) => {
    if (modalMode === 'create') {
      const newUser: User = {
        ...userData,
        id: String(Date.now()), // ID temporal
        cuponesUsados: 0,
        totalAhorrado: 0
      };
      setUsers(prev => [...prev, newUser]);
      console.log("Usuario creado:", newUser);
    } else if (modalMode === 'edit' && userData.id) {
      setUsers(prev => prev.map(user => 
        user.id === userData.id ? { ...user, ...userData } : user
      ));
      console.log("Usuario actualizado:", userData);
    }
  };

  const handleExport = () => {
    console.log("Exportar usuarios");
    const csvContent = "data:text/csv;charset=utf-8," 
      + "ID,Nombre,Email,Teléfono,Cupones Usados,Total Ahorrado\n"
      + filteredUsers.map(user => 
          `${user.id},${user.name},${user.email},${user.phone},${user.cuponesUsados},${user.totalAhorrado}`
        ).join("\n");
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "usuarios.csv");
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
      
      <div className="flex items-center space-x-3  ">
         <NewUserButton onClick={handleNewUser} />
        <RefreshButton onClick={handleRefresh} loading={loading} />
        <ExportButton onClick={handleExport} />
       
      </div>
    </div>
  );

  return (
    <>
      <DashboardLayout 
        title="Gestión de Usuarios" 
        subtitle="Administra la información de cada usuario"
        actions={actions}
      >
<div className="mb-4 w-full bg-[#efefe] border border-gray-200 rounded-lg">
<SearchInput
        placeholder="Buscar por nombre, teléfono, email..."
        value={searchTerm}
        onChange={setSearchTerm}
        className="w-full p-4"
      /></div>
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            
          <UsersTable 
            users={pagination.currentData}
            onEdit={handleEditUser}
            onDelete={handleDeleteUser}
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

      <UserModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSave={handleSaveUser}
        user={selectedUser}
        mode={modalMode}
      />
    </>
  );
}