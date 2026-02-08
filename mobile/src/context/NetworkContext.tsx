// NetworkContext - Manages internet connection status (Expo Compatible)
// Uses fetch-based connectivity check instead of native module
import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode, useRef } from 'react';
import { AppState, AppStateStatus, Platform } from 'react-native';

interface NetworkContextType {
  isConnected: boolean;
  isInternetReachable: boolean | null;
  connectionType: string | null;
  checkConnection: () => Promise<boolean>;
}

const NetworkContext = createContext<NetworkContextType | undefined>(undefined);

// Simple connectivity check using fetch
// On web, use our own backend health endpoint to avoid CORS issues
const checkInternetConnection = async (): Promise<boolean> => {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);

    // Use our backend's actuator health endpoint (CORS-enabled)
    // For local development:
    // http://localhost:8080/actuator/health
    const response = await fetch('https://product-review-app-ybmf.onrender.com/actuator/health', {
      method: 'GET',
      signal: controller.signal,
    });

    clearTimeout(timeoutId);
    return response.ok;
  } catch {
    return false;
  }
};

export const NetworkProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isConnected, setIsConnected] = useState(true);
  const [isInternetReachable, setIsInternetReachable] = useState<boolean | null>(true);
  const [connectionType, setConnectionType] = useState<string | null>('unknown');
  const appState = useRef(AppState.currentState);
  const checkIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const checkConnection = useCallback(async (): Promise<boolean> => {
    const connected = await checkInternetConnection();
    setIsConnected(connected);
    setIsInternetReachable(connected);
    return connected;
  }, []);

  useEffect(() => {
    // Initial check
    checkConnection();

    // Check periodically (every 10 seconds)
    checkIntervalRef.current = setInterval(() => {
      checkConnection();
    }, 10000);

    // Check when app comes to foreground
    const handleAppStateChange = (nextAppState: AppStateStatus) => {
      if (appState.current.match(/inactive|background/) && nextAppState === 'active') {
        checkConnection();
      }
      appState.current = nextAppState;
    };

    const subscription = AppState.addEventListener('change', handleAppStateChange);

    return () => {
      if (checkIntervalRef.current) {
        clearInterval(checkIntervalRef.current);
      }
      subscription?.remove();
    };
  }, [checkConnection]);

  return (
    <NetworkContext.Provider
      value={{
        isConnected,
        isInternetReachable,
        connectionType,
        checkConnection,
      }}
    >
      {children}
    </NetworkContext.Provider>
  );
};

export const useNetwork = (): NetworkContextType => {
  const context = useContext(NetworkContext);
  if (context === undefined) {
    throw new Error('useNetwork must be used within a NetworkProvider');
  }
  return context;
};
