'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import {
  BellIcon,
  EyeIcon,
  ShieldCheckIcon,
  CreditCardIcon,
  GlobeAltIcon,
  DevicePhoneMobileIcon,
  ChevronRightIcon,
  CheckIcon
} from '@heroicons/react/24/outline';
import { promotionApiService } from '../promociones/services/api';
import { ApiCollaborator } from '../promociones/types';

interface NotificationSettings {
  emailPromotions: boolean;
  emailRedemptions: boolean;
  emailReviews: boolean;
  pushPromotions: boolean;
  pushRedemptions: boolean;
  pushReviews: boolean;
  smsImportant: boolean;
}

interface PrivacySettings {
  profileVisible: boolean;
  showBusinessHours: boolean;
  showContactInfo: boolean;
  showSocialLinks: boolean;
}

interface BusinessSettings {
  businessName: string;
  businessDescription: string;
  businessCategory: string;
  businessHours: {
    [key: string]: { open: string; close: string; closed: boolean };
  };
  businessAddress: string;
  businessPhone: string;
  businessEmail: string;
  businessWebsite: string;
  socialMedia: {
    facebook: string;
    instagram: string;
    twitter: string;
  };
}

export default function ConfiguracionesPage() {
  const { data: session } = useSession();
  const [activeTab, setActiveTab] = useState<'notifications' | 'privacy' | 'business' | 'billing'>('notifications');
  const [collaborator, setCollaborator] = useState<ApiCollaborator | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [notifications, setNotifications] = useState<NotificationSettings>({
    emailPromotions: true,
    emailRedemptions: true,
    emailReviews: false,
    pushPromotions: false,
    pushRedemptions: true,
    pushReviews: true,
    smsImportant: true
  });

  const [privacy, setPrivacy] = useState<PrivacySettings>({
    profileVisible: true,
    showBusinessHours: true,
    showContactInfo: false,
    showSocialLinks: true
  });

  const [business, setBusiness] = useState<BusinessSettings>({
    businessName: "",
    businessDescription: "",
    businessCategory: "COMIDA",
    businessHours: {
      monday: { open: "09:00", close: "22:00", closed: false },
      tuesday: { open: "09:00", close: "22:00", closed: false },
      wednesday: { open: "09:00", close: "22:00", closed: false },
      thursday: { open: "09:00", close: "22:00", closed: false },
      friday: { open: "09:00", close: "23:00", closed: false },
      saturday: { open: "10:00", close: "23:00", closed: false },
      sunday: { open: "10:00", close: "21:00", closed: false }
    },
    businessAddress: "",
    businessPhone: "",
    businessEmail: "",
    businessWebsite: "",
    socialMedia: {
      facebook: "",
      instagram: "",
      twitter: ""
    }
  });

  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  // Cargar datos del colaborador
  useEffect(() => {
    const loadCollaboratorData = async () => {
      if (!session) {
        setLoading(false);
        return;
      }

      try {
        const sessionData = session as any;
        const cognitoUsername = sessionData.cognitoUsername || sessionData.sub || sessionData.user?.id || sessionData.user?.sub;

        if (cognitoUsername) {
          console.log('🔄 Loading collaborator data...');
          const collaboratorData = await promotionApiService.getCollaboratorByCognitoId(cognitoUsername);
          setCollaborator(collaboratorData);

          // Mapear datos del servidor al estado local
          setBusiness({
            businessName: collaboratorData.businessName || "",
            businessDescription: collaboratorData.description || "",
            businessCategory: collaboratorData.categories?.[0]?.name || "COMIDA",
            businessHours: {
              monday: { open: "09:00", close: "22:00", closed: false },
              tuesday: { open: "09:00", close: "22:00", closed: false },
              wednesday: { open: "09:00", close: "22:00", closed: false },
              thursday: { open: "09:00", close: "22:00", closed: false },
              friday: { open: "09:00", close: "23:00", closed: false },
              saturday: { open: "10:00", close: "23:00", closed: false },
              sunday: { open: "10:00", close: "21:00", closed: false }
            },
            businessAddress: collaboratorData.address || "",
            businessPhone: collaboratorData.phone || "",
            businessEmail: collaboratorData.email || "",
            businessWebsite: "",
            socialMedia: {
              facebook: "",
              instagram: "",
              twitter: ""
            }
          });

          console.log('✅ Collaborator data loaded:', collaboratorData.businessName);
        }
      } catch (err) {
        console.error('❌ Error loading collaborator data:', err);
        setError('Error al cargar los datos del colaborador');
      } finally {
        setLoading(false);
      }
    };

    if (session !== undefined) {
      loadCollaboratorData();
    }
  }, [session]);

  const handleSave = async () => {
    if (!collaborator) {
      setError('No hay datos de colaborador para guardar');
      return;
    }

    setSaving(true);
    setError(null);

    try {
      console.log('💾 Saving collaborator settings...');

      // Crear objeto con los datos actualizados
      const updateData = {
        businessName: business.businessName,
        description: business.businessDescription,
        phone: business.businessPhone,
        email: business.businessEmail,
        address: business.businessAddress,
        // Nota: El servidor podría no tener endpoint de actualización todavía
        // Por ahora simularemos el guardado
      };

      console.log('📦 Data to save:', updateData);

      // Simular guardado por ahora - aquí iría la llamada real al servidor
      // await promotionApiService.updateCollaborator(collaborator.id, updateData);
      await new Promise(resolve => setTimeout(resolve, 1000));

      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
      console.log('✅ Settings saved successfully');
    } catch (error) {
      console.error('❌ Error saving settings:', error);
      setError('Error al guardar la configuración');
    } finally {
      setSaving(false);
    }
  };

  const tabs = [
    { id: 'notifications', name: 'Notificaciones', icon: BellIcon },
    { id: 'privacy', name: 'Privacidad', icon: EyeIcon },
    { id: 'business', name: 'Negocio', icon: GlobeAltIcon },
    { id: 'billing', name: 'Facturación', icon: CreditCardIcon }
  ];

  const dayNames = {
    monday: 'Lunes',
    tuesday: 'Martes',
    wednesday: 'Miércoles',
    thursday: 'Jueves',
    friday: 'Viernes',
    saturday: 'Sábado',
    sunday: 'Domingo'
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="py-8">
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
            <div className="text-[#969696] mt-2">Cargando configuraciones...</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Configuraciones</h1>
          <p className="text-gray-600">
            {collaborator ? `${collaborator.businessName} - Gestiona tu cuenta y preferencias` : "Gestiona tu cuenta y preferencias del negocio"}
          </p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Sidebar */}
            <div className="lg:col-span-1">
              <nav className="space-y-2">
                {tabs.map((tab) => (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id as 'notifications' | 'privacy' | 'business' | 'billing')}
                    className={`w-full text-left px-4 py-3 rounded-lg flex items-center space-x-3 transition-colors ${
                      activeTab === tab.id
                        ? 'bg-[#008D96] text-white'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    <tab.icon className="h-5 w-5" />
                    <span className="font-medium">{tab.name}</span>
                    <ChevronRightIcon className="h-4 w-4 ml-auto" />
                  </button>
                ))}
              </nav>
            </div>

            {/* Content */}
            <div className="lg:col-span-3">
              <div className="bg-white rounded-lg shadow p-6">
                {/* Notifications Tab */}
                {activeTab === 'notifications' && (
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900 mb-6">Preferencias de Notificaciones</h2>

                    <div className="space-y-6">
                      {/* Email Notifications */}
                      <div>
                        <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
                          <BellIcon className="h-5 w-5 mr-2" />
                          Notificaciones por Email
                        </h3>
                        <div className="space-y-3">
                          {[
                            { key: 'emailPromotions', label: 'Nuevas promociones creadas', desc: 'Recibe confirmación cuando crees una promoción' },
                            { key: 'emailRedemptions', label: 'Canjes de promociones', desc: 'Notificación cuando alguien canjee tus promociones' },
                            { key: 'emailReviews', label: 'Nuevas reseñas', desc: 'Cuando recibas una nueva reseña de clientes' }
                          ].map((item) => (
                            <div key={item.key} className="flex items-center justify-between py-2">
                              <div>
                                <p className="font-medium text-gray-900">{item.label}</p>
                                <p className="text-sm text-gray-600">{item.desc}</p>
                              </div>
                              <button
                                onClick={() => setNotifications(prev => ({ ...prev, [item.key]: !prev[item.key as keyof NotificationSettings] }))}
                                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                                  notifications[item.key as keyof NotificationSettings] ? 'bg-[#008D96]' : 'bg-gray-200'
                                }`}
                              >
                                <span
                                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                    notifications[item.key as keyof NotificationSettings] ? 'translate-x-6' : 'translate-x-1'
                                  }`}
                                />
                              </button>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Push Notifications */}
                      <div>
                        <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
                          <DevicePhoneMobileIcon className="h-5 w-5 mr-2" />
                          Notificaciones Push
                        </h3>
                        <div className="space-y-3">
                          {[
                            { key: 'pushPromotions', label: 'Promociones destacadas', desc: 'Alertas importantes sobre tus promociones' },
                            { key: 'pushRedemptions', label: 'Canjes en tiempo real', desc: 'Notificación inmediata de canjes' },
                            { key: 'pushReviews', label: 'Nuevas reseñas', desc: 'Alertas de nuevas reseñas recibidas' }
                          ].map((item) => (
                            <div key={item.key} className="flex items-center justify-between py-2">
                              <div>
                                <p className="font-medium text-gray-900">{item.label}</p>
                                <p className="text-sm text-gray-600">{item.desc}</p>
                              </div>
                              <button
                                onClick={() => setNotifications(prev => ({ ...prev, [item.key]: !prev[item.key as keyof NotificationSettings] }))}
                                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                                  notifications[item.key as keyof NotificationSettings] ? 'bg-[#008D96]' : 'bg-gray-200'
                                }`}
                              >
                                <span
                                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                    notifications[item.key as keyof NotificationSettings] ? 'translate-x-6' : 'translate-x-1'
                                  }`}
                                />
                              </button>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Privacy Tab */}
                {activeTab === 'privacy' && (
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900 mb-6">Configuración de Privacidad</h2>

                    <div className="space-y-6">
                      <div>
                        <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
                          <ShieldCheckIcon className="h-5 w-5 mr-2" />
                          Visibilidad del Perfil
                        </h3>
                        <div className="space-y-3">
                          {[
                            { key: 'profileVisible', label: 'Perfil público', desc: 'Permitir que los usuarios vean tu perfil de negocio' },
                            { key: 'showBusinessHours', label: 'Mostrar horarios', desc: 'Mostrar horarios de atención en tu perfil' },
                            { key: 'showContactInfo', label: 'Información de contacto', desc: 'Mostrar teléfono y email públicamente' },
                            { key: 'showSocialLinks', label: 'Redes sociales', desc: 'Mostrar enlaces a redes sociales' }
                          ].map((item) => (
                            <div key={item.key} className="flex items-center justify-between py-2">
                              <div>
                                <p className="font-medium text-gray-900">{item.label}</p>
                                <p className="text-sm text-gray-600">{item.desc}</p>
                              </div>
                              <button
                                onClick={() => setPrivacy(prev => ({ ...prev, [item.key]: !prev[item.key as keyof PrivacySettings] }))}
                                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                                  privacy[item.key as keyof PrivacySettings] ? 'bg-[#008D96]' : 'bg-gray-200'
                                }`}
                              >
                                <span
                                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                    privacy[item.key as keyof PrivacySettings] ? 'translate-x-6' : 'translate-x-1'
                                  }`}
                                />
                              </button>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Business Tab */}
                {activeTab === 'business' && (
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900 mb-6">Información del Negocio</h2>

                    <div className="space-y-6">
                      {/* Basic Info */}
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">Nombre del Negocio</label>
                          <input
                            type="text"
                            value={business.businessName}
                            onChange={(e) => setBusiness(prev => ({ ...prev, businessName: e.target.value }))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#008D96]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">Categoría</label>
                          <select
                            value={business.businessCategory}
                            onChange={(e) => setBusiness(prev => ({ ...prev, businessCategory: e.target.value }))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#008D96]"
                          >
                            <option value="COMIDA">Comida</option>
                            <option value="ENTRETENIMIENTO">Entretenimiento</option>
                            <option value="ROPA">Ropa</option>
                          </select>
                        </div>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Descripción</label>
                        <textarea
                          value={business.businessDescription}
                          onChange={(e) => setBusiness(prev => ({ ...prev, businessDescription: e.target.value }))}
                          rows={3}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#008D96]"
                        />
                      </div>

                      {/* Contact Info */}
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">Teléfono</label>
                          <input
                            type="tel"
                            value={business.businessPhone}
                            onChange={(e) => setBusiness(prev => ({ ...prev, businessPhone: e.target.value }))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#008D96]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">Email</label>
                          <input
                            type="email"
                            value={business.businessEmail}
                            onChange={(e) => setBusiness(prev => ({ ...prev, businessEmail: e.target.value }))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#008D96]"
                          />
                        </div>
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Dirección</label>
                        <input
                          type="text"
                          value={business.businessAddress}
                          onChange={(e) => setBusiness(prev => ({ ...prev, businessAddress: e.target.value }))}
                          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#008D96]"
                        />
                      </div>

                      {/* Business Hours */}
                      <div>
                        <h3 className="text-lg font-medium text-gray-900 mb-4">Horarios de Atención</h3>
                        <div className="space-y-3">
                          {Object.entries(business.businessHours).map(([day, hours]) => (
                            <div key={day} className="flex items-center space-x-4">
                              <span className="w-20 text-sm font-medium text-gray-700">
                                {dayNames[day as keyof typeof dayNames]}
                              </span>
                              <button
                                onClick={() => setBusiness(prev => ({
                                  ...prev,
                                  businessHours: {
                                    ...prev.businessHours,
                                    [day]: { ...hours, closed: !hours.closed }
                                  }
                                }))}
                                className={`px-3 py-1 rounded text-xs font-medium ${
                                  hours.closed ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'
                                }`}
                              >
                                {hours.closed ? 'Cerrado' : 'Abierto'}
                              </button>
                              {!hours.closed && (
                                <>
                                  <input
                                    type="time"
                                    value={hours.open}
                                    onChange={(e) => setBusiness(prev => ({
                                      ...prev,
                                      businessHours: {
                                        ...prev.businessHours,
                                        [day]: { ...hours, open: e.target.value }
                                      }
                                    }))}
                                    className="px-2 py-1 border border-gray-300 rounded text-sm"
                                  />
                                  <span className="text-gray-500">-</span>
                                  <input
                                    type="time"
                                    value={hours.close}
                                    onChange={(e) => setBusiness(prev => ({
                                      ...prev,
                                      businessHours: {
                                        ...prev.businessHours,
                                        [day]: { ...hours, close: e.target.value }
                                      }
                                    }))}
                                    className="px-2 py-1 border border-gray-300 rounded text-sm"
                                  />
                                </>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Billing Tab */}
                {activeTab === 'billing' && (
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900 mb-6">Información de Facturación</h2>

                    <div className="space-y-6">
                      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                        <h3 className="text-lg font-medium text-yellow-800 mb-2">Plan Actual: Básico</h3>
                        <p className="text-yellow-700">Incluye hasta 10 promociones activas simultáneamente</p>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="border border-gray-200 rounded-lg p-4">
                          <h4 className="font-medium text-gray-900 mb-2">Plan Básico</h4>
                          <p className="text-2xl font-bold text-gray-900 mb-2">Gratis</p>
                          <ul className="text-sm text-gray-600 space-y-1">
                            <li>• Hasta 10 promociones</li>
                            <li>• Estadísticas básicas</li>
                            <li>• Soporte por email</li>
                          </ul>
                          <button className="w-full mt-4 px-4 py-2 bg-gray-100 text-gray-400 rounded-lg cursor-not-allowed">
                            Plan Actual
                          </button>
                        </div>

                        <div className="border border-[#008D96] rounded-lg p-4">
                          <h4 className="font-medium text-gray-900 mb-2">Plan Premium</h4>
                          <p className="text-2xl font-bold text-gray-900 mb-2">$299<span className="text-sm text-gray-600">/mes</span></p>
                          <ul className="text-sm text-gray-600 space-y-1">
                            <li>• Promociones ilimitadas</li>
                            <li>• Estadísticas avanzadas</li>
                            <li>• Soporte prioritario</li>
                            <li>• Análisis de competencia</li>
                          </ul>
                          <button className="w-full mt-4 px-4 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#007580]">
                            Actualizar Plan
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Save Button */}
                <div className="mt-8 pt-6 border-t border-gray-200">
                  <div className="flex items-center justify-between">
                    {saved && (
                      <div className="flex items-center text-green-600">
                        <CheckIcon className="h-5 w-5 mr-2" />
                        <span className="text-sm font-medium">Configuración guardada</span>
                      </div>
                    )}
                    <button
                      onClick={handleSave}
                      disabled={saving}
                      className="ml-auto px-6 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#007580] disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
                    >
                      {saving ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          Guardando...
                        </>
                      ) : (
                        'Guardar Cambios'
                      )}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
      </div>
    </div>
  );
}