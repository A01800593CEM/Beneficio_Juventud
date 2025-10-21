"use client";

import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect, useState, useCallback } from "react";
import {
  MapPinIcon,
  TagIcon,
  HeartIcon,
  StarIcon,
  GiftIcon,
  ShoppingBagIcon,
  QrCodeIcon,
  FunnelIcon,
  MagnifyingGlassIcon,
  UserCircleIcon,
  ChartBarIcon
} from "@heroicons/react/24/outline";
import { apiService } from "@/lib/api";
import { Promotion, Category } from "@/types/promotion";
import { User, UserStats } from "@/types/user";

export default function UsuarioNormal() {
  const { data: session, status } = useSession();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState("promociones");
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [userStats, setUserStats] = useState<UserStats | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [favoritePromotions, setFavoritePromotions] = useState<string[]>([]);
  const [showQRCode, setShowQRCode] = useState<string | null>(null);

  // Convert API promotion to usuario page format
  const convertApiPromotionForUser = (apiPromotion: any): Promotion => {
    // Extract discount percentage from promotionString
    let discount = 0;
    if (apiPromotion.promotionString) {
      const percentMatch = apiPromotion.promotionString.match(/(\d+)%/);
      const offMatch = apiPromotion.promotionString.match(/(\d+)% OFF/);
      if (percentMatch || offMatch) {
        discount = parseInt(percentMatch?.[1] || offMatch?.[1] || '0');
      } else if (apiPromotion.promotionString.includes('2x1')) {
        discount = 50; // 2x1 is essentially 50% off
      }
    }

    return {
      id: apiPromotion.promotionId?.toString() || '',
      title: apiPromotion.title || '',
      subtitle: apiPromotion.promotionString || `${discount}% de descuento`,
      body: apiPromotion.description || '',
      theme: 'light' as const,
      businessId: apiPromotion.collaboratorId?.toString() || '',
      businessName: `Negocio ${apiPromotion.collaboratorId || 'Local'}`,
      category: apiPromotion.categories?.[0]?.name || 'General',
      discount: discount,
      validFrom: apiPromotion.initialDate || '',
      validTo: apiPromotion.endDate || '',
      terms: `Stock disponible: ${apiPromotion.availableStock}/${apiPromotion.totalStock}. Límite por usuario: ${apiPromotion.limitPerUser}`,
      isActive: apiPromotion.promotionState === 'activa',
      createdAt: apiPromotion.created_at || new Date().toISOString(),
      updatedAt: apiPromotion.updated_at || new Date().toISOString(),
      maxRedemptions: apiPromotion.totalStock || 0,
      currentRedemptions: (apiPromotion.totalStock || 0) - (apiPromotion.availableStock || 0),
    };
  };

  const loadUserData = useCallback(async () => {
    try {
      setLoading(true);

      // Load promotions with fallback data
      try {
        console.log('🔄 Loading promotions for usuario page...');
        const promotionsData = await apiService.getPromotions();
        console.log('📦 Raw API promotions for usuario:', promotionsData);

        const convertedPromotions = promotionsData.map(convertApiPromotionForUser).filter(p => p.isActive);
        setPromotions(convertedPromotions);
        console.log('✅ Loaded and converted promotions for usuario:', convertedPromotions.length);
      } catch (error) {
        console.log('API promotions not available, using fallback data');
        // Fallback promotions data
        const fallbackPromotions: Promotion[] = [
          {
            id: "1",
            title: "20% descuento en restaurantes",
            subtitle: "Sabores únicos te esperan",
            body: "Disfruta de deliciosa comida con descuento especial en más de 50 restaurantes afiliados.",
            theme: "light" as const,
            businessId: "rest1",
            businessName: "Restaurante El Buen Sabor",
            category: "Comida",
            discount: 20,
            validFrom: new Date().toISOString(),
            validTo: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
            terms: "Válido de lunes a viernes. No acumulable con otras promociones.",
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            maxRedemptions: 100,
            currentRedemptions: 23,
          },
          {
            id: "2",
            title: "2x1 en cines",
            subtitle: "Doble diversión",
            body: "Compra un boleto y lleva otro gratis en funciones seleccionadas.",
            theme: "dark" as const,
            businessId: "cine1",
            businessName: "Cinépolis Atizapán",
            category: "Entretenimiento",
            discount: 50,
            validFrom: new Date().toISOString(),
            validTo: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString(),
            terms: "Válido en funciones de lunes a jueves. Excepto estrenos.",
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            maxRedemptions: 200,
            currentRedemptions: 87,
          },
          {
            id: "3",
            title: "15% en tiendas de ropa",
            subtitle: "Renueva tu estilo",
            body: "Descuento en toda la colección de primavera-verano.",
            theme: "light" as const,
            businessId: "fashion1",
            businessName: "Fashion Store",
            category: "Moda",
            discount: 15,
            validFrom: new Date().toISOString(),
            validTo: new Date(Date.now() + 20 * 24 * 60 * 60 * 1000).toISOString(),
            terms: "No válido en productos con descuento previo.",
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            maxRedemptions: 150,
            currentRedemptions: 45,
          }
        ];
        setPromotions(fallbackPromotions);
      }

      // Load categories from API
      try {
        console.log('🔄 Loading categories...');
        const categoriesData = await apiService.getCategories();
        console.log('📦 Raw API categories:', categoriesData);

        // Convert API categories to the expected format
        const convertedCategories = categoriesData.map((cat: any) => ({
          id: cat.id.toString(),
          name: cat.name,
          description: `Categoría ${cat.name}`,
          icon: cat.name === 'Comida' ? '🍽️' : cat.name === 'Entretenimiento' ? '🎭' : '🏷️'
        }));

        setCategories(convertedCategories);
        console.log('✅ Loaded categories:', convertedCategories);
      } catch (error) {
        console.log('API categories not available, using fallback data');
        const fallbackCategories: Category[] = [
          { id: "1", name: "Comida", description: "Restaurantes y comida", icon: "🍽️" },
          { id: "2", name: "Entretenimiento", description: "Cines, teatros y diversión", icon: "🎭" },
          { id: "3", name: "Moda", description: "Ropa y accesorios", icon: "👕" },
          { id: "4", name: "Deportes", description: "Gimnasios y deportes", icon: "⚽" },
          { id: "5", name: "Tecnología", description: "Electrónicos y gadgets", icon: "📱" },
        ];
        setCategories(fallbackCategories);
      }

      // Load user data from API - try multiple possible sources for cognito ID
      const sessionData = session as any;
      const sub = sessionData.sub ||
                 sessionData.cognitoUsername ||
                 sessionData.user?.id ||
                 sessionData.user?.sub ||
                 '';

      console.log('🔄 Loading user profile from API...');
      console.log('📋 Session details:', {
        user: session?.user,
        sessionKeys: Object.keys(sessionData),
        sub: sessionData.sub,
        cognitoUsername: sessionData.cognitoUsername,
        extractedSub: sub
      });

      if (sub) {
        try {
          console.log('🔍 Attempting to fetch user data by Cognito ID:', sub);
          console.log('🔍 Sub value type:', typeof sub, 'length:', sub.length);

          if (!sub || sub.trim() === '') {
            console.log('❌ Sub is empty or invalid');
            throw new Error('Cognito ID is empty');
          }

          const userData = await apiService.getUserByCognitoId(sub);
          setUser(userData);
          console.log('✅ User data loaded successfully:', {
            id: userData.sub,
            name: userData.name,
            email: userData.email,
            role: userData.role
          });

          // Load user stats
          try {
            console.log('🔍 Fetching user stats for ID:', userData.id);
            const statsData = await apiService.getUserStats(userData.id);
            setUserStats(statsData);
            console.log('✅ User stats loaded successfully:', statsData);
          } catch (statsError) {
            console.log('⚠️ User stats not available from API, using calculated data');
            const fallbackStats: UserStats = {
              totalPromotions: promotions.length || 3,
              activePromotions: promotions.filter(p => p.isActive).length || 3,
              totalRedemptions: 12,
              monthlyRedemptions: 3,
            };
            setUserStats(fallbackStats);
          }
        } catch (userError) {
          console.log('❌ User not found in API, creating fallback user from session');
          console.log('Error details:', userError);

          // Create fallback user from session data
          const fallbackUser: User = {
            id: 'session-user',
            name: session?.user?.name?.split(' ')[0] || 'Usuario',
            lastNamePaternal: session?.user?.name?.split(' ')[1] || '',
            lastNameMaternal: session?.user?.name?.split(' ')[2] || '',
            email: session?.user?.email || '',
            phoneNumber: '',
            birthDate: '',
            sub: sub,
            role: 'usuario',
            accountState: 'activo',
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          };
          setUser(fallbackUser);
          console.log('📝 Fallback user created:', fallbackUser);

          // Set fallback stats
          const fallbackStats: UserStats = {
            totalPromotions: promotions.length || 3,
            activePromotions: promotions.filter(p => p.isActive).length || 3,
            totalRedemptions: 5,
            monthlyRedemptions: 1,
          };
          setUserStats(fallbackStats);
        }
      } else {
        console.log('⚠️ No Cognito ID found, using basic fallback data');
        // No cognito ID, set basic fallback stats
        const fallbackStats: UserStats = {
          totalPromotions: promotions.length || 3,
          activePromotions: promotions.filter(p => p.isActive).length || 3,
          totalRedemptions: 0,
          monthlyRedemptions: 0,
        };
        setUserStats(fallbackStats);
      }

      // Load favorites from localStorage
      const savedFavorites = localStorage.getItem('favoritePromotions');
      if (savedFavorites) {
        setFavoritePromotions(JSON.parse(savedFavorites));
      }

    } catch (error) {
      console.error('Error loading user data:', error);
    } finally {
      setLoading(false);
    }
  }, [session]);

  useEffect(() => {
    if (status === "loading") return;

    if (!session) {
      router.push("/login");
      return;
    }

    const profile = (session as { profile?: string }).profile;
    if (profile === "admin") {
      router.push("/admin");
      return;
    }

    if (profile === "colaborator" || profile === "collaborator") {
      router.push("/colaborator");
      return;
    }

    loadUserData();
  }, [session, status, router, loadUserData]);

  const toggleFavorite = (promotionId: string) => {
    const newFavorites = favoritePromotions.includes(promotionId)
      ? favoritePromotions.filter(id => id !== promotionId)
      : [...favoritePromotions, promotionId];

    setFavoritePromotions(newFavorites);
    localStorage.setItem('favoritePromotions', JSON.stringify(newFavorites));
  };

  const filteredPromotions = promotions.filter(promo => {
    const matchesSearch = promo.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         promo.businessName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === "all" || promo.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const favoritePromotionsData = promotions.filter(promo =>
    favoritePromotions.includes(promo.id)
  );

  const getCategoryIcon = (category: string) => {
    const icons: { [key: string]: React.ReactElement } = {
      'Comida': <GiftIcon className="h-5 w-5" />,
      'Entretenimiento': <StarIcon className="h-5 w-5" />,
      'Moda': <ShoppingBagIcon className="h-5 w-5" />,
      'Deportes': <ChartBarIcon className="h-5 w-5" />,
      'Tecnología': <QrCodeIcon className="h-5 w-5" />,
    };
    return icons[category] || <TagIcon className="h-5 w-5" />;
  };

  if (status === "loading" || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-cyan-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Cargando promociones...</p>
        </div>
      </div>
    );
  }

  if (!session) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-100">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="bg-gradient-to-r from-[#4B4C7E] to-[#008D96] p-3 rounded-full">
                <UserCircleIcon className="h-8 w-8 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold bg-gradient-to-r from-[#4B4C7E] to-[#008D96] bg-clip-text text-transparent">
                  ¡Hola {user?.name || session.user?.name || session.user?.email}!
                </h1>
                <p className="text-gray-600">Descubre las mejores promociones para ti</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              {userStats && (
                <div className="text-right text-sm">
                  <p className="text-gray-500">Promociones usadas</p>
                  <p className="font-bold text-[#008D96]">{userStats.totalRedemptions}</p>
                </div>
              )}
              <button
                onClick={() => router.push("/api/auth/signout")}
                className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 rounded-lg transition-colors"
              >
                Salir
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="container mx-auto px-4 py-6">
        <div className="bg-white rounded-xl shadow-sm p-4 mb-6">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1 relative">
              <MagnifyingGlassIcon className="h-5 w-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Buscar promociones o negocios..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
              />
            </div>
            <div className="flex items-center space-x-3">
              <FunnelIcon className="h-5 w-5 text-gray-500" />
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#008D96] focus:border-transparent"
              >
                <option value="all">Todas las categorías</option>
                {categories.map(category => (
                  <option key={category.id} value={category.name}>{category.name}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-xl shadow-sm p-1 mb-6">
          <div className="flex space-x-1">
            <button
              onClick={() => setActiveTab("promociones")}
              className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
                activeTab === "promociones"
                  ? "bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white"
                  : "text-gray-600 hover:bg-gray-50"
              }`}
            >
              <TagIcon className="h-5 w-5 inline mr-2" />
              Promociones ({filteredPromotions.length})
            </button>
            <button
              onClick={() => setActiveTab("favoritos")}
              className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
                activeTab === "favoritos"
                  ? "bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white"
                  : "text-gray-600 hover:bg-gray-50"
              }`}
            >
              <HeartIcon className="h-5 w-5 inline mr-2" />
              Favoritos ({favoritePromotionsData.length})
            </button>
            <button
              onClick={() => setActiveTab("perfil")}
              className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all ${
                activeTab === "perfil"
                  ? "bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white"
                  : "text-gray-600 hover:bg-gray-50"
              }`}
            >
              <UserCircleIcon className="h-5 w-5 inline mr-2" />
              Mi Perfil
            </button>
          </div>
        </div>

        {/* Content */}
        {activeTab === "promociones" && (
          <div className="space-y-4">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-gray-800">Promociones Disponibles</h2>
              <p className="text-sm text-gray-500">
                {filteredPromotions.length} promoción{filteredPromotions.length !== 1 ? 'es' : ''} encontrada{filteredPromotions.length !== 1 ? 's' : ''}
              </p>
            </div>

            {filteredPromotions.length === 0 ? (
              <div className="bg-white rounded-xl shadow-sm p-8 text-center">
                <GiftIcon className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-600 mb-2">No hay promociones disponibles</h3>
                <p className="text-gray-500">Intenta cambiar los filtros de búsqueda</p>
              </div>
            ) : (
              filteredPromotions.map((promo) => (
                <div key={promo.id} className="bg-white rounded-xl shadow-sm hover:shadow-md transition-all duration-200 overflow-hidden">
                  <div className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex items-start space-x-4 flex-1">
                        <div className={`p-3 rounded-lg ${promo.theme === 'dark' ? 'bg-gray-800' : 'bg-gradient-to-r from-[#4B4C7E] to-[#008D96]'} text-white`}>
                          {getCategoryIcon(promo.category)}
                        </div>
                        <div className="flex-1">
                          <div className="flex items-start justify-between">
                            <div>
                              <h3 className="text-lg font-semibold text-gray-800 mb-1">{promo.title}</h3>
                              <p className="text-gray-600 mb-2">{promo.subtitle}</p>
                              <p className="text-sm text-gray-500 mb-3">{promo.body}</p>
                              <div className="flex items-center space-x-4 text-sm text-gray-500">
                                <span className="flex items-center">
                                  <MapPinIcon className="h-4 w-4 mr-1" />
                                  {promo.businessName}
                                </span>
                                <span className="flex items-center">
                                  <TagIcon className="h-4 w-4 mr-1" />
                                  {promo.category}
                                </span>
                                <span>📅 Válido hasta {new Date(promo.validTo).toLocaleDateString()}</span>
                              </div>
                            </div>
                            <button
                              onClick={() => toggleFavorite(promo.id)}
                              className={`p-2 rounded-full transition-colors ${
                                favoritePromotions.includes(promo.id)
                                  ? 'bg-red-100 text-red-600'
                                  : 'bg-gray-100 text-gray-400 hover:bg-red-100 hover:text-red-600'
                              }`}
                            >
                              <HeartIcon className="h-5 w-5" />
                            </button>
                          </div>
                        </div>
                      </div>
                      <div className="text-right ml-4">
                        <span className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium mb-3 block">
                          {promo.discount}% OFF
                        </span>
                        <button
                          onClick={() => setShowQRCode(promo.id)}
                          className="bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white px-6 py-2 rounded-lg hover:shadow-lg transition-all mb-2 block w-full"
                        >
                          <QrCodeIcon className="h-4 w-4 inline mr-2" />
                          Usar Cupón
                        </button>
                        <p className="text-xs text-gray-500">
                          {promo.currentRedemptions}/{promo.maxRedemptions || '∞'} usados
                        </p>
                      </div>
                    </div>
                  </div>
                  {promo.terms && (
                    <div className="px-6 pb-4">
                      <details className="text-sm">
                        <summary className="text-gray-500 cursor-pointer hover:text-gray-700">
                          Ver términos y condiciones
                        </summary>
                        <p className="text-gray-600 mt-2 pl-4">{promo.terms}</p>
                      </details>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        )}

        {activeTab === "favoritos" && (
          <div className="space-y-4">
            <h2 className="text-xl font-semibold text-gray-800 mb-6">Tus Promociones Favoritas</h2>
            {favoritePromotionsData.length === 0 ? (
              <div className="bg-white rounded-xl shadow-sm p-8 text-center">
                <HeartIcon className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-600 mb-2">No tienes favoritos aún</h3>
                <p className="text-gray-500">Marca promociones como favoritas para verlas aquí</p>
                <button
                  onClick={() => setActiveTab("promociones")}
                  className="mt-4 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white px-6 py-2 rounded-lg hover:shadow-lg transition-all"
                >
                  Explorar Promociones
                </button>
              </div>
            ) : (
              favoritePromotionsData.map((promo) => (
                <div key={promo.id} className="bg-white rounded-xl shadow-sm p-6 hover:shadow-md transition-shadow">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="bg-red-100 p-3 rounded-lg">
                        <HeartIcon className="h-6 w-6 text-red-600" />
                      </div>
                      <div>
                        <h3 className="text-lg font-semibold text-gray-800">{promo.title}</h3>
                        <p className="text-gray-600">{promo.businessName}</p>
                        <span className="text-sm text-gray-500">📅 Válido hasta {new Date(promo.validTo).toLocaleDateString()}</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
                        {promo.discount}% OFF
                      </span>
                      <button
                        onClick={() => {
                          setActiveTab("promociones");
                          setSearchTerm(promo.title);
                        }}
                        className="block mt-3 text-[#008D96] hover:text-[#00494E] font-medium text-sm"
                      >
                        Ver detalles →
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {activeTab === "perfil" && (
          <div className="space-y-6">
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h3 className="text-lg font-semibold text-gray-800 mb-4">Mi Información</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-gray-500 mb-1">Nombre completo:</label>
                  <p className="text-gray-800 font-medium">
                    {user ? `${user.name} ${user.lastNamePaternal} ${user.lastNameMaternal}` : session.user?.name || "No definido"}
                  </p>
                </div>
                <div>
                  <label className="block text-gray-500 mb-1">Email:</label>
                  <p className="text-gray-800">{user?.email || session.user?.email}</p>
                </div>
                <div>
                  <label className="block text-gray-500 mb-1">Teléfono:</label>
                  <p className="text-gray-800">{user?.phoneNumber || "No registrado"}</p>
                </div>
                <div>
                  <label className="block text-gray-500 mb-1">Fecha de nacimiento:</label>
                  <p className="text-gray-800">
                    {user?.birthDate ? new Date(user.birthDate).toLocaleDateString() : "No registrada"}
                  </p>
                </div>
                <div>
                  <label className="block text-gray-500 mb-1">Tipo de cuenta:</label>
                  <p className="text-blue-600 font-medium">Usuario</p>
                </div>
                <div>
                  <label className="block text-gray-500 mb-1">Estado:</label>
                  <p className="text-green-600 font-medium">✅ Cuenta verificada</p>
                </div>
              </div>
            </div>

            {userStats && (
              <div className="bg-white rounded-xl shadow-sm p-6">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Mis Estadísticas</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="text-center p-4 bg-blue-50 rounded-lg">
                    <p className="text-2xl font-bold text-blue-600">{userStats.totalPromotions}</p>
                    <p className="text-sm text-gray-600">Promociones disponibles</p>
                  </div>
                  <div className="text-center p-4 bg-green-50 rounded-lg">
                    <p className="text-2xl font-bold text-green-600">{userStats.activePromotions}</p>
                    <p className="text-sm text-gray-600">Promociones activas</p>
                  </div>
                  <div className="text-center p-4 bg-purple-50 rounded-lg">
                    <p className="text-2xl font-bold text-purple-600">{userStats.totalRedemptions}</p>
                    <p className="text-sm text-gray-600">Cupones usados</p>
                  </div>
                  <div className="text-center p-4 bg-orange-50 rounded-lg">
                    <p className="text-2xl font-bold text-orange-600">{userStats.monthlyRedemptions}</p>
                    <p className="text-sm text-gray-600">Este mes</p>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* QR Code Modal */}
      {showQRCode && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={() => setShowQRCode(null)}>
          <div className="bg-white rounded-xl p-8 max-w-sm w-full mx-4" onClick={(e) => e.stopPropagation()}>
            <div className="text-center">
              <h3 className="text-lg font-semibold mb-4">Código QR del Cupón</h3>
              <div className="bg-gray-100 h-48 w-48 mx-auto mb-4 flex items-center justify-center rounded-lg">
                <QrCodeIcon className="h-24 w-24 text-gray-400" />
              </div>
              <p className="text-sm text-gray-600 mb-4">
                Muestra este código QR en el establecimiento para usar tu cupón
              </p>
              <button
                onClick={() => setShowQRCode(null)}
                className="bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white px-6 py-2 rounded-lg hover:shadow-lg transition-all"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}