// src/app/providers.tsx
"use client";

import { SessionProvider } from "next-auth/react";
import AuthGuard from "./components/AuthGuard";

interface ProvidersProps {
  children: React.ReactNode;
}

export default function Providers({ children }: ProvidersProps) {
  return (
    <SessionProvider
      // Configuración optimizada para SessionProvider
      refetchInterval={0} // NO refrescar automáticamente
      refetchOnWindowFocus={false} // NO refrescar al hacer focus
      refetchWhenOffline={false} // NO refrescar cuando esté offline
    >
      <AuthGuard>
        {children}
      </AuthGuard>
    </SessionProvider>
  );
}