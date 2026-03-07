/**
 * Wishlist.ui.test.tsx — C35
 *
 * UI-level tests for WishlistScreen: list rendering, single-item removal flow,
 * and empty state rendering.
 *
 * All API calls are mocked; no real HTTP requests are made.
 */

jest.mock('../../services/api', () => ({
  getWishlistProducts: jest.fn(),
  toggleWishlistApi: jest.fn().mockResolvedValue(undefined),
  clearWishlistCache: jest.fn(),
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

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: jest.fn() }),
  useFocusEffect: jest.fn(),
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

jest.mock('../../context/WishlistContext', () => {
  const { toggleWishlistApi } = require('../../services/api');

  return {
    useWishlist: () => ({
      removeFromWishlist: jest.fn((id: string) => toggleWishlistApi(Number(id))),
      removeMultipleFromWishlist: jest.fn(),
      clearWishlist: jest.fn(),
      wishlistCount: 0,
    }),
  };
});

jest.mock('react-native-safe-area-context', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    useSafeAreaInsets: () => ({ top: 0, right: 0, bottom: 0, left: 0 }),
    SafeAreaProvider: ({ children }: { children: React.ReactNode }) => children,
    SafeAreaView: ({ children, ...props }: any) => React.createElement(View, props, children),
    SafeAreaConsumer: ({ children }: any) => children({ top: 0, right: 0, bottom: 0, left: 0 }),
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

jest.mock('../../components/SelectableWishlistCard', () => {
  const React = require('react');
  const { View, Text, TouchableOpacity } = require('react-native');

  return {
    SelectableWishlistCard: ({ item, onRemove }: any) =>
      React.createElement(
        View,
        { testID: 'selectable-wishlist-card' },
        React.createElement(Text, null, item.name),
        React.createElement(
          TouchableOpacity,
          { onPress: () => onRemove(String(item.id)) },
          React.createElement(Text, null, `Remove ${item.id}`),
        ),
      ),
  };
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
import { WishlistScreen } from '../../screens/WishlistScreen';
import { getWishlistProducts, toggleWishlistApi } from '../../services/api';
import type { ApiProduct, Page } from '../../services/api';

const mockGetWishlistProducts =
  getWishlistProducts as jest.MockedFunction<typeof getWishlistProducts>;
const mockToggleWishlistApi = toggleWishlistApi as jest.MockedFunction<typeof toggleWishlistApi>;

function makeProduct(id: number): ApiProduct {
  return {
    id,
    name: `Wishlist Item ${id}`,
    description: 'A wishlist test product',
    categories: ['Electronics'],
    price: id * 10,
    averageRating: 4.2,
    reviewCount: 10,
  };
}

function makePage(content: ApiProduct[]): Page<ApiProduct> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 10,
    last: true,
  };
}

beforeEach(() => {
  jest.clearAllMocks();
});

describe('WishlistScreen UI', () => {
  it('renders SelectableWishlistCard count matching mock wishlist items', async () => {
    mockGetWishlistProducts.mockResolvedValueOnce(
      makePage([makeProduct(1), makeProduct(2), makeProduct(3)]),
    );

    render(<WishlistScreen />);

    await waitFor(() => {
      expect(screen.getAllByTestId('selectable-wishlist-card')).toHaveLength(3);
    });
  });

  it('removing an item calls toggleWishlistApi with the correct productId', async () => {
    mockGetWishlistProducts.mockResolvedValueOnce(
      makePage([makeProduct(1), makeProduct(2)]),
    );

    render(<WishlistScreen />);

    await waitFor(() => expect(screen.getByText('Wishlist Item 2')).toBeTruthy());
    fireEvent.press(screen.getByText('Remove 2'));

    await waitFor(() => {
      expect(mockToggleWishlistApi).toHaveBeenCalledWith(2);
    });
  });

  it('after removal, the removed item is no longer rendered', async () => {
    mockGetWishlistProducts.mockResolvedValueOnce(
      makePage([makeProduct(1), makeProduct(2)]),
    );

    render(<WishlistScreen />);

    await waitFor(() => expect(screen.getByText('Wishlist Item 2')).toBeTruthy());
    fireEvent.press(screen.getByText('Remove 2'));

    await waitFor(() => {
      expect(screen.queryByText('Wishlist Item 2')).toBeNull();
      expect(screen.getByText('Wishlist Item 1')).toBeTruthy();
    });
  });

  it('renders empty state UI when wishlist content is empty', async () => {
    mockGetWishlistProducts.mockResolvedValueOnce(makePage([]));

    render(<WishlistScreen />);

    await waitFor(() => {
      expect(screen.getByText('Your wishlist is empty')).toBeTruthy();
      expect(
        screen.getByText('Save products you love and find them here later.'),
      ).toBeTruthy();
    });
  });
});
