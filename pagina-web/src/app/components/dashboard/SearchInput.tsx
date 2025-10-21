// src/components/dashboard/SearchInput.tsx
"use client";

import { useState } from "react";
import { MagnifyingGlassIcon } from "@heroicons/react/24/outline";

interface SearchInputProps {
  placeholder?: string;
  value?: string;
  onChange?: (value: string) => void;
  onSearch?: (value: string) => void;
  className?: string;
}

export default function SearchInput({ 
  placeholder = "Buscar...", 
  value, 
  onChange, 
  onSearch,
  className = ""
}: SearchInputProps) {
  const [searchValue, setSearchValue] = useState(value || "");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setSearchValue(newValue);
    onChange?.(newValue);
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      onSearch?.(searchValue);
    }
  };

  return (
    <div className={`relative  ${className}`}>
      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        <MagnifyingGlassIcon className="h-5 w-5 text-[#969696]" />
      </div>
      <input
        type="text"
        value={searchValue}
        onChange={handleChange}
        onKeyPress={handleKeyPress}
        className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] focus:border-transparent transition-all duration-200 text-[#4B4C7E] placeholder-[#969696]"
        placeholder={placeholder}
      />
    </div>
  );
}