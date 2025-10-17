'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import Image from 'next/image';
import {
  PencilIcon,
  CheckBadgeIcon,
  BuildingStorefrontIcon,
  EnvelopeIcon,
  PhoneIcon,
  MapPinIcon,
  GlobeAltIcon,
  ClockIcon,
  StarIcon,
  EyeIcon,
  TrophyIcon,
  CalendarIcon,
  UserGroupIcon,
  CameraIcon,
  ShareIcon,
  PrinterIcon,
  DocumentDuplicateIcon
} from '@heroicons/react/24/outline';

interface BusinessProfile {
  id: string;
  name: string;
  owner: string;
  email: string;
  phone: string;
  address: string;
  website?: string;
  description: string;
  category: string;
  logo?: string;
  coverImage?: string;
  isVerified: boolean;
  rating: number;
  totalReviews: number;
  totalPromotions: number;
  activePromotions: number;
  totalRedemptions: number;
  memberSince: string;
  socialMedia: {
    facebook?: string;
    instagram?: string;
    whatsapp?: string;
    twitter?: string;
  };
  schedule: {
    [key: string]: { isOpen: boolean; openTime: string; closeTime: string };
  };
  gallery: string[];
  tags: string[];
  achievements: Array<{
    id: string;
    title: string;
    description: string;
    icon: string;
    earnedDate: string;
  }>;
}

const mockBusiness: BusinessProfile = {
  id: "1",
  name: "Restaurante Luna",
  owner: "María González",
  email: "info@restauranteluna.com",
  phone: "+52 55 1234 5678",
  address: "Av. Constitución 123, Atizapán de Zaragoza, Estado de México",
  website: "https://restauranteluna.com",
  description: "Restaurante familiar especializado en cocina mexicana tradicional con un toque contemporáneo. Ofrecemos platillos auténticos preparados con ingredientes frescos y de la mejor calidad.",
  category: "COMIDA",
  logo: "",
  coverImage: "",
  isVerified: true,
  rating: 4.7,
  totalReviews: 156,
  totalPromotions: 24,
  activePromotions: 8,
  totalRedemptions: 342,
  memberSince: "2023-01-15",
  socialMedia: {
    facebook: "https://facebook.com/restauranteluna",
    instagram: "https://instagram.com/restaurante_luna",
    whatsapp: "+525512345678"
  },
  schedule: {
    monday: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
    tuesday: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
    wednesday: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
    thursday: { isOpen: true, openTime: "09:00", closeTime: "22:00" },
    friday: { isOpen: true, openTime: "09:00", closeTime: "23:00" },
    saturday: { isOpen: true, openTime: "10:00", closeTime: "23:00" },
    sunday: { isOpen: true, openTime: "10:00", closeTime: "21:00" }
  },
  gallery: [
    "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400",
    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400",
    "https://images.unsplash.com/photo-1551632811-561732d1e306?w=400"
  ],
  tags: ["Familiar", "Comida Casera", "Ambiente Acogedor", "Terraza"],
  achievements: [
    {
      id: "1",
      title: "Negocio Verificado",
      description: "Tu negocio ha sido verificado por nuestro equipo",
      icon: "🏆",
      earnedDate: "2023-02-01"
    },
    {
      id: "2",
      title: "100 Canjes",
      description: "Has alcanzado 100 canjes de promociones",
      icon: "🎯",
      earnedDate: "2023-06-15"
    },
    {
      id: "3",
      title: "Excelente Rating",
      description: "Mantienes un rating superior a 4.5 estrellas",
      icon: "⭐",
      earnedDate: "2023-08-20"
    }
  ]
};

export default function PerfilMejoradoPage() {
  const { data: session } = useSession();
  const [business, setBusiness] = useState<BusinessProfile>(mockBusiness);
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState<BusinessProfile>(mockBusiness);
  const [activeTab, setActiveTab] = useState<'general' | 'gallery' | 'achievements'>('general');

  const handleEdit = () => {
    setIsEditing(true);
    setEditData({ ...business });
  };

  const handleSave = () => {
    setBusiness(editData);
    setIsEditing(false);
    console.log("Business profile updated:", editData);
  };

  const handleCancel = () => {
    setEditData({ ...business });
    setIsEditing(false);
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: business.name,
        text: business.description,
        url: window.location.href,
      });
    } else {
      navigator.clipboard.writeText(window.location.href);
      alert('Enlace copiado al portapapeles');
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const dayNames = {
    monday: 'Lunes',
    tuesday: 'Martes',
    wednesday: 'Miércoles',
    thursday: 'Jueves',
    friday: 'Viernes',
    saturday: 'Sábado',
    sunday: 'Domingo'
  };

  const currentData = isEditing ? editData : business;

  const headerActions = (
    <div className="flex items-center space-x-3">
      <button
        onClick={handleShare}
        className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
        title="Compartir perfil"
      >
        <ShareIcon className="h-5 w-5" />
      </button>
      <button
        onClick={handlePrint}
        className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
        title="Imprimir perfil"
      >
        <PrinterIcon className="h-5 w-5" />
      </button>
      {isEditing ? (
        <div className="flex space-x-2">
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
            Guardar
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
      )}
    </div>
  );

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="py-8 space-y-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Perfil del Negocio</h1>
            <p className="text-gray-600">Gestiona la información de tu negocio</p>
          </div>
          {headerActions}
        </div>

          {/* Cover Image & Profile Header */}
          <div className="bg-white rounded-lg shadow overflow-hidden">
            {/* Cover Image */}
            <div className="h-48 bg-gradient-to-r from-[#008D96] to-[#00C0CC] relative">
              {currentData.coverImage && (
                <Image
                  src={currentData.coverImage}
                  alt="Cover"
                  fill
                  className="object-cover"
                />
              )}
              {isEditing && (
                <button className="absolute top-4 right-4 p-2 bg-black bg-opacity-50 text-white rounded-lg hover:bg-opacity-70">
                  <CameraIcon className="h-5 w-5" />
                </button>
              )}
            </div>

            {/* Profile Info */}
            <div className="px-6 pb-6">
              <div className="flex flex-col sm:flex-row items-start sm:items-end space-y-4 sm:space-y-0 sm:space-x-6 -mt-16">
                {/* Logo */}
                <div className="relative">
                  <div className="h-32 w-32 rounded-full overflow-hidden bg-white border-4 border-white shadow-lg">
                    <Image
                      src={currentData.logo || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentData.name)}&background=008D96&color=fff&size=128`}
                      alt={currentData.name}
                      width={128}
                      height={128}
                      className="object-cover"
                    />
                  </div>
                  {isEditing && (
                    <button className="absolute bottom-0 right-0 p-2 bg-[#008D96] text-white rounded-full hover:bg-[#007580]">
                      <CameraIcon className="h-4 w-4" />
                    </button>
                  )}
                </div>

                {/* Business Info */}
                <div className="flex-1 pt-4">
                  <div className="flex items-center space-x-3 mb-2">
                    {isEditing ? (
                      <input
                        type="text"
                        value={currentData.name}
                        onChange={(e) => setEditData(prev => ({ ...prev, name: e.target.value }))}
                        className="text-2xl font-bold text-gray-900 bg-transparent border-b-2 border-[#008D96] focus:outline-none"
                      />
                    ) : (
                      <h1 className="text-2xl font-bold text-gray-900">{currentData.name}</h1>
                    )}

                    {currentData.isVerified && (
                      <CheckBadgeIcon className="h-6 w-6 text-blue-500" title="Negocio Verificado" />
                    )}
                  </div>

                  <div className="flex flex-wrap items-center gap-4 text-sm text-gray-600 mb-4">
                    <span className="flex items-center">
                      <BuildingStorefrontIcon className="h-4 w-4 mr-1" />
                      {currentData.category}
                    </span>
                    <span className="flex items-center">
                      <StarIcon className="h-4 w-4 mr-1 text-yellow-500" />
                      {currentData.rating} ({currentData.totalReviews} reseñas)
                    </span>
                    <span className="flex items-center">
                      <CalendarIcon className="h-4 w-4 mr-1" />
                      Miembro desde {new Date(currentData.memberSince).toLocaleDateString()}
                    </span>
                  </div>

                  {/* Tags */}
                  <div className="flex flex-wrap gap-2">
                    {currentData.tags.map((tag, index) => (
                      <span key={index} className="px-3 py-1 bg-[#008D96] bg-opacity-10 text-[#008D96] rounded-full text-sm">
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Stats */}
                <div className="flex space-x-8 pt-4">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-gray-900">{currentData.totalPromotions}</div>
                    <div className="text-sm text-gray-600">Promociones</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-gray-900">{currentData.totalRedemptions}</div>
                    <div className="text-sm text-gray-600">Canjes</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-gray-900">{currentData.activePromotions}</div>
                    <div className="text-sm text-gray-600">Activas</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Tabs Navigation */}
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {[
                { id: 'general', name: 'Información General', icon: BuildingStorefrontIcon },
                { id: 'gallery', name: 'Galería', icon: EyeIcon },
                { id: 'achievements', name: 'Logros', icon: TrophyIcon }
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as 'general' | 'gallery' | 'achievements')}
                  className={`flex items-center space-x-2 py-4 px-1 border-b-2 font-medium text-sm ${
                    activeTab === tab.id
                      ? 'border-[#008D96] text-[#008D96]'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <tab.icon className="h-5 w-5" />
                  <span>{tab.name}</span>
                </button>
              ))}
            </nav>
          </div>

          {/* Tab Content */}
          <div className="space-y-6">
            {/* General Tab */}
            {activeTab === 'general' && (
              <>
                {/* Description */}
                <div className="bg-white rounded-lg shadow p-6">
                  <h2 className="text-xl font-semibold text-gray-900 mb-4">Descripción</h2>
                  {isEditing ? (
                    <textarea
                      value={currentData.description}
                      onChange={(e) => setEditData(prev => ({ ...prev, description: e.target.value }))}
                      rows={4}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                    />
                  ) : (
                    <p className="text-gray-700 leading-relaxed">{currentData.description}</p>
                  )}
                </div>

                {/* Contact & Hours Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {/* Contact Information */}
                  <div className="bg-white rounded-lg shadow p-6">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
                      <EnvelopeIcon className="h-5 w-5 mr-2" />
                      Información de Contacto
                    </h2>

                    <div className="space-y-4">
                      <div className="flex items-center space-x-3">
                        <EnvelopeIcon className="h-5 w-5 text-gray-400" />
                        <span className="text-gray-900">{currentData.email}</span>
                      </div>
                      <div className="flex items-center space-x-3">
                        <PhoneIcon className="h-5 w-5 text-gray-400" />
                        <span className="text-gray-900">{currentData.phone}</span>
                      </div>
                      <div className="flex items-start space-x-3">
                        <MapPinIcon className="h-5 w-5 text-gray-400 mt-0.5" />
                        <span className="text-gray-900">{currentData.address}</span>
                      </div>
                      {currentData.website && (
                        <div className="flex items-center space-x-3">
                          <GlobeAltIcon className="h-5 w-5 text-gray-400" />
                          <a
                            href={currentData.website}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-[#008D96] hover:underline"
                          >
                            {currentData.website}
                          </a>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Business Hours */}
                  <div className="bg-white rounded-lg shadow p-6">
                    <h2 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
                      <ClockIcon className="h-5 w-5 mr-2" />
                      Horarios de Atención
                    </h2>

                    <div className="space-y-3">
                      {Object.entries(currentData.schedule).map(([day, schedule]) => (
                        <div key={day} className="flex items-center justify-between">
                          <span className="text-sm font-medium text-gray-700">
                            {dayNames[day as keyof typeof dayNames]}
                          </span>
                          <div className="text-sm">
                            {schedule.isOpen ? (
                              <span className="text-green-600">
                                {schedule.openTime} - {schedule.closeTime}
                              </span>
                            ) : (
                              <span className="text-red-600">Cerrado</span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Social Media */}
                <div className="bg-white rounded-lg shadow p-6">
                  <h2 className="text-xl font-semibold text-gray-900 mb-4">Redes Sociales</h2>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {Object.entries(currentData.socialMedia).map(([platform, url]) => (
                      url && (
                        <a
                          key={platform}
                          href={url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-center space-x-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                        >
                          <div className="w-8 h-8 bg-[#008D96] rounded text-white flex items-center justify-center text-sm font-medium">
                            {platform.charAt(0).toUpperCase()}
                          </div>
                          <span className="text-gray-700 capitalize">{platform}</span>
                        </a>
                      )
                    ))}
                  </div>
                </div>
              </>
            )}

            {/* Gallery Tab */}
            {activeTab === 'gallery' && (
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-semibold text-gray-900">Galería de Fotos</h2>
                  {isEditing && (
                    <button className="flex items-center px-4 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#007580]">
                      <CameraIcon className="h-5 w-5 mr-2" />
                      Agregar Foto
                    </button>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {currentData.gallery.map((image, index) => (
                    <div key={index} className="relative group">
                      <div className="aspect-square rounded-lg overflow-hidden">
                        <Image
                          src={image}
                          alt={`Imagen ${index + 1}`}
                          width={300}
                          height={300}
                          className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-200"
                        />
                      </div>
                      {isEditing && (
                        <button className="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
                          ×
                        </button>
                      )}
                    </div>
                  ))}

                  {/* Add new photo placeholder */}
                  {isEditing && (
                    <div className="aspect-square border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center hover:border-[#008D96] transition-colors cursor-pointer">
                      <div className="text-center">
                        <CameraIcon className="h-12 w-12 text-gray-400 mx-auto mb-2" />
                        <span className="text-sm text-gray-600">Agregar Foto</span>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Achievements Tab */}
            {activeTab === 'achievements' && (
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
                  <TrophyIcon className="h-6 w-6 mr-2 text-yellow-500" />
                  Logros y Reconocimientos
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {currentData.achievements.map((achievement) => (
                    <div key={achievement.id} className="border border-gray-200 rounded-lg p-6 text-center hover:shadow-md transition-shadow">
                      <div className="text-4xl mb-3">{achievement.icon}</div>
                      <h3 className="font-semibold text-gray-900 mb-2">{achievement.title}</h3>
                      <p className="text-sm text-gray-600 mb-3">{achievement.description}</p>
                      <span className="text-xs text-gray-500">
                        Obtenido el {new Date(achievement.earnedDate).toLocaleDateString()}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
      </div>
    </div>
  );
}