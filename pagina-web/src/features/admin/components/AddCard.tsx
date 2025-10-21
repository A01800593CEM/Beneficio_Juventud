// src/components/dashboard/AddCard.tsx
"use client";

import { PlusIcon } from "@heroicons/react/24/outline";

interface AddCardProps {
  label?: string;
  onClick?: () => void;
}

export default function AddCard({ label = "Agregar Tarjeta", onClick }: AddCardProps) {
  return (
    <button
      onClick={onClick}
      className="bg-white rounded-xl border-2 border-dashed border-[#969696] p-6 hover:border-[#008D96] hover:bg-gray-50 transition-all duration-200 flex flex-col items-center justify-center max-w-[110px] max-h-[110px] group"
    >
      <div className="w-8 h-8 rounded-full bg-gray-100 group-hover:bg-[#008D96]/10 flex items-center justify-center mb-2 transition-colors duration-200">
        <PlusIcon className="h-5 w-5 text-[#969696] group-hover:text-[#008D96] transition-colors duration-200" />
      </div>
      <span className="text-sm text-[#969696] group-hover:text-[#008D96] transition-colors duration-200">
        {label}
      </span>
    </button>
  );
}