'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import {
  BellIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  CheckCircleIcon,
  TrashIcon,
  EyeIcon,
  ClockIcon,
  MagnifyingGlassIcon,
  FunnelIcon
} from '@heroicons/react/24/outline';
import { promotionApiService } from '../promociones/services/api';
import { ApiCollaborator } from '../promociones/types';

interface Notification {
  id: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message: string;
  timestamp: string;
  isRead: boolean;
  category: 'promotion' | 'redemption' | 'review' | 'system' | 'payment';
  actionUrl?: string;
  metadata?: {
    promotionId?: string;
    userId?: string;
    amount?: number;
  };
}

const mockNotifications: Notification[] = [
  {
    id: '1',
    type: 'success',
    title: 'Nueva promoción creada',
    message: 'Tu promoción "Descuento 50% en Pizzas" ha sido publicada exitosamente.',
    timestamp: '2024-01-15T10:30:00Z',
    isRead: false,
    category: 'promotion',
    actionUrl: '/colaborator/promociones',
    metadata: { promotionId: 'promo_123' }
  },
  {
    id: '2',
    type: 'info',
    title: 'Promoción canjeada',
    message: 'Un cliente ha canjeado tu promoción "Mesa para 4 personas".',
    timestamp: '2024-01-15T09:15:00Z',
    isRead: false,
    category: 'redemption',
    metadata: { promotionId: 'promo_456', userId: 'user_789' }
  },
  {
    id: '3',
    type: 'warning',
    title: 'Promoción por vencer',
    message: 'Tu promoción "Combo Familiar" vence en 2 días.',
    timestamp: '2024-01-14T16:45:00Z',
    isRead: true,
    category: 'promotion'
  },
  {
    id: '4',
    type: 'success',
    title: 'Nueva reseña recibida',
    message: 'Recibiste una nueva reseña de 5 estrellas: "Excelente servicio y comida deliciosa"',
    timestamp: '2024-01-14T14:20:00Z',
    isRead: true,
    category: 'review'
  },
  {
    id: '5',
    type: 'error',
    title: 'Problema con promoción',
    message: 'Tu promoción "Descuento 30%" ha sido pausada por violación de términos.',
    timestamp: '2024-01-13T11:30:00Z',
    isRead: false,
    category: 'system'
  },
  {
    id: '6',
    type: 'info',
    title: 'Pago procesado',
    message: 'Se ha procesado tu pago mensual de $299.00 MXN.',
    timestamp: '2024-01-12T08:00:00Z',
    isRead: true,
    category: 'payment',
    metadata: { amount: 299 }
  }
];

export default function NotificacionesPage() {
  const { data: session } = useSession();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [collaborator, setCollaborator] = useState<ApiCollaborator | null>(null);
  const [filter, setFilter] = useState<'all' | 'unread' | 'promotion' | 'redemption' | 'review' | 'system' | 'payment'>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedNotifications, setSelectedNotifications] = useState<string[]>([]);

  // Cargar datos del colaborador y generar notificaciones
  useEffect(() => {
    loadCollaboratorAndNotifications();
  }, [session]);

  const loadCollaboratorAndNotifications = async () => {
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
        console.log('🔄 Loading collaborator notifications...');

        // Cargar datos del colaborador
        const collaboratorData = await promotionApiService.getCollaboratorByCognitoId(cognitoUsername);
        setCollaborator(collaboratorData);

        // Cargar promociones del colaborador
        const promotionsData = await promotionApiService.getPromotions(cognitoUsername);

        // Generar notificaciones basadas en los datos reales
        const generatedNotifications: Notification[] = [];

        // Notificaciones para promociones activas
        promotionsData.forEach((promotion) => {
          if (promotion.promotionState === 'activa') {
            generatedNotifications.push({
              id: `promo_${promotion.promotionId}`,
              type: 'success',
              title: 'Promoción activa',
              message: `Tu promoción "${promotion.title}" está activa y recibiendo visualizaciones.`,
              timestamp: promotion.created_at || new Date().toISOString(),
              isRead: Math.random() > 0.7, // Algunas leídas, otras no
              category: 'promotion',
              actionUrl: '/colaborator/promociones',
              metadata: { promotionId: promotion.promotionId.toString() }
            });
          }

          // Simular notificaciones de canjes basadas en stock usado
          const stockUsed = (promotion.totalStock || 0) - (promotion.availableStock || 0);
          if (stockUsed > 0) {
            const canjesCount = Math.floor(stockUsed / 3); // Simular múltiples canjes
            for (let i = 0; i < Math.min(canjesCount, 2); i++) {
              generatedNotifications.push({
                id: `redemption_${promotion.promotionId}_${i}`,
                type: 'info',
                title: 'Promoción canjeada',
                message: `Un cliente ha canjeado tu promoción "${promotion.title}".`,
                timestamp: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(), // Últimos 7 días
                isRead: Math.random() > 0.5,
                category: 'redemption',
                metadata: { promotionId: promotion.promotionId.toString(), userId: `user_${Math.floor(Math.random() * 1000)}` }
              });
            }
          }

          // Advertencias para promociones que vencen pronto
          const endDate = new Date(promotion.endDate);
          const now = new Date();
          const daysUntilEnd = Math.ceil((endDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));

          if (daysUntilEnd <= 3 && daysUntilEnd > 0) {
            generatedNotifications.push({
              id: `warning_${promotion.promotionId}`,
              type: 'warning',
              title: 'Promoción por vencer',
              message: `Tu promoción "${promotion.title}" vence en ${daysUntilEnd} día${daysUntilEnd === 1 ? '' : 's'}.`,
              timestamp: new Date().toISOString(),
              isRead: false,
              category: 'promotion'
            });
          }
        });

        // Notificación de bienvenida
        generatedNotifications.push({
          id: 'welcome',
          type: 'info',
          title: '¡Bienvenido a tu panel!',
          message: `Hola ${collaboratorData.businessName}, aquí podrás gestionar todas tus promociones y ver el rendimiento de tu negocio.`,
          timestamp: collaboratorData.registrationDate,
          isRead: true,
          category: 'system'
        });

        // Notificación de sistema sobre estadísticas
        if (promotionsData.length > 0) {
          generatedNotifications.push({
            id: 'stats_update',
            type: 'success',
            title: 'Estadísticas actualizadas',
            message: `Tienes ${promotionsData.length} promociones activas. ¡Revisa tus estadísticas detalladas!`,
            timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), // Hace 2 horas
            isRead: false,
            category: 'system'
          });
        }

        // Ordenar por fecha más reciente
        generatedNotifications.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

        setNotifications(generatedNotifications);
        console.log('✅ Notifications generated for:', collaboratorData.businessName);
      }
    } catch (err) {
      console.error('❌ Error loading notifications:', err);
      setError('Error al cargar las notificaciones');
      // Usar notificaciones mock en caso de error
      setNotifications(mockNotifications);
    } finally {
      setLoading(false);
    }
  };

  const getIcon = (type: Notification['type']) => {
    switch (type) {
      case 'success':
        return CheckCircleIcon;
      case 'warning':
        return ExclamationTriangleIcon;
      case 'error':
        return ExclamationTriangleIcon;
      case 'info':
      default:
        return InformationCircleIcon;
    }
  };

  const getIconColor = (type: Notification['type']) => {
    switch (type) {
      case 'success':
        return 'text-green-500';
      case 'warning':
        return 'text-yellow-500';
      case 'error':
        return 'text-red-500';
      case 'info':
      default:
        return 'text-blue-500';
    }
  };

  const getCategoryName = (category: Notification['category']) => {
    switch (category) {
      case 'promotion':
        return 'Promociones';
      case 'redemption':
        return 'Canjes';
      case 'review':
        return 'Reseñas';
      case 'system':
        return 'Sistema';
      case 'payment':
        return 'Pagos';
      default:
        return 'General';
    }
  };

  const getCategoryColor = (category: Notification['category']) => {
    switch (category) {
      case 'promotion':
        return 'bg-purple-100 text-purple-800';
      case 'redemption':
        return 'bg-green-100 text-green-800';
      case 'review':
        return 'bg-yellow-100 text-yellow-800';
      case 'system':
        return 'bg-red-100 text-red-800';
      case 'payment':
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const filteredNotifications = notifications.filter(notification => {
    const matchesFilter = filter === 'all' ||
      (filter === 'unread' && !notification.isRead) ||
      notification.category === filter;

    const matchesSearch = searchTerm === '' ||
      notification.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      notification.message.toLowerCase().includes(searchTerm.toLowerCase());

    return matchesFilter && matchesSearch;
  });

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const markAsRead = (id: string) => {
    setNotifications(prev =>
      prev.map(notification =>
        notification.id === id
          ? { ...notification, isRead: true }
          : notification
      )
    );
  };

  const markAllAsRead = () => {
    setNotifications(prev =>
      prev.map(notification => ({ ...notification, isRead: true }))
    );
  };

  const deleteNotification = (id: string) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  const deleteSelected = () => {
    setNotifications(prev => prev.filter(n => !selectedNotifications.includes(n.id)));
    setSelectedNotifications([]);
  };

  const toggleSelectAll = () => {
    if (selectedNotifications.length === filteredNotifications.length) {
      setSelectedNotifications([]);
    } else {
      setSelectedNotifications(filteredNotifications.map(n => n.id));
    }
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);
    const diffDays = diffMs / (1000 * 60 * 60 * 24);

    if (diffHours < 1) {
      const diffMins = Math.floor(diffMs / (1000 * 60));
      return `Hace ${diffMins} min`;
    } else if (diffHours < 24) {
      return `Hace ${Math.floor(diffHours)} h`;
    } else if (diffDays < 7) {
      return `Hace ${Math.floor(diffDays)} días`;
    } else {
      return date.toLocaleDateString();
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="py-8">
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
            <div className="text-[#969696] mt-2">Cargando notificaciones...</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 flex items-center">
              <BellIcon className="h-8 w-8 mr-3 text-[#008D96]" />
              Notificaciones
              {unreadCount > 0 && (
                <span className="ml-3 bg-red-500 text-white text-sm font-medium px-2 py-1 rounded-full">
                  {unreadCount}
                </span>
              )}
            </h1>
            <p className="text-gray-600">
              {collaborator ? `${collaborator.businessName} - Mantente al día con las actividades` : "Mantente al día con las actividades de tu negocio"}
            </p>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

            <div className="flex items-center space-x-3">
              {selectedNotifications.length > 0 && (
                <button
                  onClick={deleteSelected}
                  className="flex items-center px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                >
                  <TrashIcon className="h-4 w-4 mr-2" />
                  Eliminar ({selectedNotifications.length})
                </button>
              )}
              {unreadCount > 0 && (
                <button
                  onClick={markAllAsRead}
                  className="flex items-center px-4 py-2 bg-[#008D96] text-white rounded-lg hover:bg-[#007580]"
                >
                  <EyeIcon className="h-4 w-4 mr-2" />
                  Marcar todas como leídas
                </button>
              )}
            </div>
          </div>

          {/* Filters and Search */}
          <div className="bg-white rounded-lg shadow p-6 mb-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center space-y-4 sm:space-y-0 sm:space-x-4">
              {/* Search */}
              <div className="relative flex-1">
                <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Buscar notificaciones..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                />
              </div>

              {/* Filter */}
              <div className="flex items-center space-x-2">
                <FunnelIcon className="h-5 w-5 text-gray-400" />
                <select
                  value={filter}
                  onChange={(e) => setFilter(e.target.value as 'all' | 'unread' | 'promotion' | 'redemption' | 'review' | 'system' | 'payment')}
                  className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
                >
                  <option value="all">Todas</option>
                  <option value="unread">No leídas</option>
                  <option value="promotion">Promociones</option>
                  <option value="redemption">Canjes</option>
                  <option value="review">Reseñas</option>
                  <option value="system">Sistema</option>
                  <option value="payment">Pagos</option>
                </select>
              </div>
            </div>
          </div>

          {/* Notifications List */}
          <div className="bg-white rounded-lg shadow overflow-hidden">
            {/* Select All Header */}
            {filteredNotifications.length > 0 && (
              <div className="px-6 py-3 bg-gray-50 border-b border-gray-200">
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={selectedNotifications.length === filteredNotifications.length && filteredNotifications.length > 0}
                    onChange={toggleSelectAll}
                    className="rounded border-gray-300 text-[#008D96] focus:ring-[#008D96]"
                  />
                  <span className="ml-2 text-sm text-gray-700">
                    Seleccionar todas ({filteredNotifications.length})
                  </span>
                </label>
              </div>
            )}

            {/* Notifications */}
            <div className="divide-y divide-gray-200">
              {filteredNotifications.length === 0 ? (
                <div className="px-6 py-12 text-center">
                  <BellIcon className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">
                    {filter === 'unread' ? 'No hay notificaciones sin leer' : 'No hay notificaciones'}
                  </h3>
                  <p className="text-gray-600">
                    {filter === 'unread'
                      ? 'Todas tus notificaciones están al día'
                      : 'Las notificaciones aparecerán aquí cuando ocurran eventos importantes'
                    }
                  </p>
                </div>
              ) : (
                filteredNotifications.map((notification) => {
                  const IconComponent = getIcon(notification.type);
                  const isSelected = selectedNotifications.includes(notification.id);

                  return (
                    <div
                      key={notification.id}
                      className={`px-6 py-4 hover:bg-gray-50 transition-colors ${
                        !notification.isRead ? 'bg-blue-50' : ''
                      } ${isSelected ? 'bg-blue-100' : ''}`}
                    >
                      <div className="flex items-start space-x-4">
                        {/* Checkbox */}
                        <input
                          type="checkbox"
                          checked={isSelected}
                          onChange={(e) => {
                            if (e.target.checked) {
                              setSelectedNotifications(prev => [...prev, notification.id]);
                            } else {
                              setSelectedNotifications(prev => prev.filter(id => id !== notification.id));
                            }
                          }}
                          className="mt-1 rounded border-gray-300 text-[#008D96] focus:ring-[#008D96]"
                        />

                        {/* Icon */}
                        <div className="flex-shrink-0">
                          <IconComponent className={`h-6 w-6 ${getIconColor(notification.type)}`} />
                        </div>

                        {/* Content */}
                        <div className="flex-1 min-w-0">
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center space-x-2 mb-1">
                                <h3 className={`text-sm font-medium ${!notification.isRead ? 'text-gray-900' : 'text-gray-700'}`}>
                                  {notification.title}
                                </h3>
                                <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getCategoryColor(notification.category)}`}>
                                  {getCategoryName(notification.category)}
                                </span>
                                {!notification.isRead && (
                                  <div className="w-2 h-2 bg-[#008D96] rounded-full"></div>
                                )}
                              </div>
                              <p className="text-sm text-gray-600 mb-2">{notification.message}</p>
                              <div className="flex items-center space-x-4 text-xs text-gray-500">
                                <span className="flex items-center">
                                  <ClockIcon className="h-3 w-3 mr-1" />
                                  {formatTimestamp(notification.timestamp)}
                                </span>
                                {notification.metadata?.amount && (
                                  <span className="font-medium">
                                    ${notification.metadata.amount.toLocaleString()} MXN
                                  </span>
                                )}
                              </div>
                            </div>

                            {/* Actions */}
                            <div className="flex items-center space-x-2 ml-4">
                              {!notification.isRead && (
                                <button
                                  onClick={() => markAsRead(notification.id)}
                                  className="p-1 text-gray-400 hover:text-[#008D96] transition-colors"
                                  title="Marcar como leída"
                                >
                                  <EyeIcon className="h-4 w-4" />
                                </button>
                              )}
                              <button
                                onClick={() => deleteNotification(notification.id)}
                                className="p-1 text-gray-400 hover:text-red-600 transition-colors"
                                title="Eliminar notificación"
                              >
                                <TrashIcon className="h-4 w-4" />
                              </button>
                            </div>
                          </div>

                          {/* Action Button */}
                          {notification.actionUrl && (
                            <div className="mt-3">
                              <button className="text-sm text-[#008D96] hover:text-[#007580] font-medium">
                                Ver detalles →
                              </button>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>

          {/* Summary Stats */}
          {filteredNotifications.length > 0 && (
            <div className="mt-6 bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Resumen de Notificaciones</h3>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-900">{notifications.length}</div>
                  <div className="text-sm text-gray-600">Total</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-[#008D96]">{unreadCount}</div>
                  <div className="text-sm text-gray-600">Sin leer</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {notifications.filter(n => n.category === 'redemption').length}
                  </div>
                  <div className="text-sm text-gray-600">Canjes</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-purple-600">
                    {notifications.filter(n => n.category === 'promotion').length}
                  </div>
                  <div className="text-sm text-gray-600">Promociones</div>
                </div>
              </div>
            </div>
          )}
      </div>
    </div>
  );
}