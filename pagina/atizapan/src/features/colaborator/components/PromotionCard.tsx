"use client";

import { useState } from "react";
import Image from "next/image";
import {
  PencilIcon,
  TrashIcon,
  EyeIcon,
  TicketIcon,
  CalendarIcon
} from "@heroicons/react/24/outline";

export interface Promotion {
  id: string;
  title: string;
  description: string;
  discount: number;
  discountType: "percentage" | "fixed";
  category: string;
  startDate: string;
  endDate: string;
  status: "active" | "expired" | "scheduled" | "paused";
  image?: string;
  totalViews: number;
  totalRedemptions: number;
  conversionRate: number;
  terms?: string;
}

interface PromotionCardProps {
  promotion: Promotion;
  onEdit: (id: string) => void;
  onDelete: (id: string) => void;
  onClick?: (id: string) => void;
}

export default function PromotionCard({
  promotion,
  onEdit,
  onDelete,
  onClick
}: PromotionCardProps) {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const getStatusConfig = (status: string) => {
    switch (status) {
      case "active":
        return { bg: "bg-green-100", text: "text-green-800", label: "Activa" };
      case "expired":
        return { bg: "bg-red-100", text: "text-red-800", label: "Expirada" };
      case "scheduled":
        return { bg: "bg-blue-100", text: "text-blue-800", label: "Programada" };
      case "paused":
        return { bg: "bg-yellow-100", text: "text-yellow-800", label: "Pausada" };
      default:
        return { bg: "bg-gray-100", text: "text-gray-800", label: "Desconocido" };
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("es-ES", {
      day: "2-digit",
      month: "short",
      year: "numeric"
    });
  };

  const getDiscountText = () => {
    if (promotion.discountType === "percentage") {
      return `${promotion.discount}% OFF`;
    }
    return `$${promotion.discount} OFF`;
  };

  const statusConfig = getStatusConfig(promotion.status);

  const handleDelete = () => {
    if (showDeleteConfirm) {
      onDelete(promotion.id);
      setShowDeleteConfirm(false);
    } else {
      setShowDeleteConfirm(true);
    }
  };

  const handleCardClick = () => {
    if (!showDeleteConfirm && onClick) {
      onClick(promotion.id);
    }
  };

  return (
    <div
      className={`
        bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden
        transition-all duration-200 hover:shadow-md hover:border-[#008D96]
        ${onClick ? "cursor-pointer" : ""}
        ${showDeleteConfirm ? "ring-2 ring-red-500" : ""}
      `}
      onClick={handleCardClick}
    >
      {/* Header with image and discount */}
      <div className="relative h-32 bg-gradient-to-r from-[#008D96] to-[#00C0CC]">
        {promotion.image ? (
          <Image
            src={promotion.image}
            alt={promotion.title}
            fill
            className="object-cover"
          />
        ) : (
          <div className="flex items-center justify-center h-full">
            <div className="text-center text-white">
              <TicketIcon className="h-8 w-8 mx-auto mb-2" />
              <span className="text-2xl font-bold">{getDiscountText()}</span>
            </div>
          </div>
        )}

        {/* Status Badge */}
        <div className="absolute top-3 right-3">
          <span className={`
            inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
            ${statusConfig.bg} ${statusConfig.text}
          `}>
            {statusConfig.label}
          </span>
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        {/* Title and Description */}
        <div className="mb-3">
          <h3 className="text-lg font-semibold text-gray-900 mb-1 line-clamp-1">
            {promotion.title}
          </h3>
          <p className="text-sm text-gray-600 line-clamp-2">
            {promotion.description}
          </p>
        </div>

        {/* Validity Period */}
        <div className="flex items-center text-sm text-gray-500 mb-3">
          <CalendarIcon className="h-4 w-4 mr-1" />
          <span>
            {formatDate(promotion.startDate)} - {formatDate(promotion.endDate)}
          </span>
        </div>

        {/* Metrics */}
        <div className="grid grid-cols-3 gap-4 mb-4">
          <div className="text-center">
            <div className="flex items-center justify-center mb-1">
              <EyeIcon className="h-4 w-4 text-gray-400 mr-1" />
              <span className="text-xs text-gray-500">Vistas</span>
            </div>
            <span className="text-lg font-semibold text-gray-900">
              {promotion.totalViews.toLocaleString()}
            </span>
          </div>
          <div className="text-center">
            <div className="flex items-center justify-center mb-1">
              <TicketIcon className="h-4 w-4 text-gray-400 mr-1" />
              <span className="text-xs text-gray-500">Canjeos</span>
            </div>
            <span className="text-lg font-semibold text-gray-900">
              {promotion.totalRedemptions.toLocaleString()}
            </span>
          </div>
          <div className="text-center">
            <div className="flex items-center justify-center mb-1">
              <span className="text-xs text-gray-500">Conversión</span>
            </div>
            <span className="text-lg font-semibold text-[#008D96]">
              {promotion.conversionRate.toFixed(1)}%
            </span>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center justify-between pt-3 border-t border-gray-100">
          <span className="text-xs text-gray-500 px-2 py-1 bg-gray-100 rounded">
            {promotion.category}
          </span>

          <div className="flex items-center space-x-2">
            <button
              onClick={(e) => {
                e.stopPropagation();
                onEdit(promotion.id);
              }}
              className="p-2 text-gray-400 hover:text-[#008D96] hover:bg-gray-100 rounded-lg transition-colors"
              title="Editar promoción"
            >
              <PencilIcon className="h-4 w-4" />
            </button>

            <button
              onClick={(e) => {
                e.stopPropagation();
                handleDelete();
              }}
              className={`
                p-2 rounded-lg transition-colors
                ${showDeleteConfirm
                  ? "text-white bg-red-500 hover:bg-red-600"
                  : "text-gray-400 hover:text-red-500 hover:bg-gray-100"
                }
              `}
              title={showDeleteConfirm ? "Confirmar eliminación" : "Eliminar promoción"}
            >
              <TrashIcon className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Delete Confirmation */}
        {showDeleteConfirm && (
          <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-800 mb-2">
              ¿Estás seguro de que quieres eliminar esta promoción?
            </p>
            <div className="flex space-x-2">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleDelete();
                }}
                className="px-3 py-1 bg-red-500 text-white text-xs rounded hover:bg-red-600"
              >
                Eliminar
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowDeleteConfirm(false);
                }}
                className="px-3 py-1 bg-gray-200 text-gray-800 text-xs rounded hover:bg-gray-300"
              >
                Cancelar
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}