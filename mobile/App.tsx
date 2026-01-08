// React Native App Entry Point with SafeAreaProvider, Notifications, Toast, Wishlist, Theme
import React from 'react';
import { NavigationContainer, DefaultTheme, DarkTheme } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { ProductListScreen } from './screens/ProductListScreen';
import { ProductDetailsScreen } from './screens/ProductDetailsScreen';
import { NotificationsScreen } from './screens/NotificationsScreen';
import { NotificationDetailScreen } from './screens/NotificationDetailScreen';
import { WishlistScreen } from './screens/WishlistScreen';
import { NotificationProvider } from './context/NotificationContext';
import { WishlistProvider } from './context/WishlistContext';
import { ToastProvider } from './context/ToastContext';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import { RootStackParamList } from './types';

const Stack = createNativeStackNavigator<RootStackParamList>();

// Navigation wrapper that consumes theme
function AppNavigator() {
  const { colors, colorScheme } = useTheme();

  // Create navigation theme based on current colorScheme
  const navigationTheme = {
    ...(colorScheme === 'dark' ? DarkTheme : DefaultTheme),
    colors: {
      ...(colorScheme === 'dark' ? DarkTheme.colors : DefaultTheme.colors),
      background: colors.background,
      card: colors.card,
      text: colors.foreground,
      border: colors.border,
      notification: colors.primary,
      primary: colors.primary,
    },
  };

  return (
    <NavigationContainer theme={navigationTheme}>
      <Stack.Navigator
        initialRouteName="ProductList"
        screenOptions={{
          headerShown: false,
          animation: 'slide_from_right',
          // FIX: Ensure consistent background during transitions
          contentStyle: {
            backgroundColor: colors.background,
          },
        }}
      >
        <Stack.Screen name="ProductList" component={ProductListScreen} />
        <Stack.Screen name="ProductDetails" component={ProductDetailsScreen} />
        <Stack.Screen 
          name="Notifications" 
          component={NotificationsScreen}
          options={{ animation: 'slide_from_bottom' }}
        />
        <Stack.Screen 
          name="NotificationDetail" 
          component={NotificationDetailScreen}
          options={{ animation: 'slide_from_right' }}
        />
        <Stack.Screen 
          name="Wishlist" 
          component={WishlistScreen}
          options={{ animation: 'slide_from_right' }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <ThemeProvider>
        <NotificationProvider>
          <WishlistProvider>
            <ToastProvider>
              <AppNavigator />
            </ToastProvider>
          </WishlistProvider>
        </NotificationProvider>
      </ThemeProvider>
    </SafeAreaProvider>
  );
}
