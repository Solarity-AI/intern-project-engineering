// ThemeContext - Manages dark/light mode state
import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useColorScheme } from 'react-native';
import { Colors } from '../constants/theme';

const THEME_STORAGE_KEY = 'app_theme_mode';

type ColorScheme = 'light' | 'dark';

interface ThemeContextType {
  colorScheme: ColorScheme;
  colors: typeof Colors.light;
  toggleTheme: () => void;
  setTheme: (scheme: ColorScheme) => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  // FIX: Use system color scheme as initial value to prevent white flash
  const systemColorScheme = useColorScheme();
  const [colorScheme, setColorScheme] = useState<ColorScheme>(systemColorScheme === 'dark' ? 'dark' : 'light');

  // Load saved theme on mount
  useEffect(() => {
    loadTheme();
  }, []);

  const loadTheme = async () => {
    try {
      const saved = await AsyncStorage.getItem(THEME_STORAGE_KEY);
      if (saved === 'light' || saved === 'dark') {
        setColorScheme(saved);
      }
    } catch (error) {
      console.error('Error loading theme:', error);
    }
  };

  const saveTheme = async (scheme: ColorScheme) => {
    try {
      await AsyncStorage.setItem(THEME_STORAGE_KEY, scheme);
    } catch (error) {
      console.error('Error saving theme:', error);
    }
  };

  const toggleTheme = useCallback(() => {
    setColorScheme((prev) => {
      const newScheme = prev === 'light' ? 'dark' : 'light';
      saveTheme(newScheme);
      return newScheme;
    });
  }, []);

  const setTheme = useCallback((scheme: ColorScheme) => {
    setColorScheme(scheme);
    saveTheme(scheme);
  }, []);

  const colors = Colors[colorScheme];

  return (
    <ThemeContext.Provider
      value={{
        colorScheme,
        colors,
        toggleTheme,
        setTheme,
      }}
    >
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};
