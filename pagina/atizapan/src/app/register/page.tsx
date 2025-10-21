"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { cognitoSignUp, SignUpData } from '@/lib/cognito';
import { apiService, CollaboratorRegistrationData } from '@/lib/api';
import { EyeIcon, EyeSlashIcon, ArrowLeftIcon } from "@heroicons/react/24/outline";

interface ExtendedFormData extends SignUpData {
  apellidoPaterno: string;
  apellidoMaterno: string;
  fechaNacimiento: string;
  telefono: string;
}

interface CollaboratorFormData extends SignUpData {
  businessName: string;
  rfc: string;
  representativeName: string;
  phone: string;
  address: string;
  postalCode: string;
  description: string;
  acceptTerms: boolean;
}

export default function Register() {
  const [userType, setUserType] = useState<'user' | 'collaborator'>('user');
  const [formData, setFormData] = useState<ExtendedFormData>({
    email: '',
    password: '',
    name: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    fechaNacimiento: '',
    telefono: '',
  });
  const [collaboratorData, setCollaboratorData] = useState<CollaboratorFormData>({
    email: '',
    password: '',
    name: '',
    businessName: '',
    rfc: '',
    representativeName: '',
    phone: '',
    address: '',
    postalCode: '',
    description: '',
    acceptTerms: false,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const router = useRouter();

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    if (userType === 'user') {
      setFormData(prev => ({
        ...prev,
        [name]: value,
      }));
    } else {
      setCollaboratorData(prev => ({
        ...prev,
        [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
      }));
    }
  };

  const validatePassword = (password: string) => {
    const minLength = password.length >= 8;
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    
    return minLength && hasUpper && hasLower && hasNumber;
  };

  const getPasswordStrength = (password: string) => {
    let score = 0;
    const feedback: string[] = [];

    if (password.length >= 8) score += 1;
    else feedback.push("Mínimo 8 caracteres");

    if (/[A-Z]/.test(password)) score += 1;
    else feedback.push("Una mayúscula");

    if (/[a-z]/.test(password)) score += 1;
    else feedback.push("Una minúscula");

    if (/\d/.test(password)) score += 1;
    else feedback.push("Un número");

    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score += 1;

    return { score, feedback };
  };

  const validatePhone = (phone: string) => {
    return phone.trim().length === 10 && /^\d{10}$/.test(phone);
  };

  const isCollaboratorFormValid = () => {
    const data = collaboratorData;
    return (
      data.businessName.trim() !== '' &&
      data.rfc.trim() !== '' &&
      data.representativeName.trim() !== '' &&
      validatePhone(data.phone) &&
      data.email.trim() !== '' &&
      data.address.trim() !== '' &&
      data.postalCode.trim() !== '' &&
      data.description.trim() !== '' &&
      validatePassword(data.password) &&
      data.acceptTerms
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (userType === 'user') {
      // User validation
      if (!formData.name.trim()) {
        setError('El nombre es obligatorio');
        return;
      }

      if (!formData.apellidoPaterno.trim()) {
        setError('El apellido paterno es obligatorio');
        return;
      }

      if (!formData.apellidoMaterno.trim()) {
        setError('El apellido materno es obligatorio');
        return;
      }

      if (!formData.fechaNacimiento.trim()) {
        setError('La fecha de nacimiento es obligatoria');
        return;
      }

      if (!formData.telefono.trim()) {
        setError('El número de teléfono es obligatorio');
        return;
      }

      if (!formData.email.trim()) {
        setError('El correo electrónico es obligatorio');
        return;
      }

      if (!validatePassword(formData.password)) {
        setError('La contraseña debe tener al menos 8 caracteres con mayúsculas, minúsculas y números');
        return;
      }
    } else {
      // Collaborator validation
      if (!isCollaboratorFormValid()) {
        setError('Por favor completa todos los campos correctamente');
        return;
      }
    }
    
    setLoading(true);
    setError('');

    try {
      console.log('🚀 Iniciando proceso de registro en Cognito...');

      if (userType === 'user') {
        console.log('📝 Datos del formulario usuario:', formData);

        // Solo registrar en Cognito (email y password)
        const cognitoData: SignUpData = {
          email: formData.email,
          password: formData.password,
          name: formData.name,
        };
        console.log('☁️ Registrando usuario en Cognito con datos:', cognitoData);
        await cognitoSignUp(cognitoData);
        console.log('✅ Registro de usuario en Cognito exitoso');

        // Almacenar datos del formulario en localStorage para usar después de verificar email
        const tempUserData = {
          name: formData.name,
          lastNamePaternal: formData.apellidoPaterno,
          lastNameMaternal: formData.apellidoMaterno,
          birthDate: formData.fechaNacimiento,
          phoneNumber: formData.telefono,
          email: formData.email,
          password: formData.password,
        };
        localStorage.setItem('tempUserData', JSON.stringify(tempUserData));
      } else {
        console.log('📝 Datos del formulario colaborador:', collaboratorData);

        // Registrar colaborador en Cognito con profile attribute
        const cognitoData: SignUpData = {
          email: collaboratorData.email,
          password: collaboratorData.password,
          name: collaboratorData.representativeName,
          profile: 'collaborator',
        };
        console.log('☁️ Registrando colaborador en Cognito con datos:', cognitoData);
        await cognitoSignUp(cognitoData);
        console.log('✅ Registro de colaborador en Cognito exitoso');

        // Almacenar datos del colaborador para después de verificar email
        const tempCollaboratorData = {
          businessName: collaboratorData.businessName,
          rfc: collaboratorData.rfc,
          representativeName: collaboratorData.representativeName,
          phone: collaboratorData.phone,
          email: collaboratorData.email,
          address: collaboratorData.address,
          postalCode: collaboratorData.postalCode,
          description: collaboratorData.description,
          password: collaboratorData.password,
          userType: 'collaborator',
        };
        localStorage.setItem('tempCollaboratorData', JSON.stringify(tempCollaboratorData));
      }

      setSuccess(true);

      // Redirigir a página de confirmación después de unos segundos
      const email = userType === 'user' ? formData.email : collaboratorData.email;
      setTimeout(() => {
        router.push(`/confirm-email?email=${encodeURIComponent(email)}`);
      }, 2000);
      
    } catch (err: unknown) {
      console.error('💥 Error en registro completo:', err);

      // Log detallado del error para debugging
      if (err && typeof err === 'object') {
        console.log('🔍 Error object keys:', Object.keys(err));
        console.log('🔍 Error details:', JSON.stringify(err, null, 2));
      }

      // Manejar errores de la API primero
      if (err && typeof err === 'object' && 'code' in err) {
        const apiError = err as { code?: string; message?: string; status?: number; rawResponse?: string };
        console.log('🌐 Error de API detectado:', apiError);

        if (apiError.code === 'NETWORK_ERROR') {
          setError('No se pudo conectar con el servidor. Verifica tu conexión a internet.');
        } else {
          const errorMsg = `Error del servidor (${apiError.status || 'N/A'}): ${apiError.message || 'Error desconocido'}`;
          setError(errorMsg);

          // Log adicional para debugging
          if (apiError.rawResponse) {
            console.log('📜 Raw API response:', apiError.rawResponse);
          }
        }
      } else {
        // Manejar errores de Cognito
        console.log('☁️ Error de Cognito detectado');
        const error = err as { code?: string; name?: string; message?: string };
        const code = error?.code || error?.name || "";
        const message = error?.message || "";

        if (/UsernameExistsException/i.test(code + message)) {
          setError('Ya existe una cuenta con este correo electrónico');
        } else if (/InvalidPasswordException/i.test(code + message)) {
          setError('La contraseña no cumple los requisitos mínimos de seguridad');
        } else if (/InvalidParameterException/i.test(code + message)) {
          if (/email/i.test(message)) {
            setError('Formato de correo electrónico inválido');
          } else {
            setError('Datos inválidos. Verifica la información ingresada.');
          }
        } else if (/TooManyRequestsException/i.test(code + message)) {
          setError('Demasiados intentos. Espera un momento antes de volver a intentar.');
        } else {
          setError(`Error al crear la cuenta: ${message || 'Error desconocido'}`);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  const passwordStrength = getPasswordStrength(userType === 'user' ? formData.password : collaboratorData.password);

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
              <h1 className="text-2xl font-bold text-gray-900 mb-2">¡Cuenta Creada!</h1>
              <p className="text-gray-600 mb-2">
                Hemos enviado un código de confirmación a:
              </p>
              <p className="font-medium text-[#008D96]">
                {userType === 'user' ? formData.email : collaboratorData.email}
              </p>
            </div>
            
            <div className="text-center">
              <p className="text-sm text-gray-500 mb-6">
                Serás redirigido para confirmar tu cuenta...
              </p>
              
              <button
                onClick={() => {
                  const email = userType === 'user' ? formData.email : collaboratorData.email;
                  router.push(`/confirm-email?email=${encodeURIComponent(email)}`);
                }}
                className="text-[#008D96] hover:text-[#00494E] font-medium"
              >
                Continuar ahora →
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
            onClick={() => router.back()}
            className="flex items-center text-gray-500 hover:text-gray-700 mb-6 transition-colors"
          >
            <ArrowLeftIcon className="h-5 w-5 mr-2" />
            Regresar
          </button>

          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="text-3xl font-extrabold bg-gradient-to-r from-[#4B4C7E] to-[#008D96] bg-clip-text text-transparent mb-2">
              Crear Cuenta
            </h1>
            <p className="text-gray-600 text-sm">
              Regístrate para acceder a todos los beneficios
            </p>
          </div>

          {/* User Type Selection */}
          <div className="mb-6">
            <div className="flex bg-gray-100 rounded-xl p-1">
              <button
                type="button"
                onClick={() => setUserType('user')}
                className={`flex-1 py-2 px-4 rounded-lg font-medium transition-all ${
                  userType === 'user'
                    ? 'bg-white text-[#008D96] shadow-sm'
                    : 'text-gray-600 hover:text-gray-800'
                }`}
              >
                Usuario
              </button>
              <button
                type="button"
                onClick={() => setUserType('collaborator')}
                className={`flex-1 py-2 px-4 rounded-lg font-medium transition-all ${
                  userType === 'collaborator'
                    ? 'bg-white text-[#008D96] shadow-sm'
                    : 'text-gray-600 hover:text-gray-800'
                }`}
              >
                Colaborador
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Message */}
            {error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-xl">
                <p className="text-red-600 text-sm text-center">{error}</p>
              </div>
            )}

            {/* User Form Fields */}
            {userType === 'user' && (
              <>
                {/* Name Field */}
                <div className="space-y-2">
                  <label htmlFor="name" className="block text-sm font-medium text-gray-500">
                    Nombre
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <input
                      id="name"
                      name="name"
                      type="text"
                      required
                      value={formData.name}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="Tu nombre"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Apellido Paterno Field */}
                <div className="space-y-2">
                  <label htmlFor="apellidoPaterno" className="block text-sm font-medium text-gray-500">
                    Apellido Paterno
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <input
                      id="apellidoPaterno"
                      name="apellidoPaterno"
                      type="text"
                      required
                      value={formData.apellidoPaterno}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="Tu apellido paterno"
                      disabled={loading}
                    />
                  </div>
                </div>
                {/* Apellido Materno Field */}
                <div className="space-y-2">
                  <label htmlFor="apellidoMaterno" className="block text-sm font-medium text-gray-500">
                    Apellido Materno
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <input
                      id="apellidoMaterno"
                      name="apellidoMaterno"
                      type="text"
                      required
                      value={formData.apellidoMaterno}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="Tu apellido materno"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Fecha de Nacimiento Field */}
                <div className="space-y-2">
                  <label htmlFor="fechaNacimiento" className="block text-sm font-medium text-gray-500">
                    Fecha de Nacimiento
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <input
                      id="fechaNacimiento"
                      name="fechaNacimiento"
                      type="date"
                      required
                      value={formData.fechaNacimiento}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Teléfono Field */}
                <div className="space-y-2">
                  <label htmlFor="telefono" className="block text-sm font-medium text-gray-500">
                    Número de Teléfono
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                    </div>
                    <input
                      id="telefono"
                      name="telefono"
                      type="tel"
                      required
                      value={formData.telefono}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="Tu número de teléfono"
                      disabled={loading}
                    />
                  </div>
                </div>

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
                      value={formData.email}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
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
                      value={formData.password}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-12 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
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

                  {/* Password Strength Indicator */}
                  {formData.password && (
                    <div className="mt-3">
                      <div className="flex items-center space-x-1 mb-2">
                        {[...Array(5)].map((_, i) => (
                          <div
                            key={i}
                            className={`h-1 flex-1 rounded-full ${
                              i < passwordStrength.score
                                ? passwordStrength.score <= 2
                                  ? "bg-red-500"
                                  : passwordStrength.score <= 3
                                  ? "bg-yellow-500"
                                  : "bg-green-500"
                                : "bg-gray-200"
                            }`}
                          />
                        ))}
                      </div>
                      <div className="text-xs">
                        <span className={`font-medium ${
                          passwordStrength.score <= 2
                            ? "text-red-600"
                            : passwordStrength.score <= 3
                            ? "text-yellow-600"
                            : "text-green-600"
                        }`}>
                          {passwordStrength.score <= 2
                            ? "Débil"
                            : passwordStrength.score <= 3
                            ? "Media"
                            : "Fuerte"}
                        </span>
                        {passwordStrength.feedback.length > 0 && (
                          <span className="text-gray-500 ml-2">
                            Falta: {passwordStrength.feedback.join(", ")}
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  <p className="text-xs text-gray-500">
                    Mínimo 8 caracteres con mayúsculas, minúsculas y números
                  </p>
                </div>
              </>
            )}

            {/* Collaborator Form Fields */}
            {userType === 'collaborator' && (
              <>
                {/* Business Name Field */}
                <div className="space-y-2">
                  <label htmlFor="businessName" className="block text-sm font-medium text-gray-500">
                    Nombre del Negocio
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                      </svg>
                    </div>
                    <input
                      id="businessName"
                      name="businessName"
                      type="text"
                      required
                      value={collaboratorData.businessName}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="Nombre de tu negocio"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* RFC Field */}
                <div className="space-y-2">
                  <label htmlFor="rfc" className="block text-sm font-medium text-gray-500">
                    RFC
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    </div>
                    <input
                      id="rfc"
                      name="rfc"
                      type="text"
                      required
                      value={collaboratorData.rfc}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="RFC de tu negocio"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Representative Name Field */}
                <div className="space-y-2">
                  <label htmlFor="representativeName" className="block text-sm font-medium text-gray-500">
                    Nombre del Representante
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <input
                      id="representativeName"
                      name="representativeName"
                      type="text"
                      required
                      value={collaboratorData.representativeName}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="Tu nombre completo"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Phone Field */}
                <div className="space-y-2">
                  <label htmlFor="phone" className="block text-sm font-medium text-gray-500">
                    Teléfono
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                    </div>
                    <input
                      id="phone"
                      name="phone"
                      type="tel"
                      required
                      value={collaboratorData.phone}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="10 dígitos"
                      disabled={loading}
                    />
                  </div>
                  {collaboratorData.phone && !validatePhone(collaboratorData.phone) && (
                    <p className="text-xs text-red-500">El teléfono debe tener exactamente 10 dígitos</p>
                  )}
                </div>

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
                      value={collaboratorData.email}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="correo@negocio.com"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Address Field */}
                <div className="space-y-2">
                  <label htmlFor="address" className="block text-sm font-medium text-gray-500">
                    Dirección
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                    </div>
                    <input
                      id="address"
                      name="address"
                      type="text"
                      required
                      value={collaboratorData.address}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="Dirección completa del negocio"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Postal Code Field */}
                <div className="space-y-2">
                  <label htmlFor="postalCode" className="block text-sm font-medium text-gray-500">
                    Código Postal
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    </div>
                    <input
                      id="postalCode"
                      name="postalCode"
                      type="text"
                      required
                      value={collaboratorData.postalCode}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
                      placeholder="12345"
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Description Field */}
                <div className="space-y-2">
                  <label htmlFor="description" className="block text-sm font-medium text-gray-500">
                    Descripción del Negocio
                  </label>
                  <div className="relative">
                    <div className="absolute top-3 left-4 flex items-center pointer-events-none">
                      <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
                      </svg>
                    </div>
                    <textarea
                      id="description"
                      name="description"
                      rows={3}
                      required
                      value={collaboratorData.description}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white resize-none"
                      placeholder="Describe tu negocio y los servicios que ofreces..."
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
                      value={collaboratorData.password}
                      onChange={handleInputChange}
                      className="w-full pl-12 pr-12 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent transition-all duration-200 bg-gray-50 hover:bg-white"
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

                  {/* Password Strength Indicator */}
                  {collaboratorData.password && (
                    <div className="mt-3">
                      <div className="flex items-center space-x-1 mb-2">
                        {[...Array(5)].map((_, i) => (
                          <div
                            key={i}
                            className={`h-1 flex-1 rounded-full ${
                              i < getPasswordStrength(collaboratorData.password).score
                                ? getPasswordStrength(collaboratorData.password).score <= 2
                                  ? "bg-red-500"
                                  : getPasswordStrength(collaboratorData.password).score <= 3
                                  ? "bg-yellow-500"
                                  : "bg-green-500"
                                : "bg-gray-200"
                            }`}
                          />
                        ))}
                      </div>
                      <div className="text-xs">
                        <span className={`font-medium ${
                          getPasswordStrength(collaboratorData.password).score <= 2
                            ? "text-red-600"
                            : getPasswordStrength(collaboratorData.password).score <= 3
                            ? "text-yellow-600"
                            : "text-green-600"
                        }`}>
                          {getPasswordStrength(collaboratorData.password).score <= 2
                            ? "Débil"
                            : getPasswordStrength(collaboratorData.password).score <= 3
                            ? "Media"
                            : "Fuerte"}
                        </span>
                        {getPasswordStrength(collaboratorData.password).feedback.length > 0 && (
                          <span className="text-gray-500 ml-2">
                            Falta: {getPasswordStrength(collaboratorData.password).feedback.join(", ")}
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  <p className="text-xs text-gray-500">
                    Mínimo 8 caracteres con mayúsculas, minúsculas y números
                  </p>
                </div>

                {/* Accept Terms Checkbox */}
                <div className="flex items-center space-x-3">
                  <input
                    id="acceptTerms"
                    name="acceptTerms"
                    type="checkbox"
                    checked={collaboratorData.acceptTerms}
                    onChange={handleInputChange}
                    className="h-4 w-4 text-[#008D96] focus:ring-[#008D96] border-gray-300 rounded"
                    disabled={loading}
                  />
                  <label htmlFor="acceptTerms" className="text-sm text-gray-600">
                    Acepto los{" "}
                    <a href="#" className="text-[#008D96] hover:text-[#00494E] font-medium">
                      términos y condiciones
                    </a>{" "}
                    y la{" "}
                    <a href="#" className="text-[#008D96] hover:text-[#00494E] font-medium">
                      política de privacidad
                    </a>
                  </label>
                </div>
              </>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={
                loading ||
                (userType === 'user'
                  ? !formData.name || !formData.apellidoPaterno || !formData.apellidoMaterno || !formData.fechaNacimiento || !formData.telefono || !formData.email || !validatePassword(formData.password)
                  : !isCollaboratorFormValid()
                )
              }
              className={`w-full py-3 px-4 rounded-xl font-medium text-white transition-all duration-200 ${
                loading ||
                (userType === 'user'
                  ? !formData.name || !formData.apellidoPaterno || !formData.apellidoMaterno || !formData.fechaNacimiento || !formData.telefono || !formData.email || !validatePassword(formData.password)
                  : !isCollaboratorFormValid()
                )
                  ? "bg-gray-400 cursor-not-allowed"
                  : "bg-gradient-to-r from-[#4B4C7E] to-[#008D96] hover:active-98 active:scale-95 shadow-lg hover:shadow-xl"
              }`}
            >
              {loading ? (
                <div className="flex items-center justify-center">
                  <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Creando cuenta...
                </div>
              ) : (
                userType === 'user' ? "Crear Cuenta de Usuario" : "Crear Cuenta de Colaborador"
              )}
            </button>
          </form>

          {/* Terms and Conditions */}
          <div className="mt-6 p-4 bg-gray-50 rounded-xl">
            <p className="text-xs text-gray-600 text-center">
              Al crear una cuenta, aceptas nuestros{" "}
              <a href="#" className="text-[#008D96] hover:text-[#00494E] font-medium">
                Términos de Servicio
              </a>{" "}
              y{" "}
              <a href="#" className="text-[#008D96] hover:text-[#00494E] font-medium">
                Política de Privacidad
              </a>
            </p>
          </div>

          {/* Login Link */}
          <div className="mt-8 text-center">
            <p className="text-sm text-gray-500">
              ¿Ya tienes cuenta?{" "}
              <button
                onClick={() => router.push("/login")}
                className="text-[#008D96] hover:text-[#00494E] font-medium"
              >
                Inicia sesión aquí
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