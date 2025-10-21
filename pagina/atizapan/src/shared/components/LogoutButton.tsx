"use client";

import { signOut } from "next-auth/react";
import { useState } from "react";
import { cognitoLogout } from "../lib/cognito";

export default function LogoutButton() {
  const [loading, setLoading] = useState(false);

  const handleLogout = async () => {
    setLoading(true);
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
    } finally {
      setLoading(false);
    }
  };

  return (
    <button
      onClick={handleLogout}
      disabled={loading}
      className={`
        px-4 py-2 rounded-md font-medium text-white
        transition-colors duration-200
        ${loading 
          ? "bg-gray-400 cursor-not-allowed" 
          : "bg-red-600 hover:bg-red-700"
        }
      `}
    >
      {loading ? (
        <span className="flex items-center">
          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Cerrando sesión...
        </span>
      ) : (
        "Cerrar Sesión"
      )}
    </button>
  );
}