/**
 * ProductListViewModel tests — C30
 *
 * Tests the async state and fetch logic of useProductListViewModel without
 * rendering any UI. All API calls are intercepted by jest.mock so no real
 * HTTP requests are made.
 *
 * Design note: sort/category/search are passed as explicit params to
 * fetchProducts (not kept as internal state). ProductListScreen owns those
 * filter states; the ViewModel only owns loading/error/pagination state.
 * isOffline is passed by the caller (derived from NetworkContext) so the
 * hook needs no Provider tree in tests — this is intentional DI, not a gap.
 */

// Factory form prevents api.ts from executing (and pulling in AsyncStorage/uuid
// native modules) while still providing typed jest.fn() stubs.
jest.mock('../services/api', () => ({
  getProducts: jest.fn(),
  getGlobalStats: jest.fn(),
}));

import { renderHook, act } from '@testing-library/react-native';
import { getProducts } from '../services/api';
import { useProductListViewModel } from '../screens/useProductListViewModel';
import type { Page, ApiProduct } from '../services/api';

// ---------------------------------------------------------------------------
// Typed mock reference
// ---------------------------------------------------------------------------

const mockGetProducts = getProducts as jest.MockedFunction<typeof getProducts>;

// ---------------------------------------------------------------------------
// Factories
// ---------------------------------------------------------------------------

function makePage(
  content: ApiProduct[],
  overrides: Partial<Page<ApiProduct>> = {},
): Page<ApiProduct> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 20,
    last: true,
    ...overrides,
  };
}

function makeProduct(id: number): ApiProduct {
  return { id, name: `Product ${id}`, description: '', categories: [], price: 10 };
}

// ---------------------------------------------------------------------------
// Setup
// ---------------------------------------------------------------------------

beforeEach(() => {
  jest.clearAllMocks();
});

// ---------------------------------------------------------------------------
// Initial load
// ---------------------------------------------------------------------------

describe('initial load', () => {
  it('loading is true during fetch, false after resolution', async () => {
    let resolveProducts!: (page: Page<ApiProduct>) => void;
    mockGetProducts.mockReturnValueOnce(
      new Promise<Page<ApiProduct>>(r => {
        resolveProducts = r;
      }),
    );

    const { result } = renderHook(() => useProductListViewModel(false));

    let fetchDone = false;
    act(() => {
      result.current.fetchProducts(0, false).then(() => {
        fetchDone = true;
      });
    });

    expect(result.current.loading).toBe(true);
    expect(fetchDone).toBe(false);

    await act(async () => {
      resolveProducts(makePage([makeProduct(1)]));
    });

    expect(result.current.loading).toBe(false);
  });

  it('successful fetch populates apiProducts with typed data', async () => {
    const products = [makeProduct(1), makeProduct(2)];
    mockGetProducts.mockResolvedValueOnce(makePage(products));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.apiProducts).toEqual(products);
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('fetch rejection sets error state', async () => {
    mockGetProducts.mockRejectedValueOnce(new Error('Network error'));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.error).toBe('Network error');
    expect(result.current.loading).toBe(false);
    expect(result.current.apiProducts).toHaveLength(0);
  });

  it('empty content response sets empty product list', async () => {
    mockGetProducts.mockResolvedValueOnce(makePage([]));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.apiProducts).toHaveLength(0);
    expect(result.current.error).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// Sort
// ---------------------------------------------------------------------------

describe('sort', () => {
  it('fetchProducts passes the given sort param to getProducts', async () => {
    mockGetProducts.mockResolvedValue(makePage([makeProduct(1)]));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false, { sort: 'price,asc' });
    });

    expect(mockGetProducts).toHaveBeenCalledWith(
      expect.objectContaining({ sort: 'price,asc' }),
    );
  });

  it('sort param changes between successive fetches', async () => {
    mockGetProducts.mockResolvedValue(makePage([]));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false, { sort: 'name,asc' });
    });
    expect(mockGetProducts).toHaveBeenLastCalledWith(
      expect.objectContaining({ sort: 'name,asc' }),
    );

    await act(async () => {
      await result.current.fetchProducts(0, false, { sort: 'price,asc' });
    });
    expect(mockGetProducts).toHaveBeenLastCalledWith(
      expect.objectContaining({ sort: 'price,asc' }),
    );
  });
});

// ---------------------------------------------------------------------------
// Pagination
// ---------------------------------------------------------------------------

describe('pagination', () => {
  it('hasMore is true when there are more pages (last: false)', async () => {
    mockGetProducts.mockResolvedValueOnce(
      makePage([makeProduct(1)], { totalPages: 3, last: false, number: 0 }),
    );

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.hasMore).toBe(true);
  });

  it('hasMore is false on the last page (last: true, single page)', async () => {
    mockGetProducts.mockResolvedValueOnce(
      makePage([makeProduct(1)], { totalPages: 1, last: true }),
    );

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.hasMore).toBe(false);
  });

  it('page 1 fetch with append:true extends the product list', async () => {
    mockGetProducts
      .mockResolvedValueOnce(
        makePage([makeProduct(1)], { totalPages: 2, number: 0, last: false }),
      )
      .mockResolvedValueOnce(
        makePage([makeProduct(2)], { totalPages: 2, number: 1, last: true }),
      );

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.hasMore).toBe(true);
    expect(result.current.apiProducts).toHaveLength(1);

    await act(async () => {
      await result.current.fetchProducts(1, true);
    });

    expect(result.current.apiProducts).toHaveLength(2);
    expect(result.current.apiProducts[0]).toEqual(makeProduct(1));
    expect(result.current.apiProducts[1]).toEqual(makeProduct(2));
    expect(mockGetProducts).toHaveBeenCalledTimes(2);
  });
});

// ---------------------------------------------------------------------------
// Offline
// ---------------------------------------------------------------------------

describe('offline', () => {
  it('getProducts is NOT called when isOffline is true', async () => {
    const { result } = renderHook(() => useProductListViewModel(true));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(mockGetProducts).not.toHaveBeenCalled();
  });

  it('loading is reset to false when offline', async () => {
    const { result } = renderHook(() => useProductListViewModel(true));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.loading).toBe(false);
  });

  it('no error is set when offline', async () => {
    const { result } = renderHook(() => useProductListViewModel(true));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.error).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// Category filter
// ---------------------------------------------------------------------------

describe('category filter', () => {
  it('passes the category param to getProducts', async () => {
    mockGetProducts.mockResolvedValue(makePage([]));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false, { category: 'Electronics' });
    });

    expect(mockGetProducts).toHaveBeenCalledWith(
      expect.objectContaining({ category: 'Electronics' }),
    );
  });

  it('category "All" is sent as undefined to the API', async () => {
    mockGetProducts.mockResolvedValue(makePage([]));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false, { category: 'All' });
    });

    expect(mockGetProducts).toHaveBeenCalledWith(
      expect.objectContaining({ category: undefined }),
    );
  });
});

// ---------------------------------------------------------------------------
// Search
// ---------------------------------------------------------------------------

describe('search', () => {
  it('passes the search param to getProducts', async () => {
    mockGetProducts.mockResolvedValue(makePage([]));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false, { search: 'headphones' });
    });

    expect(mockGetProducts).toHaveBeenCalledWith(
      expect.objectContaining({ search: 'headphones' }),
    );
  });

  it('blank search term is passed as undefined to the API', async () => {
    mockGetProducts.mockResolvedValue(makePage([]));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false, { search: '   ' });
    });

    expect(mockGetProducts).toHaveBeenCalledWith(
      expect.objectContaining({ search: undefined }),
    );
  });
});
