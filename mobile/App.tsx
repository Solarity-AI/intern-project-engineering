// React Native App Entry Point with SafeAreaProvider, Notifications, Toast, Wishlist, Theme, Network, and Linking
import React from 'react';
import { View, ActivityIndicator, StyleSheet, Platform } from 'react-native';
import { NavigationContainer, DefaultTheme, DarkTheme, LinkingOptions } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import * as Linking from 'expo-linking';
import { ClerkProvider } from '@clerk/expo';
import { tokenCache } from '@clerk/expo/token-cache';
import { useFonts } from 'expo-font';
import Ionicons from '@expo/vector-icons/Ionicons';

import { ProductListScreen } from './src/screens/ProductListScreen';
import { ProductDetailsScreen } from './src/screens/ProductDetailsScreen';
import { NotificationsScreen } from './src/screens/NotificationsScreen';
import { NotificationDetailScreen } from './src/screens/NotificationDetailScreen';
import { WishlistScreen } from './src/screens/WishlistScreen';
import { AIAssistantScreen } from './src/screens/AIAssistantScreen';
import { NotificationProvider } from './src/context/NotificationContext';
import { WishlistProvider } from './src/context/WishlistContext';
import { SearchProvider } from './src/context/SearchContext';
import { ToastProvider } from './src/context/ToastContext';
import { ThemeProvider, useTheme } from './src/context/ThemeContext';
import { NetworkProvider } from './src/context/NetworkContext';
import { RootStackParamList } from './src/types';

const Stack = createNativeStackNavigator<RootStackParamList>();

// ✨ Define linking configuration for Web support
const linking: LinkingOptions<RootStackParamList> = {
  prefixes: [Linking.createURL('/')],
  config: {
    screens: {
      ProductList: '',
      ProductDetails: 'product/:productId',
      Notifications: 'notifications',
      NotificationDetail: 'notifications/:notificationId',
      Wishlist: 'wishlist',
      AIAssistant: 'product/:productId/chat',
    },
  },
};

// ✨ Loading screen component to prevent white flash
const LoadingScreen: React.FC<{ backgroundColor: string }> = ({ backgroundColor }) => (
  <View style={[styles.loadingContainer, { backgroundColor }]}>
    <ActivityIndicator size="large" color="#10B981" />
  </View>
);

// Navigation wrapper that consumes theme
function AppNavigator() {
  const { colors, colorScheme, isThemeLoaded } = useTheme();

  // ✨ Show loading screen until theme is loaded - prevents white flash
  if (!isThemeLoaded) {
    const bgColor = colorScheme === 'dark' ? '#0B1120' : '#F8FAFC';
    return <LoadingScreen backgroundColor={bgColor} />;
  }

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
    <NavigationContainer 
      theme={navigationTheme} 
      linking={linking}
    >
      <Stack.Navigator
        initialRouteName="ProductList"
        screenOptions={{
          headerShown: false,
          animation: 'slide_from_right',
          contentStyle: {
            backgroundColor: colors.background,
          },
          // ✨ Prevent white flash during screen transitions
          animationTypeForReplace: 'push',
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
        <Stack.Screen 
          name="AIAssistant" 
          component={AIAssistantScreen}
          options={{ animation: 'slide_from_bottom' }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default function App() {
  const publishableKey = process.env.EXPO_PUBLIC_CLERK_PUBLISHABLE_KEY;

  if (!publishableKey) {
    throw new Error(
      'Missing EXPO_PUBLIC_CLERK_PUBLISHABLE_KEY. ' +
      'Set it in your .env.local file before starting the app.'
    );
  }

  const [fontsLoaded] = useFonts({
    Ionicons: require('./assets/fonts/Ionicons.ttf'),
  });

  if (!fontsLoaded) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#10B981" />
      </View>
    );
  }

  return (
    <ClerkProvider publishableKey={publishableKey} tokenCache={Platform.OS !== 'web' ? tokenCache : undefined}>
      <SafeAreaProvider>
        <ThemeProvider>
          {/* ✨ NetworkProvider added - must be inside ThemeProvider for colors */}
          <NetworkProvider>
            <NotificationProvider>
              <WishlistProvider>
                <SearchProvider>
                  <ToastProvider>
                    <AppNavigator />
                  </ToastProvider>
                </SearchProvider>
              </WishlistProvider>
            </NotificationProvider>
          </NetworkProvider>
        </ThemeProvider>
      </SafeAreaProvider>
    </ClerkProvider>
  );
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
