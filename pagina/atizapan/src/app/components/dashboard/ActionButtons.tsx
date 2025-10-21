// src/components/dashboard/ActionButtons.tsx
"use client";

import { 
  PlusIcon, 
  ArrowUpTrayIcon, 
  ArrowPathIcon,
  BuildingStorefrontIcon
} from "@heroicons/react/24/outline";

interface ActionButtonProps {
  variant?: "primary" | "secondary" | "outline" | "tertiary";
  size?: "sm" | "md" | "lg";
  icon?: React.ReactNode;
  children: React.ReactNode;
  onClick?: () => void;
  disabled?: boolean;
  loading?: boolean;
}

export function ActionButton({ 
  variant = "secondary", 
  size = "lg", 
  icon, 
  children, 
  onClick,
  disabled = false,
  loading = false
}: ActionButtonProps) {
  const baseClasses = "inline-flex items-center font-normal rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2";
  
  const variants = {
    primary: "bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white hover:from-[#3d3f6b] hover:to-[#007780] focus:ring-[#008D96] shadow-sm hover:shadow-md",
    secondary: "bg-white text-[#4B4C7E] border border-gray-200 hover:bg-gray-50 hover:border-[#008D96] focus:ring-[#008D96]",
    tertiary: "bg-[#008D96] text-[#FFF] border border-gray-200 hover:text-[#cdc] hover:border-[#008D96] focus:ring-[#008D96]",
    outline: "bg-transparent text-[#969696] border border-gray-200 hover:text-[#4B4C7E] hover:border-[#008D96] focus:ring-[#008D96]"
  };

  const sizes = {
    sm: "px-3 py-1.5 text-sm",
    md: "px-4 py-2 text-sm",
    lg: "px-5 py-2 text-base"
  };

  const iconSizes = {
    sm: "h-5 w-5",
    md: "h-5 w-5", 
    lg: "h-6 w-6"
  };

  return (
    <button
      onClick={onClick}
      disabled={disabled || loading}
      className={`
        ${baseClasses}
        ${variants[variant]}
        ${sizes[size]}
        ${disabled || loading ? "opacity-50 cursor-not-allowed" : ""}
      `}
    >
      {loading ? (
        <svg className={`animate-spin -ml-1 mr-2 ${iconSizes[size]}`} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      ) : icon ? (
        <span className={`mr-2 mt-1 ${iconSizes[size]}`}>{icon}</span>
      ) : null}
      {children}
    </button>
  );
}

// Botones específicos del dashboard
export function NewUserButton({ onClick }: { onClick?: () => void }) {
  return (
    <ActionButton
      variant="tertiary"
      icon={<PlusIcon className="h-5 w-5" />}
      onClick={onClick}
    >
      Nuevo Usuario
    </ActionButton>
  );
}

export function NewCommerceButton({ onClick }: { onClick?: () => void }) {
  return (
    <ActionButton
      variant="tertiary"
      icon={<BuildingStorefrontIcon className="h-4 w-4" />}
      onClick={onClick}
    >
      Nuevo Comercio
    </ActionButton>
  );
}



export function ExportButton({ onClick }: { onClick?: () => void }) {
  return (
    <ActionButton
      variant="outline"
      icon={<ArrowUpTrayIcon className="h-4 w-4" />}
      onClick={onClick}
    >
      Exportar
    </ActionButton>
  );
}

export function RefreshButton({ onClick, loading }: { onClick?: () => void; loading?: boolean }) {
  return (
    <ActionButton
      variant="outline"
      icon={<ArrowPathIcon className="h-4 w-4" />}
      onClick={onClick}
      loading={loading}
    >
      Actualizar
    </ActionButton>
  );
}