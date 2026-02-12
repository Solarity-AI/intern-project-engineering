import { useState, useCallback } from 'react';
import { getProduct, getReviews, getUserVotedReviews } from '../services/api';
import type { ApiProduct, ApiReview } from '../services/api';
import type { Review } from '../types';

const mapApiReviewToReview = (apiReview: ApiReview, productId: string): Review => {
  const reviewerName = apiReview.reviewerName || 'Anonymous';
  return {
    id: String(apiReview.id ?? Date.now()),
    productId,
    userName: reviewerName,
    reviewerName,
    rating: apiReview.rating,
    comment: apiReview.comment,
    createdAt: apiReview.createdAt || new Date().toISOString(),
    helpfulCount: apiReview.helpfulCount ?? 0,
  };
};

/**
 * Manages the async fetch state for the product details screen.
 *
 * Exposes three independent fetch operations:
 *   - fetchProduct: loads a single product by id
 *   - fetchReviews: loads paginated reviews for a product
 *   - fetchUserVotes: loads the set of review ids the user has voted helpful
 *
 * No context dependencies — passes productId as an explicit argument so the
 * hook is trivially testable without a Provider tree.
 */
export function useProductDetailViewModel() {
  const [product, setProduct] = useState<ApiProduct | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [reviews, setReviews] = useState<Review[]>([]);
  const [hasMore, setHasMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);

  const [helpfulReviews, setHelpfulReviews] = useState<string[]>([]);

  const fetchProduct = useCallback(async (productId: string): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const data = await getProduct(productId);
      setProduct(data);
    } catch (err: unknown) {
      const e = err as { message?: string };
      setError(e?.message ?? 'Failed to load product');
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchReviews = useCallback(
    async (
      productId: string,
      pageNum: number = 0,
      append: boolean = false,
      rating: number | null = null,
    ): Promise<void> => {
      if (append) setLoadingMore(true);
      try {
        const reviewsData = await getReviews(productId, {
          page: pageNum,
          size: 10,
          rating,
        });

        const newReviews: Review[] = (reviewsData.content || []).map(
          (r: ApiReview) => mapApiReviewToReview(r, productId),
        );

        if (append) {
          setReviews(prev => [...prev, ...newReviews]);
        } else {
          setReviews(newReviews);
        }

        setCurrentPage(pageNum);
        setTotalPages(reviewsData.totalPages);
        setHasMore(!reviewsData.last);
      } catch {
        // matches ProductDetailsScreen behaviour: log only, no error state
      } finally {
        setLoadingMore(false);
      }
    },
    [],
  );

  const fetchUserVotes = useCallback(async (): Promise<void> => {
    try {
      const votedIds = await getUserVotedReviews();
      setHelpfulReviews(votedIds.map(String));
    } catch {
      // matches ProductDetailsScreen behaviour: log only
    }
  }, []);

  return {
    product,
    loading,
    error,
    reviews,
    setReviews,
    hasMore,
    currentPage,
    totalPages,
    loadingMore,
    helpfulReviews,
    setHelpfulReviews,
    fetchProduct,
    fetchReviews,
    fetchUserVotes,
  };
}
