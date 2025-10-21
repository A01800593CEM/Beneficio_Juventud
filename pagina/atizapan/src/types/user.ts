export interface User {
  id: string;
  name: string;
  lastNamePaternal: string;
  lastNameMaternal: string;
  email: string;
  phoneNumber: string;
  birthDate: string;
  sub: string;
  role: UserRole;
  accountState: 'activo' | 'inactivo' | 'suspendido';
  profileImage?: string;
  businessId?: string; // For collaborators
  createdAt: string;
  updatedAt: string;
}

export type UserRole = 'usuario' | 'colaborador' | 'administrador';

export interface UserStats {
  totalPromotions: number;
  activePromotions: number;
  totalRedemptions: number;
  monthlyRedemptions: number;
}

export interface AdminStats {
  totalUsers: number;
  totalBusinesses: number;
  totalPromotions: number;
  totalRedemptions: number;
  monthlyGrowth: {
    users: number;
    businesses: number;
    promotions: number;
    redemptions: number;
  };
}

export interface CollaboratorStats {
  businessPromotions: number;
  activePromotions: number;
  totalRedemptions: number;
  monthlyRedemptions: number;
  topPromotions: Array<{
    id: string;
    title: string;
    redemptions: number;
  }>;
}