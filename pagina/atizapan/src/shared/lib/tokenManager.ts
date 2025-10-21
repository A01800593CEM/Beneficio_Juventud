// src/lib/tokenManager.ts
"use client";

import { getSession, signOut } from "next-auth/react";

interface TokenInfo {
  isValid: boolean;
  expiresAt?: number;
  timeUntilExpiry?: number;
}

// Cache del último check para evitar llamadas excesivas
let lastTokenCheck: { timestamp: number; result: TokenInfo } | null = null;
const TOKEN_CHECK_CACHE_DURATION = 30 * 1000; // 30 segundos

/**
 * Verifica si el token actual es válido (con cache)
 */
export async function checkTokenValidity(): Promise<TokenInfo> {
  const now = Date.now();
  
  // Usar cache si está disponible y es reciente
  if (lastTokenCheck && (now - lastTokenCheck.timestamp) < TOKEN_CHECK_CACHE_DURATION) {
    return lastTokenCheck.result;
  }

  try {
    const session = await getSession();
    
    if (!session) {
      const result = { isValid: false };
      lastTokenCheck = { timestamp: now, result };
      return result;
    }

    const expiresAt = (session as { expiresAt?: number })?.expiresAt;
    
    if (!expiresAt) {
      const result = { isValid: true };
      lastTokenCheck = { timestamp: now, result };
      return result;
    }

    const currentTimeSeconds = Math.floor(now / 1000);
    const timeUntilExpiry = expiresAt - currentTimeSeconds;
    
    const result = {
      isValid: timeUntilExpiry > 0,
      expiresAt,
      timeUntilExpiry: Math.max(0, timeUntilExpiry)
    };
    
    lastTokenCheck = { timestamp: now, result };
    return result;
    
  } catch (error) {
    console.error("Error checking token validity:", error);
    const result = { isValid: false };
    lastTokenCheck = { timestamp: now, result };
    return result;
  }
}

/**
 * Limpia las sesiones cuando el token expira
 */
export async function handleTokenExpiration() {
  console.log("🔄 Token expirado, limpiando sesiones...");
  
  // Limpiar cache
  lastTokenCheck = null;
  
  try {
    // Limpiar storage local
    if (typeof window !== 'undefined') {
      localStorage.clear();
      sessionStorage.clear();
    }

    // Limpiar sesión de Amplify
    try {
      const { cognitoLogout } = await import("./cognito");
      await cognitoLogout();
      console.log("✅ Sesión de Cognito limpiada");
    } catch (error) {
      console.warn("⚠️ Error limpiando sesión de Cognito:", error);
    }

    // Limpiar sesión de NextAuth
    await signOut({ 
      callbackUrl: "/login",
      redirect: true 
    });

  } catch (error) {
    console.error("❌ Error durante limpieza de token expirado:", error);
    // Fallback: redirección forzada
    if (typeof window !== 'undefined') {
      window.location.href = "/login";
    }
  }
}

/**
 * Programa UNA SOLA verificación exacta cuando expire el token
 */
export function scheduleTokenCheck(): () => void {
  let timeoutId: NodeJS.Timeout | null = null;
  
  const scheduleExactExpiration = async () => {
    // Limpiar timeout anterior si existe
    if (timeoutId) {
      clearTimeout(timeoutId);
      timeoutId = null;
    }
    
    const tokenInfo = await checkTokenValidity();
    
    if (!tokenInfo.isValid) {
      console.log("🔐 Token ya expirado, limpiando...");
      await handleTokenExpiration();
      return;
    }

    if (tokenInfo.timeUntilExpiry && tokenInfo.timeUntilExpiry > 0) {
      // Programar limpieza exacta 30 segundos antes de que expire
      const cleanupTime = Math.max(0, (tokenInfo.timeUntilExpiry - 30) * 1000);
      
      console.log(`🔐 Token expira en ${tokenInfo.timeUntilExpiry} segundos. Limpieza programada en ${Math.floor(cleanupTime / 1000)} segundos.`);
      
      timeoutId = setTimeout(async () => {
        console.log("⏰ Ejecutando limpieza programada...");
        await handleTokenExpiration();
      }, cleanupTime);
    }
  };

  // Programar verificación inicial
  scheduleExactExpiration();

  // Función para limpiar
  return () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
      timeoutId = null;
    }
  };
}