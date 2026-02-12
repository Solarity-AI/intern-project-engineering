/**
 * ProductList.ui.test.tsx — C37
 *
 * UI-level tests for ProductListScreen: loading skeleton, product card grid,
 * and scroll-triggered pagination via LoadMoreCard.
 *
 * All API calls are intercepted via jest.mock; no real HTTP requests are made.
 * Heavy context providers and native-only components are stubbed so the test
 * focuses on the screen's own rendering and interaction logic.
 */

// ---------------------------------------------------------------------------
// Module mocks (declared before any imports — babel-jest hoists these)
// ---------------------------------------------------------------------------

jest.mock('../../services/api', () => ({
  getProducts: jest.fn(),
  getGlobalStats: jest.fn().mockResolvedValue({
    totalProducts: 0,
    totalReviews: 0,
    averageRating: 0,
  }),
}));

jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn().mockResolvedValue(null),
  setItem: jest.fn().mockResolvedValue(undefined),
}));

// expo-navigation-bar is used by ThemeContext on Android
jest.mock('expo-navigation-bar', () => ({
  setBackgroundColorAsync: jest.fn().mockResolvedValue(undefined),
  setPositionAsync: jest.fn().mockResolvedValue(undefined),
  setButtonStyleAsync: jest.fn().mockResolvedValue(undefined),
}));

// React Navigation hooks — provide minimal stubs
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: jest.fn(), setParams: jest.fn(), canGoBack: jest.fn() }),
  useRoute: () => ({ params: {} }),
  useFocusEffect: jest.fn(),
}));

// ---------------------------------------------------------------------------
// Context stubs — each hook returns the minimal shape the screen needs
// ---------------------------------------------------------------------------

jest.mock('../../context/ThemeContext', () => ({
  useTheme: () => ({
    colors: {
      background: '#FAFAFA',
      foreground: '#1A1A1A',
      card: '#FFFFFF',
      primary: '#0066FF',
      primaryForeground: '#FFFFFF',
      secondary: '#F5F5F5',
      muted: '#F5F5F5',
      mutedForeground: '#737373',
      accent: '#EEF2FF',
      border: '#E5E5E5',
      destructive: '#EF4444',
      starFilled: '#F59E0B',
      starEmpty: '#D4D4D4',
    },
    colorScheme: 'light',
    toggleTheme: jest.fn(),
    isThemeLoaded: true,
  }),
}));

jest.mock('../../context/NetworkContext', () => ({
  useNetwork: () => ({
    isConnected: true,
    isInternetReachable: true,
    checkConnection: jest.fn().mockResolvedValue(true),
  }),
}));

jest.mock('../../context/NotificationContext', () => ({
  useNotifications: () => ({
    unreadCount: 0,
    addNotification: jest.fn(),
  }),
}));

jest.mock('../../context/WishlistContext', () => ({
  useWishlist: () => ({
    wishlistCount: 0,
    isInWishlist: jest.fn().mockReturnValue(false),
    toggleWishlist: jest.fn(),
    addMultipleToWishlist: jest.fn(),
  }),
}));

jest.mock('../../context/SearchContext', () => ({
  useSearch: () => ({
    addSearchTerm: jest.fn(),
  }),
}));

// ---------------------------------------------------------------------------
// Component stubs — replace heavy/native-dependent UI with inert Views
// ---------------------------------------------------------------------------

jest.mock('../../components/ScreenWrapper', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    ScreenWrapper: ({ children }: { children: React.ReactNode }) =>
      React.createElement(View, null, children),
  };
});

jest.mock('../../components/OfflineBanner', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { OfflineBanner: () => React.createElement(View, { testID: 'offline-banner' }) };
});

jest.mock('../../components/SearchBar', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { SearchBar: () => React.createElement(View, { testID: 'search-bar' }) };
});

jest.mock('../../components/CategoryFilter', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { CategoryFilter: () => React.createElement(View, { testID: 'category-filter' }) };
});

jest.mock('../../components/SortFilter', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { SortFilter: () => React.createElement(View, { testID: 'sort-filter' }) };
});

jest.mock('expo-linear-gradient', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    LinearGradient: ({ children, ...props }: any) =>
      React.createElement(View, props, children),
  };
});

jest.mock('@expo/vector-icons', () => {
  const React = require('react');
  const { Text } = require('react-native');
  const Ionicons = ({ name }: { name: string }) =>
    React.createElement(Text, { testID: `icon-${name}` }, name);
  return { Ionicons };
});

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import React from 'react';
import { render, fireEvent, waitFor, screen, act } from '@testing-library/react-native';
import { getProducts } from '../../services/api';
import { ProductListScreen } from '../../screens/ProductListScreen';
import type { ApiProduct, Page } from '../../services/api';

// ---------------------------------------------------------------------------
// Typed mock reference
// ---------------------------------------------------------------------------

const mockGetProducts = getProducts as jest.MockedFunction<typeof getProducts>;

// ---------------------------------------------------------------------------
// Factories
// ---------------------------------------------------------------------------

function makeProduct(id: number): ApiProduct {
  return {
    id,
    name: `Product ${id}`,
    description: 'A test product',
    categories: ['Electronics'],
    price: 9.99,
    averageRating: 4.0,
    reviewCount: 3,
  };
}

function makePage(
  content: ApiProduct[],
  last: boolean = true,
): Page<ApiProduct> {
  return {
    content,
    totalElements: content.length,
    totalPages: last ? 1 : 2,
    number: 0,
    size: 20,
    last,
  };
}

// ---------------------------------------------------------------------------
// Setup
// ---------------------------------------------------------------------------

beforeEach(() => {
  jest.clearAllMocks();
});

// ---------------------------------------------------------------------------
// Loading state
// ---------------------------------------------------------------------------

describe('loading state', () => {
  it('shows ActivityIndicator (loading text) before data resolves', async () => {
    let resolveProducts!: (v: Page<ApiProduct>) => void;
    mockGetProducts.mockReturnValueOnce(
      new Promise<Page<ApiProduct>>(r => { resolveProducts = r; }),
    );

    render(<ProductListScreen />);

    // Wait for the fetch to be triggered (sortLoaded → useEffect → fetchProducts)
    await waitFor(() => expect(mockGetProducts).toHaveBeenCalledTimes(1));

    // While getProducts is pending, loading === true → ListEmptyComponent shows ActivityIndicator
    expect(screen.getByText('Loading products...')).toBeTruthy();

    // Resolve to avoid pending-promise warnings after test ends
    await act(async () => {
      resolveProducts(makePage([]));
    });
  });
});

// ---------------------------------------------------------------------------
// Loaded state
// ---------------------------------------------------------------------------

describe('loaded state', () => {
  it('renders one product name per item in the mock response', async () => {
    const products = [makeProduct(1), makeProduct(2), makeProduct(3)];
    mockGetProducts.mockResolvedValueOnce(makePage(products));

    render(<ProductListScreen />);

    await waitFor(() => {
      expect(screen.getByText('Product 1')).toBeTruthy();
      expect(screen.getByText('Product 2')).toBeTruthy();
      expect(screen.getByText('Product 3')).toBeTruthy();
    });
  });

  it('renders price for each product card', async () => {
    mockGetProducts.mockResolvedValueOnce(makePage([makeProduct(1), makeProduct(2)]));

    render(<ProductListScreen />);

    await waitFor(() => {
      // Each card renders "$9.99"
      const prices = screen.getAllByText('$9.99');
      expect(prices.length).toBe(2);
    });
  });
});

// ---------------------------------------------------------------------------
// LoadMoreCard — last: false (more pages available)
// ---------------------------------------------------------------------------

describe('LoadMoreCard when last: false', () => {
  it('is rendered when the first page is not the last', async () => {
    mockGetProducts.mockResolvedValueOnce(makePage([makeProduct(1)], false));

    render(<ProductListScreen />);

    await waitFor(() => {
      expect(screen.getByText('Load More Products')).toBeTruthy();
    });
  });

  it('pressing LoadMoreCard calls getProducts with page: 1', async () => {
    mockGetProducts
      .mockResolvedValueOnce(makePage([makeProduct(1)], false))
      .mockResolvedValueOnce(makePage([makeProduct(2)], true));

    render(<ProductListScreen />);

    await waitFor(() => expect(screen.getByText('Load More Products')).toBeTruthy());

    fireEvent.press(screen.getByText('Load More Products'));

    await waitFor(() => {
      expect(mockGetProducts).toHaveBeenCalledTimes(2);
      expect(mockGetProducts).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 1 }),
      );
    });
  });
});

// ---------------------------------------------------------------------------
// LoadMoreCard — last: true (final page)
// ---------------------------------------------------------------------------

describe('LoadMoreCard when last: true', () => {
  it('does not render the Load More button on the final page', async () => {
    mockGetProducts.mockResolvedValueOnce(makePage([makeProduct(1)], true));

    render(<ProductListScreen />);

    await waitFor(() => expect(screen.getByText('Product 1')).toBeTruthy());

    expect(screen.queryByText('Load More Products')).toBeNull();
  });

  it('renders the end-of-list message instead', async () => {
    mockGetProducts.mockResolvedValueOnce(makePage([makeProduct(1)], true));

    render(<ProductListScreen />);

    await waitFor(() =>
      expect(screen.getByText("You've reached the end")).toBeTruthy(),
    );
  });
});
