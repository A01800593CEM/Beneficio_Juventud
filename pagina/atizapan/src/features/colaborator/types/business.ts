export interface BusinessProfile {
  id: string;
  name: string;
  owner: string;
  email: string;
  phone: string;
  address: string;
  website?: string;
  description: string;
  category: string;
  logo?: string;
  isVerified: boolean;
  socialMedia: {
    facebook?: string;
    instagram?: string;
    twitter?: string;
    whatsapp?: string;
  };
  schedule: {
    [key: string]: {
      isOpen: boolean;
      openTime: string;
      closeTime: string;
    };
  };
}

export interface BusinessHours {
  day: string;
  isOpen: boolean;
  openTime: string;
  closeTime: string;
}