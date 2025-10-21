// src/app/components/dashboard/Pagination.tsx
"use client";

import { ChevronLeftIcon, ChevronRightIcon } from "@heroicons/react/24/outline";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  totalItems: number;
  startIndex: number;
  endIndex: number;
  onPageChange: (page: number) => void;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
}

export default function Pagination({
  currentPage,
  totalPages,
  totalItems,
  startIndex,
  endIndex,
  onPageChange,
  hasNextPage,
  hasPreviousPage
}: PaginationProps) {
  const renderPageNumbers = () => {
    const pages = [];
    const maxVisiblePages = 5;
    
    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    const endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    // Primera página
    if (startPage > 1) {
      pages.push(
        <button
          key={1}
          onClick={() => onPageChange(1)}
          className="px-3 py-1 text-sm text-[#969696] hover:text-[#4B4C7E] transition-colors"
        >
          1
        </button>
      );
      
      if (startPage > 2) {
        pages.push(
          <span key="ellipsis1" className="px-2 text-[#969696]">
            ...
          </span>
        );
      }
    }

    // Páginas del rango visible
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button
          key={i}
          onClick={() => onPageChange(i)}
          className={`px-3 py-1 text-sm rounded transition-colors ${
            i === currentPage
              ? "bg-[#008D96] text-white"
              : "text-[#969696] hover:text-[#4B4C7E] hover:bg-gray-100"
          }`}
        >
          {i}
        </button>
      );
    }

    // Última página
    if (endPage < totalPages) {
      if (endPage < totalPages - 1) {
        pages.push(
          <span key="ellipsis2" className="px-2 text-[#969696]">
            ...
          </span>
        );
      }
      
      pages.push(
        <button
          key={totalPages}
          onClick={() => onPageChange(totalPages)}
          className="px-3 py-1 text-sm text-[#969696] hover:text-[#4B4C7E] transition-colors"
        >
          {totalPages}
        </button>
      );
    }

    return pages;
  };

  if (totalPages <= 1) return null;

  return (
    <div className="bg-white px-6 py-3 border-t border-gray-200 flex flex-col sm:flex-row items-center justify-between space-y-3 sm:space-y-0">
      <div className="text-sm text-[#969696]">
        Mostrando {startIndex} al {endIndex} de {totalItems} usuarios
      </div>
      
      <div className="flex items-center space-x-1">
        {/* Botón Anterior */}
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={!hasPreviousPage}
          className={`flex items-center px-3 py-1 text-sm rounded transition-colors ${
            hasPreviousPage
              ? "text-[#969696] hover:text-[#4B4C7E] hover:bg-gray-100"
              : "text-gray-300 cursor-not-allowed"
          }`}
        >
          <ChevronLeftIcon className="h-4 w-4 mr-1" />
          <span className="hidden sm:inline">Anterior</span>
        </button>

        {/* Números de página */}
        <div className="flex items-center space-x-1">
          {renderPageNumbers()}
        </div>

        {/* Botón Siguiente */}
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={!hasNextPage}
          className={`flex items-center px-3 py-1 text-sm rounded transition-colors ${
            hasNextPage
              ? "text-[#969696] hover:text-[#4B4C7E] hover:bg-gray-100"
              : "text-gray-300 cursor-not-allowed"
          }`}
        >
          <span className="hidden sm:inline">Siguiente</span>
          <ChevronRightIcon className="h-4 w-4 ml-1" />
        </button>
      </div>
    </div>
  );
}