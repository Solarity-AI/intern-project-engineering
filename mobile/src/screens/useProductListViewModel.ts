import { useState, useCallback, useRef } from 'react';
import { getProducts } from '../services/api';
import type { ApiProduct } from '../services/api';

export interface FetchParams {
  sort?: string;
  category?: string;
  search?: string;
}

/**
 * Manages the async fetch and pagination state for the product list.
 *
 * Sort, category, and search values are passed as explicit parameters to
 * `fetchProducts` so that the caller (ProductListScreen) owns those filter
 * states and can persist/navigate them independently.
 *
 * `isOffline` is provided by the caller (derived from NetworkContext) rather
 * than consumed internally — this keeps the hook free of context dependencies
 * and makes it trivially testable without a Provider tree.
 */
export function useProductListViewModel(isOffline: boolean) {
  const [apiProducts, setApiProducts] = useState<ApiProduct[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);

  // Monotonically-increasing ID for race-condition protection
  const fetchIdRef = useRef(0);

  const fetchProducts = useCallback(
    async (page: number, append: boolean, params: FetchParams = {}): Promise<void> => {
      fetchIdRef.current += 1;
      const currentFetchId = fetchIdRef.current;

      if (page === 0) setLoading(true);
      else setLoadingMore(true);
      setError(null);

      if (isOffline) {
        setLoading(false);
        setLoadingMore(false);
        return;
      }

      const { sort = 'name,asc', category, search } = params;

      try {
        const res = await getProducts({
          page,
          size: 20,
          category: !category || category === 'All' ? undefined : category,
          search: search?.trim() || undefined,
          sort,
        });

        if (currentFetchId !== fetchIdRef.current) return;

        setTotalPages(res.totalPages);
        setTotalElements(res.totalElements);
        setCurrentPage(page);
        setHasMore(page < res.totalPages - 1);
        setApiProducts(prev => (append ? [...prev, ...res.content] : res.content));
      } catch (err: unknown) {
        const e = err as { name?: string; message?: string };
        if (e.name === 'AbortError') return;
        if (currentFetchId !== fetchIdRef.current) return;
        setError(e?.message ?? 'Failed to fetch products');
      } finally {
        if (currentFetchId === fetchIdRef.current) {
          setLoading(false);
          setLoadingMore(false);
        }
      }
    },
    [isOffline],
  );

  return {
    apiProducts,
    loading,
    error,
    hasMore,
    currentPage,
    totalPages,
    totalElements,
    loadingMore,
    fetchProducts,
  };
}
