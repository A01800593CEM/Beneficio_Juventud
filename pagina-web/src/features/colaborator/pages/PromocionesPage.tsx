'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import DashboardLayout from '@/features/admin/components/DashboardLayout';
import KPICard from '@/features/admin/components/KPICard';

// Estructura exacta según la documentación de la API
interface ApiPromotion {
  promotionId: number;
  collaboratorId: number;
  title: string;
  description: string;
  imageUrl?: string;
  initialDate: string; // ISO date
  endDate: string; // ISO date
  categoryId?: number;
  promotionType: 'descuento' | 'multicompra' | 'regalo' | 'otro';
  promotionString?: string;
  totalStock?: number;
  availableStock?: number;
  limitPerUser?: number;
  dailyLimitPerUser?: number;
  promotionState: 'activa' | 'inactiva' | 'finalizada';
  categoryIds?: number[];
  created_at?: string;
  updated_at?: string;
}

// Estructura para crear promoción según documentación
interface CreatePromotionData {
  collaboratorId: number;
  title: string;
  description: string;
  imageUrl?: string;
  initialDate: string;
  endDate: string;
  categoryId?: number;
  promotionType: 'descuento' | 'multicompra' | 'regalo' | 'otro';
  promotionString?: string;
  totalStock?: number;
  availableStock?: number;
  limitPerUser?: number;
  dailyLimitPerUser?: number;
  promotionState: 'activa' | 'inactiva' | 'finalizada';
  categoryIds?: number[];
}

// Estructura para respuesta del webhook IA
interface AIPromotionResponse {
  title: string;
  description: string;
  initialDate: string;
  endDate: string;
  promotionType: string;
  totalStock: number;
  limitPerUser: number;
  dailyLimitPerUser: number;
  promotionState: string;
  categories: Array<{ id: number; name: string }>;
}

// Estructura del colaborador según la API real
interface ApiCollaborator {
  id: number;
  cognitoId: string;  // Correcto: es cognitoId según tu API
  businessName: string;
  rfc: string;
  representativeName: string;
  phone: string;
  email: string;
  address: string;
  postalCode: string;
  logoUrl?: string;
  description?: string;
  registrationDate: string;
  updatedAt: string;
  state: 'activo' | 'inactivo' | 'suspendido';
  categories?: Array<{
    id: number;
    name: string;
  }>;
}

// API Service simplificado
class SimpleApiService {
  private baseUrl = process.env.NODE_ENV === 'development' ? '/api/proxy' : 'https://beneficiojoven.lat';
  private aiWebhookUrl = 'https://primary-production-0858b.up.railway.app/webhook/bdd4b48a-4f48-430f-a443-a14a19009340';

  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    console.log(`🌐 API Request: ${options.method || 'GET'} ${url}`);

    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    if (config.body) {
      console.log('📦 Request body:', config.body);
    }

    const response = await fetch(url, config);
    console.log(`📬 Response: ${response.status} ${response.statusText}`);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ API Error:', errorText);
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();
    console.log('✅ Response data:', data);
    return data;
  }

  // Webhook IA para generar promoción
  async generatePromotionWithAI(idea: string): Promise<AIPromotionResponse> {
    console.log(`🤖 AI Webhook Request: POST ${this.aiWebhookUrl}`);
    console.log('💡 Idea:', idea);

    const response = await fetch(this.aiWebhookUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ text: idea }),
    });

    console.log(`🤖 AI Response: ${response.status} ${response.statusText}`);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ AI Webhook Error:', errorText);
      throw new Error(`AI Webhook Error: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();
    console.log('🤖 AI Response data:', data);

    // El webhook devuelve un array, tomamos el primer elemento
    if (Array.isArray(data) && data.length > 0) {
      return data[0];
    } else {
      throw new Error('Respuesta inválida del webhook IA');
    }
  }

  // GET /promotions - Listar Promociones
  async getPromotions(): Promise<ApiPromotion[]> {
    return this.request('/promotions');
  }

  // GET /promotions/:id - Obtener Promoción
  async getPromotion(id: number): Promise<ApiPromotion> {
    return this.request(`/promotions/${id}`);
  }

  // POST /promotions - Crear Promoción
  async createPromotion(data: CreatePromotionData): Promise<ApiPromotion> {
    return this.request('/promotions', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // PATCH /promotions/:id - Actualizar Promoción
  async updatePromotion(id: number, data: Partial<CreatePromotionData>): Promise<ApiPromotion> {
    return this.request(`/promotions/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  // DELETE /promotions/:id - Eliminar Promoción
  async deletePromotion(id: number): Promise<void> {
    return this.request(`/promotions/${id}`, {
      method: 'DELETE',
    });
  }

  // GET /collaborators - Buscar colaborador por cognitoId
  async getCollaboratorByCognitoId(cognitoUsername: string): Promise<ApiCollaborator> {
    console.log('🔍 Searching collaborator with cognitoUsername:', cognitoUsername);

    // Buscar todos los colaboradores y filtrar por cognitoId
    const collaborators: ApiCollaborator[] = await this.request('/collaborators');
    console.log(`🔍 Found ${collaborators.length} total collaborators`);
    console.log('🔍 All collaborators:', collaborators.map(c => ({ id: c.id, cognitoId: c.cognitoId, email: c.email })));

    const collaborator = collaborators.find(c => c.cognitoId === cognitoUsername);

    if (!collaborator) {
      console.error('❌ Collaborator not found with cognitoUsername:', cognitoUsername);
      console.log('🔍 Available cognitoIds:', collaborators.map(c => c.cognitoId).filter(Boolean));
      throw new Error(`Colaborador no encontrado con cognitoId: ${cognitoUsername}`);
    }

    console.log('✅ Found collaborator:', {
      id: collaborator.id,
      cognitoId: collaborator.cognitoId,
      businessName: collaborator.businessName,
      email: collaborator.email
    });

    return collaborator;
  }
}

const apiService = new SimpleApiService();

export default function PromocionesPage() {
  const { data: session } = useSession();
  const [promotions, setPromotions] = useState<ApiPromotion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [showAIForm, setShowAIForm] = useState(false);
  const [editingPromotion, setEditingPromotion] = useState<ApiPromotion | null>(null);
  const [collaborator, setCollaborator] = useState<ApiCollaborator | null>(null);
  const [authLoading, setAuthLoading] = useState(true);

  // Cargar promociones al iniciar
  useEffect(() => {
    loadPromotions();
  }, []);

  // Autenticar colaborador al cargar session
  useEffect(() => {
    const authenticateCollaborator = async () => {
      console.log('🔍 Session data:', session);

      if (!session) {
        console.log('❌ No session found');
        setAuthLoading(false);
        return;
      }

      // Extraer cognitoUsername de la sesión
      const sessionData = session as any;

      // Buscar en todas las posibles ubicaciones
      console.log('🔍 DEBUGGING SESSION STRUCTURE:');
      console.log('🔍 sessionData:', JSON.stringify(sessionData, null, 2));
      console.log('🔍 sessionData.profile:', sessionData.profile);
      console.log('🔍 sessionData.cognitoUsername:', sessionData.cognitoUsername);
      console.log('🔍 sessionData.sub:', sessionData.sub);
      console.log('🔍 sessionData.user:', sessionData.user);
      console.log('🔍 sessionData.accessToken:', sessionData.accessToken);

      const cognitoUsername = sessionData.cognitoUsername || sessionData.sub || sessionData.user?.id || sessionData.user?.sub;

      console.log('🔍 Extracted cognitoUsername:', cognitoUsername);

      // Intentar buscar colaborador siempre que tengamos cognitoUsername (sin verificar profile por ahora)
      if (cognitoUsername) {
        try {
          console.log('🔄 Authenticating collaborator with cognitoUsername:', cognitoUsername);
          const collaboratorData = await apiService.getCollaboratorByCognitoId(cognitoUsername);
          setCollaborator(collaboratorData);
          console.log('✅ Collaborator authenticated:', collaboratorData.email);
        } catch (err) {
          console.error('❌ Error authenticating collaborator:', err);
          setError('Error de autenticación. No se encontró el colaborador.');
        }
      } else {
        console.log('ℹ️ No cognitoUsername found in session');
        console.log('🔍 Available session keys:', Object.keys(sessionData));
      }

      setAuthLoading(false);
    };

    if (session !== undefined) {
      authenticateCollaborator();
    }
  }, [session]);

  const loadPromotions = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('🔄 Loading promotions...');

      const data = await apiService.getPromotions();
      setPromotions(data);
      console.log(`✅ Loaded ${data.length} promotions`);
    } catch (err) {
      console.error('❌ Error loading promotions:', err);
      setError('Error al cargar las promociones');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    console.log('➕ Opening create form');
    setEditingPromotion(null);
    setShowForm(true);
  };

  const handleCreateWithAI = () => {
    console.log('🤖 Opening AI create form');
    setShowAIForm(true);
  };

  const handleEdit = (promotion: ApiPromotion) => {
    console.log('✏️ Opening edit form for promotion:', promotion.promotionId);
    setEditingPromotion(promotion);
    setShowForm(true);
  };

  const handleDelete = async (promotion: ApiPromotion) => {
    if (!confirm(`¿Eliminar la promoción "${promotion.title}"?`)) return;

    try {
      setLoading(true);
      console.log('🗑️ Deleting promotion:', promotion.promotionId);

      await apiService.deletePromotion(promotion.promotionId);
      await loadPromotions(); // Recargar lista
      console.log('✅ Promotion deleted');
    } catch (err) {
      console.error('❌ Error deleting promotion:', err);
      setError('Error al eliminar la promoción');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateWithAI = async (idea: string) => {
    try {
      setLoading(true);
      setError(null);
      console.log('🤖 Generating promotion with AI for idea:', idea);

      // Llamar al webhook IA
      const aiResponse = await apiService.generatePromotionWithAI(idea);
      console.log('🤖 AI generated promotion:', aiResponse);

      // Verificar que el colaborador esté autenticado
      if (!collaborator) {
        setError('Debe estar autenticado como colaborador para crear promociones');
        return;
      }

      // Convertir respuesta IA a formato de la API
      const promotionData: CreatePromotionData = {
        collaboratorId: collaborator.id,
        title: aiResponse.title,
        description: aiResponse.description,
        imageUrl: 'https://example.com/ai-generated.jpg',
        initialDate: aiResponse.initialDate,
        endDate: aiResponse.endDate,
        promotionType: aiResponse.promotionType as any,
        promotionString: '',
        totalStock: aiResponse.totalStock,
        availableStock: aiResponse.totalStock,
        limitPerUser: aiResponse.limitPerUser,
        dailyLimitPerUser: aiResponse.dailyLimitPerUser,
        promotionState: aiResponse.promotionState as any,
        categoryIds: aiResponse.categories.map(cat => cat.id)
      };

      console.log('🤖 Creating AI promotion:', promotionData);

      // Crear la promoción
      await apiService.createPromotion(promotionData);
      console.log('✅ AI Promotion created');

      setShowAIForm(false);
      await loadPromotions(); // Recargar lista
    } catch (err) {
      console.error('❌ Error generating promotion with AI:', err);
      setError('Error al generar la promoción con IA');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (formData: any) => {
    try {
      setLoading(true);
      setError(null);

      // Verificar que el colaborador esté autenticado
      if (!collaborator) {
        setError('Debe estar autenticado como colaborador para crear/editar promociones');
        return;
      }

      // Estructura de datos según documentación
      const promotionData: CreatePromotionData = {
        collaboratorId: collaborator.id,
        title: formData.title,
        description: formData.description,
        imageUrl: formData.imageUrl || 'https://example.com/default.jpg',
        initialDate: new Date(formData.startDate).toISOString(),
        endDate: new Date(formData.endDate).toISOString(),
        promotionType: formData.type || 'descuento',
        promotionString: formData.code || '',
        totalStock: parseInt(formData.stock) || 100,
        availableStock: parseInt(formData.stock) || 100,
        limitPerUser: parseInt(formData.limitPerUser) || 1,
        dailyLimitPerUser: parseInt(formData.dailyLimit) || 1,
        promotionState: 'activa',
        categoryIds: formData.categories || []
      };

      console.log('💾 Saving promotion:', promotionData);

      if (editingPromotion) {
        // Actualizar
        await apiService.updatePromotion(editingPromotion.promotionId, promotionData);
        console.log('✅ Promotion updated');
      } else {
        // Crear
        await apiService.createPromotion(promotionData);
        console.log('✅ Promotion created');
      }

      setShowForm(false);
      setEditingPromotion(null);
      await loadPromotions(); // Recargar lista
    } catch (err) {
      console.error('❌ Error saving promotion:', err);
      setError('Error al guardar la promoción');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('es-ES');
  };

  // Calcular estadísticas para KPIs
  const activePromotions = promotions.filter(p => p.promotionState === 'activa').length;
  const totalStock = promotions.reduce((sum, p) => sum + (p.totalStock || 0), 0);
  const usedStock = promotions.reduce((sum, p) => sum + ((p.totalStock || 0) - (p.availableStock || 0)), 0);

  // Componente de acciones para el header
  const headerActions = (
    <div className="flex items-center gap-4">
      {/* Mostrar email del colaborador autenticado */}
      {authLoading ? (
        <div className="text-sm text-[#969696]">Verificando autenticación...</div>
      ) : collaborator ? (
        <div className="text-sm bg-[#008D96]/10 text-[#008D96] px-3 py-1 rounded-full border border-[#008D96]/20">
          👤 {collaborator.email}
        </div>
      ) : (
        <div className="text-sm bg-red-100 text-red-800 px-3 py-1 rounded-full border border-red-200">
          ❌ No autenticado
        </div>
      )}

      <div className="flex gap-3">
        <button
          onClick={handleCreateWithAI}
          disabled={loading}
          className="bg-[#4B4C7E] text-white px-4 py-2 rounded-lg hover:bg-[#4B4C7E]/90 disabled:opacity-50 flex items-center gap-2 transition-colors"
        >
          🤖 Crear con IA
        </button>
        <button
          onClick={handleCreate}
          disabled={loading}
          className="bg-[#008D96] text-white px-4 py-2 rounded-lg hover:bg-[#008D96]/90 disabled:opacity-50 transition-colors"
        >
          ➕ Manual
        </button>
      </div>
    </div>
  );

  return (
    <DashboardLayout
      title="Panel de Colaborador"
      subtitle={collaborator ? `${collaborator.businessName} - Gestión de Promociones` : "Gestión de Promociones"}
      actions={headerActions}
    >
      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <KPICard
          title="Total Promociones"
          value={promotions.length}
          icon={<span className="text-[#008D96]">📊</span>}
        />
        <KPICard
          title="Promociones Activas"
          value={activePromotions}
          icon={<span className="text-[#008D96]">✅</span>}
        />
        <KPICard
          title="Cupones Otorgados"
          value={totalStock}
          icon={<span className="text-[#008D96]">📦</span>}
        />
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {/* Loading State */}
      {loading && (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-[#008D96]"></div>
          <div className="text-[#969696] mt-2">Cargando...</div>
        </div>
      )}

      {/* Promociones Grid */}
      {!loading && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100">
            <h2 className="text-lg font-medium text-[#4B4C7E]">Mis Promociones</h2>
          </div>

          {promotions.length > 0 ? (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 p-6">
              {promotions.map((promotion) => (
                <div key={promotion.promotionId} className="bg-gray-50 rounded-lg p-4 border border-gray-200 hover:shadow-md transition-shadow">
                  <div className="flex justify-between items-start mb-3">
                    <h3 className="font-medium text-[#015463] text-lg">{promotion.title}</h3>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      promotion.promotionState === 'activa' ? 'bg-[#008D96]/10 text-[#008D96]' :
                      promotion.promotionState === 'inactiva' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {promotion.promotionState}
                    </span>
                  </div>

                  <p className="text-[#969696] text-sm mb-4 line-clamp-2">{promotion.description}</p>

                  <div className="text-xs text-[#969696] mb-4 space-y-1">
                    <div className="flex justify-between">
                      <span>Tipo:</span>
                      <span className="text-[#015463]">{promotion.promotionType}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Stock:</span>
                      <span className="text-[#015463]">{promotion.availableStock}/{promotion.totalStock}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Vigencia:</span>
                      <span className="text-[#015463]">{formatDate(promotion.initialDate)} - {formatDate(promotion.endDate)}</span>
                    </div>
                  </div>

                  <div className="flex gap-2">
                    <button
                      onClick={() => handleEdit(promotion)}
                      disabled={loading}
                      className="flex-1 bg-[#4B4C7E]/10 text-[#4B4C7E] px-3 py-2 rounded text-sm hover:bg-[#4B4C7E]/20 disabled:opacity-50 transition-colors"
                    >
                      ✏️ Editar
                    </button>
                    <button
                      onClick={() => handleDelete(promotion)}
                      disabled={loading}
                      className="flex-1 bg-red-50 text-red-600 px-3 py-2 rounded text-sm hover:bg-red-100 disabled:opacity-50 transition-colors"
                    >
                      🗑️ Eliminar
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <div className="text-[#969696] text-lg mb-4">No hay promociones</div>
              <button
                onClick={handleCreate}
                className="bg-[#008D96] text-white px-6 py-3 rounded-lg hover:bg-[#008D96]/90 transition-colors"
              >
                Crear Primera Promoción
              </button>
            </div>
          )}
        </div>
      )}

      {/* Formulario Modal Simple */}
      {showForm && (
        <PromotionFormModal
          promotion={editingPromotion}
          onSave={handleSave}
          onCancel={() => {
            setShowForm(false);
            setEditingPromotion(null);
          }}
        />
      )}

      {/* Formulario Modal IA */}
      {showAIForm && (
        <AIPromotionModal
          onGenerate={handleGenerateWithAI}
          onCancel={() => setShowAIForm(false)}
        />
      )}
    </DashboardLayout>
  );
}

// Componente de formulario simplificado
function PromotionFormModal({
  promotion,
  onSave,
  onCancel
}: {
  promotion: ApiPromotion | null;
  onSave: (data: any) => void;
  onCancel: () => void;
}) {
  const [formData, setFormData] = useState({
    title: promotion?.title || '',
    description: promotion?.description || '',
    imageUrl: promotion?.imageUrl || '',
    startDate: promotion?.initialDate ? promotion.initialDate.split('T')[0] : '',
    endDate: promotion?.endDate ? promotion.endDate.split('T')[0] : '',
    type: promotion?.promotionType || 'descuento',
    code: promotion?.promotionString || '',
    stock: promotion?.totalStock?.toString() || '100',
    limitPerUser: promotion?.limitPerUser?.toString() || '1',
    dailyLimit: promotion?.dailyLimitPerUser?.toString() || '1',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('📋 Form submitted:', formData);
    onSave(formData);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <h2 className="text-xl font-bold mb-4">
            {promotion ? 'Editar Promoción' : 'Nueva Promoción'}
          </h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">Título *</label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                required
                className="w-full border rounded-lg px-3 py-2"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Descripción *</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                required
                rows={3}
                className="w-full border rounded-lg px-3 py-2"
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Tipo</label>
              <select
                name="type"
                value={formData.type}
                onChange={handleChange}
                className="w-full border rounded-lg px-3 py-2"
              >
                <option value="descuento">Descuento</option>
                <option value="multicompra">Multicompra</option>
                <option value="regalo">Regalo</option>
                <option value="otro">Otro</option>
              </select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">Fecha Inicio *</label>
                <input
                  type="date"
                  name="startDate"
                  value={formData.startDate}
                  onChange={handleChange}
                  required
                  className="w-full border rounded-lg px-3 py-2"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Fecha Fin *</label>
                <input
                  type="date"
                  name="endDate"
                  value={formData.endDate}
                  onChange={handleChange}
                  required
                  className="w-full border rounded-lg px-3 py-2"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Código Promocional</label>
              <input
                type="text"
                name="code"
                value={formData.code}
                onChange={handleChange}
                className="w-full border rounded-lg px-3 py-2"
              />
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">Stock</label>
                <input
                  type="number"
                  name="stock"
                  value={formData.stock}
                  onChange={handleChange}
                  min="1"
                  className="w-full border rounded-lg px-3 py-2"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Límite Usuario</label>
                <input
                  type="number"
                  name="limitPerUser"
                  value={formData.limitPerUser}
                  onChange={handleChange}
                  min="1"
                  className="w-full border rounded-lg px-3 py-2"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Límite Diario</label>
                <input
                  type="number"
                  name="dailyLimit"
                  value={formData.dailyLimit}
                  onChange={handleChange}
                  min="1"
                  className="w-full border rounded-lg px-3 py-2"
                />
              </div>
            </div>

            <div className="flex gap-3 pt-4">
              <button
                type="button"
                onClick={onCancel}
                className="flex-1 bg-gray-100 text-[#969696] py-2 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="flex-1 bg-[#008D96] text-white py-2 rounded-lg hover:bg-[#008D96]/90 transition-colors"
              >
                Guardar
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

// Componente modal para crear promoción con IA
function AIPromotionModal({
  onGenerate,
  onCancel
}: {
  onGenerate: (idea: string) => void;
  onCancel: () => void;
}) {
  const [idea, setIdea] = useState('');
  const [generating, setGenerating] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!idea.trim()) return;

    console.log('🤖 Submitting AI idea:', idea);
    setGenerating(true);
    try {
      await onGenerate(idea.trim());
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-lg w-full">
        <div className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <span className="text-2xl">🤖</span>
            <h2 className="text-xl font-bold">Crear Promoción con IA</h2>
          </div>

          <p className="text-gray-600 mb-4">
            Describe tu idea de promoción y la IA generará automáticamente todos los detalles.
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">
                💡 Describe tu idea de promoción:
              </label>
              <textarea
                value={idea}
                onChange={(e) => setIdea(e.target.value)}
                placeholder="Ejemplo: Quiero una promoción de descuento del 20% en pizzas para estudiantes universitarios, válida por este mes..."
                required
                rows={4}
                className="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
              />
            </div>

            <div className="text-xs text-gray-500 bg-gray-50 p-3 rounded">
              <strong>💡 Consejos:</strong>
              <ul className="mt-1 space-y-1">
                <li>• Incluye el tipo de descuento o beneficio</li>
                <li>• Menciona el público objetivo</li>
                <li>• Especifica duración o fechas</li>
                <li>• Añade cualquier restricción importante</li>
              </ul>
            </div>

            <div className="flex gap-3 pt-4">
              <button
                type="button"
                onClick={onCancel}
                disabled={generating}
                className="flex-1 bg-gray-100 text-[#969696] py-2 px-4 rounded-lg hover:bg-gray-200 disabled:opacity-50 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={!idea.trim() || generating}
                className="flex-1 bg-[#4B4C7E] text-white py-2 px-4 rounded-lg hover:bg-[#4B4C7E]/90 disabled:opacity-50 flex items-center justify-center gap-2 transition-colors"
              >
                {generating ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    Generando...
                  </>
                ) : (
                  <>
                    🤖 Generar Promoción
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}