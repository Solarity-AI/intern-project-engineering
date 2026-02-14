import AsyncStorage from '@react-native-async-storage/async-storage';
import 'react-native-get-random-values';
import { v4 as uuidv4 } from 'uuid';

export const BASE_URL = process.env.EXPO_PUBLIC_API_URL;
if (!BASE_URL) throw new Error("EXPO_PUBLIC_API_URL is not set");

const USER_ID_KEY = 'device_user_id';

const DEFAULT_TIMEOUT_MS = 15_000;
const AI_TIMEOUT_MS = 30_000;

const RETRIABLE_STATUS_CODES = new Set([408, 429, 500, 502, 503, 504]);
const MAX_RETRY_ATTEMPTS = 3;
const BASE_RETRY_DELAY_MS = 1000;

// --- In-flight request deduplication ---
const inflightRequests = new Map<string, Promise<any>>();

// --- Memory cache (TTL-based) ---
const CACHE_TTL_MS = 60_000; // 1 minute default
const memoryCache = new Map<string, { data: any; expiresAt: number }>();

export function clearApiCache() {
  memoryCache.clear();
  inflightRequests.clear();
}

/** Remove cached entries and inflight requests whose keys contain any of the given substrings. */
function invalidateCache(...patterns: string[]) {
  for (const key of memoryCache.keys()) {
    if (patterns.some(p => key.includes(p))) memoryCache.delete(key);
  }
  for (const key of inflightRequests.keys()) {
    if (patterns.some(p => key.includes(p))) inflightRequests.delete(key);
  }
}

function getCached<T>(key: string): T | undefined {
  const entry = memoryCache.get(key);
  if (!entry) return undefined;
  if (Date.now() > entry.expiresAt) {
    memoryCache.delete(key);
    return undefined;
  }
  return entry.data as T;
}

function setCache(key: string, data: any, ttlMs: number = CACHE_TTL_MS) {
  memoryCache.set(key, { data, expiresAt: Date.now() + ttlMs });
}

// --- ApiError ---

export class ApiError extends Error {
  status: number;
  statusText: string;
  code: string;
  serverMessage: string;
  details: string | null;

  constructor(
    status: number,
    statusText: string,
    code: string,
    serverMessage: string,
    details: string | null = null,
  ) {
    super(`${status} ${statusText}: ${serverMessage}`);
    this.name = 'ApiError';
    this.status = status;
    this.statusText = statusText;
    this.code = code;
    this.serverMessage = serverMessage;
    this.details = details;
  }
}

export function getUserMessage(error: unknown): string {
  if (error instanceof ApiError) {
    switch (error.status) {
      case 400: return 'Invalid request. Please check your input and try again.';
      case 401: return 'You need to sign in to continue.';
      case 403: return 'You don\'t have permission to do that.';
      case 404: return 'The item you\'re looking for was not found.';
      case 409: return 'This action conflicts with another operation. Please try again.';
      case 422: return 'Some of your input is invalid. Please review and try again.';
      case 429: return 'Too many requests. Please wait a moment and try again.';
      case 500:
      case 502:
      case 503:
      case 504:
        return 'The server is having trouble right now. Please try again later.';
      default:
        return error.serverMessage || 'Something went wrong. Please try again.';
    }
  }

  if (error instanceof TypeError && error.message === 'Network request failed') {
    return 'Unable to connect. Please check your internet connection.';
  }

  if (error instanceof DOMException && error.name === 'AbortError') {
    return 'The request took too long. Please try again.';
  }

  if (error instanceof Error) {
    if (error.name === 'AbortError') {
      return 'The request took too long. Please try again.';
    }
    if (error.message.includes('Network request failed') || error.message.includes('Failed to fetch')) {
      return 'Unable to connect. Please check your internet connection.';
    }
  }

  return 'Something went wrong. Please try again.';
}

// Get or create a persistent User ID
export async function getUserId(): Promise<string> {
  try {
    let userId = await AsyncStorage.getItem(USER_ID_KEY);
    if (!userId) {
      userId = uuidv4();
      await AsyncStorage.setItem(USER_ID_KEY, userId);
    }
    return userId;
  } catch (e) {
    console.error('Error getting user ID', e);
    return 'anonymous-user';
  }
}

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // page index
  size: number;
  last: boolean;
};

export type ApiProduct = {
  id: number;
  name: string;
  description: string;
  categories: string[]; // ✨ Changed from category: string to categories: string[]
  price: number;
  averageRating?: number;
  reviewCount?: number;
  ratingBreakdown?: Record<number, number>;
  imageUrl?: string;
  aiSummary?: string;
};

export type ApiReview = {
  id?: number;
  reviewerName?: string;
  rating: number;
  comment: string;
  helpfulCount?: number;
  createdAt?: string;
};

export type ApiNotification = {
  id: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  productId?: number;
};

// ✨ NEW: Global stats type
export type GlobalStats = {
  totalProducts: number;
  totalReviews: number;
  averageRating: number;
};

async function request<T>(url: string, options?: RequestInit & { timeoutMs?: number }): Promise<T> {
  const userId = await getUserId();

  const timeoutMs = options?.timeoutMs ?? DEFAULT_TIMEOUT_MS;
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

  // If caller already provided a signal, listen for its abort too
  if (options?.signal) {
    options.signal.addEventListener('abort', () => controller.abort());
  }

  const headers = {
    ...options?.headers,
    'X-User-ID': userId,
  };

  try {
    const res = await fetch(url, { ...options, headers, signal: controller.signal });

    if (!res.ok) {
      let code = `HTTP_${res.status}`;
      let serverMessage = res.statusText;
      let details: string | null = null;

      try {
        const body = await res.json();
        if (body && typeof body === 'object') {
          code = body.code || code;
          serverMessage = body.message || serverMessage;
          details = body.details || null;
        }
      } catch {
        // body was not JSON — keep defaults
      }

      throw new ApiError(res.status, res.statusText, code, serverMessage, details);
    }

    const text = await res.text();
    return text ? JSON.parse(text) : {} as T;
  } finally {
    clearTimeout(timeoutId);
  }
}

export function isRetriable(error: unknown): boolean {
  if (error instanceof ApiError) {
    return RETRIABLE_STATUS_CODES.has(error.status);
  }
  // Network errors (fetch failed, DNS, connection refused, etc.)
  if (error instanceof TypeError) return true;
  if (error instanceof Error) {
    if (error.name === 'AbortError') return false; // Don't retry timeouts
    if (error.message.includes('Network request failed') || error.message.includes('Failed to fetch')) return true;
  }
  return false;
}

async function requestWithRetryInternal<T>(url: string, options?: RequestInit & { timeoutMs?: number }): Promise<T> {
  const method = (options?.method || 'GET').toUpperCase();
  const canRetry = method === 'GET' || method === 'PUT';

  let lastError: unknown;
  const attempts = canRetry ? MAX_RETRY_ATTEMPTS : 1;

  for (let attempt = 1; attempt <= attempts; attempt++) {
    try {
      return await request<T>(url, options);
    } catch (error) {
      lastError = error;
      if (attempt < attempts && canRetry && isRetriable(error)) {
        const delay = BASE_RETRY_DELAY_MS * Math.pow(2, attempt - 1);
        await new Promise((resolve) => setTimeout(resolve, delay));
        continue;
      }
      throw error;
    }
  }

  throw lastError;
}

/** GET requests are deduplicated (same URL shares one in-flight promise) and cached. */
async function requestWithRetry<T>(url: string, options?: RequestInit & { timeoutMs?: number }): Promise<T> {
  const method = (options?.method || 'GET').toUpperCase();

  // Only deduplicate & cache GET requests
  if (method !== 'GET') {
    return requestWithRetryInternal<T>(url, options);
  }

  // Check memory cache first
  const cached = getCached<T>(url);
  if (cached !== undefined) return cached;

  // Deduplicate in-flight requests
  const existing = inflightRequests.get(url);
  if (existing) return existing as Promise<T>;

  const promise = requestWithRetryInternal<T>(url, options)
    .then(data => {
      setCache(url, data);
      return data;
    })
    .finally(() => {
      inflightRequests.delete(url);
    });

  inflightRequests.set(url, promise);
  return promise;
}

// ✨ NEW: Get global stats for hero section (supports filtering)
export function getGlobalStats(params?: { category?: string; search?: string }) {
  const q = new URLSearchParams();

  if (params?.category && params.category !== 'All') {
    q.append('category', params.category);
  }

  if (params?.search) {
    q.append('search', params.search);
  }

  const queryString = q.toString();
  const url = queryString
    ? `${BASE_URL}/api/v1/products/stats?${queryString}`
    : `${BASE_URL}/api/v1/products/stats`;

  return requestWithRetry<GlobalStats>(url);
}

export function getProducts(params?: { page?: number; size?: number; sort?: string; category?: string; search?: string }) {
  const q = new URLSearchParams({
    page: String(params?.page ?? 0),
    size: String(params?.size ?? 10),
    sort: params?.sort ?? "name,asc",
  });

  if (params?.category && params.category !== 'All') {
    q.append('category', params.category);
  }

  if (params?.search) {
    q.append('search', params.search);
  }

  return requestWithRetry<Page<ApiProduct>>(`${BASE_URL}/api/v1/products?${q.toString()}`);
}

export function getProduct(id: number | string) {
  return requestWithRetry<ApiProduct>(`${BASE_URL}/api/v1/products/${id}`);
}

export function getReviews(productId: number | string, params?: { page?: number; size?: number; sort?: string; rating?: number | null }) {
  const q = new URLSearchParams({
    page: String(params?.page ?? 0),
    size: String(params?.size ?? 10),
    sort: params?.sort ?? "createdAt,desc",
  });

  if (params?.rating) {
    q.append('rating', String(params.rating));
  }

  return requestWithRetry<Page<ApiReview>>(`${BASE_URL}/api/v1/products/${productId}/reviews?${q.toString()}`);
}

export async function postReview(productId: number | string, body: ApiReview) {
  const result = await request<ApiReview>(`${BASE_URL}/api/v1/products/${productId}/reviews`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  invalidateCache(`/api/v1/products/${productId}`, '/api/v1/products/stats');
  return result;
}

export async function markReviewAsHelpful(reviewId: number | string) {
  const result = await requestWithRetry<ApiReview>(`${BASE_URL}/api/v1/products/reviews/${reviewId}/helpful`, {
    method: "PUT",
  });
  invalidateCache('/api/v1/products/reviews');
  return result;
}

export function getUserVotedReviews() {
  return requestWithRetry<number[]>(`${BASE_URL}/api/v1/products/reviews/voted`);
}

export function chatWithAI(productId: number | string, question: string) {
  return request<{ answer: string }>(`${BASE_URL}/api/v1/products/${productId}/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ question }),
    timeoutMs: AI_TIMEOUT_MS,
  });
}

// --- User Persistence (Wishlist & Notifications) ---

export function getWishlist() {
  return requestWithRetry<number[]>(`${BASE_URL}/api/v1/user/wishlist`);
}

// ✨ New function for paged wishlist products
export function getWishlistProducts(params?: { page?: number; size?: number; sort?: string }) {
  const q = new URLSearchParams({
    page: String(params?.page ?? 0),
    size: String(params?.size ?? 10),
    sort: params?.sort ?? "id,desc",
  });

  return requestWithRetry<Page<ApiProduct>>(`${BASE_URL}/api/v1/user/wishlist/products?${q.toString()}`);
}

export async function toggleWishlistApi(productId: number) {
  const result = await request<void>(`${BASE_URL}/api/v1/user/wishlist/${productId}`, {
    method: "POST",
  });
  invalidateCache('/api/v1/user/wishlist');
  return result;
}

export function getNotifications() {
  return requestWithRetry<ApiNotification[]>(`${BASE_URL}/api/v1/user/notifications`);
}

export function getUnreadCount() {
  return requestWithRetry<{ count: number }>(`${BASE_URL}/api/v1/user/notifications/unread-count`);
}

export function markNotificationAsRead(id: number) {
  return requestWithRetry<void>(`${BASE_URL}/api/v1/user/notifications/${id}/read`, {
    method: "PUT",
  });
}

export function markAllNotificationsAsRead() {
  return requestWithRetry<void>(`${BASE_URL}/api/v1/user/notifications/read-all`, {
    method: "PUT",
  });
}

export function createNotification(title: string, message: string, productId?: number) {
  return request<void>(`${BASE_URL}/api/v1/user/notifications`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title, message, productId }),
  });
}

export function deleteNotification(id: number) {
  return request<void>(`${BASE_URL}/api/v1/user/notifications/${id}`, {
    method: "DELETE",
  });
}

export function deleteAllNotifications() {
  return request<void>(`${BASE_URL}/api/v1/user/notifications`, {
    method: "DELETE",
  });
}
