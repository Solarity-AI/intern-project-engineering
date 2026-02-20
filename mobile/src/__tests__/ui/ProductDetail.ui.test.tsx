/**
 * ProductDetail.ui.test.tsx — C34
 *
 * UI navigation test for the primary user journey:
 * ProductList -> ProductDetails -> back to ProductList.
 *
 * All API calls are intercepted via jest.mock; no real HTTP requests are made.
 */

jest.mock('../../services/api', () => ({
  getProducts: jest.fn(),
  getGlobalStats: jest.fn().mockResolvedValue({
    totalProducts: 1,
    totalReviews: 1,
    averageRating: 4.5,
  }),
  getProduct: jest.fn(),
  getReviews: jest.fn(),
  getUserVotedReviews: jest.fn(),
  postReview: jest.fn(),
  markReviewAsHelpful: jest.fn(),
  getUserMessage: jest.fn((error: unknown) =>
    error instanceof Error ? error.message : 'Unexpected error'
  ),
}));

jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn().mockResolvedValue(null),
  setItem: jest.fn().mockResolvedValue(undefined),
}));

jest.mock('expo-navigation-bar', () => ({
  setBackgroundColorAsync: jest.fn().mockResolvedValue(undefined),
  setPositionAsync: jest.fn().mockResolvedValue(undefined),
  setButtonStyleAsync: jest.fn().mockResolvedValue(undefined),
}));

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

jest.mock('../../context/ToastContext', () => {
  const React = require('react');
  return {
    ToastProvider: ({ children }: { children: React.ReactNode }) =>
      React.createElement(React.Fragment, null, children),
    useToast: () => ({ showToast: jest.fn() }),
  };
});

jest.mock('../../components/ScreenWrapper', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    ScreenWrapper: ({ children }: { children: React.ReactNode }) =>
      React.createElement(View, null, children),
  };
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

jest.mock('../../components/OfflineBanner', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { OfflineBanner: () => React.createElement(View, { testID: 'offline-banner' }) };
});

jest.mock('../../components/StarRating', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { StarRating: () => React.createElement(View, { testID: 'star-rating' }) };
});

jest.mock('../../components/RatingBreakdown', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { RatingBreakdown: () => React.createElement(View, { testID: 'rating-breakdown' }) };
});

jest.mock('../../components/ReviewCard', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { ReviewCard: () => React.createElement(View, { testID: 'review-card' }) };
});

jest.mock('../../components/Button', () => {
  const React = require('react');
  const { TouchableOpacity, Text } = require('react-native');
  return {
    Button: ({ children, onPress }: { children: React.ReactNode; onPress?: () => void }) =>
      React.createElement(
        TouchableOpacity,
        { onPress },
        React.createElement(Text, null, children),
      ),
  };
});

jest.mock('../../components/AddReviewModal', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { AddReviewModal: () => React.createElement(View, { testID: 'add-review-modal' }) };
});

jest.mock('../../components/AISummaryCard', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { AISummaryCard: () => React.createElement(View, { testID: 'ai-summary-card' }) };
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

import React from 'react';
import { render, fireEvent, waitFor, screen } from '@testing-library/react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import {
  getProducts,
  getProduct,
  getReviews,
  getUserVotedReviews,
} from '../../services/api';
import { ProductListScreen } from '../../screens/ProductListScreen';
import { ProductDetailsScreen } from '../../screens/ProductDetailsScreen';
import type { ApiProduct, ApiReview, Page } from '../../services/api';
import type { RootStackParamList } from '../../types';

const mockGetProducts = getProducts as jest.MockedFunction<typeof getProducts>;
const mockGetProduct = getProduct as jest.MockedFunction<typeof getProduct>;
const mockGetReviews = getReviews as jest.MockedFunction<typeof getReviews>;
const mockGetUserVotedReviews =
  getUserVotedReviews as jest.MockedFunction<typeof getUserVotedReviews>;

const Stack = createNativeStackNavigator<RootStackParamList>();

function makeProduct(
  id: number,
  name: string,
  price: number,
  overrides: Partial<ApiProduct> = {},
): ApiProduct {
  return {
    id,
    name,
    description: 'Test product',
    categories: ['Electronics'],
    price,
    averageRating: 4.5,
    reviewCount: 12,
    imageUrl: `https://example.com/product-${id}.jpg`,
    ...overrides,
  };
}

function makeProductPage(content: ApiProduct[]): Page<ApiProduct> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 20,
    last: true,
  };
}

function makeReviewPage(content: ApiReview[] = []): Page<ApiReview> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 10,
    last: true,
  };
}

function renderFlow() {
  return render(
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="ProductList" component={ProductListScreen} />
        <Stack.Screen name="ProductDetails" component={ProductDetailsScreen} />
      </Stack.Navigator>
    </NavigationContainer>,
  );
}

async function navigateToDetails() {
  const listProduct = makeProduct(101, 'List Product 101', 19.99);
  const detailProduct = makeProduct(101, 'Detail Product 101', 129.99);

  mockGetProducts.mockResolvedValueOnce(makeProductPage([listProduct]));
  mockGetProduct.mockResolvedValue(detailProduct);
  mockGetReviews.mockResolvedValue(makeReviewPage());
  mockGetUserVotedReviews.mockResolvedValue([]);

  renderFlow();

  await waitFor(() => expect(screen.getByText('List Product 101')).toBeTruthy());
  fireEvent.press(screen.getByText('List Product 101'));

  await waitFor(() => expect(screen.getByText('Detail Product 101')).toBeTruthy());

  return { listProduct, detailProduct };
}

beforeEach(() => {
  jest.clearAllMocks();
});

describe('ProductList -> ProductDetails navigation', () => {
  it('pressing a SelectableProductCard navigates to ProductDetailsScreen', async () => {
    await navigateToDetails();
    expect(mockGetProduct).toHaveBeenCalledWith(101);
  });

  it('ProductDetailsScreen renders the product name from mock ApiProduct', async () => {
    const { detailProduct } = await navigateToDetails();
    expect(screen.getByText(detailProduct.name)).toBeTruthy();
  });

  it('ProductDetailsScreen renders the product price', async () => {
    const { detailProduct } = await navigateToDetails();
    expect(screen.getByText(`$${detailProduct.price.toFixed(2)}`)).toBeTruthy();
  });

  it('back action returns to the product list screen', async () => {
    await navigateToDetails();

    fireEvent.press(screen.getByTestId('icon-chevron-back'));

    await waitFor(() => {
      expect(screen.getByText('List Product 101')).toBeTruthy();
    });
  });
});
