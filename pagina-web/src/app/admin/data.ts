
// 1. src/app/admin/data.ts
import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";
import { Session } from "next-auth";

interface ExtendedSession extends Session {
  accessToken?: string;
}

export async function fetchFromApi(path: string) {
  const session = await getServerSession(authOptions);
  const token = (session as ExtendedSession)?.accessToken;
  const base = process.env.NEXT_PUBLIC_API_BASE_URL!;
  const res = await fetch(`${base}${path}`, {
    headers: {
      Authorization: token ? `Bearer ${token}` : "",
    },
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`API error: ${res.status}`);
  return res.json();
}
