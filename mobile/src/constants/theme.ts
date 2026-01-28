// Theme constants for React Native
// Professional & Modern Color Palette - WCAG AA Compliant

export const Colors = {
  light: {
    background: '#FAFAFA',
    foreground: '#1A1A1A',
    card: '#FFFFFF',
    cardForeground: '#1A1A1A',
    primary: '#0066FF',
    primaryForeground: '#FFFFFF',
    secondary: '#F5F5F5',
    secondaryForeground: '#525252',
    muted: '#F5F5F5',
    mutedForeground: '#737373',
    accent: '#EEF2FF',
    accentForeground: '#4F46E5',
    border: '#E5E5E5',
    starFilled: '#F59E0B',
    starEmpty: '#D4D4D4',
    success: '#10B981',
    destructive: '#EF4444',
  },
  dark: {
    background: '#0A0A0A',
    foreground: '#FAFAFA',
    card: '#171717',
    cardForeground: '#FAFAFA',
    primary: '#3B82F6',
    primaryForeground: '#FFFFFF',
    secondary: '#262626',
    secondaryForeground: '#D4D4D4',
    muted: '#262626',
    mutedForeground: '#A3A3A3',
    accent: '#1E1B4B',
    accentForeground: '#C7D2FE',
    border: '#262626',
    starFilled: '#FBBF24',
    starEmpty: '#404040',
    success: '#10B981',
    destructive: '#F87171',
  },
};

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  '2xl': 24,
  '3xl': 32,
  '4xl': 40,
  '5xl': 48,
};

export const FontSize = {
  xs: 12,
  sm: 14,
  base: 16,
  lg: 18,
  xl: 20,
  '2xl': 24,
  '3xl': 30,
  '4xl': 36,
};

export const FontWeight = {
  normal: '400' as const,
  medium: '500' as const,
  semibold: '600' as const,
  bold: '700' as const,
};

export const BorderRadius = {
  sm: 6,
  md: 8,
  lg: 12,
  xl: 16,
  '2xl': 20,
  full: 9999,
};

export const Shadow = {
  soft: {
    shadowColor: '#1C1917',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 10,

  },
  hover: {
    shadowColor: '#1C1917',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.15,
    shadowRadius: 16,
    elevation: 6,
  },
};
