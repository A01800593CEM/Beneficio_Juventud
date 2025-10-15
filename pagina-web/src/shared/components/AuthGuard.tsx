
// 3. src/app/components/AuthGuard.tsx
"use client";

import { useEffect } from "react";
import { useSession, signOut } from "next-auth/react";
import { usePathname } from "next/navigation";
import { useSessionMonitor } from "../hooks/useSessionMonitor";
import { Session } from "next-auth";

interface ExtendedSession extends Session {
  expiresAt?: number;
}

interface AuthGuardProps {
  children: React.ReactNode;
}

export default function AuthGuard({ children }: AuthGuardProps) {
  const { data: session, status } = useSession();
  const pathname = usePathname();
  
  useSessionMonitor();

  useEffect(() => {
    const authPages = ['/login', '/register', '/forgot-password', '/reset-password', '/confirm-email'];
    const isAuthPage = authPages.some(page => pathname.startsWith(page));
    
    if (isAuthPage || status === "loading") {
      return;
    }

    const performSingleAuthCheck = async () => {
      if (!session) {
        return;
      }

      try {
        const expiresAt = (session as ExtendedSession)?.expiresAt;
        if (expiresAt) {
          const now = Math.floor(Date.now() / 1000);
          if (now >= expiresAt) {
            console.log("🔄 Token ya expirado en AuthGuard, limpiando...");
            
            const { handleTokenExpiration } = await import("../lib/tokenManager");
            await handleTokenExpiration();
            return;
          }
        }

        const { isAuthenticated, getCurrentUser } = await import("../lib/cognito");
        
        const amplifyAuth = await isAuthenticated();
        const amplifyUser = await getCurrentUser();
        
        if (!amplifyAuth || !amplifyUser) {
          console.log("🔄 Sesión de NextAuth activa pero no hay sesión de Amplify");
          await signOut({ 
            callbackUrl: "/login",
            redirect: false 
          });
          return;
        }
        
      } catch (error) {
        console.error("❌ Error en verificación única de autenticación:", error);
      }
    };

    const timeoutId = setTimeout(performSingleAuthCheck, 100);
    
    return () => clearTimeout(timeoutId);
  }, [session, status, pathname]);

  return <>{children}</>;
}
