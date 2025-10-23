// ============================================================================
// TYPES & INTERFACES - Promociones
// ============================================================================

// Estructura exacta según la respuesta real del servidor
export interface ApiPromotion {
  promotionId: number;
  collaboratorId: string; // Es un cognitoId string, no un número
  title: string;
  description: string;
  imageUrl?: string;
  initialDate: string; // ISO date
  endDate: string; // ISO date
  promotionType: 'descuento' | 'multicompra' | 'regalo' | 'otro';
  promotionString?: string;
  totalStock?: number;
  availableStock?: number;
  limitPerUser?: number;
  dailyLimitPerUser?: number;
  promotionState: 'activa' | 'inactiva' | 'finalizada';
  created_at?: string;
  updated_at?: string;
  theme?: 'light' | 'dark'; // Campo adicional del servidor
  is_bookable?: boolean; // Campo adicional del servidor
  categories?: Array<{ // Categorías incluidas en la respuesta
    id: number;
    name: string;
  }>;
  businessName?: string; // Nombre del negocio incluido en la respuesta
}

// Estructura para crear promoción según lo que acepta el servidor
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
  theme: 'light' | 'dark'; // Campo que acepta el servidor
  is_bookable: boolean; // Campo que acepta el servidor
  categories?: Array<{ id: number; name: string }>; // Opcional para creación
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
  editingPromotion: ApiPromotion | null;
  collaborator: ApiCollaborator | null;
  authLoading: boolean;
}

// Props para componentes
export interface PromotionFormModalProps {
  promotion: ApiPromotion | null;
  onSave: (data: Record<string, unknown>) => void;
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