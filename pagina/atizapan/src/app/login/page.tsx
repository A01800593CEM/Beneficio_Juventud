"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  cognitoLogin,
  completeNewPassword,
  getSessionTokens,
  type LoginCredentials,
} from "../../lib/cognito";
import { signIn, useSession } from "next-auth/react";
import { EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";
import { getProfileBasedRedirect } from "../../lib/profile-redirect";

export default function CustomLogin() {
  const [credentials, setCredentials] = useState<LoginCredentials>({
    email: "",
    password: ""
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [needsNewPassword, setNeedsNewPassword] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  const router = useRouter();
  const { data: _session } = useSession();

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCredentials((prev) => ({ ...prev, [name]: value }));
  };

  const bridgeToNextAuth = async (idToken: string) => {
    try {
      const result = await signIn("credentials", {
        idToken,
        redirect: false,
      });

      if (result?.error) {
        throw new Error(`Error en signIn: ${result.error}`);
      }

      if (result?.ok) {
        await new Promise(resolve => setTimeout(resolve, 100));

        // Obtener la nueva sesión para acceder al profile
        const { getSession } = await import("next-auth/react");
        const session = await getSession();

        // Redirigir basado en el profile
        const redirectUrl = getProfileBasedRedirect((session as { profile?: string })?.profile);
        console.log("🔄 Redirecting based on profile:", (session as { profile?: string })?.profile, "to:", redirectUrl);

        router.push(redirectUrl);
        return;
      }

      throw new Error("Respuesta inesperada del signIn");

    } catch (error) {
      console.error("❌ Error en el proceso de signIn:", error);
      throw error;
    }
  };

  const handleSubmitLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      await cognitoLogin(credentials);
      const tokens = await getSessionTokens();
      
      if (!tokens.idToken) {
        throw new Error("No se pudo obtener el idToken");
      }
      
      await bridgeToNextAuth(tokens.idToken);
      
    } catch (err: unknown) {
      const error = err as { message?: string; code?: string; name?: string };
      const message = error?.message || "";
      const code = error?.code || error?.name || "";

      if (message === "NEW_PASSWORD_REQUIRED" || code === "NEW_PASSWORD_REQUIRED") {
        setNeedsNewPassword(true);
        setError("");
        return;
      }

      if (/NotAuthorizedException|NOT_AUTHORIZED|Incorrect username or password/i.test(code + message)) {
        setError("Correo electrónico o contraseña incorrectos");
      } else if (/UserNotConfirmedException|USER_NOT_CONFIRMED/i.test(code + message)) {
        setError("Debes confirmar tu email antes de iniciar sesión");
      } else if (/UserNotFoundException|USER_NOT_FOUND/i.test(code + message)) {
        setError("No existe una cuenta con ese correo electrónico");
      } else {
        setError("Error al iniciar sesión. Inténtalo de nuevo.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitNewPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPassword) {
      setError("Ingresa tu nueva contraseña");
      return;
    }
    
    setLoading(true);
    setError("");

    try {
      await completeNewPassword(newPassword);
      const tokens = await getSessionTokens();
      if (!tokens.idToken) {
        throw new Error("No se pudo obtener el idToken después del cambio");
      }
      
      await bridgeToNextAuth(tokens.idToken);
      
    } catch {
      setError("No se pudo completar el cambio de contraseña. Inténtalo de nuevo.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-gradient-to-br from-blue-50 via-white to-cyan-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-3xl shadow-xl p-8 border border-gray-100">
          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="text-3xl font-black bg-gradient-to-r from-[#4B4C7E] to-[#008D96] bg-clip-text text-transparent">
              {needsNewPassword ? "Nueva Contraseña Requerida" : "Inicia Sesión"}
            </h1>
          </div>

          {!needsNewPassword ? (
            <form onSubmit={handleSubmitLogin} className="space-y-6">
              {/* Error Message */}
              {error && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-xl">
                  <p className="text-red-600 text-sm text-center">{error}</p>
                </div>
              )}

              {/* Email Field */}
              <div className="space-y-2">
                <label htmlFor="email" className="block text-sm font-medium text-gray-500">
                  Correo Electrónico
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                    </svg>
                  </div>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    required
                    value={credentials.email}
                    onChange={handleInputChange}
                    className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                    placeholder="tu@email.com"
                    disabled={loading}
                  />
                </div>
              </div>

              {/* Password Field */}
              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-medium text-gray-500">
                  Contraseña
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? "text" : "password"}
                    required
                    value={credentials.password}
                    onChange={handleInputChange}
                    className="w-full pl-12 pr-12 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                    placeholder="••••••••••••"
                    disabled={loading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-4 flex items-center"
                  >
                    {showPassword ? (
                      <EyeSlashIcon className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    ) : (
                      <EyeIcon className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    )}
                  </button>
                </div>
              </div>

              {/* Remember Me & Forgot Password */}
              <div className="flex items-center justify-between">
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="h-4 w-4 text-gray-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <span className="ml-2 text-sm text-gray-500">Recuérdame</span>
                </label>
                <button
                  type="button"
                  onClick={() => router.push('/forgot-password')}
                  className="text-sm text-gray-600 hover:text-gray-500 font-medium"
                >
                  ¿Olvidaste tu contraseña?
                </button>
              </div>

              {/* Login Button */}
              <button
                type="submit"
                disabled={loading}
                className={`w-full py-3 px-4 rounded-xl font-medium text-white transition-all duration-200 ${
                  loading 
                    ? "bg-gray-400 cursor-not-allowed" 
                    : "bg-gradient-to-r from-[#4B4C7E] to-[#008D96] hover:scale-98 active:scale-95 shadow-lg hover:shadow-xl"
                }`}
              >
                {loading ? (
                  <div className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Iniciando sesión...
                  </div>
                ) : (
                  <div className="text-lg font-semibold">
                  Inicia Sesión
                  </div>
                )}
              </button>
            </form>
          ) : (
            <form onSubmit={handleSubmitNewPassword} className="space-y-6">
              {error && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-xl">
                  <p className="text-red-600 text-sm text-center">{error}</p>
                </div>
              )}
              
              <div className="space-y-2">
                <label htmlFor="newPassword" className="block text-sm font-medium text-gray-500">
                  Nueva Contraseña
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input
                    id="newPassword"
                    name="newPassword"
                    type={showNewPassword ? "text" : "password"}
                    required
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="w-full pl-12 pr-12 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                    placeholder="Nueva contraseña"
                    disabled={loading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPassword(!showNewPassword)}
                    className="absolute inset-y-0 right-0 pr-4 flex items-center"
                  >
                    {showNewPassword ? (
                      <EyeSlashIcon className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    ) : (
                      <EyeIcon className="h-5 w-5 text-gray-400 hover:text-gray-600" />
                    )}
                  </button>
                </div>
                <p className="text-xs text-gray-500">
                  Mínimo 8 caracteres con mayúsculas, minúsculas y números
                </p>
              </div>
              
              <button
                type="submit"
                disabled={loading}
                className={`w-full py-3 px-4 rounded-xl font-medium text-white transition-all duration-200 ${
                  loading 
                    ? "bg-gray-400 cursor-not-allowed" 
                    : "bg-gradient-to-r from-blue-600 to-cyan-600 hover:from-blue-700 hover:to-cyan-700 active:scale-95 shadow-lg hover:shadow-xl"
                }`}
              >
                {loading ? "Guardando..." : "Guardar y Continuar"}
              </button>
            </form>
          )}

          {/* Alternative Login Options */}
          {!needsNewPassword && (
            <div className="mt-8 space-y-4">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-200" />
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-4 bg-white text-gray-500">O</span>
                </div>
              </div>

              <button
                onClick={() => signIn("cognito")}
                disabled={loading}
                className="w-full py-3 px-4 border border-gray-200 rounded-xl text-gray-700 hover:bg-gray-50 transition-all duration-200 disabled:opacity-50 font-medium"
              >
                Usar Hosted UI de AWS
              </button>
              
              <div className="text-center">
                <p className="text-sm text-gray-500">
                  ¿No tienes cuenta?{" "}
                  <button
                    onClick={() => router.push("/register")}
                    className="text-[#008D96] hover:text-[#00494E] font-medium"
                  >
                    Regístrate aquí
                  </button>
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="text-center mt-8">
          <p className="text-sm text-gray-400">
            2025 Beneficio Joven. Todos los derechos reservados.
          </p>
        </div>
      </div>
    </div>
  );
}