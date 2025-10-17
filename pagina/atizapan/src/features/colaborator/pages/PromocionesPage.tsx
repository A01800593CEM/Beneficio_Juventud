'use client';

import { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
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
  collaboratorId: string; // cognitoId del colaborador
  title: string;
  description: string;
  imageUrl?: string;
  initialDate: string;
  endDate: string;
  promotionType: 'descuento' | 'multicompra' | 'regalo' | 'otro';
  promotionString?: string;
  totalStock?: number;
  availableStock?: number;
  limitPerUser?: number;
  dailyLimitPerUser?: number;
  promotionState: 'activa' | 'inactiva' | 'finalizada';
  categories: string[];
  promotionTheme: 'light' | 'dark';
  is_bookable: boolean;
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
  private baseUrl = process.env.NODE_ENV === 'development' ? '/api/proxy' : 'http://3.226.114.132:3000';
  private aiWebhookUrl = 'https://primary-production-0858b.up.railway.app/webhook/bdd4b48a-4f48-430f-a443-a14a19009340';

  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    console.log(`🌐 API Request: ${options.method || 'GET'} ${url}`);

    // Solo agregar Content-Type si hay body
    const headers: Record<string, string> = {};
    if (options.body) {
      headers['Content-Type'] = 'application/json';
    }

    const config: RequestInit = {
      headers: {
        ...headers,
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

    // Para DELETE, la respuesta puede estar vacía
    const responseText = await response.text();

    if (responseText) {
      try {
        const data = JSON.parse(responseText);
        console.log('✅ Response data:', data);
        return data;
      } catch {
        console.log('✅ Response text:', responseText);
        return responseText;
      }
    } else {
      console.log('✅ Empty response (success)');
      return null;
    }
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
  async getPromotions(cognitoId: string): Promise<ApiPromotion[]> {
    return this.request(`/collaborators/promotions/${cognitoId}`);
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

  // Cargar promociones cuando el colaborador esté autenticado
  useEffect(() => {
    if (collaborator?.cognitoId) {
      loadPromotions();
    }
  }, [collaborator]);

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

      if (!collaborator?.cognitoId) {
        console.log('❌ No collaborator cognitoId available');
        setPromotions([]);
        return;
      }

      const data = await apiService.getPromotions(collaborator.cognitoId);
      setPromotions(data);
      console.log(`✅ Loaded ${data.length} promotions for cognitoId: ${collaborator.cognitoId}`);
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

      // Convertir respuesta IA a formato del formulario
      const promotionData = {
        title: aiResponse.title,
        description: aiResponse.description,
        imageUrl: 'https://example.com/ai-generated.jpg',
        startDate: aiResponse.initialDate ? aiResponse.initialDate.split('T')[0] : '',
        endDate: aiResponse.endDate ? aiResponse.endDate.split('T')[0] : '',
        type: aiResponse.promotionType || 'descuento',
        code: '',
        stock: aiResponse.totalStock?.toString() || '100',
        limitPerUser: aiResponse.limitPerUser?.toString() || '1',
        dailyLimit: aiResponse.dailyLimitPerUser?.toString() || '1',
      };

      console.log('🤖 AI promotion data for form:', promotionData);

      // Cerrar formulario IA y abrir formulario manual con datos prellenados
      setShowAIForm(false);
      setEditingPromotion(promotionData as any); // Usar los datos IA como "promoción a editar"
      setShowForm(true);
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

      // Estructura de datos según lo que acepta el servidor
      const promotionData: CreatePromotionData = {
        collaboratorId: collaborator.cognitoId,
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
        theme: (formData.promotionTheme as 'light' | 'dark') || 'light',
        is_bookable: formData.isBookable || false
      };

      console.log('💾 Saving promotion with cognitoId:', collaborator.cognitoId);
      console.log('💾 Promotion data:', promotionData);

      // Verificar si es realmente una promoción existente (con promotionId) o datos de IA
      const isExistingPromotion = editingPromotion && editingPromotion.promotionId;

      if (isExistingPromotion) {
        // Actualizar promoción existente
        await apiService.updatePromotion(editingPromotion.promotionId, promotionData);
        console.log('✅ Promotion updated');
      } else {
        // Crear nueva promoción (incluyendo las generadas por IA)
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
    <div>
      {/* Header */}
      <div className="mb-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Panel de Colaborador</h1>
            <p className="mt-1 text-sm text-gray-500">
              {collaborator ? `${collaborator.businessName} - Gestión de Promociones` : "Gestión de Promociones"}
            </p>
          </div>
          <div className="mt-4 sm:mt-0">
            {headerActions}
          </div>
        </div>
      </div>
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
    </div>
  );
}

// Componente de formulario mejorado
function PromotionFormModal({
  promotion,
  onSave,
  onCancel
}: {
  promotion: ApiPromotion | any | null;
  onSave: (data: any) => void;
  onCancel: () => void;
}) {
  // Detectar si es una promoción existente (tiene promotionId) o datos de IA
  const isExistingPromotion = promotion && 'promotionId' in promotion;

  const [formData, setFormData] = useState({
    title: isExistingPromotion ? promotion.title : (promotion?.title || ''),
    description: isExistingPromotion ? promotion.description : (promotion?.description || ''),
    imageUrl: isExistingPromotion ? promotion.imageUrl : (promotion?.imageUrl || ''),
    startDate: isExistingPromotion
      ? (promotion.initialDate ? promotion.initialDate.split('T')[0] : '')
      : (promotion?.startDate || ''),
    endDate: isExistingPromotion
      ? (promotion.endDate ? promotion.endDate.split('T')[0] : '')
      : (promotion?.endDate || ''),
    type: isExistingPromotion ? promotion.promotionType : (promotion?.type || 'descuento'),
    code: isExistingPromotion ? promotion.promotionString : (promotion?.code || ''),
    stock: isExistingPromotion
      ? (promotion.totalStock?.toString() || '100')
      : (promotion?.stock || '100'),
    limitPerUser: isExistingPromotion
      ? (promotion.limitPerUser?.toString() || '1')
      : (promotion?.limitPerUser || '1'),
    dailyLimit: isExistingPromotion
      ? (promotion.dailyLimitPerUser?.toString() || '1')
      : (promotion?.dailyLimit || '1'),
    categories: isExistingPromotion ? (promotion.categories || ['COMIDA']) : (promotion?.categories || ['COMIDA']),
    promotionTheme: isExistingPromotion ? (promotion.theme || 'light') : (promotion?.promotionTheme || 'light'),
    isBookable: isExistingPromotion ? (promotion.is_bookable || false) : (promotion?.isBookable || false),
  });

  // Estados para manejo de imágenes
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('📋 Form submitted:', formData);
    onSave(formData);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;

    if (type === 'checkbox') {
      const checkbox = e.target as HTMLInputElement;
      setFormData(prev => ({
        ...prev,
        [name]: checkbox.checked
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const handleCategoryChange = (category: string) => {
    setFormData(prev => ({
      ...prev,
      categories: prev.categories.includes(category)
        ? prev.categories.filter((c: string) => c !== category)
        : [...prev.categories, category]
    }));
  };

  // Función para subir imagen a S3
  const uploadImageToS3 = async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch('/api/upload', {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || 'Error subiendo imagen');
    }

    const result = await response.json();
    return result.imageUrl;
  };

  // Manejar selección de archivo
  const handleFileSelect = async (file: File) => {
    setUploading(true);
    setUploadError(null);

    try {
      console.log('📤 Subiendo imagen:', file.name);
      const imageUrl = await uploadImageToS3(file);

      setFormData(prev => ({
        ...prev,
        imageUrl
      }));

      console.log('✅ Imagen subida exitosamente:', imageUrl);
    } catch (error) {
      console.error('❌ Error subiendo imagen:', error);
      setUploadError(error instanceof Error ? error.message : 'Error subiendo imagen');
    } finally {
      setUploading(false);
    }
  };

  // Manejar click en el área de imagen
  const handleImageAction = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        handleFileSelect(file);
      }
    };
    input.click();
  };

  // Manejar drag & drop
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const files = Array.from(e.dataTransfer.files);
    const imageFile = files.find(file => file.type.startsWith('image/'));

    if (imageFile) {
      handleFileSelect(imageFile);
    } else {
      setUploadError('Por favor selecciona un archivo de imagen válido');
    }
  };

  // Generar imagen con IA usando webhook de n8n
  const handleGenerateImageWithAI = async () => {
    // Validar que haya título y descripción
    if (!formData.title.trim() || !formData.description.trim()) {
      setUploadError('Necesitas completar el título y descripción antes de generar una imagen con IA');
      return;
    }

    setUploading(true);
    setUploadError(null);

    try {
      console.log('🤖 Generando imagen con IA para:', formData.title);

      // Preparar el texto para el webhook
      const promptText = `${formData.title}. ${formData.description}`;

      // Llamar al proxy local que se conecta al webhook de n8n
      const response = await fetch('/api/generate-image', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          text: promptText
        }),
      });

      if (!response.ok) {
        throw new Error(`Error del webhook de IA: ${response.status} ${response.statusText}`);
      }

      const result = await response.json();
      console.log('🤖 Respuesta del webhook:', result);

      // Asumir que el webhook devuelve la URL de S3 en el campo imageUrl o uri
      const imageUrl = result.imageUrl || result.uri || result.url;

      if (!imageUrl) {
        throw new Error('El webhook no devolvió una URL de imagen válida');
      }

      // Actualizar el formData con la nueva imagen
      setFormData(prev => ({
        ...prev,
        imageUrl
      }));

      console.log('✅ Imagen generada exitosamente con IA:', imageUrl);

    } catch (error) {
      console.error('❌ Error generando imagen con IA:', error);
      setUploadError(
        error instanceof Error
          ? error.message
          : 'Error al generar imagen con IA. Inténtalo de nuevo.'
      );
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-start justify-center p-4 z-50 overflow-y-auto">
      <div className="bg-white rounded-xl w-full max-w-6xl my-8 shadow-2xl border border-gray-100">
        {/* Header simplificado */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
            </div>
            <div>
              <h2 className="text-xl font-semibold text-[#015463]">
                {isExistingPromotion ? 'Editar Promoción' : promotion ? 'Promoción Generada por IA' : 'Nueva Promoción'}
              </h2>
              {!isExistingPromotion && promotion && (
                <p className="text-sm text-[#008D96]">💡 Revisa y edita los campos antes de publicar</p>
              )}
            </div>
          </div>
          <button
            onClick={onCancel}
            className="text-gray-400 hover:text-gray-600 p-2 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <form onSubmit={handleSubmit} className="px-6 py-4">
          <div className="grid grid-cols-12 gap-6">
            {/* Columna Principal - 8/12 */}
            <div className="col-span-12 lg:col-span-8 space-y-4">
              {/* Información Básica */}
              <div className="bg-white border border-gray-200 rounded-lg p-5">
                <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
                  <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Información Básica
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Título de la Promoción *
                    </label>
                    <input
                      type="text"
                      name="title"
                      value={formData.title}
                      onChange={handleChange}
                      required
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                      placeholder="Ej: 2x1 en Pizzas Familiares"
                    />
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Descripción *
                    </label>
                    <textarea
                      name="description"
                      value={formData.description}
                      onChange={handleChange}
                      required
                      rows={3}
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all resize-none"
                      placeholder="Describe los detalles de tu promoción..."
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Tipo de Promoción
                    </label>
                    <select
                      name="type"
                      value={formData.type}
                      onChange={handleChange}
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                    >
                      <option value="descuento">💸 Descuento</option>
                      <option value="multicompra">🛒 Multicompra</option>
                      <option value="regalo">🎁 Regalo</option>
                      <option value="otro">✨ Otro</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Código Promocional
                    </label>
                    <input
                      type="text"
                      name="code"
                      value={formData.code}
                      onChange={handleChange}
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                      placeholder="PIZZA2X1"
                    />
                  </div>
                </div>
              </div>

              {/* Fechas y Stock */}
              <div className="bg-white border border-gray-200 rounded-lg p-5">
                <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
                  <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  Fechas y Disponibilidad
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Fecha de Inicio *
                    </label>
                    <input
                      type="date"
                      name="startDate"
                      value={formData.startDate}
                      onChange={handleChange}
                      required
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Fecha de Fin *
                    </label>
                    <input
                      type="date"
                      name="endDate"
                      value={formData.endDate}
                      onChange={handleChange}
                      required
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Stock Total
                    </label>
                    <input
                      type="number"
                      name="stock"
                      value={formData.stock}
                      onChange={handleChange}
                      min="1"
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Límite Usuario
                    </label>
                    <input
                      type="number"
                      name="limitPerUser"
                      value={formData.limitPerUser}
                      onChange={handleChange}
                      min="1"
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-2">
                      Límite Diario
                    </label>
                    <input
                      type="number"
                      name="dailyLimit"
                      value={formData.dailyLimit}
                      onChange={handleChange}
                      min="1"
                      className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                    />
                  </div>
                </div>
              </div>

              {/* Categorías y Configuración */}
              <div className="bg-white border border-gray-200 rounded-lg p-5">
                <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
                  <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                  </svg>
                  Categorías y Configuración
                </h3>

                <div className="space-y-4">
                  {/* Categorías */}
                  <div>
                    <label className="block text-sm font-medium text-[#015463] mb-3">
                      Categorías *
                    </label>
                    <div className="flex flex-wrap gap-2">
                      {['COMIDA', 'ENTRETENIMIENTO', 'ROPA'].map((category) => (
                        <button
                          key={category}
                          type="button"
                          onClick={() => handleCategoryChange(category)}
                          className={`inline-flex items-center gap-2 px-4 py-2 rounded-lg border-2 transition-all text-sm font-medium ${
                            formData.categories.includes(category)
                              ? 'border-[#008D96] bg-[#008D96] text-white shadow-sm'
                              : 'border-gray-200 bg-white text-gray-700 hover:border-[#008D96] hover:bg-[#008D96]/5'
                          }`}
                        >
                          {category === 'COMIDA' && '🍕'}
                          {category === 'ENTRETENIMIENTO' && '🎬'}
                          {category === 'ROPA' && '👕'}
                          {category}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Tema y Reservabilidad */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-[#015463] mb-2">
                        Tema de Promoción
                      </label>
                      <select
                        name="promotionTheme"
                        value={formData.promotionTheme}
                        onChange={handleChange}
                        className="w-full border-2 border-gray-200 rounded-lg px-4 py-3 focus:ring-2 focus:ring-[#008D96]/20 focus:border-[#008D96] transition-all"
                      >
                        <option value="light">☀️ Claro</option>
                        <option value="dark">🌙 Oscuro</option>
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-[#015463] mb-2">
                        ¿Se puede reservar?
                      </label>
                      <button
                        type="button"
                        onClick={() => setFormData(prev => ({ ...prev, isBookable: !prev.isBookable }))}
                        className={`inline-flex items-center gap-3 px-4 py-3 rounded-lg border-2 transition-all w-full ${
                          formData.isBookable
                            ? 'border-[#008D96] bg-[#008D96]/5'
                            : 'border-gray-200 bg-white'
                        }`}
                      >
                        <div className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${
                          formData.isBookable ? 'bg-[#008D96]' : 'bg-gray-300'
                        }`}>
                          <span className={`inline-block h-3 w-3 transform rounded-full bg-white transition-transform ${
                            formData.isBookable ? 'translate-x-5' : 'translate-x-1'
                          }`} />
                        </div>
                        <span className="text-sm font-medium text-[#015463]">
                          {formData.isBookable ? 'Sí, reservable' : 'No reservable'}
                        </span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Sidebar - 4/12 */}
            <div className="col-span-12 lg:col-span-4 space-y-6">
              {/* Imagen */}
              <div className="bg-white border border-gray-200 rounded-lg p-5">
                <div className='flex flex-row justify-between '>
                <h3 className="text-lg font-medium text-[#015463] mb-4 flex items-center gap-2">
                  <svg className="w-5 h-5 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  Imagen

                  
                </h3>
                <button
                  onClick={handleGenerateImageWithAI}
                  disabled={uploading || !formData.title.trim() || !formData.description.trim()}
                  className={`px-4 py-2 mb-2 rounded-lg transition-colors text-sm font-medium ${
                    uploading || !formData.title.trim() || !formData.description.trim()
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : 'bg-[#008D96] text-white hover:bg-[#00565B]'
                  }`}
                >
                  {uploading ? '🤖 Generando...' : '🤖 Genera con IA'}
                </button>
</div>
                {/* Error Message */}
                {uploadError && (
                  <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-red-600 text-sm">{uploadError}</p>
                  </div>
                )}

                {/* Upload Area */}
                <div
                  onClick={!uploading ? handleImageAction : undefined}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onDrop={handleDrop}
                  className={`
                    border-2 border-dashed rounded-lg p-6 text-center transition-all
                    ${dragActive
                      ? 'border-[#008D96] bg-[#008D96]/10'
                      : 'border-gray-300 hover:border-[#008D96] hover:bg-[#008D96]/5'
                    }
                    ${uploading
                      ? 'cursor-not-allowed opacity-50'
                      : 'cursor-pointer'
                    }
                    group
                  `}
                >
                  <div className="space-y-3">
                    {uploading ? (
                      <>
                        <div className="mx-auto w-12 h-12 bg-[#008D96]/20 rounded-full flex items-center justify-center">
                          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-[#008D96]"></div>
                        </div>
                        <p className="text-sm font-medium text-[#008D96]">
                          {formData.title ? 'Generando imagen con IA...' : 'Subiendo imagen...'}
                        </p>
                        {formData.title && (
                          <p className="text-xs text-gray-500">
                            Creando imagen para: &quot;{formData.title}&quot;
                          </p>
                        )}
                      </>
                    ) : (
                      <>
                        <div className="mx-auto w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center group-hover:bg-[#008D96]/20 transition-colors">
                          <svg className="w-6 h-6 text-gray-400 group-hover:text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-700">
                            Suelta aquí la imagen o
                          </p>
                          <p className="text-sm font-medium text-[#008D96]">
                            genera con IA
                          </p>
                        </div>
                        <p className="text-xs text-gray-500">
                          PNG, JPG, WebP hasta 5MB
                        </p>
                      </>
                    )}
                  </div>
                </div>

                {/* Image Preview */}
                {formData.imageUrl && (
                  <div className="mt-4 relative">
                    <img
                      src={formData.imageUrl}
                      alt="Preview de la promoción"
                      className="w-full h-48 object-cover rounded-lg border"
                    />
                    <button
                      type="button"
                      onClick={() => setFormData(prev => ({ ...prev, imageUrl: '' }))}
                      className="absolute top-2 right-2 bg-white text-red-500 rounded-full p-1.5 hover:bg-red-50 transition-colors shadow-sm border"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                    <div className="mt-2 text-xs text-[#008D96] text-center font-medium">
                      ✅ Imagen subida exitosamente
                    </div>
                  </div>
                )}
              </div>

              {/* Tips */}
              <div className="bg-[#008D96]/5 border border-[#008D96]/20 rounded-lg p-5">
                <h4 className="font-medium text-[#015463] mb-3 flex items-center gap-2">
                  <svg className="w-4 h-4 text-[#008D96]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                  </svg>
                  Tips para tu promoción
                </h4>
                <ul className="text-sm text-[#015463] space-y-2">
                  <li className="flex items-start gap-2">
                    <span className="text-[#008D96] mt-0.5">•</span>
                    Usa títulos llamativos y claros
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-[#008D96] mt-0.5">•</span>
                    Define fechas realistas
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-[#008D96] mt-0.5">•</span>
                    Limita el stock para crear urgencia
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-[#008D96] mt-0.5">•</span>
                    Agrega una imagen atractiva
                  </li>
                </ul>
              </div>

               {/* Footer Buttons */}
          <div className="flex justify-end gap-3 pt-6 mt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={onCancel}
              className="px-6 py-2.5 border-2 border-gray-200 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-8 py-2.5 bg-gradient-to-r from-[#4B4C7E] to-[#008D96] text-white rounded-lg hover:shadow-lg transition-all font-medium"
            >
              {isExistingPromotion ? 'Actualizar Promoción' : 'Crear Promoción'}
            </button>
          </div>
            </div>
          </div>

         
        </form>
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