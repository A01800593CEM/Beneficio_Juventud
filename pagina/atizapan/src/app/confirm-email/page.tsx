"use client";

import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { confirmSignUp, resendSignUpCode, cognitoLogin, getCurrentUser, cognitoLogout } from "../../lib/cognito";
import { apiService, UserRegistrationData, CollaboratorRegistrationData } from "../../lib/api";
import { ArrowLeftIcon } from "@heroicons/react/24/outline";
import "../../styles/auth-design-system.css";

function ConfirmEmailContent() {
  const [code, setCode] = useState("");
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [resendMessage, setResendMessage] = useState("");
  const [apiRegistrationStatus, setApiRegistrationStatus] = useState<'pending' | 'loading' | 'success' | 'error'>('pending');
  const [userType, setUserType] = useState<'user' | 'collaborator'>('user');

  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const emailFromQuery = searchParams.get('email');
    if (emailFromQuery) {
      setEmail(emailFromQuery);
    } else {
      // Si no hay email en la query, redirigir a registro
      router.push('/register');
    }

    // Detectar tipo de usuario basado en localStorage
    const tempUserData = localStorage.getItem('tempUserData');
    const tempCollaboratorData = localStorage.getItem('tempCollaboratorData');

    if (tempCollaboratorData) {
      setUserType('collaborator');
    } else if (tempUserData) {
      setUserType('user');
    }
  }, [searchParams, router]);

  const registerInAPI = async (cognitoUserId: string) => {
    console.log(`🔑 Iniciando registro de ${userType} en API con Cognito ID:`, cognitoUserId);
    setApiRegistrationStatus('loading');

    try {
      if (userType === 'user') {
        // Obtener datos temporales del usuario
        const tempUserDataStr = localStorage.getItem('tempUserData');
        if (!tempUserDataStr) {
          throw new Error('No se encontraron datos de usuario temporales');
        }

        const tempUserData = JSON.parse(tempUserDataStr);
        console.log('📦 Datos de usuario recuperados de localStorage:', tempUserData);

        // Preparar datos para la API de usuarios
        const apiData: UserRegistrationData = {
          name: tempUserData.name,
          lastNamePaternal: tempUserData.lastNamePaternal,
          lastNameMaternal: tempUserData.lastNameMaternal,
          birthDate: tempUserData.birthDate,
          phoneNumber: tempUserData.phoneNumber,
          email: tempUserData.email,
          cognitoId: cognitoUserId,
        };

        console.log('🌐 Enviando datos de usuario a la API:', apiData);
        const apiResponse = await apiService.registerUser(apiData);
        console.log('✅ Usuario registrado en API exitosamente:', apiResponse);

        // Limpiar datos temporales
        localStorage.removeItem('tempUserData');
        console.log('🧹 Datos temporales de usuario limpiados');

      } else if (userType === 'collaborator') {
        // Obtener datos temporales del colaborador
        const tempCollaboratorDataStr = localStorage.getItem('tempCollaboratorData');
        if (!tempCollaboratorDataStr) {
          throw new Error('No se encontraron datos de colaborador temporales');
        }

        const tempCollaboratorData = JSON.parse(tempCollaboratorDataStr);
        console.log('📦 Datos de colaborador recuperados de localStorage:', tempCollaboratorData);

        // Preparar datos para la API de colaboradores
        const apiData: CollaboratorRegistrationData = {
          businessName: tempCollaboratorData.businessName,
          cognitoId: cognitoUserId,
          rfc: tempCollaboratorData.rfc,
          representativeName: tempCollaboratorData.representativeName,
          phone: tempCollaboratorData.phone,
          email: tempCollaboratorData.email,
          address: tempCollaboratorData.address,
          postalCode: tempCollaboratorData.postalCode,
          description: tempCollaboratorData.description,
          state: 'activo',
        };

        console.log('🌐 Enviando datos de colaborador a la API:', apiData);
        const apiResponse = await apiService.registerCollaborator(apiData);
        console.log('✅ Colaborador registrado en API exitosamente:', apiResponse);

        // Limpiar datos temporales
        localStorage.removeItem('tempCollaboratorData');
        console.log('🧹 Datos temporales de colaborador limpiados');
      }

      setApiRegistrationStatus('success');
    } catch (error) {
      console.error(`❌ Error registrando ${userType} en API:`, error);
      setApiRegistrationStatus('error');
      // No mostrar error aquí, ya está confirmado en Cognito
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!code) {
      setError("Por favor ingresa el código de verificación");
      return;
    }
    
    if (code.length !== 6) {
      setError("El código debe tener 6 dígitos");
      return;
    }

    setLoading(true);
    setError("");

    try {
      // Confirmar el signup
      console.log('☁️ Confirmando email en Cognito...');
      await confirmSignUp(email, code);
      console.log('✅ Email confirmado exitosamente en Cognito');

      setSuccess(true);

      // Obtener el sub del usuario autenticándose temporalmente
      console.log('🔐 Obteniendo sub del usuario de Cognito...');
      try {
        // Obtener datos temporales incluyendo la contraseña basado en el tipo de usuario
        const tempDataStr = userType === 'user'
          ? localStorage.getItem('tempUserData')
          : localStorage.getItem('tempCollaboratorData');

        if (tempDataStr) {
          const tempData = JSON.parse(tempDataStr);
          console.log('📦 Datos recuperados para login:', { email: tempData.email, userType });

          // Hacer login temporal para obtener el sub
          console.log('🔑 Haciendo login temporal para obtener sub...');
          await cognitoLogin({
            email: tempData.email,
            password: tempData.password
          });

          // Obtener el usuario actual que ahora debería estar autenticado
          const currentUser = await getCurrentUser();
          console.log('👤 Usuario autenticado obtenido:', currentUser);

          if (currentUser && currentUser.userId) {
            console.log('🆔 Sub del usuario:', currentUser.userId);
            await registerInAPI(currentUser.userId);
          } else {
            throw new Error('No se pudo obtener el sub del usuario');
          }
        } else {
          throw new Error(`No se encontraron datos de ${userType} temporales`);
        }
      } catch (userError) {
        console.error(`⚠️ Error obteniendo sub del ${userType}, pero la confirmación fue exitosa:`, userError);
        setApiRegistrationStatus('error');
      }

      // Limpiar la sesión de Cognito antes de redirigir al login
      try {
        console.log('🧹 Limpiando sesión temporal de Cognito...');
        await cognitoLogout();
        console.log('✅ Sesión de Cognito limpiada exitosamente');
      } catch (logoutError) {
        console.warn('⚠️ Error limpiando sesión de Cognito (no crítico):', logoutError);
      }

      // Redirigir al login después de 5 segundos para dar tiempo a la API
      setTimeout(() => {
        router.push('/login');
      }, 5000);
      
    } catch (err: unknown) {
      console.error("Error confirmando registro:", err);
      const error = err as { code?: string; name?: string; message?: string };

      const code = error?.code || error?.name || "";
      const message = error?.message || "";
      
      if (/CodeMismatchException|CODE_MISMATCH/i.test(code + message)) {
        setError("Código de verificación incorrecto");
      } else if (/ExpiredCodeException|EXPIRED_CODE/i.test(code + message)) {
        setError("El código ha expirado. Solicita uno nuevo.");
      } else if (/AliasExistsException/i.test(code + message)) {
        setError("Ya existe una cuenta confirmada con este email");
      } else if (/NotAuthorizedException/i.test(code + message)) {
        setError("Usuario ya confirmado o código inválido");
      } else if (/LimitExceededException|LIMIT_EXCEEDED/i.test(code + message)) {
        setError("Has excedido el límite de intentos. Espera antes de volver a intentar.");
      } else {
        setError("Error al confirmar la cuenta. Inténtalo de nuevo.");
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
      await resendSignUpCode(email);
      setResendMessage("Código reenviado exitosamente");
      
      // Limpiar mensaje después de 5 segundos
      setTimeout(() => {
        setResendMessage("");
      }, 5000);
      
    } catch (err: unknown) {
      console.error("Error reenviando código:", err);
      const error = err as { code?: string; name?: string; message?: string };

      const code = error?.code || error?.name || "";
      const message = error?.message || "";
      
      if (/LimitExceededException|LIMIT_EXCEEDED/i.test(code + message)) {
        setError("Has excedido el límite de reenvíos. Espera antes de volver a intentar.");
      } else if (/InvalidParameterException/i.test(code + message)) {
        setError("Email inválido o usuario no encontrado");
      } else {
        setError("Error al reenviar el código. Inténtalo de nuevo.");
      }
    } finally {
      setResendLoading(false);
    }
  };

  if (success) {
    return (
      <div className="auth-container">
        <div className="auth-card">
            {/* Logo */}
            <div className="auth-logo">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              </div>
            </div>

            <div className="text-center mb-6">
              <h1 className="auth-title text-2xl">
                ¡{userType === 'user' ? 'Usuario' : 'Colaborador'} Registrado!
              </h1>
              <p className="auth-subtitle">
                Tu cuenta de {userType === 'user' ? 'usuario' : 'colaborador'} ha sido activada exitosamente.
              </p>

              {/* Estado del registro en API */}
              <div className="mt-4">
                {apiRegistrationStatus === 'loading' && (
                  <div className="flex items-center justify-center text-blue-600">
                    <svg className="animate-spin -ml-1 mr-3 h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <span className="text-sm">Completando registro...</span>
                  </div>
                )}

                {apiRegistrationStatus === 'success' && (
                  <p className="text-sm text-green-600 font-medium">
                    ✅ Registro completado exitosamente
                  </p>
                )}

                {apiRegistrationStatus === 'error' && (
                  <p className="text-sm text-yellow-600">
                    ⚠️ Cuenta confirmada. Completa tu perfil al iniciar sesión.
                  </p>
                )}
              </div>

              <p className="text-sm text-gray-500 mt-2">
                Ya puedes iniciar sesión con tu email y contraseña.
              </p>
            </div>
            
            <div className="text-center">
              <p className="text-sm text-gray-500 mb-6">
                Serás redirigido al login automáticamente...
              </p>
              
              <button
                onClick={async () => {
                  // Asegurar limpieza de sesión antes de ir al login
                  try {
                    await cognitoLogout();
                  } catch (e) {
                    console.warn('Error limpiando sesión:', e);
                  }
                  router.push('/login');
                }}
                className="auth-button-primary"
              >
                Iniciar Sesión Ahora
              </button>
            </div>
          </div>
          
        </div>
    );
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
          {/* Back Button */}
          <button
            onClick={() => router.push('/register')}
            className="auth-back-button"
          >
            <ArrowLeftIcon className="w-5 h-5" />
            Regresar
          </button>

          {/* Logo */}
          <div className="auth-logo">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
              <svg className="w-8 h-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
          </div>

          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="auth-title text-2xl">
              Confirma tu Email
            </h1>
            <p className="auth-subtitle">
              Hemos enviado un código de 6 dígitos a:
              <br />
              <span className="font-medium text-blue-600">{email}</span>
              <br />
              <span className="text-xs">
                Registrándote como: <span className="font-medium text-[#008D96]">
                  {userType === 'user' ? 'Usuario' : 'Colaborador'}
                </span>
              </span>
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Message */}
            {error && (
              <div className="auth-error">
                <p className="auth-error-text">{error}</p>
              </div>
            )}

            {/* Success Message for Resend */}
            {resendMessage && (
              <div className="auth-success">
                <p className="auth-success-text">{resendMessage}</p>
              </div>
            )}

            {/* Verification Code */}
            <div className="auth-field">
              <label htmlFor="code" className="auth-field-label">
                Código de Verificación
              </label>
              <div className="auth-field-container">
                <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <input
                  id="code"
                  name="code"
                  type="text"
                  required
                  value={code}
                  onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  className="auth-input text-center text-lg tracking-widest font-mono"
                  placeholder="123456"
                  maxLength={6}
                  disabled={loading}
                />
              </div>
              <div className="flex justify-between items-center mt-2">
                <p className="text-xs text-gray-500">Revisa tu bandeja de entrada y spam</p>
                <button
                  type="button"
                  onClick={handleResendCode}
                  disabled={resendLoading}
                  className="auth-link text-xs disabled:opacity-50"
                >
                  {resendLoading ? "Reenviando..." : "Reenviar código"}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading || code.length !== 6}
              className="auth-button-primary"
            >
              {loading ? (
                <div className="auth-loading">
                  <svg className="w-5 h-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Confirmando...
                </div>
              ) : (
                "Confirmar Cuenta"
              )}
            </button>
          </form>

          {/* Info Box */}
          <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-xl">
            <div className="flex items-start">
              <svg className="h-5 w-5 text-blue-600 mt-0.5 mr-3 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div className="text-sm">
                <p className="text-blue-800 font-medium mb-1">¿No recibiste el código?</p>
                <ul className="text-blue-700 space-y-1">
                  <li>• Revisa tu carpeta de spam o correo no deseado</li>
                  <li>• El código expira en 24 horas</li>
                  <li>• Puedes solicitar un nuevo código haciendo clic en &quot;Reenviar&quot;</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Login Link */}
          <div className="mt-8 text-center">
            <p className="text-sm text-gray-500">
              ¿Ya tienes una cuenta confirmada?{" "}
              <button
                onClick={() => router.push("/login")}
                className="auth-link"
              >
                Inicia sesión
              </button>
            </p>
          </div>
        </div>

      </div>
  );
}

export default function ConfirmEmail() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <ConfirmEmailContent />
    </Suspense>
  );
}