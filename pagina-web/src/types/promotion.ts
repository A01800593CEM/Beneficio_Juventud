export interface Promotion {
  id: string;
  title: string;
  subtitle: string;
  body: string;
  theme: 'light' | 'dark';
  bg?: string;
  businessId: string;
  businessName: string;
  category: string;
  discount: number;
  validFrom: string;
  validTo: string;
  terms: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  qrCode?: string;
  maxRedemptions?: number;
  currentRedemptions: number;
}

export interface PromotionCreateData {
  title: string;
  subtitle: string;
  body: string;
  category: string;
  discount: number;
  validFrom: string;
  validTo: string;
  terms: string;
  maxRedemptions?: number;
}

export interface PromotionAIRequest {
  businessType: string;
  targetAudience: string;
  promotionGoal: string;
  discountRange: string;
  additionalInfo?: string;
}

export type PromoTheme = 'light' | 'dark';

export interface Category {
  id: string;
  name: string;
  description: string;
  icon: string;
}

export interface Business {
  id: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  category: string;
  description: string;
  logo?: string;
  isActive: boolean;
  cognitoId: string;
  createdAt: string;
  updatedAt: string;
}