"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { cognitoSignUp, SignUpData } from '@/lib/cognito';
import { EyeIcon, EyeSlashIcon, ArrowLeftIcon } from "@heroicons/react/24/outline";
import "../../styles/auth-design-system.css";

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
      <div className="auth-container">
        <div className="auth-card">
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
          
          <div className="auth-footer">
            <p className="auth-footer-text">
              2025 Beneficio Joven. Todos los derechos reservados.
            </p>
          </div>
        </div>
    );
  }

  return (
    <div className="auth-container">
      <div className="auth-card-wide">
          {/* Back Button */}
          <button
            onClick={() => router.back()}
            className="auth-back-button "
          >
            <ArrowLeftIcon className="w-5 h-5" />
            Regresar
          </button>

          {/* Logo */}
          <div className="auth-logo">
            <img src="/logo_beneficio_joven.png" alt="" />
          </div>

          {/* Header */}
          <div className="text-center mb-8">
            <h1 className="auth-title">
              Crear Cuenta
            </h1>
            <p className="auth-subtitle">
              Regístrate para acceder a todos los beneficios
            </p>
          </div>

          {/* User Type Selection */}
          <div className="user-type-selector">
            <button
              type="button"
              onClick={() => setUserType('user')}
              className={`user-type-option ${userType === 'user' ? 'active' : ''}`}
            >
              Usuario
            </button>
            <button
              type="button"
              onClick={() => setUserType('collaborator')}
              className={`user-type-option ${userType === 'collaborator' ? 'active' : ''}`}
            >
              Colaborador
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            {/* Error Message */}
            {error && (
              <div className="auth-error">
                <p className="auth-error-text">{error}</p>
              </div>
            )}

            {/* User Form Fields */}
            {userType === 'user' && (
              <div className="auth-form-grid">
                {/* Columna Izquierda */}
                <div className="auth-form-column">
                    {/* Name Field */}
                    <div className="auth-field">
                      <label htmlFor="name" className="auth-field-label">Nombre</label>
                      <div className="auth-field-container">
                        <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                        <input
                          id="name"
                          name="name"
                          type="text"
                          required
                          value={formData.name}
                          onChange={handleInputChange}
                          className="auth-input"
                          placeholder="Tu nombre"
                          disabled={loading}
                        />
                      </div>
                    </div>

                    {/* Apellido Paterno Field */}
                    <div className="auth-field">
                      <label htmlFor="apellidoPaterno" className="auth-field-label">Apellido Paterno</label>
                      <div className="auth-field-container">
                        <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                        <input
                          id="apellidoPaterno"
                          name="apellidoPaterno"
                          type="text"
                          required
                          value={formData.apellidoPaterno}
                          onChange={handleInputChange}
                          className="auth-input"
                          placeholder="Tu apellido paterno"
                          disabled={loading}
                        />
                      </div>
                    </div>
                    {/* Teléfono Field */}
                    <div className="auth-field">
                      <label htmlFor="telefono" className="auth-field-label">Teléfono</label>
                      <div className="auth-field-container">
                        <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                        </svg>
                        <input
                          id="telefono"
                          name="telefono"
                          type="tel"
                          required
                          value={formData.telefono}
                          onChange={handleInputChange}
                          className="auth-input"
                          placeholder="Tu número de teléfono"
                          disabled={loading}
                        />
                      </div>
                    </div>
                  </div>

                  {/* Columna Derecha */}
                  <div className="auth-form-column">
                    {/* Apellido Materno Field */}
                    <div className="auth-field">
                      <label htmlFor="apellidoMaterno" className="auth-field-label">Apellido Materno</label>
                      <div className="auth-field-container">
                        <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                        <input
                          id="apellidoMaterno"
                          name="apellidoMaterno"
                          type="text"
                          required
                          value={formData.apellidoMaterno}
                          onChange={handleInputChange}
                          className="auth-input"
                          placeholder="Tu apellido materno"
                          disabled={loading}
                        />
                      </div>
                    </div>

                    {/* Fecha de Nacimiento Field */}
                    <div className="auth-field">
                      <label htmlFor="fechaNacimiento" className="auth-field-label">Fecha de Nacimiento</label>
                      <div className="auth-field-container">
                        <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <input
                          id="fechaNacimiento"
                          name="fechaNacimiento"
                          type="date"
                          required
                          value={formData.fechaNacimiento}
                          onChange={handleInputChange}
                          className="auth-input"
                          disabled={loading}
                        />
                      </div>
                    </div>

                  </div>

                  {/* Email y Password span ambas columnas */}
                  <div className="auth-field auth-field-full">
                    <label htmlFor="email" className="auth-field-label">Correo Electrónico</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                      </svg>
                      <input
                        id="email"
                        name="email"
                        type="email"
                        required
                        value={formData.email}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="tu@email.com"
                        disabled={loading}
                      />
                    </div>
                  </div>

                  <div className="auth-field auth-field-full">
                    <label htmlFor="password" className="auth-field-label">Contraseña</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                      </svg>
                      <input
                        id="password"
                        name="password"
                        type={showPassword ? "text" : "password"}
                        required
                        value={formData.password}
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

                    {/* Password Strength Indicator */}
                    {formData.password && (
                      <div className="password-strength">
                        <div className="password-strength-bars">
                          {[...Array(5)].map((_, i) => (
                            <div
                              key={i}
                              className={`password-strength-bar ${
                                i < passwordStrength.score
                                  ? passwordStrength.score <= 2
                                    ? "weak"
                                    : passwordStrength.score <= 3
                                    ? "medium"
                                    : "strong"
                                  : ""
                              }`}
                            />
                          ))}
                        </div>
                        <div className="password-strength-text">
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
                            <span className="text-gray-500">
                              Falta: {passwordStrength.feedback.join(", ")}
                            </span>
                          )}
                        </div>
                      </div>
                    )}

                    <p className="text-xs text-gray-500 mt-2">
                      Mínimo 8 caracteres con mayúsculas, minúsculas y números
                    </p>
                  </div>
              </div>
            )}

            {/* Collaborator Form Fields */}
            {userType === 'collaborator' && (
              <div className="auth-form-grid">
                {/* Columna Izquierda */}
                <div className="auth-form-column">
                  {/* Business Name Field */}
                  <div className="auth-field">
                    <label htmlFor="businessName" className="auth-field-label">Nombre del Negocio</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                      </svg>
                      <input
                        id="businessName"
                        name="businessName"
                        type="text"
                        required
                        value={collaboratorData.businessName}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="Nombre de tu negocio"
                        disabled={loading}
                      />
                    </div>
                  </div>

                  {/* RFC Field */}
                  <div className="auth-field">
                    <label htmlFor="rfc" className="auth-field-label">RFC</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                      <input
                        id="rfc"
                        name="rfc"
                        type="text"
                        required
                        value={collaboratorData.rfc}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="RFC de tu negocio"
                        disabled={loading}
                      />
                    </div>
                  </div>

                  {/* Representative Name Field */}
                  <div className="auth-field">
                    <label htmlFor="representativeName" className="auth-field-label">Nombre del Representante</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <input
                        id="representativeName"
                        name="representativeName"
                        type="text"
                        required
                        value={collaboratorData.representativeName}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="Tu nombre completo"
                        disabled={loading}
                      />
                    </div>
                  </div>

                  {/* Phone Field */}
                  <div className="auth-field">
                    <label htmlFor="phone" className="auth-field-label">Teléfono</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                      <input
                        id="phone"
                        name="phone"
                        type="tel"
                        required
                        value={collaboratorData.phone}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="10 dígitos"
                        disabled={loading}
                      />
                    </div>
                    {collaboratorData.phone && !validatePhone(collaboratorData.phone) && (
                      <p className="text-xs text-red-500 mt-1">El teléfono debe tener exactamente 10 dígitos</p>
                    )}
                  </div>
                </div>

                {/* Columna Derecha */}
                <div className="auth-form-column">

                  {/* Email Field */}
                  <div className="auth-field">
                    <label htmlFor="email" className="auth-field-label">Correo Electrónico</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                      </svg>
                      <input
                        id="email"
                        name="email"
                        type="email"
                        required
                        value={collaboratorData.email}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="correo@negocio.com"
                        disabled={loading}
                      />
                    </div>
                  </div>

                  {/* Address Field */}
                  <div className="auth-field">
                    <label htmlFor="address" className="auth-field-label">Dirección</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                      <input
                        id="address"
                        name="address"
                        type="text"
                        required
                        value={collaboratorData.address}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="Dirección completa del negocio"
                        disabled={loading}
                      />
                    </div>
                  </div>

                  {/* Postal Code Field */}
                  <div className="auth-field">
                    <label htmlFor="postalCode" className="auth-field-label">Código Postal</label>
                    <div className="auth-field-container">
                      <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                      <input
                        id="postalCode"
                        name="postalCode"
                        type="text"
                        required
                        value={collaboratorData.postalCode}
                        onChange={handleInputChange}
                        className="auth-input"
                        placeholder="12345"
                        disabled={loading}
                      />
                    </div>
                  </div>
                </div>

                {/* Campos que abarcan toda la fila */}
                <div className="auth-field auth-field-full">
                  <label htmlFor="description" className="auth-field-label">Descripción del Negocio</label>
                  <div className="auth-field-container">
                    <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
                    </svg>
                    <textarea
                      id="description"
                      name="description"
                      rows={3}
                      required
                      value={collaboratorData.description}
                      onChange={handleInputChange}
                      className="auth-input resize-none"
                      style={{ paddingLeft: '2rem', minHeight: '4rem' }}
                      placeholder="Describe tu negocio y los servicios que ofreces..."
                      disabled={loading}
                    />
                  </div>
                </div>


                <div className="auth-field auth-field-full">
                  <label htmlFor="password" className="auth-field-label">Contraseña</label>
                  <div className="auth-field-container">
                    <svg className="auth-field-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? "text" : "password"}
                      required
                      value={collaboratorData.password}
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

                  {/* Password Strength Indicator */}
                  {collaboratorData.password && (
                    <div className="password-strength">
                      <div className="password-strength-bars">
                        {[...Array(5)].map((_, i) => (
                          <div
                            key={i}
                            className={`password-strength-bar ${
                              i < getPasswordStrength(collaboratorData.password).score
                                ? getPasswordStrength(collaboratorData.password).score <= 2
                                  ? "weak"
                                  : getPasswordStrength(collaboratorData.password).score <= 3
                                  ? "medium"
                                  : "strong"
                                : ""
                            }`}
                          />
                        ))}
                      </div>
                      <div className="password-strength-text">
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
                          <span className="text-gray-500">
                            Falta: {getPasswordStrength(collaboratorData.password).feedback.join(", ")}
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  <p className="text-xs text-gray-500 mt-2">
                    Mínimo 8 caracteres con mayúsculas, minúsculas y números
                  </p>
                </div>

                <div className="auth-checkbox auth-field-full">
                  <input
                    id="acceptTerms"
                    name="acceptTerms"
                    type="checkbox"
                    checked={collaboratorData.acceptTerms}
                    onChange={handleInputChange}
                    disabled={loading}
                  />
                  <label htmlFor="acceptTerms" className="auth-checkbox-label">
                    Acepto los{" "}
                    <a href="#" className="auth-link">
                      términos y condiciones
                    </a>{" "}
                    y la{" "}
                    <a href="#" className="auth-link">
                      política de privacidad
                    </a>
                  </label>
                </div>
              </div>
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

      </div>
  );
}