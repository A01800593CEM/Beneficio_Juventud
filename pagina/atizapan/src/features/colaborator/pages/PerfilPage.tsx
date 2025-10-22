"use client";

import { useState, useEffect } from "react";
import { useSession } from 'next-auth/react';
import Image from "next/image";
import {
  PencilIcon,
  CheckBadgeIcon,
  BuildingStorefrontIcon,
  EnvelopeIcon,
  ClockIcon
} from "@heroicons/react/24/outline";
import { BusinessProfile } from "../types/business";
import { promotionApiService } from '../promociones/services/api';
import { ApiCollaborator } from '../promociones/types';

// Mock business data
const mockBusiness: BusinessProfile = {
  id: "1",
  name: "La Bella Italia",
  owner: "Giuseppe Rossi",
  email: "info@labellaitaliarestaurante.com",
  phone: "+52 55 1234 5678",
  address: "Av. Revolución 1234, Col. San Ángel, CDMX 01000",
  website: "https://labellaitaliarestaurante.com",
  description: "Auténtica cocina italiana en el corazón de la Ciudad de México. Especialistas en pasta fresca, pizza artesanal y los mejores vinos italianos. Un ambiente familiar y acogedor que te transportará directamente a Italia.",
  category: "Restaurante Italiano",
  logo: "",
  isVerified: true,
  socialMedia: {
    facebook: "https://facebook.com/labellaitaliacdmx",
    instagram: "https://instagram.com/labellaitaliacdmx",
    whatsapp: "+525512345678"
  },
  schedule: {
    lunes: { isOpen: true, openTime: "12:00", closeTime: "22:00" },
    martes: { isOpen: true, openTime: "12:00", closeTime: "22:00" },
    miércoles: { isOpen: true, openTime: "12:00", closeTime: "22:00" },
    jueves: { isOpen: true, openTime: "12:00", closeTime: "22:00" },
    viernes: { isOpen: true, openTime: "12:00", closeTime: "23:00" },
    sábado: { isOpen: true, openTime: "13:00", closeTime: "23:00" },
    domingo: { isOpen: true, openTime: "13:00", closeTime: "21:00" }
  }
};

export default function PerfilPage() {
  const { data: session } = useSession();
  const [business, setBusiness] = useState<BusinessProfile | null>(null);
  const [_collaborator, setCollaborator] = useState<ApiCollaborator | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState<BusinessProfile | null>(null);

  // Cargar datos del colaborador
  useEffect(() => {
    loadCollaboratorProfile();
  }, [session]);

  const loadCollaboratorProfile = async () => {
    if (!session) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const sessionData = session as unknown as Record<string, unknown>;
      const cognitoUsername = (sessionData.cognitoUsername as string | undefined) ||
                             (sessionData.sub as string | undefined) ||
                             ((sessionData.user as Record<string, unknown> | undefined)?.id as string | undefined) ||
                             ((sessionData.user as Record<string, unknown> | undefined)?.sub as string | undefined);

      if (cognitoUsername) {
        console.log('🔄 Loading collaborator profile...');

        // Cargar datos del colaborador
        const collaboratorData = await promotionApiService.getCollaboratorByCognitoId(cognitoUsername);
        setCollaborator(collaboratorData);

        // Mapear datos del colaborador al perfil de negocio
        const businessProfile: BusinessProfile = {
          id: collaboratorData.id.toString(),
          name: collaboratorData.businessName,
          owner: collaboratorData.representativeName,
          email: collaboratorData.email,
          phone: collaboratorData.phone,
          address: collaboratorData.address,
          website: "",
          description: collaboratorData.description || "Descripción no disponible",
          category: collaboratorData.categories?.[0]?.name || "GENERAL",
          logo: collaboratorData.logoUrl || "",
          isVerified: true, // Asumir que están verificados
          socialMedia: {
            facebook: "",
            instagram: "",
            whatsapp: collaboratorData.phone
          },
          schedule: {
            lunes: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
            martes: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
            miércoles: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
            jueves: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
            viernes: { isOpen: true, openTime: "09:00", closeTime: "23:00" },
            sábado: { isOpen: true, openTime: "10:00", closeTime: "23:00" },
            domingo: { isOpen: true, openTime: "10:00", closeTime: "21:00" }
          }
        };

        setBusiness(businessProfile);
        setEditData(businessProfile);
        console.log('✅ Profile loaded for:', collaboratorData.businessName);
      }
    } catch (err) {
      console.error('❌ Error loading profile:', err);
      setError('Error al cargar el perfil del colaborador');
      // Usar datos mock en caso de error
      setBusiness(mockBusiness);
      setEditData(mockBusiness);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = () => {
    setIsEditing(true);
    setEditData(business ? { ...business } : null);
  };

  const handleSave = () => {
    setBusiness(editData);
    setIsEditing(false);
    console.log("Business profile updated:", editData);
    // TODO: Save to backend
  };

  const handleCancel = () => {
    setEditData(business ? { ...business } : null);
    setIsEditing(false);
  };

  const handleInputChange = (field: string, value: string) => {
    setEditData(prev => prev ? ({
      ...prev,
      [field]: value
    }) : null);
  };

  const handleSocialMediaChange = (platform: string, value: string) => {
    setEditData(prev => prev ? ({
      ...prev,
      socialMedia: {
        ...prev.socialMedia,
        [platform]: value
      }
    }) : null);
  };

  const handleScheduleChange = (day: string, field: string, value: string | boolean) => {
    setEditData(prev => prev ? ({
      ...prev,
      schedule: {
        ...prev.schedule,
        [day]: {
          ...prev.schedule[day],
          [field]: value
        }
      }
    }) : null);
  };

  const formatPhone = (phone: string) => {
    return phone.replace(/(\+52)(\d{2})(\d{4})(\d{4})/, '$1 $2 $3 $4');
  };

  const actions = isEditing ? (
    <div className="flex space-x-3">
      <button
        onClick={handleCancel}
        className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
      >
        Cancelar
      </button>
      <button
        onClick={handleSave}
        className="px-4 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#007580] transition-colors"
      >
        Guardar Cambios
      </button>
    </div>
  ) : (
    <button
      onClick={handleEdit}
      className="inline-flex items-center px-4 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#007580] transition-colors"
    >
      <PencilIcon className="h-5 w-5 mr-2" />
      Editar Perfil
    </button>
  );

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
          <div className="text-[#969696] mt-2">Cargando perfil...</div>
        </div>
      </div>
    );
  }

  if (!business) {
    return (
      <div className="space-y-6">
        <div className="text-center py-12">
          <div className="text-gray-500">No se pudo cargar el perfil del negocio</div>
          {error && <div className="text-red-500 mt-2">{error}</div>}
        </div>
      </div>
    );
  }

  const currentData = isEditing ? editData : business;

  if (!currentData) {
    return null;
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Perfil del Negocio</h1>
          <p className="text-gray-600">
            {business.name} - Gestiona la información de tu negocio
          </p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {actions}
      </div>
        {/* Header Card */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex flex-col md:flex-row items-start md:items-center space-y-4 md:space-y-0 md:space-x-6">
            {/* Logo */}
            <div className="relative h-20 w-20 rounded-full overflow-hidden bg-gray-200 flex-shrink-0">
              <Image
                src={currentData.logo || ""}
                alt={currentData.name}
                fill
                className="object-cover"
                onError={(e) => {
                  const target = e.target as HTMLImageElement;
                  target.src = ``;
                }}
              />
            </div>

            {/* Business Info */}
            <div className="flex-1">
              <div className="flex items-center space-x-3 mb-2">
                {isEditing ? (
                  <input
                    type="text"
                    value={currentData.name}
                    onChange={(e) => handleInputChange("name", e.target.value)}
                    className="text-2xl font-bold text-gray-900 bg-transparent border-b-2 border-[#008D96] focus:outline-none"
                  />
                ) : (
                  <h1 className="text-2xl font-bold text-gray-900">{currentData.name}</h1>
                )}

                {currentData.isVerified && (
                  <CheckBadgeIcon className="h-6 w-6 text-blue-500" title="Negocio Verificado" />
                )}

                <span className="bg-gray-100 text-gray-800 px-3 py-1 rounded-full text-sm">
                  {currentData.category}
                </span>
              </div>

              {isEditing ? (
                <input
                  type="text"
                  value={currentData.owner}
                  onChange={(e) => handleInputChange("owner", e.target.value)}
                  className="text-gray-600 bg-transparent border-b border-gray-300 focus:outline-none focus:border-[#008D96]"
                  placeholder="Propietario"
                />
              ) : (
                <p className="text-gray-600">Propietario: {currentData.owner}</p>
              )}
            </div>
          </div>
        </div>

        {/* Contact Information */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
            <EnvelopeIcon className="h-5 w-5 mr-2" />
            Información de Contacto
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Email */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              {isEditing ? (
                <input
                  type="email"
                  value={currentData.email}
                  onChange={(e) => handleInputChange("email", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                />
              ) : (
                <p className="text-gray-900">{currentData.email}</p>
              )}
            </div>

            {/* Phone */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Teléfono
              </label>
              {isEditing ? (
                <input
                  type="tel"
                  value={currentData.phone}
                  onChange={(e) => handleInputChange("phone", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                />
              ) : (
                <p className="text-gray-900">{formatPhone(currentData.phone)}</p>
              )}
            </div>

            {/* Address */}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Dirección
              </label>
              {isEditing ? (
                <textarea
                  value={currentData.address}
                  onChange={(e) => handleInputChange("address", e.target.value)}
                  rows={2}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                />
              ) : (
                <p className="text-gray-900">{currentData.address}</p>
              )}
            </div>

            {/* Website */}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Página Web
              </label>
              {isEditing ? (
                <input
                  type="url"
                  value={currentData.website || ""}
                  onChange={(e) => handleInputChange("website", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                  placeholder="https://..."
                />
              ) : (
                currentData.website ? (
                  <a
                    href={currentData.website}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-[#008D96] hover:underline"
                  >
                    {currentData.website}
                  </a>
                ) : (
                  <p className="text-gray-500">No especificado</p>
                )
              )}
            </div>
          </div>
        </div>

        {/* Description */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
            <BuildingStorefrontIcon className="h-5 w-5 mr-2" />
            Descripción del Negocio
          </h2>

          {isEditing ? (
            <textarea
              value={currentData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
              placeholder="Describe tu negocio..."
            />
          ) : (
            <p className="text-gray-700 leading-relaxed">{currentData.description}</p>
          )}
        </div>

        {/* Social Media */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">
            Redes Sociales
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Object.entries(currentData.socialMedia).map(([platform, url]) => (
              <div key={platform}>
                <label className="block text-sm font-medium text-gray-700 mb-2 capitalize">
                  {platform}
                </label>
                {isEditing ? (
                  <input
                    type="url"
                    value={url || ""}
                    onChange={(e) => handleSocialMediaChange(platform, e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                    placeholder={`URL de ${platform}`}
                  />
                ) : (
                  url ? (
                    <a
                      href={url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-[#008D96] hover:underline"
                    >
                      {url}
                    </a>
                  ) : (
                    <p className="text-gray-500">No especificado</p>
                  )
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Schedule */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
            <ClockIcon className="h-5 w-5 mr-2" />
            Horarios de Atención
          </h2>

          <div className="space-y-4">
            {Object.entries(currentData.schedule).map(([day, schedule]) => (
              <div key={day} className="flex items-center space-x-4">
                <div className="w-20">
                  <span className="text-sm font-medium text-gray-700 capitalize">
                    {day}
                  </span>
                </div>

                {isEditing ? (
                  <>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={schedule.isOpen}
                        onChange={(e) => handleScheduleChange(day, "isOpen", e.target.checked)}
                        className="rounded border-gray-300 text-[#008D96] focus:ring-[#008D96]"
                      />
                      <span className="ml-2 text-sm text-gray-600">Abierto</span>
                    </label>

                    {schedule.isOpen && (
                      <>
                        <input
                          type="time"
                          value={schedule.openTime}
                          onChange={(e) => handleScheduleChange(day, "openTime", e.target.value)}
                          className="px-2 py-1 border border-gray-300 rounded text-sm focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                        />
                        <span className="text-gray-500">-</span>
                        <input
                          type="time"
                          value={schedule.closeTime}
                          onChange={(e) => handleScheduleChange(day, "closeTime", e.target.value)}
                          className="px-2 py-1 border border-gray-300 rounded text-sm focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                        />
                      </>
                    )}
                  </>
                ) : (
                  <div className="flex items-center space-x-2">
                    {schedule.isOpen ? (
                      <>
                        <span className="text-green-600 text-sm font-medium">Abierto</span>
                        <span className="text-gray-600 text-sm">
                          {schedule.openTime} - {schedule.closeTime}
                        </span>
                      </>
                    ) : (
                      <span className="text-red-600 text-sm font-medium">Cerrado</span>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
    </div>
  );
}