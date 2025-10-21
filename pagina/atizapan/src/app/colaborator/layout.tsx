"use client";

import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { ColaboratorLayout } from "@/features/colaborator";

interface ColaboratorAppLayoutProps {
  children: React.ReactNode;
}

export default function ColaboratorAppLayout({ children }: ColaboratorAppLayoutProps) {
  const { data: session, status } = useSession();
  const router = useRouter();

  useEffect(() => {
    if (status === "loading") return;

    if (!session) {
      router.push("/login");
      return;
    }

    const profile = (session as { profile?: string }).profile;

    if (profile === "admin") {
      router.push("/admin");
      return;
    }

    if (!profile || (profile !== "colaborator" && profile !== "collaborator")) {
      router.push("/usuario");
      return;
    }
  }, [session, status, router]);

  if (status === "loading") {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#008D96] mx-auto"></div>
          <p className="mt-4 text-gray-600">Cargando panel del colaborador...</p>
        </div>
      </div>
    );
  }

  if (!session) {
    return null;
  }

  return (
    <ColaboratorLayout variant="sidebar">
      {children}
    </ColaboratorLayout>
  );
}