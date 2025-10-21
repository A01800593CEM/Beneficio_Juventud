import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";

export async function fetchFromApi(path: string) {
  const session = await getServerSession(authOptions);
  const token = (session as { accessToken?: string })?.accessToken;
  const base = 'https://beneficiojoven.lat';
  const res = await fetch(`${base}${path}`, {
    headers: { Authorization: token ? `Bearer ${token}` : "" },
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`API error: ${res.status}`);
  return res.json();
}
