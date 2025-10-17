// ============================================================================
// TYPES & INTERFACES - Promociones
// ============================================================================

// Estructura exacta según la documentación de la API
export interface ApiPromotion {
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
export interface CreatePromotionData {
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
export interface AIPromotionResponse {
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
export interface ApiCollaborator {
  id: number;
  cognitoId: string;
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

// Tipos para el formulario
export interface PromotionFormData {
  title: string;
  description: string;
  imageUrl: string;
  startDate: string;
  endDate: string;
  type: string;
  code: string;
  stock: string;
  limitPerUser: string;
  dailyLimit: string;
  categories: string[];
  promotionTheme: string;
  isBookable: boolean;
}

// Estados del componente
export interface PromotionPageState {
  promotions: ApiPromotion[];
  loading: boolean;
  error: string | null;
  showForm: boolean;
  showAIForm: boolean;
  editingPromotion: ApiPromotion | any | null;
  collaborator: ApiCollaborator | null;
  authLoading: boolean;
}

// Props para componentes
export interface PromotionFormModalProps {
  promotion: ApiPromotion | any | null;
  onSave: (data: any) => void;
  onCancel: () => void;
}

export interface AIPromotionModalProps {
  onGenerate: (idea: string) => void;
  onCancel: () => void;
}

export interface PromotionCardProps {
  promotion: ApiPromotion;
  onEdit: (promotion: ApiPromotion) => void;
  onDelete: (promotion: ApiPromotion) => void;
  loading: boolean;
}