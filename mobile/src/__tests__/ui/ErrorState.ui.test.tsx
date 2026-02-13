/**
 * ErrorState.ui.test.tsx — C38
 *
 * UI-level error/offline tests for ProductListScreen and ProductDetailsScreen.
 */

const mockCheckConnection = jest.fn().mockResolvedValue(true);
const mockSetParams = jest.fn();
const mockNavigate = jest.fn();
const mockGoBack = jest.fn();
const mockCanGoBack = jest.fn(() => true);
const mockShowToast = jest.fn();
const mockNetworkState = {
  isConnected: true,
  isInternetReachable: true as boolean | null,
};
const mockRouteParams: Record<string, any> = {};

jest.mock('../../services/api', () => ({
  getProducts: jest.fn(),
  getGlobalStats: jest.fn().mockResolvedValue({
    totalProducts: 0,
    totalReviews: 0,
    averageRating: 0,
  }),
  getProduct: jest.fn(),
  getReviews: jest.fn().mockResolvedValue({
    content: [],
    totalElements: 0,
    totalPages: 1,
    number: 0,
    size: 10,
    last: true,
  }),
  getUserVotedReviews: jest.fn().mockResolvedValue([]),
  postReview: jest.fn(),
  markReviewAsHelpful: jest.fn(),
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

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({
    navigate: mockNavigate,
    setParams: mockSetParams,
    canGoBack: mockCanGoBack,
    goBack: mockGoBack,
  }),
  useRoute: () => ({ params: mockRouteParams }),
  useFocusEffect: jest.fn(),
}));

jest.mock('../../context/NetworkContext', () => ({
  useNetwork: () => ({
    isConnected: mockNetworkState.isConnected,
    isInternetReachable: mockNetworkState.isInternetReachable,
    checkConnection: mockCheckConnection,
  }),
}));

jest.mock('../../context/SearchContext', () => ({
  useSearch: () => ({
    addSearchTerm: jest.fn(),
    searchHistory: [],
    removeSearchTerm: jest.fn(),
    clearHistory: jest.fn(),
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

jest.mock('../../context/ToastContext', () => {
  const React = require('react');
  return {
    ToastProvider: ({ children }: { children: React.ReactNode }) =>
      React.createElement(React.Fragment, null, children),
    useToast: () => ({ showToast: mockShowToast }),
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

jest.mock('../../components/SelectableProductCard', () => {
  const React = require('react');
  const { Text, TouchableOpacity } = require('react-native');
  return {
    SelectableProductCard: ({ product, onPress }: any) =>
      React.createElement(
        TouchableOpacity,
        { onPress: () => onPress(product) },
        React.createElement(Text, null, product.name),
      ),
  };
});

jest.mock('../../components/OfflineBanner', () => {
  const React = require('react');
  const { View, Text } = require('react-native');
  return {
    OfflineBanner: () =>
      React.createElement(
        View,
        { testID: 'offline-banner' },
        React.createElement(Text, null, 'Offline Banner'),
      ),
  };
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
import { fireEvent, render, screen, waitFor } from '@testing-library/react-native';
import { getProducts, getProduct } from '../../services/api';
import { ProductListScreen } from '../../screens/ProductListScreen';
import { ProductDetailsScreen } from '../../screens/ProductDetailsScreen';
import type { ApiProduct, Page } from '../../services/api';

const mockGetProducts = getProducts as jest.MockedFunction<typeof getProducts>;
const mockGetProduct = getProduct as jest.MockedFunction<typeof getProduct>;

function makePage(content: ApiProduct[]): Page<ApiProduct> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 20,
    last: true,
  };
}

beforeEach(() => {
  jest.clearAllMocks();
  mockGetProducts.mockReset();
  mockGetProduct.mockReset();
  mockNetworkState.isConnected = true;
  mockNetworkState.isInternetReachable = true;
  mockCheckConnection.mockReset();
  mockCheckConnection.mockResolvedValue(true);
  Object.keys(mockRouteParams).forEach(k => delete mockRouteParams[k]);
});

describe('ProductListScreen offline + error state', () => {
  it('renders OfflineBanner when network is offline', async () => {
    mockNetworkState.isConnected = false;
    mockGetProducts.mockResolvedValueOnce(makePage([]));

    render(<ProductListScreen />);

    await waitFor(() => {
      expect(screen.getByTestId('offline-banner')).toBeTruthy();
      expect(screen.getByText('Offline Banner')).toBeTruthy();
    });
  });

  it('renders error UI and retry affordance when getProducts rejects', async () => {
    mockGetProducts.mockRejectedValueOnce(new Error('Server down'));

    render(<ProductListScreen />);

    await waitFor(() => {
      expect(screen.getByText('Server down')).toBeTruthy();
      expect(screen.getByText('Retry')).toBeTruthy();
    });
  });

  it('pressing retry calls getProducts again', async () => {
    mockGetProducts
      .mockRejectedValueOnce(new Error('Temporary failure'))
      .mockResolvedValueOnce(makePage([]));

    render(<ProductListScreen />);

    await waitFor(() => expect(screen.getByText('Retry')).toBeTruthy());
    fireEvent.press(screen.getByText('Retry'));

    await waitFor(() => {
      expect(mockGetProducts).toHaveBeenCalledTimes(2);
    });
  });
});

describe('ProductDetailsScreen error state', () => {
  it('renders fallback error UI when getProduct rejects', async () => {
    mockRouteParams.productId = '42';
    mockGetProduct.mockRejectedValueOnce(new Error('Request failed'));

    render(<ProductDetailsScreen />);

    await waitFor(() => {
      expect(screen.getByText('Product not found')).toBeTruthy();
      expect(screen.getByText('Go Back')).toBeTruthy();
    });
  });
});
