/**
 * ProductDetailViewModel tests — C33
 *
 * Tests the async state and fetch logic of useProductDetailViewModel without
 * rendering any UI. All API calls are intercepted by jest.mock so no real
 * HTTP requests are made.
 *
 * Covered fetch paths:
 *   - fetchProduct: success, failure
 *   - fetchReviews: success, pagination (last flag), append behaviour
 *   - fetchUserVotes: success → helpfulReviews state
 *   - aiSummary: present and absent
 */

// Factory form prevents api.ts from executing (and pulling in AsyncStorage/uuid
// native modules) while still providing typed jest.fn() stubs.
jest.mock('../services/api', () => ({
  getProduct: jest.fn(),
  getReviews: jest.fn(),
  getUserVotedReviews: jest.fn(),
}));

import { act } from 'react-test-renderer';
import { getProduct, getReviews, getUserVotedReviews } from '../services/api';
import { useProductDetailViewModel } from '../screens/useProductDetailViewModel';
import type { ApiProduct, ApiReview, Page } from '../services/api';
import { renderHook } from './test-utils/renderHook';

// ---------------------------------------------------------------------------
// Typed mock references
// ---------------------------------------------------------------------------

const mockGetProduct = getProduct as jest.MockedFunction<typeof getProduct>;
const mockGetReviews = getReviews as jest.MockedFunction<typeof getReviews>;
const mockGetUserVotedReviews = getUserVotedReviews as jest.MockedFunction<typeof getUserVotedReviews>;

// ---------------------------------------------------------------------------
// Factories
// ---------------------------------------------------------------------------

function makeProduct(id: number, overrides: Partial<ApiProduct> = {}): ApiProduct {
  return {
    id,
    name: `Product ${id}`,
    description: 'A test product',
    categories: ['Electronics'],
    price: 9.99,
    averageRating: 4.2,
    reviewCount: 10,
    ...overrides,
  };
}

function makeApiReview(id: number, overrides: Partial<ApiReview> = {}): ApiReview {
  return {
    id,
    reviewerName: `User ${id}`,
    rating: 4,
    comment: `Review comment ${id}`,
    helpfulCount: 0,
    createdAt: '2024-01-01T00:00:00Z',
    ...overrides,
  };
}

function makeReviewPage(
  content: ApiReview[],
  overrides: Partial<Page<ApiReview>> = {},
): Page<ApiReview> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 10,
    last: true,
    ...overrides,
  };
}

// ---------------------------------------------------------------------------
// Setup
// ---------------------------------------------------------------------------

beforeEach(() => {
  jest.clearAllMocks();
});

// ---------------------------------------------------------------------------
// fetchProduct — success
// ---------------------------------------------------------------------------

describe('fetchProduct — success', () => {
  it('populates product state with the resolved ApiProduct', async () => {
    const product = makeProduct(1);
    mockGetProduct.mockResolvedValueOnce(product);

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.product).toEqual(product);
    expect(result.current.error).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('stores all ApiProduct fields correctly', async () => {
    const product = makeProduct(42, {
      name: 'Widget Pro',
      description: 'A premium widget',
      categories: ['Tools', 'Hardware'],
      price: 49.99,
      averageRating: 4.8,
      reviewCount: 123,
    });
    mockGetProduct.mockResolvedValueOnce(product);

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('42');
    });

    expect(result.current.product?.id).toBe(42);
    expect(result.current.product?.name).toBe('Widget Pro');
    expect(result.current.product?.categories).toEqual(['Tools', 'Hardware']);
    expect(result.current.product?.price).toBe(49.99);
    expect(result.current.product?.averageRating).toBe(4.8);
    expect(result.current.product?.reviewCount).toBe(123);
  });

  it('loading is true during fetch, false after resolution', async () => {
    let resolveProduct!: (p: ApiProduct) => void;
    mockGetProduct.mockReturnValueOnce(
      new Promise<ApiProduct>(r => {
        resolveProduct = r;
      }),
    );

    const { result } = renderHook(() => useProductDetailViewModel());

    let fetchDone = false;
    act(() => {
      result.current.fetchProduct('1').then(() => {
        fetchDone = true;
      });
    });

    expect(result.current.loading).toBe(true);
    expect(fetchDone).toBe(false);

    await act(async () => {
      resolveProduct(makeProduct(1));
    });

    expect(result.current.loading).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// fetchProduct — failure
// ---------------------------------------------------------------------------

describe('fetchProduct — failure', () => {
  it('sets error state on rejection', async () => {
    mockGetProduct.mockRejectedValueOnce(new Error('Network error'));

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.error).toBe('Network error');
    expect(result.current.product).toBeNull();
    expect(result.current.loading).toBe(false);
  });

  it('sets a fallback error message when the error has no message', async () => {
    mockGetProduct.mockRejectedValueOnce({});

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.error).toBe('Failed to load product');
  });
});

// ---------------------------------------------------------------------------
// aiSummary
// ---------------------------------------------------------------------------

describe('aiSummary', () => {
  it('aiSummary field is populated when present in product data', async () => {
    const product = makeProduct(1, { aiSummary: 'Users love this product for its durability.' });
    mockGetProduct.mockResolvedValueOnce(product);

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.product?.aiSummary).toBe('Users love this product for its durability.');
  });

  it('aiSummary is undefined when absent in product data', async () => {
    const product = makeProduct(1); // no aiSummary field
    mockGetProduct.mockResolvedValueOnce(product);

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchProduct('1');
    });

    expect(result.current.product?.aiSummary).toBeUndefined();
  });
});

// ---------------------------------------------------------------------------
// fetchReviews — success
// ---------------------------------------------------------------------------

describe('fetchReviews — success', () => {
  it('populates reviews list with mapped Review objects', async () => {
    mockGetReviews.mockResolvedValueOnce(
      makeReviewPage([makeApiReview(1), makeApiReview(2)]),
    );

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1');
    });

    expect(result.current.reviews).toHaveLength(2);
    expect(result.current.reviews[0].id).toBe('1');
    expect(result.current.reviews[0].rating).toBe(4);
    expect(result.current.reviews[0].userName).toBe('User 1');
    expect(result.current.reviews[0].comment).toBe('Review comment 1');
    expect(result.current.error).toBeNull();
  });

  it('maps reviewerName to userName, defaulting to Anonymous', async () => {
    mockGetReviews.mockResolvedValueOnce(
      makeReviewPage([makeApiReview(1, { reviewerName: undefined })]),
    );

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1');
    });

    expect(result.current.reviews[0].userName).toBe('Anonymous');
  });

  it('replace mode (append: false) overwrites previous reviews', async () => {
    mockGetReviews
      .mockResolvedValueOnce(makeReviewPage([makeApiReview(1), makeApiReview(2)]))
      .mockResolvedValueOnce(makeReviewPage([makeApiReview(3)]));

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1', 0, false);
    });

    expect(result.current.reviews).toHaveLength(2);

    await act(async () => {
      await result.current.fetchReviews('1', 0, false);
    });

    expect(result.current.reviews).toHaveLength(1);
    expect(result.current.reviews[0].id).toBe('3');
  });
});

// ---------------------------------------------------------------------------
// fetchReviews — pagination
// ---------------------------------------------------------------------------

describe('fetchReviews — pagination', () => {
  it('hasMore is true when last is false', async () => {
    mockGetReviews.mockResolvedValueOnce(
      makeReviewPage([makeApiReview(1)], { last: false, totalPages: 3 }),
    );

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1');
    });

    expect(result.current.hasMore).toBe(true);
  });

  it('hasMore is false when last is true', async () => {
    mockGetReviews.mockResolvedValueOnce(
      makeReviewPage([makeApiReview(1)], { last: true, totalPages: 1 }),
    );

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1');
    });

    expect(result.current.hasMore).toBe(false);
  });

  it('append mode extends the existing reviews list', async () => {
    mockGetReviews
      .mockResolvedValueOnce(
        makeReviewPage([makeApiReview(1)], { last: false, totalPages: 2, number: 0 }),
      )
      .mockResolvedValueOnce(
        makeReviewPage([makeApiReview(2)], { last: true, totalPages: 2, number: 1 }),
      );

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1', 0, false);
    });

    expect(result.current.reviews).toHaveLength(1);
    expect(result.current.hasMore).toBe(true);

    await act(async () => {
      await result.current.fetchReviews('1', 1, true);
    });

    expect(result.current.reviews).toHaveLength(2);
    expect(result.current.reviews[0].id).toBe('1');
    expect(result.current.reviews[1].id).toBe('2');
    expect(result.current.hasMore).toBe(false);
    expect(mockGetReviews).toHaveBeenCalledTimes(2);
  });

  it('currentPage and totalPages are updated after each fetch', async () => {
    mockGetReviews.mockResolvedValueOnce(
      makeReviewPage([makeApiReview(1)], { last: false, totalPages: 5, number: 0 }),
    );

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchReviews('1', 0);
    });

    expect(result.current.currentPage).toBe(0);
    expect(result.current.totalPages).toBe(5);
  });
});

// ---------------------------------------------------------------------------
// fetchUserVotes
// ---------------------------------------------------------------------------

describe('fetchUserVotes', () => {
  it('reflects voted review ids in helpfulReviews state as strings', async () => {
    mockGetUserVotedReviews.mockResolvedValueOnce([1, 2, 3]);

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchUserVotes();
    });

    expect(result.current.helpfulReviews).toEqual(['1', '2', '3']);
  });

  it('sets empty helpfulReviews when no votes exist', async () => {
    mockGetUserVotedReviews.mockResolvedValueOnce([]);

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchUserVotes();
    });

    expect(result.current.helpfulReviews).toHaveLength(0);
  });

  it('does not set error state when getUserVotedReviews rejects', async () => {
    mockGetUserVotedReviews.mockRejectedValueOnce(new Error('Unauthorized'));

    const { result } = renderHook(() => useProductDetailViewModel());

    await act(async () => {
      await result.current.fetchUserVotes();
    });

    // error is silently swallowed (matches screen behaviour)
    expect(result.current.error).toBeNull();
    expect(result.current.helpfulReviews).toHaveLength(0);
  });
});
