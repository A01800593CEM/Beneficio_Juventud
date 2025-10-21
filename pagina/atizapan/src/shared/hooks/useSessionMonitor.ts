// src/hooks/useSessionMonitor.ts
"use client";

import { useEffect, useRef } from "react";
import { useSession } from "next-auth/react";
import { scheduleTokenCheck } from "@/lib/tokenManager";

/**
 * Hook optimizado que monitorea la sesión SIN verificaciones constantes
 */
export function useSessionMonitor() {
  const { data: session, status } = useSession();
  const cleanupRef = useRef<(() => void) | null>(null);
  const isMonitoringRef = useRef(false);

  useEffect(() => {
    // Solo iniciar monitoreo si hay sesión Y no está ya monitoreando
    if (status === "authenticated" && session && !isMonitoringRef.current) {
      console.log("🔐 Iniciando monitoreo único de sesión...");
      
      // Limpiar monitoreo previo si existe
      if (cleanupRef.current) {
        cleanupRef.current();
      }
      
      // Iniciar nuevo monitoreo
      cleanupRef.current = scheduleTokenCheck();
      isMonitoringRef.current = true;
      
    } else if (status === "unauthenticated" && isMonitoringRef.current) {
      // Limpiar monitoreo si no hay sesión
      console.log("🔐 Deteniendo monitoreo de sesión...");
      if (cleanupRef.current) {
        cleanupRef.current();
        cleanupRef.current = null;
      }
      isMonitoringRef.current = false;
    }

    // Cleanup cuando el componente se desmonte
    return () => {
      if (cleanupRef.current && isMonitoringRef.current) {
        cleanupRef.current();
        isMonitoringRef.current = false;
      }
    };
  }, [session, status]); // Solo reaccionar a cambios de sesión

  // Cleanup en unmount/reload (ejecutar solo una vez)
  useEffect(() => {
    const handleBeforeUnload = () => {
      if (cleanupRef.current) {
        cleanupRef.current();
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, []); // Array vacío = solo una vez

  return {
    isMonitoring: isMonitoringRef.current,
    session,
    status
  };
}