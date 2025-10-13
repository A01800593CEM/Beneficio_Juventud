// src/lib/authUtils.ts
"use client";

import { signOut, getSession } from "next-auth/react";

/**
 * Limpia completamente todas las sesiones (NextAuth + Amplify)
 * Solo funciona en el cliente
 */
export async function clearAllSessions(redirectUrl = "/login") {
  console.log("🔄 Limpiando todas las sesiones...");
  
  if (typeof window === 'undefined') {
    console.warn("clearAllSessions solo funciona en el cliente");
    return;
  }
  
  try {
    // 1. Logout de Amplify/Cognito
    try {
      const { cognitoLogout } = await import("./cognito");
      await cognitoLogout();
      console.log("✅ Sesión de Cognito limpiada");
    } catch (error) {
      console.warn("⚠️ Error limpiando Cognito (puede ser normal):", error);
    }
    
    // 2. Limpiar almacenamiento local/session
    try {
      localStorage.clear();
      sessionStorage.clear();
      console.log("✅ Storage local limpiado");
    } catch (error) {
      console.warn("⚠️ Error limpiando storage:", error);
    }
    
    // 3. Logout de NextAuth (esto debe ir al final)
    await signOut({ 
      callbackUrl: redirectUrl,
      redirect: true 
    });
    
  } catch (error) {
    console.error("❌ Error durante limpieza completa:", error);
    
    // Fallback: redirección forzada
    window.location.href = redirectUrl;
  }
}

/**
 * Verifica si las sesiones están sincronizadas
 * Solo funciona en el cliente
 */
export async function areSessionsSynced(): Promise<boolean> {
  if (typeof window === 'undefined') {
    return true; // En servidor, asumimos que están sincronizadas
  }
  
  try {
    const nextAuthSession = await getSession();
    
    const { isAuthenticated, getCurrentUser } = await import("./cognito");
    const amplifyAuth = await isAuthenticated();
    const amplifyUser = await getCurrentUser();
    
    const hasNextAuth = !!nextAuthSession;
    const hasAmplify = amplifyAuth && !!amplifyUser;
    
    console.log("🔍 Estado de sesiones:", {
      nextAuth: hasNextAuth,
      amplify: hasAmplify,
      synced: hasNextAuth === hasAmplify
    });
    
    return hasNextAuth === hasAmplify;
  } catch (error) {
    console.error("❌ Error verificando sincronización:", error);
    return false;
  }
}

/**
 * Limpia sesiones no sincronizadas
 * Solo funciona en el cliente
 */
export async function syncSessions() {
  if (typeof window === 'undefined') {
    return;
  }
  
  try {
    const synced = await areSessionsSynced();
    
    if (!synced) {
      console.log("🔄 Sesiones no sincronizadas, limpiando...");
      await clearAllSessions();
    }
  } catch (error) {
    console.error("❌ Error sincronizando sesiones:", error);
  }
}