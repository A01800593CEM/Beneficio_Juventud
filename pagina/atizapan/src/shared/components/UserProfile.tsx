
// 4. src/app/components/UserProfile.tsx
"use client";

import { Session } from "next-auth";

interface ExtendedSession extends Session {
  cognitoUsername?: string;
  expiresAt?: number;
}

interface UserProfileProps {
  session: Session;
}

export default function UserProfile({ session }: UserProfileProps) {
  const userInfo = session.user;
  const extendedSession = session as ExtendedSession;
  const cognitoUsername = extendedSession.cognitoUsername;
  const expiresAt = extendedSession.expiresAt;

  const formatExpirationDate = (timestamp: number) => {
    return new Date(timestamp * 1000).toLocaleString("es-MX", {
      timeZone: "America/Mexico_City",
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center">
              <span className="text-blue-600 font-medium text-lg">
                {userInfo?.name?.charAt(0) || userInfo?.email?.charAt(0) || "U"}
              </span>
            </div>
          </div>
          <div className="ml-4">
            <h3 className="text-lg leading-6 font-medium text-gray-900">
              Bienvenido, {userInfo?.name || userInfo?.email || "Usuario"}
            </h3>
            <p className="text-sm text-gray-500">
              Usuario ID: {cognitoUsername || "N/A"}
            </p>
            {expiresAt && (
              <p className="text-sm text-gray-500">
                Sesión expira: {formatExpirationDate(expiresAt)}
              </p>
            )}
          </div>
        </div>

        <div className="mt-6 grid grid-cols-1 gap-5 sm:grid-cols-2">
          <div className="bg-gray-50 px-4 py-5 sm:p-6 rounded-md">
            <dt className="text-sm font-medium text-gray-500">Email</dt>
            <dd className="mt-1 text-sm text-gray-900">
              {userInfo?.email || "No disponible"}
            </dd>
          </div>
          <div className="bg-gray-50 px-4 py-5 sm:p-6 rounded-md">
            <dt className="text-sm font-medium text-gray-500">Estado</dt>
            <dd className="mt-1 text-sm text-green-600 font-medium">
              Autenticado
            </dd>
          </div>
        </div>
      </div>
    </div>
  );
}