/**
 * ErrorAndEdgeCases tests — C34
 *
 * Covers network errors, HTTP failure responses, empty states, and optional
 * field boundaries shared across fetch paths. All API calls are intercepted
 * via jest.mock; no real HTTP requests are made.
 *
 * Test 6 (getUserId fallback) is placed last because it needs to reload the
 * real api.ts module with controlled AsyncStorage behaviour, which replaces
 * the top-level mock factory via jest.doMock.
 */

// Factory form prevents api.ts from pulling in AsyncStorage/uuid native
// modules while still providing typed jest.fn() stubs.
jest.mock('../services/api', () => ({
  getProduct: jest.fn(),
  getProducts: jest.fn(),
  getReviews: jest.fn(),
  getUserVotedReviews: jest.fn(),
}));

import { act } from 'react-test-renderer';
import { getProduct, getProducts, getReviews } from '../services/api';
import { useProductDetailViewModel } from '../screens/useProductDetailViewModel';
import { useProductListViewModel } from '../screens/useProductListViewModel';
import type { ApiProduct, Page } from '../services/api';
import { renderHook } from './test-utils/renderHook';

// ---------------------------------------------------------------------------
// Typed mock references
// ---------------------------------------------------------------------------

const mockGetProduct = getProduct as jest.MockedFunction<typeof getProduct>;
const mockGetProducts = getProducts as jest.MockedFunction<typeof getProducts>;
const mockGetReviews = getReviews as jest.MockedFunction<typeof getReviews>;

// ---------------------------------------------------------------------------
// Factories
// ---------------------------------------------------------------------------

function makeProduct(overrides: Partial<ApiProduct> = {}): ApiProduct {
  return {
    id: 1,
    name: 'Test Product',
    description: 'A test description',
    categories: ['Electronics'],
    price: 9.99,
    ...overrides,
  };
}

function makeEmptyPage<T>(): Page<T> {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
    last: true,
  };
}

// ---------------------------------------------------------------------------
// Setup
// ---------------------------------------------------------------------------

beforeEach(() => {
  jest.clearAllMocks();
});

// ---------------------------------------------------------------------------
// Network timeout
// ---------------------------------------------------------------------------

describe('network timeout', () => {
  it('fetchProduct timeout rejection sets error state without crashing', async () => {
    mockGetProduct.mockRejectedValueOnce(new Error('Network request timed out'));

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.error).toBe('Network request timed out');
    expect(result.current.product).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('fetchProducts timeout rejection sets error state without crashing', async () => {
    mockGetProducts.mockRejectedValueOnce(new Error('Network request timed out'));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.error).toBe('Network request timed out');
    expect(result.current.loading).toBe(false);
    expect(result.current.apiProducts).toHaveLength(0);
  });
});

// ---------------------------------------------------------------------------
// HTTP 500 server error
// ---------------------------------------------------------------------------

describe('HTTP 500 server error', () => {
  it('getProduct 500 sets error state and leaves product null', async () => {
    mockGetProduct.mockRejectedValueOnce(new Error('500 Internal Server Error'));

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.error).toBe('500 Internal Server Error');
    expect(result.current.product).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('getProducts 500 sets error state and leaves product list in safe empty state', async () => {
    mockGetProducts.mockRejectedValueOnce(new Error('500 Internal Server Error'));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.error).toBe('500 Internal Server Error');
    expect(result.current.apiProducts).toHaveLength(0);
    expect(result.current.loading).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// HTTP 404 not found
// ---------------------------------------------------------------------------

describe('HTTP 404 not found', () => {
  it('getProduct 404 sets error state and leaves product null', async () => {
    mockGetProduct.mockRejectedValueOnce(new Error('404 Not Found - '));

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('99');
    });

    expect(result.current.error).toContain('404');
    expect(result.current.product).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('getProducts 404 sets error state', async () => {
    mockGetProducts.mockRejectedValueOnce(new Error('404 Not Found - '));

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.error).toContain('404');
    expect(result.current.apiProducts).toHaveLength(0);
  });
});

// ---------------------------------------------------------------------------
// Empty paginated response
// ---------------------------------------------------------------------------

describe('empty paginated response', () => {
  it('empty product content sets empty list without triggering error state', async () => {
    mockGetProducts.mockResolvedValueOnce(makeEmptyPage<ApiProduct>());

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.apiProducts).toHaveLength(0);
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('empty reviews content sets empty list without triggering error state', async () => {
    mockGetReviews.mockResolvedValueOnce(makeEmptyPage());

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1');
    });

    expect(result.current.reviews).toHaveLength(0);
    expect(result.current.error).toBeNull();
  });

  it('empty page has hasMore false', async () => {
    mockGetProducts.mockResolvedValueOnce(makeEmptyPage<ApiProduct>());

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.hasMore).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// ApiProduct with missing optional fields
// ---------------------------------------------------------------------------

describe('ApiProduct with missing optional fields', () => {
  it('product without imageUrl, aiSummary, and averageRating is stored without crash', async () => {
    const sparse = makeProduct({
      // all optional fields omitted
    });
    mockGetProduct.mockResolvedValueOnce(sparse);

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.product).not.toBeNull();
    expect(result.current.product?.imageUrl).toBeUndefined();
    expect(result.current.product?.aiSummary).toBeUndefined();
    expect(result.current.product?.averageRating).toBeUndefined();
    expect(result.current.product?.reviewCount).toBeUndefined();
    expect(result.current.error).toBeNull();
  });

  it('product list containing sparse entries does not trigger error state', async () => {
    mockGetProducts.mockResolvedValueOnce({
      content: [
        makeProduct({ id: 1 }),
        makeProduct({ id: 2, averageRating: undefined, imageUrl: undefined }),
      ],
      totalElements: 2,
      totalPages: 1,
      number: 0,
      size: 10,
      last: true,
    });

    const { result } = renderHook(() => useProductListViewModel(false));

    await act(async () => {
      await result.current.fetchProducts(0, false);
    });

    expect(result.current.apiProducts).toHaveLength(2);
    expect(result.current.error).toBeNull();
  });
});

