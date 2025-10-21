// src/app/components/users/UsersTable.tsx
"use client";

import { PencilIcon, TrashIcon } from "@heroicons/react/24/outline";

interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  cuponesUsados: number;
  totalAhorrado: number;
}

interface UsersTableProps {
  users: User[];
  onEdit: (userId: string) => void;
  onDelete: (userId: string) => void;
}

export default function UsersTable({ users, onEdit, onDelete }: UsersTableProps) {
  const formatCurrency = (amount: number) => {
    return `${amount}`;
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-full">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-3 sm:px-2 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              ID
            </th>
            <th className="px-3 sm:px-2 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              NOMBRE
            </th>
            <th className="hidden sm:table-cell px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              CORREO
            </th>
            <th className="hidden md:table-cell px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              TELÉFONO
            </th>
            <th className="px-3 sm:px-2 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              CUPONES
            </th>
            <th className="hidden lg:table-cell px-6 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              TOTAL AHORRADO
            </th>
            <th className="px-3 sm:px-2 py-3 text-left text-xs font-medium text-[#969696] uppercase tracking-wider">
              ACCIONES
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {users.length === 0 ? (
            <tr>
              <td colSpan={7} className="px-6 py-12 text-center text-[#969696]">
                <div className="flex flex-col items-center">
                  <svg className="w-12 h-12 text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
                  </svg>
                  <p className="text-lg font-medium text-gray-900">No se encontraron usuarios</p>
                  <p className="text-sm text-gray-500">Intenta con diferentes criterios de búsqueda</p>
                </div>
              </td>
            </tr>
          ) : (
            users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors duration-150">
                <td className="px-3 sm:px-2 py-4 whitespace-nowrap text-sm font-medium text-[#4B4C7E]">
                  {user.id}
                </td>
                <td className="px-3 sm:px-2 py-4">
                  <div className="text-sm font-medium text-[#015463]">
                    {user.name}
                  </div>
                  {/* En móvil, mostrar email debajo del nombre */}
                  <div className="sm:hidden text-xs text-[#969696] mt-1">
                    {user.email}
                  </div>
                  {/* En tablets, mostrar teléfono debajo del nombre */}
                  <div className="md:hidden sm:block text-xs text-[#969696] mt-1">
                    {user.phone}
                  </div>
                </td>
                <td className="hidden sm:table-cell px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-[#969696]">
                    {user.email}
                  </div>
                </td>
                <td className="hidden md:table-cell px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-[#969696]">
                    {user.phone}
                  </div>
                </td>
                <td className="px-3 sm:px-2 py-4 whitespace-nowrap">
                  <div className="text-sm text-[#4B4C7E] pl-7">
                    {user.cuponesUsados}
                  </div>
                  {/* En móvil, mostrar total ahorrado debajo */}
                  <div className="lg:hidden text-xs text-[#008D96] text-center mt-1">
                     {formatCurrency(user.totalAhorrado)}
                  </div>
                </td>
                <td className="hidden lg:table-cell px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-[#008D96]">
                    ${formatCurrency(user.totalAhorrado)}
                  </div>
                </td>
                <td className="px-3 sm:px-2 py-4 whitespace-nowrap text-sm font-medium">
                  <div className="flex space-x-2 sm:space-x-3">
                    <button
                      onClick={() => onEdit(user.id)}
                      className="text-[#008D96] hover:text-[#00565B] transition-colors duration-200 p-1"
                      title="Editar usuario"
                    >
                      <PencilIcon className="h-4 w-4 sm:h-5 sm:w-5" />
                    </button>
                    <button
                      onClick={() => onDelete(user.id)}
                      className="text-red-500 hover:text-red-700 transition-colors duration-200 p-1"
                      title="Eliminar usuario"
                    >
                      <TrashIcon className="h-4 w-4 sm:h-5 sm:w-5" />
                    </button>
                  </div>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}