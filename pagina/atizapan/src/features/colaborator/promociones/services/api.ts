// ============================================================================
// API SERVICE - Promociones
// ============================================================================

import { ApiPromotion, CreatePromotionData, AIPromotionResponse, ApiCollaborator } from '../types';

export class PromotionApiService {
  private baseUrl = process.env.NODE_ENV === 'development' ? '/api/proxy' : 'https://api.beneficiojoven.lat';
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

  // ============================================================================
  // PROMOCIONES CRUD
  // ============================================================================

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

  // ============================================================================
  // COLABORADORES
  // ============================================================================

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

  // ============================================================================
  // IA WEBHOOK
  // ============================================================================

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
}

// Instancia singleton del servicio
export const promotionApiService = new PromotionApiService();