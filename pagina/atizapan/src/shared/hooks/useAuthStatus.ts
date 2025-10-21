// src/hooks/useAuthStatus.ts
"use client";

import { useSession } from "next-auth/react";
import { useState, useEffect } from "react";

export function useAuthStatus() {
  const { data: session, status: nextAuthStatus } = useSession();
  const [amplifyAuth, setAmplifyAuth] = useState<boolean | null>(null);
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const checkAmplifyAuth = async () => {
      // Solo ejecutar en el cliente
      if (typeof window === 'undefined') {
        setIsChecking(false);
        return;
      }

      try {
        const { isAuthenticated, getCurrentUser } = await import("@/lib/cognito");
        const isAuth = await isAuthenticated();
        const user = await getCurrentUser();
        setAmplifyAuth(isAuth && !!user);
      } catch {
        console.log("No hay sesión de Amplify activa");
        setAmplifyAuth(false);
      } finally {
        setIsChecking(false);
      }
    };

    if (nextAuthStatus !== "loading") {
      checkAmplifyAuth();
    }
  }, [nextAuthStatus]);

  const isAuthenticated = !!session && amplifyAuth === true;
  const isLoading = nextAuthStatus === "loading" || isChecking;

  return {
    isAuthenticated,
    isLoading,
    session,
    nextAuthStatus,
    amplifyAuth,
    hasNextAuthSession: !!session,
    hasAmplifySession: amplifyAuth === true,
  };
}