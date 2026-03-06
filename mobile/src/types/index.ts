// TypeScript type definitions for navigation and data models

export type RootStackParamList = {
  SignIn: undefined;
  SignUp: undefined;
  ProductList: undefined;
  ProductDetails: { 
    productId: string;
    imageUrl?: string;
    name?: string;
  };
  Notifications: undefined;
  NotificationDetail: {
    notificationId: string;
  };
  Wishlist: undefined;
  AIAssistant: {
    productName: string;
    productId: string;
    reviews: Review[];
  };
};

export interface Review {
  id: string;
  productId: string;
  userName: string;
  reviewerName?: string;
  rating: number;
  comment: string;
  createdAt: string;
  helpfulCount?: number; // ✨ Updated from 'helpful' to 'helpfulCount' to match API
  helpful?: number; // Keep for backward compatibility if needed
}
export interface Product {
  id: string;
  name: string;
  description: string;
  category: string;
  price: number;
  image: string;
  averageRating: number;
  reviewCount: number;
  reviews: Review[];
}
