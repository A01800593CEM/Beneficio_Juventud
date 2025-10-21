
// 7. src/app/components/dashboard/Sidebar.tsx
"use client";

import { useRouter, usePathname } from "next/navigation";
import { useSession } from "next-auth/react";
import { clearAllSessions } from "@/lib/authUtils";
import {
  HomeIcon,
  UsersIcon,
  BuildingStorefrontIcon,
  TagIcon,
  XMarkIcon,
} from "@heroicons/react/24/outline";


interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

interface MenuItem {
  name: string;
  href: string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  current?: boolean;
}

export default function Sidebar({ isOpen, onClose }: SidebarProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { data: session } = useSession();

  const menuItems: MenuItem[] = [
    {
      name: "Dashboard",
      href: "/admin",
      icon: HomeIcon,
      current: pathname === "/admin",
    },
    {
      name: "Usuarios",
      href: "/admin/usuarios",
      icon: UsersIcon,
      current: pathname === "/admin/usuarios",
    },
    {
      name: "Comercios",
      href: "/admin/comercios",
      icon: BuildingStorefrontIcon,
      current: pathname === "/admin/comercios",
    },
    {
      name: "Promociones",
      href: "/admin/promociones",
      icon: TagIcon,
      current: pathname === "/admin/promociones",
    },
  ];

  const handleLogout = async () => {
    try {
      await clearAllSessions("/login");
    } catch (error) {
      console.error("Error during logout:", error);
      router.push("/login");
    }
  };

  const handleNavigation = (href: string) => {
    router.push(href);
    onClose();
  };

  const userName = session?.user?.name || session?.user?.email || "Usuario";

  return (
    <>
      {isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      <div
        className={`
          fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out
          lg:translate-x-0 lg:static lg:inset-0
          ${isOpen ? "translate-x-0" : "-translate-x-full"}
        `}
      >
        <div className="flex flex-col h-full">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <div className="flex items-center">
              <div className="w-8 h-8 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">BJ</span>
              </div>
              <span className="ml-3 text-lg font-bold text-from-[#4B4C7E] to-[#008D96]">
                Beneficio Joven
              </span>
            </div>
            <button
              onClick={onClose}
              className="lg:hidden p-1 rounded-md hover:bg-gray-100"
            >
              <XMarkIcon className="h-6 w-6 text-gray-500" />
            </button>
          </div>

          <nav className="flex-1 px-4 py-6 space-y-2">
            <div className="text-xs font-semibold text-[#969696] uppercase tracking-wide mb-4">
              Páginas
            </div>
            {menuItems.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.name}
                  onClick={() => handleNavigation(item.href)}
                  className={`
                    w-full flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-colors duration-200
                    ${
                      item.current
                        ? "bg-gradient-to-r from-[#00C0CC]/10 to-[#008D96]/10 text-[#015463] border-r-2 border-[#008D96]"
                        : "text-[#969696] hover:text-[#4B4C7E] hover:bg-gray-50"
                    }
                  `}
                >
                  <Icon
                    className={`mr-3 h-5 w-5 ${
                      item.current ? "text-[#008D96]" : "text-current"
                    }`}
                  />
                  {item.name}
                </button>
              );
            })}
          </nav>

          <div className="border-t border-gray-200 p-4">
            <div className="flex items-center mb-4">
              <div className="w-10 h-10 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] rounded-full flex items-center justify-center">
                <span className="text-white font-medium text-sm">
                  {userName.charAt(0).toUpperCase()}
                </span>
              </div>
              <div className="ml-3 flex-1 min-w-0">
                <p className="text-sm font-medium text-[#4B4C7E] truncate">
                  {userName}
                </p>
                <p className="text-xs text-[#969696]">Directora General</p>
              </div>
            </div>
            
            <button
              onClick={handleLogout}
              className="w-full flex items-center px-3 py-2 text-sm text-[#969696] hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors duration-200"
            >
              <svg
                className="mr-3 h-4 w-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                />
              </svg>
              Cerrar sesión
            </button>
          </div>
        </div>
      </div>
    </>
  );
}