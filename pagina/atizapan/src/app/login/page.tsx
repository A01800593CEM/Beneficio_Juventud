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
import "../../styles/auth-design-system.css";

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
    <div className="auth-container">
      <div className="auth-card">
          {/* Logo */}
          <div className="auth-logo">
            <div className="w-12 h-12 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] rounded-xl flex items-center justify-center">
              <span className="text-white font-black text-xl">BJ</span>
            </div>
          </div>

          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="auth-title">
              {needsNewPassword ? "Nueva Contraseña Requerida" : "Inicia Sesión"}
            </h1>
            <p className="auth-subtitle">
              {needsNewPassword ? "Crea una nueva contraseña segura" : "Accede a tu cuenta de Beneficio Joven"}
            </p>
          </div>

          {!needsNewPassword ? (
            <form onSubmit={handleSubmitLogin} className="space-y-6">
              {/* Error Message */}
              {error && (
                <div className="auth-error">
                  <p className="auth-error-text">{error}</p>
                </div>
              )}

              {/* Email Field */}
              <div className="auth-field">
                <label htmlFor="email" className="auth-field-label">
                  Correo Electrónico
                </label>
                <div className="auth-field-container">
                  <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                  </svg>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    required
                    value={credentials.email}
                    onChange={handleInputChange}
                    className="auth-input"
                    placeholder="tu@email.com"
                    disabled={loading}
                  />
                </div>
              </div>

              {/* Password Field */}
              <div className="auth-field">
                <label htmlFor="password" className="auth-field-label">
                  Contraseña
                </label>
                <div className="auth-field-container">
                  <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? "text" : "password"}
                    required
                    value={credentials.password}
                    onChange={handleInputChange}
                    className="auth-input auth-input-with-action"
                    placeholder="••••••••••••"
                    disabled={loading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="auth-field-action"
                  >
                    {showPassword ? (
                      <EyeSlashIcon className="w-5 h-5" />
                    ) : (
                      <EyeIcon className="w-5 h-5" />
                    )}
                  </button>
                </div>
              </div>

              {/* Remember Me & Forgot Password */}
              <div className="flex items-center justify-between mb-6">
                <label className="auth-checkbox">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                  />
                  <span className="auth-checkbox-label">Recuérdame</span>
                </label>
                <button
                  type="button"
                  onClick={() => router.push('/forgot-password')}
                  className="auth-link text-sm"
                >
                  ¿Olvidaste tu contraseña?
                </button>
              </div>

              {/* Login Button */}
              <button
                type="submit"
                disabled={loading}
                className="auth-button-primary"
              >
                {loading ? (
                  <div className="auth-loading">
                    <svg className="w-5 h-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Iniciando sesión...
                  </div>
                ) : (
                  "Inicia Sesión"
                )}
              </button>
            </form>
          ) : (
            <form onSubmit={handleSubmitNewPassword} className="space-y-6">
              {error && (
                <div className="auth-error">
                  <p className="auth-error-text">{error}</p>
                </div>
              )}
              
              <div className="auth-field">
                <label htmlFor="newPassword" className="auth-field-label">
                  Nueva Contraseña
                </label>
                <div className="auth-field-container">
                  <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                  <input
                    id="newPassword"
                    name="newPassword"
                    type={showNewPassword ? "text" : "password"}
                    required
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="auth-input auth-input-with-action"
                    placeholder="Nueva contraseña"
                    disabled={loading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPassword(!showNewPassword)}
                    className="auth-field-action"
                  >
                    {showNewPassword ? (
                      <EyeSlashIcon className="w-5 h-5" />
                    ) : (
                      <EyeIcon className="w-5 h-5" />
                    )}
                  </button>
                </div>
                <p className="text-xs text-gray-500 mt-2">
                  Mínimo 8 caracteres con mayúsculas, minúsculas y números
                </p>
              </div>
              
              <button
                type="submit"
                disabled={loading}
                className="auth-button-primary"
              >
                {loading ? (
                  <div className="auth-loading">
                    <svg className="w-5 h-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Guardando...
                  </div>
                ) : (
                  "Guardar y Continuar"
                )}
              </button>
            </form>
          )}

          {/* Alternative Login Options */}
          {!needsNewPassword && (
            <div className="mt-8 space-y-4">

              <div className="text-center">
                <p className="text-sm text-gray-500">
                  ¿No tienes cuenta?{" "}
                  <button
                    onClick={() => router.push("/register")}
                    className="auth-link"
                  >
                    Regístrate aquí
                  </button>
                </p>
              </div>
            </div>
          )}
        </div>

      </div>
  );
}