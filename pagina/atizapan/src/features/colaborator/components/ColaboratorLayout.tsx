"use client";

import { useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useSession, signOut } from "next-auth/react";
import Image from "next/image";
import { cognitoLogout } from "@/shared/lib/cognito";
import {
  TagIcon,
  ChartBarIcon,
  BuildingStorefrontIcon,
  Bars3Icon,
  XMarkIcon,
  ArrowLeftEndOnRectangleIcon
} from "@heroicons/react/24/outline";

interface ColaboratorLayoutProps {
  children: React.ReactNode;
  title?: string;
  subtitle?: string;
  businessName?: string;
  businessLogo?: string;
  actions?: React.ReactNode;
  variant?: 'header' | 'sidebar';
}

export default function ColaboratorLayout({
  children,
  title,
  subtitle,
  businessName = "La Bella Italia",
  businessLogo = "",
  actions,
  variant = 'header'
}: ColaboratorLayoutProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const pathname = usePathname();
  const router = useRouter();
  const { data: session } = useSession();

  const navigation: Array<{
    name: string;
    href: string;
    icon: typeof TagIcon;
    current: boolean;
    badge?: number;
  }> = [
    {
      name: "Promociones",
      href: "/colaborator",
      icon: TagIcon,
      current: pathname === "/colaborator"
    },
    {
      name: "Estadísticas",
      href: "/colaborator/estadisticas-avanzadas",
      icon: ChartBarIcon,
      current: pathname?.startsWith("/colaborator/estadisticas-avanzadas")
    },
    {
      name: "Perfil del Negocio",
      href: "/colaborator/perfil",
      icon: BuildingStorefrontIcon,
      current: pathname?.startsWith("/colaborator/perfil") && !pathname?.includes("mejorado")
    },
  ];

  const handleLogout = async () => {
    try {
      console.log("🔄 Iniciando logout completo...");

      // 1. Logout de Amplify/Cognito primero
      try {
        await cognitoLogout();
        console.log("✅ Logout de Cognito exitoso");
      } catch (error) {
        console.warn("⚠️ Error en logout de Cognito (puede ser normal):", error);
        // No fallar si Cognito ya no tiene sesión
      }

      // 2. Logout de NextAuth
      await signOut({
        callbackUrl: "/login",
        redirect: true
      });

      console.log("✅ Logout completo exitoso");

    } catch (error) {
      console.error("❌ Error durante logout:", error);

      // Fallback: limpiar almacenamiento local y redirigir
      try {
        // Limpiar cualquier storage local que pueda estar interfiriendo
        localStorage.clear();
        sessionStorage.clear();

        // Forzar logout de NextAuth sin importar errores anteriores
        await signOut({
          callbackUrl: "/login",
          redirect: true
        });
      } catch (fallbackError) {
        console.error("❌ Error en fallback logout:", fallbackError);
        // Último recurso: redirección manual
        window.location.href = "/login";
      }
    }
  };

  const handleNavigation = (href: string) => {
    router.push(href);
    if (variant === 'sidebar') {
      setSidebarOpen(false);
    } else {
      setMobileMenuOpen(false);
    }
  };

  if (variant === 'sidebar') {
    return (
      <div className="h-screen flex bg-gray-50">
        {/* Mobile overlay */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
            onClick={() => setSidebarOpen(false)}
          />
        )}

        {/* Sidebar */}
        <div
          className={`
            fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-xl transform transition-transform duration-300 ease-in-out
            lg:translate-x-0 lg:static lg:inset-0
            ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
          `}
        >
          <div className="flex flex-col h-full">
            {/* Header */}
            <div className="flex items-center justify-between p-6 border-b border-gray-200">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 bg-gradient-to-r from-[#008D96] to-[#00C0CC] rounded-lg flex items-center justify-center">
                  <span className="text-white font-bold text-lg">BJ</span>
                </div>
                <div>
                  <h1 className="text-lg font-bold text-gray-900">Beneficio Joven</h1>
                  <p className="text-sm text-gray-500">Colaborador</p>
                </div>
              </div>
              <button
                onClick={() => setSidebarOpen(false)}
                className="lg:hidden p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
              >
                <XMarkIcon className="h-5 w-5" />
              </button>
            </div>

            {/* Navigation */}
            <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
              <div className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-4">
                Menú Principal
              </div>
              {navigation.map((item) => {
                const Icon = item.icon;
                const active = item.current;

                return (
                  <button
                    key={item.href}
                    onClick={() => handleNavigation(item.href)}
                    className={`
                      w-full flex items-center justify-between px-3 py-3 text-sm font-medium rounded-lg transition-all duration-200
                      ${active
                        ? 'bg-[#008D96] text-white shadow-md'
                        : 'text-gray-700 hover:text-[#008D96] hover:bg-gray-50'
                      }
                    `}
                  >
                    <div className="flex items-center space-x-3">
                      <Icon className={`h-5 w-5 ${active ? 'text-white' : 'text-gray-400'}`} />
                      <span>{item.name}</span>
                    </div>

                    {item.badge && item.badge > 0 && (
                      <span className={`
                        inline-flex items-center justify-center px-2 py-1 text-xs font-bold rounded-full
                        ${active ? 'bg-white text-[#008D96]' : 'bg-red-500 text-white'}
                      `}>
                        {item.badge > 9 ? '9+' : item.badge}
                      </span>
                    )}
                  </button>
                );
              })}
            </nav>

            {/* User section */}
            <div className="border-t border-gray-200 p-4">
              <div className="flex items-center space-x-3 mb-4">
                <div className="w-10 h-10 bg-gradient-to-r from-[#008D96] to-[#00C0CC] rounded-full flex items-center justify-center">
                  <span className="text-white font-medium text-sm">
                    {session?.user?.name?.charAt(0) || session?.user?.email?.charAt(0) || 'U'}
                  </span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    {session?.user?.name || session?.user?.email || 'Usuario'}
                  </p>
                  <p className="text-xs text-gray-500">Colaborador</p>
                </div>
              </div>

              <button
                onClick={handleLogout}
                className="w-full flex items-center space-x-3 px-3 py-2 text-sm text-gray-700 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors duration-200"
              >
                <ArrowLeftEndOnRectangleIcon className="h-4 w-4" />
                <span>Cerrar sesión</span>
              </button>
            </div>
          </div>
        </div>

        {/* Main content */}
        <div className="flex-1 flex flex-col overflow-hidden">
          {/* Top bar */}
          <header className="bg-white shadow-sm border-b border-gray-200 lg:hidden">
            <div className="flex items-center justify-between px-4 py-3">
              <button
                onClick={() => setSidebarOpen(true)}
                className="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
              >
                <Bars3Icon className="h-6 w-6" />
              </button>
              <div className="flex items-center space-x-3">
                <div className="w-8 h-8 bg-gradient-to-r from-[#008D96] to-[#00C0CC] rounded-lg flex items-center justify-center">
                  <span className="text-white font-bold text-sm">BJ</span>
                </div>
                <span className="text-lg font-bold text-gray-900">Beneficio Joven</span>
              </div>
              <div className="w-10" />
            </div>
          </header>

          {/* Page header */}
          {(title || actions) && (
            <div className="bg-white border-b border-gray-200 px-6 py-4">
              <div className="flex items-center justify-between">
                <div>
                  {title && (
                    <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
                  )}
                  {subtitle && (
                    <p className="mt-1 text-sm text-gray-500">{subtitle}</p>
                  )}
                </div>
                {actions && (
                  <div className="flex items-center space-x-3">
                    {actions}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Main content */}
          <main className="flex-1 overflow-y-auto">
            <div className="p-6">
              {children}
            </div>
          </main>
        </div>
      </div>
    );
  }

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
                  <button
                    key={item.name}
                    onClick={() => handleNavigation(item.href)}
                    className={`
                      flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors relative
                      ${item.current
                        ? "bg-[#008D96] text-white"
                        : "text-gray-700 hover:text-[#008D96] hover:bg-gray-100"
                      }
                    `}
                  >
                    <Icon className="h-5 w-5" />
                    <span>{item.name}</span>
                    {item.badge && item.badge > 0 && (
                      <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                        {item.badge > 9 ? '9+' : item.badge}
                      </span>
                    )}
                  </button>
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
                  <button
                    key={item.name}
                    onClick={() => handleNavigation(item.href)}
                    className={`
                      flex items-center space-x-3 px-3 py-2 rounded-md text-base font-medium relative w-full
                      ${item.current
                        ? "bg-[#008D96] text-white"
                        : "text-gray-700 hover:text-[#008D96] hover:bg-gray-100"
                      }
                    `}
                  >
                    <Icon className="h-5 w-5" />
                    <span>{item.name}</span>
                    {item.badge && item.badge > 0 && (
                      <span className="ml-auto bg-red-500 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                        {item.badge > 9 ? '9+' : item.badge}
                      </span>
                    )}
                  </button>
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