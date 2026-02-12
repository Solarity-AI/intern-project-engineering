/**
 * Search.ui.test.tsx — C36
 *
 * UI-level search tests for ProductListScreen:
 * - submit query -> getProducts(search) + addSearchTerm
 * - clear query -> getProducts without search
 * - result list updates according to mock response
 */

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

jest.mock('expo-navigation-bar', () => ({
  setBackgroundColorAsync: jest.fn().mockResolvedValue(undefined),
  setPositionAsync: jest.fn().mockResolvedValue(undefined),
  setButtonStyleAsync: jest.fn().mockResolvedValue(undefined),
}));

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: jest.fn(), setParams: jest.fn(), canGoBack: jest.fn() }),
  useRoute: () => ({ params: {} }),
  useFocusEffect: jest.fn(),
}));

const mockAddSearchTerm = jest.fn();

jest.mock('../../context/SearchContext', () => ({
  useSearch: () => ({
    addSearchTerm: mockAddSearchTerm,
    searchHistory: [],
    removeSearchTerm: jest.fn(),
    clearHistory: jest.fn(),
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

jest.mock('../../components/ScreenWrapper', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    ScreenWrapper: ({ children }: { children: React.ReactNode }) =>
      React.createElement(View, null, children),
  };
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

jest.mock('../../components/SearchBar', () => {
  const React = require('react');
  const { TextInput, View } = require('react-native');
  return {
    SearchBar: ({
      value,
      onChangeText,
      onSearchSubmit,
    }: {
      value: string;
      onChangeText: (t: string) => void;
      onSearchSubmit: (t: string) => void;
    }) =>
      React.createElement(
        View,
        { testID: 'search-bar' },
        React.createElement(TextInput, {
          testID: 'search-input',
          value,
          onChangeText,
          onSubmitEditing: () => onSearchSubmit(value),
        }),
      ),
  };
});

jest.mock('../../components/LoadMoreCard', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { LoadMoreCard: () => React.createElement(View, { testID: 'load-more-card' }) };
});

jest.mock('../../components/OfflineBanner', () => {
  const React = require('react');
  const { View } = require('react-native');
  return { OfflineBanner: () => React.createElement(View, { testID: 'offline-banner' }) };
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

import React from 'react';
import { render, fireEvent, waitFor, screen } from '@testing-library/react-native';
import { getProducts } from '../../services/api';
import { ProductListScreen } from '../../screens/ProductListScreen';
import type { ApiProduct, Page } from '../../services/api';

const mockGetProducts = getProducts as jest.MockedFunction<typeof getProducts>;

function makeProduct(id: number, name: string): ApiProduct {
  return {
    id,
    name,
    description: 'A test product',
    categories: ['Electronics'],
    price: 9.99,
    averageRating: 4.2,
    reviewCount: 5,
  };
}

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
  mockAddSearchTerm.mockClear();
});

describe('ProductList search behavior', () => {
  it('submitting a query calls getProducts with search and updates results', async () => {
    mockGetProducts
      .mockResolvedValueOnce(makePage([makeProduct(1, 'Initial Product')]))
      .mockResolvedValueOnce(makePage([makeProduct(2, 'Headphones Pro')]));

    render(<ProductListScreen />);

    await waitFor(() => expect(screen.getByText('Initial Product')).toBeTruthy());

    const searchInput = screen.getByTestId('search-input');
    fireEvent.changeText(searchInput, 'headphones');
    fireEvent(searchInput, 'submitEditing');

    await waitFor(() => {
      expect(mockGetProducts).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'headphones' }),
      );
      expect(mockAddSearchTerm).toHaveBeenCalledWith('headphones');
      expect(screen.getByText('Headphones Pro')).toBeTruthy();
    });
  });

  it('clearing search calls getProducts without search param and resets results', async () => {
    mockGetProducts
      .mockResolvedValueOnce(makePage([makeProduct(1, 'Initial Product')]))
      .mockResolvedValueOnce(makePage([makeProduct(2, 'Headphones Pro')]))
      .mockResolvedValueOnce(makePage([makeProduct(3, 'All Products Restored')]));

    render(<ProductListScreen />);

    const searchInput = await screen.findByTestId('search-input');

    await waitFor(() => expect(screen.getByText('Initial Product')).toBeTruthy());

    fireEvent.changeText(searchInput, 'headphones');
    fireEvent(searchInput, 'submitEditing');
    await waitFor(() => expect(screen.getByText('Headphones Pro')).toBeTruthy());

    fireEvent.changeText(searchInput, '');
    fireEvent(searchInput, 'submitEditing');

    await waitFor(() => {
      expect(mockGetProducts).toHaveBeenLastCalledWith(
        expect.objectContaining({ search: undefined }),
      );
      expect(screen.getByText('All Products Restored')).toBeTruthy();
    });
  });
});
