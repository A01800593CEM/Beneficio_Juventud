"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import Image from "next/image";
import {
  TagIcon,
  ChartBarIcon,
  BuildingStorefrontIcon,
  Bars3Icon,
  XMarkIcon
} from "@heroicons/react/24/outline";

interface ColaboratorLayoutProps {
  children: React.ReactNode;
  title?: string;
  businessName?: string;
  businessLogo?: string;
  actions?: React.ReactNode;
}

export default function ColaboratorLayout({
  children,
  title,
  businessName = "La Bella Italia",
  businessLogo = "",
  actions
}: ColaboratorLayoutProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const pathname = usePathname();

  const navigation = [
    {
      name: "Promociones",
      href: "/colaborator/promociones",
      icon: TagIcon,
      current: pathname?.startsWith("/colaborator/promociones") || pathname === "/colaborator"
    },
    {
      name: "Estadísticas",
      href: "/colaborator/estadisticas",
      icon: ChartBarIcon,
      current: pathname?.startsWith("/colaborator/estadisticas")
    },
    {
      name: "Perfil del Negocio",
      href: "/colaborator/perfil",
      icon: BuildingStorefrontIcon,
      current: pathname?.startsWith("/colaborator/perfil")
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-40">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 justify-between items-center">
            {/* Logo y nombre del negocio */}
            <div className="flex items-center space-x-4">
              <div className="relative h-10 w-10 rounded-full overflow-hidden bg-gray-200">
                <Image
                  src={businessLogo}
                  alt={businessName}
                  fill
                  className="object-cover"
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(businessName)}&background=008D96&color=fff&size=40`;
                  }}
                />
              </div>
              <div>
                <h1 className="text-lg font-semibold text-gray-900">{businessName}</h1>
                <p className="text-sm text-gray-500">Panel de Colaborador</p>
              </div>
            </div>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex space-x-8">
              {navigation.map((item) => {
                const Icon = item.icon;
                return (
                  <Link
                    key={item.name}
                    href={item.href}
                    className={`
                      flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors
                      ${item.current
                        ? "bg-[#008D96] text-white"
                        : "text-gray-700 hover:text-[#008D96] hover:bg-gray-100"
                      }
                    `}
                  >
                    <Icon className="h-5 w-5" />
                    <span>{item.name}</span>
                  </Link>
                );
              })}
            </nav>

            {/* Mobile menu button */}
            <div className="md:hidden">
              <button
                type="button"
                className="text-gray-700 hover:text-gray-900"
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              >
                {mobileMenuOpen ? (
                  <XMarkIcon className="h-6 w-6" />
                ) : (
                  <Bars3Icon className="h-6 w-6" />
                )}
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Navigation */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 bg-white">
            <nav className="px-4 py-2 space-y-1">
              {navigation.map((item) => {
                const Icon = item.icon;
                return (
                  <Link
                    key={item.name}
                    href={item.href}
                    className={`
                      flex items-center space-x-3 px-3 py-2 rounded-md text-base font-medium
                      ${item.current
                        ? "bg-[#008D96] text-white"
                        : "text-gray-700 hover:text-[#008D96] hover:bg-gray-100"
                      }
                    `}
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <Icon className="h-5 w-5" />
                    <span>{item.name}</span>
                  </Link>
                );
              })}
            </nav>
          </div>
        )}
      </header>

      {/* Main Content */}
      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-6">
        {/* Page Title and Actions */}
        {(title || actions) && (
          <div className="mb-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
              {title && (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">{title}</h2>
                </div>
              )}
              {actions && (
                <div className="mt-4 sm:mt-0">
                  {actions}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Page Content */}
        {children}
      </main>
    </div>
  );
}