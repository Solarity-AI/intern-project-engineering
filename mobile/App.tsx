// React Native App Entry Point with SafeAreaProvider, Notifications, Toast, Wishlist, Theme, Network, and Linking
import React from 'react';
import { View, ActivityIndicator, StyleSheet, Platform } from 'react-native';
import { NavigationContainer, DefaultTheme, DarkTheme, LinkingOptions } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import * as Linking from 'expo-linking';
import { ClerkProvider, useAuth } from '@clerk/expo';
import { tokenCache } from '@clerk/expo/token-cache';
import { useFonts } from 'expo-font';
import Ionicons from '@expo/vector-icons/Ionicons';

import { ProductListScreen } from './src/screens/ProductListScreen';
import { ProductDetailsScreen } from './src/screens/ProductDetailsScreen';
import { NotificationsScreen } from './src/screens/NotificationsScreen';
import { NotificationDetailScreen } from './src/screens/NotificationDetailScreen';
import { WishlistScreen } from './src/screens/WishlistScreen';
import { AIAssistantScreen } from './src/screens/AIAssistantScreen';
import { SignInScreen } from './src/screens/SignInScreen';
import { SignUpScreen } from './src/screens/SignUpScreen';
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
      SignIn: 'sign-in',
      SignUp: 'sign-up',
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
  const { isLoaded: isAuthLoaded, isSignedIn, getToken } = useAuth();

  React.useEffect(() => {
    if (!isAuthLoaded || !isSignedIn) {
      return;
    }

    let isCancelled = false;

    const verifySessionToken = async () => {
      try {
        const token = await getToken();

        if (!isCancelled) {
          if (token) {
            if (__DEV__) {
              console.log('[Auth] Clerk session token is available.');
            }
          } else {
            console.warn('[Auth] Signed-in user has no Clerk session token.');
          }
        }
      } catch (error) {
        if (!isCancelled) {
          console.error('[Auth] Unable to read Clerk session token.', error);
        }
      }
    };

    verifySessionToken();

    return () => {
      isCancelled = true;
    };
  }, [getToken, isAuthLoaded, isSignedIn]);

  // ✨ Show loading screen until theme is loaded - prevents white flash
  if (!isThemeLoaded) {
    const bgColor = colorScheme === 'dark' ? '#0B1120' : '#F8FAFC';
    return <LoadingScreen backgroundColor={bgColor} />;
  }

  if (!isAuthLoaded) {
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
        initialRouteName={isSignedIn ? 'ProductList' : 'SignIn'}
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
        {!isSignedIn ? (
          <>
            <Stack.Screen name="SignIn" component={SignInScreen} />
            <Stack.Screen
              name="SignUp"
              component={SignUpScreen}
              options={{ animation: 'slide_from_right' }}
            />
          </>
        ) : (
          <>
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
          </>
        )}
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
