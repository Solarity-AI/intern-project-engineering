/**
 * MockAPIClient
 *
 * Typed jest.fn() doubles for every export in `mobile/src/services/api.ts`.
 * Import this module in tests instead of the real api to avoid any network calls.
 *
 * Usage:
 *   import * as API from '../mocks/MockAPIClient';
 *   API.getProduct.mockResolvedValueOnce({ id: 1, name: 'Widget', ... });
 */

import type {
  Page,
  ApiProduct,
  ApiReview,
  ApiNotification,
  GlobalStats,
} from '../../services/api';

// ---------------------------------------------------------------------------
// User identity
// ---------------------------------------------------------------------------

export const getUserId = jest.fn<Promise<string>, []>();

// ---------------------------------------------------------------------------
// Products
// ---------------------------------------------------------------------------

export const getGlobalStats = jest.fn<
  Promise<GlobalStats>,
  [params?: { category?: string; search?: string }]
>();

export const getProducts = jest.fn<
  Promise<Page<ApiProduct>>,
  [params?: { page?: number; size?: number; sort?: string; category?: string; search?: string }]
>();

export const getProduct = jest.fn<Promise<ApiProduct>, [id: number | string]>();

// ---------------------------------------------------------------------------
// Reviews
// ---------------------------------------------------------------------------

export const getReviews = jest.fn<
  Promise<Page<ApiReview>>,
  [
    productId: number | string,
    params?: { page?: number; size?: number; sort?: string; rating?: number | null },
  ]
>();

export const postReview = jest.fn<
  Promise<ApiReview>,
  [productId: number | string, body: ApiReview]
>();

export const markReviewAsHelpful = jest.fn<
  Promise<ApiReview>,
  [reviewId: number | string]
>();

export const getUserVotedReviews = jest.fn<Promise<number[]>, []>();

// ---------------------------------------------------------------------------
// AI
// ---------------------------------------------------------------------------

export const chatWithAI = jest.fn<
  Promise<{ answer: string }>,
  [productId: number | string, question: string]
>();

// ---------------------------------------------------------------------------
// Wishlist
// ---------------------------------------------------------------------------

export const getWishlist = jest.fn<Promise<number[]>, []>();

export const getWishlistProducts = jest.fn<
  Promise<Page<ApiProduct>>,
  [params?: { page?: number; size?: number; sort?: string }]
>();

export const toggleWishlistApi = jest.fn<Promise<void>, [productId: number]>();

// ---------------------------------------------------------------------------
// Notifications
// ---------------------------------------------------------------------------

export const getNotifications = jest.fn<Promise<ApiNotification[]>, []>();

export const getUnreadCount = jest.fn<Promise<{ count: number }>, []>();

export const markNotificationAsRead = jest.fn<Promise<void>, [id: number]>();

export const markAllNotificationsAsRead = jest.fn<Promise<void>, []>();

export const createNotification = jest.fn<
  Promise<void>,
  [title: string, message: string, productId?: number]
>();

export const deleteNotification = jest.fn<Promise<void>, [id: number]>();

export const deleteAllNotifications = jest.fn<Promise<void>, []>();
