import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";
import { Promotion, PromotionCreateData, PromotionAIRequest, Business, Category } from "@/types/promotion";
import { User, UserStats, AdminStats, CollaboratorStats } from "@/types/user";

const API_BASE_URL = process.env.NODE_ENV === 'development'
  ? '/api/proxy'  // Usar proxy local en desarrollo
  : 'https://beneficiojoven.lat';  // Usar API directa en producción

export interface UserRegistrationData {
  name: string;
  lastNamePaternal: string;
  lastNameMaternal: string;
  birthDate: string;
  phoneNumber: string;
  email: string;
  cognitoId: string;
  accountState?: string;
  userPrefCategories?: string[];
}

export interface CollaboratorRegistrationData {
  businessName: string;
  cognitoId: string;
  rfc: string;
  representativeName: string;
  phone: string;
  email: string;
  address: string;
  postalCode: string;
  state?: string;
  categoryIds?: number[];
  logoUrl?: string;
  description: string;
}

export interface ApiError {
  message: string;
  code?: string;
  status?: number;
  rawResponse?: string;
}

export async function fetchFromApi(path: string) {
  const session = await getServerSession(authOptions);
  const token = (session as { accessToken?: string })?.accessToken;
  const base = 'https://beneficiojoven.lat';
  const res = await fetch(`${base}${path}`, {
    headers: { Authorization: token ? `Bearer ${token}` : "" },
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`API error: ${res.status}`);
  return res.json();
}

class ApiService {
  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;

    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    console.log('🔗 API Request Details:');
    console.log('📍 URL:', url);
    console.log('🛠️ Method:', config.method || 'GET');
    console.log('📋 Headers:', config.headers);
    console.log('📦 Body:', config.body);

    try {
      console.log('📡 Enviando request a la API...');
      const response = await fetch(url, config);

      console.log('📬 Respuesta recibida:');
      console.log('🔢 Status:', response.status);
      console.log('📝 Status Text:', response.statusText);
      console.log('🏷️ Headers:', Object.fromEntries(response.headers.entries()));

      if (!response.ok) {
        const errorText = await response.text();
        console.log('❌ Error response body:', errorText);

        let errorData = {};
        try {
          errorData = JSON.parse(errorText);
        } catch {
          console.log('⚠️ Error response is not valid JSON');
        }

        throw {
          message: (errorData as { message?: string }).message || `HTTP ${response.status}: ${response.statusText}`,
          status: response.status,
          code: (errorData as { code?: string }).code,
          rawResponse: errorText
        } as ApiError;
      }

      const responseText = await response.text();
      console.log('✅ Success response body:', responseText);

      try {
        return JSON.parse(responseText);
      } catch {
        console.log('⚠️ Success response is not valid JSON, returning as text');
        return responseText as T;
      }
    } catch (error) {
      console.log('💥 Request failed with error:', error);

      if (error instanceof TypeError && error.message.includes('fetch')) {
        throw {
          message: 'No se pudo conectar con el servidor. Verifica tu conexión a internet.',
          code: 'NETWORK_ERROR'
        } as ApiError;
      }
      throw error;
    }
  }

  async registerUser(userData: UserRegistrationData): Promise<{ success: boolean; message?: string }> {
    console.log('👤 API RegisterUser - Input data:', userData);

    const dataWithDefaults = {
      ...userData,
      accountState: userData.accountState || 'activo',
      userPrefCategories: ["Comida", "Entretenimiento"]
    };

    console.log('👤 API RegisterUser - Data with defaults:', dataWithDefaults);
    console.log('👤 API RegisterUser - JSON payload:', JSON.stringify(dataWithDefaults, null, 2));

    return this.request('/users', {
      method: 'POST',
      body: JSON.stringify(dataWithDefaults),
    });
  }

  async registerCollaborator(collaboratorData: CollaboratorRegistrationData): Promise<{ success: boolean; message?: string }> {
    console.log('🏢 API RegisterCollaborator - Input data:', collaboratorData);

    const dataWithDefaults = {
      ...collaboratorData,
      state: collaboratorData.state || 'activo'
    };

    console.log('🏢 API RegisterCollaborator - Data with defaults:', dataWithDefaults);
    console.log('🏢 API RegisterCollaborator - JSON payload:', JSON.stringify(dataWithDefaults, null, 2));

    return this.request('/collaborators', {
      method: 'POST',
      body: JSON.stringify(dataWithDefaults),
    });
  }

  // Promotions API
  async getPromotions(): Promise<Promotion[]> {
    return this.request('/promotions');
  }

  async getPromotionById(id: string): Promise<Promotion> {
    return this.request(`/promotions/${id}`);
  }

  async createPromotion(promotionData: PromotionCreateData): Promise<Promotion> {
    return this.request('/promotions', {
      method: 'POST',
      body: JSON.stringify(promotionData),
    });
  }

  async createPromotionRaw(promotionData: any): Promise<any> {
    return this.request('/promotions', {
      method: 'POST',
      body: JSON.stringify(promotionData),
    });
  }

  async createPromotionWithAI(aiRequest: PromotionAIRequest): Promise<Promotion> {
    return this.request('/promotions/ai-generate', {
      method: 'POST',
      body: JSON.stringify(aiRequest),
    });
  }

  async updatePromotion(id: string, promotionData: Partial<PromotionCreateData>): Promise<Promotion> {
    return this.request(`/promotions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(promotionData),
    });
  }

  async updatePromotionRaw(id: string, promotionData: any): Promise<any> {
    return this.request(`/promotions/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(promotionData),
    });
  }

  async deletePromotion(id: string): Promise<{ success: boolean }> {
    return this.request(`/promotions/${id}`, {
      method: 'DELETE',
    });
  }

  async getPromotionsByBusiness(businessId: string): Promise<Promotion[]> {
    return this.request(`/promotions/business/${businessId}`);
  }

  // Users API
  async getUsers(): Promise<User[]> {
    return this.request('/users');
  }

  async getUserById(id: string): Promise<User> {
    return this.request(`/users/${id}`);
  }

  async getUserByCognitoId(cognitoId: string): Promise<User> {
    console.log('🔍 API getUserByCognitoId called with:', cognitoId);
    console.log('🔍 CognitoId type:', typeof cognitoId, 'length:', cognitoId?.length);

    if (!cognitoId || cognitoId.trim() === '') {
      throw new Error('CognitoId is required and cannot be empty');
    }

    // Since /users/cognito/{id} doesn't exist, get all users and filter
    console.log('🔍 Fetching all users to find by cognitoId...');
    const allUsers: any[] = await this.request('/users');
    console.log(`🔍 Found ${allUsers.length} total users`);

    const matchingUser = allUsers.find(user => user.cognitoId === cognitoId);

    if (!matchingUser) {
      console.log('❌ User not found with cognitoId:', cognitoId);
      console.log('🔍 Available cognitoIds:', allUsers.map(u => u.cognitoId).filter(Boolean));
      throw new Error(`User not found with cognitoId: ${cognitoId}`);
    }

    console.log('✅ Found matching user:', {
      id: matchingUser.id,
      cognitoId: matchingUser.cognitoId,
      name: matchingUser.name,
      email: matchingUser.email
    });

    return matchingUser;
  }

  async updateUser(id: string, userData: Partial<User>): Promise<User> {
    return this.request(`/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(userData),
    });
  }

  async deleteUser(id: string): Promise<{ success: boolean }> {
    return this.request(`/users/${id}`, {
      method: 'DELETE',
    });
  }

  // Businesses API
  async getBusinesses(): Promise<Business[]> {
    return this.request('/businesses');
  }

  async getBusinessById(id: string): Promise<Business> {
    return this.request(`/businesses/${id}`);
  }

  async createBusiness(businessData: Omit<Business, 'id' | 'createdAt' | 'updatedAt'>): Promise<Business> {
    return this.request('/businesses', {
      method: 'POST',
      body: JSON.stringify(businessData),
    });
  }

  async updateBusiness(id: string, businessData: Partial<Business>): Promise<Business> {
    return this.request(`/businesses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(businessData),
    });
  }

  async deleteBusiness(id: string): Promise<{ success: boolean }> {
    return this.request(`/businesses/${id}`, {
      method: 'DELETE',
    });
  }

  // Categories API
  async getCategories(): Promise<Category[]> {
    return this.request('/categories');
  }

  // Stats API
  async getUserStats(userId: string): Promise<UserStats> {
    return this.request(`/stats/user/${userId}`);
  }

  async getCollaboratorStats(businessId: string): Promise<CollaboratorStats> {
    return this.request(`/stats/collaborator/${businessId}`);
  }

  async getAdminStats(): Promise<AdminStats> {
    return this.request('/stats/admin');
  }
}

export const apiService = new ApiService();
