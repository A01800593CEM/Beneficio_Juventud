"use client";

import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { resetPasswordWithCode, resendForgotPasswordCode } from "../../lib/cognito";
import { ArrowLeftIcon, EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";

function ResetPasswordContent() {
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [resendMessage, setResendMessage] = useState("");

  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const emailFromQuery = searchParams.get('email');
    if (emailFromQuery) {
      setEmail(emailFromQuery);
    } else {
      // Si no hay email en la query, redirigir a forgot-password
      router.push('/forgot-password');
    }
  }, [searchParams, router]);

  const validatePassword = (password: string) => {
    const minLength = password.length >= 8;
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    
    return minLength && hasUpper && hasLower && hasNumber;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!code) {
      setError("Por favor ingresa el código de verificación");
      return;
    }
    
    if (!newPassword) {
      setError("Por favor ingresa una nueva contraseña");
      return;
    }
    
    if (!validatePassword(newPassword)) {
      setError("La contraseña debe tener al menos 8 caracteres con mayúsculas, minúsculas y números");
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError("Las contraseñas no coinciden");
      return;
    }

    setLoading(true);
    setError("");

    try {
      await resetPasswordWithCode(email, code, newPassword);
      setSuccess(true);
      
      // Redirigir al login después de 3 segundos
      setTimeout(() => {
        router.push('/login');
      }, 3000);
      
    } catch (err: unknown) {
      console.error("Error restableciendo contraseña:", err);
      const error = err as { code?: string; name?: string; message?: string };

      const code = error?.code || error?.name || "";
      const message = error?.message || "";
      
      if (/CodeMismatchException|CODE_MISMATCH/i.test(code + message)) {
        setError("Código de verificación incorrecto");
      } else if (/ExpiredCodeException|EXPIRED_CODE/i.test(code + message)) {
        setError("El código ha expirado. Solicita uno nuevo.");
      } else if (/InvalidPasswordException/i.test(code + message)) {
        setError("La contraseña no cumple con los requisitos de seguridad");
      } else if (/LimitExceededException|LIMIT_EXCEEDED/i.test(code + message)) {
        setError("Has excedido el límite de intentos. Espera antes de volver a intentar.");
      } else {
        setError("Error al restablecer la contraseña. Inténtalo de nuevo.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    setResendLoading(true);
    setResendMessage("");
    setError("");

    try {
      await resendForgotPasswordCode(email);
      setResendMessage("Código reenviado exitosamente");
      
      // Limpiar mensaje después de 5 segundos
      setTimeout(() => {
        setResendMessage("");
      }, 5000);
      
    } catch {
      console.error("Error reenviando código");
      setError("Error al reenviar el código. Inténtalo de nuevo.");
    } finally {
      setResendLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center p-6 bg-gradient-to-br from-blue-50 via-white to-cyan-50">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-3xl shadow-xl p-8 border border-gray-100">
            <div className="text-center mb-6">
              <div className="w-16 h-16 mx-auto mb-4 bg-green-100 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              </div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">¡Contraseña Restablecida!</h1>
              <p className="text-gray-600">
                Tu contraseña ha sido cambiada exitosamente.
              </p>
            </div>
            
            <div className="text-center">
              <p className="text-sm text-gray-500 mb-6">
                Serás redirigido al login automáticamente...
              </p>
              
              <button
                onClick={() => router.push('/login')}
                className="bg-gradient-to-r from-[#4B4C7E] to-[#008D96] hover:scale-98 text-white font-medium py-2 px-6 rounded-xl transition-all duration-200"
              >
                Iniciar sesión ahora
              </button>
            </div>
          </div>
          
          <div className="text-center mt-8">
            <p className="text-sm text-gray-400">
              2025 Beneficio Joven. Todos los derechos reservados.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-gradient-to-br from-blue-50 via-white to-cyan-50">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-3xl shadow-xl p-8 border border-gray-100">
          {/* Back Button */}
          <button
            onClick={() => router.push('/forgot-password')}
            className="flex items-center text-gray-500 hover:text-gray-700 mb-6 transition-colors"
          >
            <ArrowLeftIcon className="h-5 w-5 mr-2" />
            Regresar
          </button>

          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="text-2xl font-bold bg-gradient-to-r from-[#4B4C7E] to-[#008D96] bg-clip-text text-transparent mb-2">
              Restablecer Contraseña
            </h1>
            <p className="text-gray-600 text-sm">
              Ingresa el código enviado a: <span className="font-medium text-[#008D96]">{email}</span>
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Message */}
            {error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-xl">
                <p className="text-red-600 text-sm text-center">{error}</p>
              </div>
            )}

            {/* Success Message for Resend */}
            {resendMessage && (
              <div className="p-4 bg-green-50 border border-green-200 rounded-xl">
                <p className="text-green-600 text-sm text-center">{resendMessage}</p>
              </div>
            )}

            {/* Verification Code */}
            <div className="space-y-2">
              <label htmlFor="code" className="block text-sm font-medium text-gray-500">
                Código de Verificación
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <input
                  id="code"
                  name="code"
                  type="text"
                  required
                  value={code}
                  onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white text-center text-lg tracking-widest"
                  placeholder="123456"
                  maxLength={6}
                  disabled={loading}
                />
              </div>
              <div className="flex justify-between items-center">
                <p className="text-xs text-gray-500">Código de 6 dígitos</p>
                <button
                  type="button"
                  onClick={handleResendCode}
                  disabled={resendLoading}
                  className="text-xs text-[#008D96] hover:text-[#00494E] font-medium disabled:opacity-50"
                >
                  {resendLoading ? "Reenviando..." : "Reenviar código"}
                </button>
              </div>
            </div>

            {/* New Password */}
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
                  type={showPassword ? "text" : "password"}
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full pl-12 pr-12 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                  placeholder="Nueva contraseña"
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

            {/* Confirm Password */}
            <div className="space-y-2">
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-500">
                Confirmar Contraseña
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full pl-12 pr-12 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                  placeholder="Confirmar contraseña"
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute inset-y-0 right-0 pr-4 flex items-center"
                >
                  {showConfirmPassword ? (
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

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading || !code || !newPassword || !confirmPassword}
              className={`w-full py-3 px-4 rounded-xl font-medium text-white transition-all duration-200 ${
                loading || !code || !newPassword || !confirmPassword
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
                  Restableciendo...
                </div>
              ) : (
                "Restablecer Contraseña"
              )}
            </button>
          </form>

          {/* Login Link */}
          <div className="mt-8 text-center">
            <p className="text-sm text-gray-500">
              ¿Recordaste tu contraseña?{" "}
              <button
                onClick={() => router.push("/login")}
                className="text-[#008D96] hover:text-[#00494E] font-medium"
              >
                Inicia sesión
              </button>
            </p>
          </div>
        </div>

        <div className="text-center mt-8">
          <p className="text-sm text-gray-400">
            2025 Beneficio Joven. Todos los derechos reservados.
          </p>
        </div>
      </div>
    </div>
  );
}

export default function ResetPassword() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <ResetPasswordContent />
    </Suspense>
  );
}