"use client";

import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import PromocionesPage from "../../../features/colaborator/pages/PromocionesPage";

export default function ColaboratorPromocionesRoute() {
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
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!session) {
    return null;
  }

  return <PromocionesPage />;
}