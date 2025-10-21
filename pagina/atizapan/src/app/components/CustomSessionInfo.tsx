// 2. ARREGLAR: src/app/components/CustomSessionInfo.tsx
"use client";

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { cognitoLogout } from '@/lib/cognito';

interface CognitoTokens {
  accessToken: string;
  idToken: string;
  refreshToken: string;
  expiresAt: number;
}

interface UserInfo {
  name?: string;
  email?: string;
  sub?: string;
  [key: string]: unknown;
}

export default function CustomSessionInfo() {
  const [tokens, setTokens] = useState<CognitoTokens | null>(null);
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const checkCustomSession = useCallback(() => {
    try {
      const storedTokens = localStorage.getItem('cognito_tokens');
      console.log('Tokens almacenados:', storedTokens); // Debug
      
      if (!storedTokens) {
        console.log('No hay tokens, redirigiendo a login');
        router.push('/login');
        return;
      }

      const parsedTokens: CognitoTokens = JSON.parse(storedTokens);
      console.log('Tokens parseados:', parsedTokens); // Debug
      
      // Verificar si el token no ha expirado
      const currentTime = Math.floor(Date.now() / 1000);
      console.log('Tiempo actual:', currentTime, 'Expira en:', parsedTokens.expiresAt); // Debug
      
      if (currentTime >= parsedTokens.expiresAt) {
        console.log('Token expirado, limpiando y redirigiendo');
        localStorage.removeItem('cognito_tokens');
        router.push('/login');
        return;
      }

      setTokens(parsedTokens);
      
      // Decodificar el ID token para obtener información del usuario
      try {
        const base64Url = parsedTokens.idToken.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split('')
            .map(function(c) {
              return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            })
            .join('')
        );

        const decodedUserInfo = JSON.parse(jsonPayload);
        console.log('Info del usuario decodificada:', decodedUserInfo); // Debug
        setUserInfo(decodedUserInfo);
      } catch (error) {
        console.error('Error decodificando ID token:', error);
        setError('Error al decodificar información del usuario');
      }
      
      setLoading(false);
    } catch (error) {
      console.error('Error verificando sesión custom:', error);
      setError('Error verificando sesión');
      setLoading(false);
      // No redirigir inmediatamente para mostrar el error
      setTimeout(() => {
        router.push('/login');
      }, 3000);
    }
  }, [router]);

  useEffect(() => {
    checkCustomSession();
  }, [checkCustomSession]);

  const handleLogout = async () => {
    try {
      await cognitoLogout();
      localStorage.removeItem('cognito_tokens');
      router.push('/');
    } catch (error) {
      console.error('Error en logout:', error);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Verificando sesión...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 mb-4">
            <svg className="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <p className="text-red-600 font-medium">{error}</p>
          <p className="text-gray-500 text-sm mt-2">Redirigiendo al login...</p>
        </div>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <h1 className="text-3xl font-bold text-gray-900">
              Panel de Administración
              <span className="text-sm text-blue-600 ml-2">(Custom Login)</span>
            </h1>
            <button
              onClick={handleLogout}
              className="px-4 py-2 rounded-md font-medium text-white bg-red-600 hover:bg-red-700 transition-colors duration-200"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Perfil de usuario */}
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
                    Usuario ID: {userInfo?.sub || "N/A"}
                  </p>
                  {tokens && (
                    <p className="text-sm text-gray-500">
                      Sesión expira: {new Date(tokens.expiresAt * 1000).toLocaleString("es-MX", {
                        timeZone: "America/Mexico_City",
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
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
                    Autenticado (Custom)
                  </dd>
                </div>
              </div>
            </div>
          </div>

          {/* Información de tokens */}
          <div className="mt-8 bg-white overflow-hidden shadow rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900">
                Información de Sesión (Custom Login)
              </h3>
              <div className="mt-4">
                <div className="grid grid-cols-1 gap-4">
                  <div>
                    <h4 className="text-sm font-medium text-gray-700">Información del Usuario</h4>
                    <pre className="mt-2 bg-gray-100 p-4 rounded-md overflow-auto text-sm">
                      {JSON.stringify(userInfo, null, 2)}
                    </pre>
                  </div>
                  <div>
                    <h4 className="text-sm font-medium text-gray-700">Tokens (primeros caracteres)</h4>
                    <pre className="mt-2 bg-gray-100 p-4 rounded-md overflow-auto text-sm">
{`Access Token: ${tokens?.accessToken.substring(0, 50)}...
ID Token: ${tokens?.idToken.substring(0, 50)}...
Refresh Token: ${tokens?.refreshToken.substring(0, 50)}...
Expira: ${tokens?.expiresAt ? new Date(tokens.expiresAt * 1000).toISOString() : 'N/A'}`}
                    </pre>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
