// src/app/admin/layout.tsx
"use client";

import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { data: session, status } = useSession();
  const router = useRouter();

  useEffect(() => {
    if (status === "loading") return; // Aún cargando

    if (!session) {
      // No hay sesión, redirigir al login
      router.push("/login");
      return;
    }

    // Verificar que la sesión sea válida
    const checkSession = async () => {
      try {
        const { isAuthenticated } = await import("../../shared/lib/cognito");
        const amplifyAuth = await isAuthenticated();
        
        if (!amplifyAuth) {
          console.log("🔄 Sesión de NextAuth válida pero no hay sesión de Amplify");
          router.push("/login");
        }
      } catch (error) {
        console.error("Error verificando sesión de Amplify:", error);
        router.push("/login");
      }
    };

    checkSession();
  }, [session, status, router]);

  // Mostrar loading mientras verifica
  if (status === "loading") {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="flex items-center space-x-2">
          <svg className="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span className="text-gray-600">Verificando autenticación...</span>
        </div>
      </div>
    );
  }

  // No mostrar nada si no hay sesión (ya se redirigió)
  if (!session) {
    return null;
  }

  return <>{children}</>;
}