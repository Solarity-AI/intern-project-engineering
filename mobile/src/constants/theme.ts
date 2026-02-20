// Theme constants for React Native
// Solarity Review — Premium Dark SaaS Design
import { Platform } from 'react-native';

export const Colors = {
  light: {
    background: '#F8FAFC',
    foreground: '#0F172A',
    card: '#FFFFFF',
    cardForeground: '#0F172A',
    primary: '#059669',
    primaryForeground: '#FFFFFF',
    secondary: '#F1F5F9',
    secondaryForeground: '#475569',
    muted: '#F1F5F9',
    mutedForeground: '#64748B',
    accent: '#FEF3C7',
    accentForeground: '#D97706',
    border: '#E2E8F0',
    starFilled: '#F59E0B',
    starEmpty: '#CBD5E1',
    success: '#10B981',
    destructive: '#EF4444',
  },
  dark: {
    background: '#0B1120',
    foreground: '#F1F5F9',
    card: 'rgba(15,23,42,0.55)',
    cardForeground: '#F1F5F9',
    primary: '#10B981',
    primaryForeground: '#FFFFFF',
    secondary: 'rgba(30,41,59,0.8)',
    secondaryForeground: '#CBD5E1',
    muted: 'rgba(30,41,59,0.5)',
    mutedForeground: '#94A3B8',
    accent: 'rgba(251,191,36,0.12)',
    accentForeground: '#FBBF24',
    border: 'rgba(148,163,184,0.15)',
    starFilled: '#FBBF24',
    starEmpty: 'rgba(148,163,184,0.3)',
    success: '#10B981',
    destructive: '#F87171',
  },
};

export const Gradients = {
  brand: ['#10B981', '#059669'] as [string, string],
  brandVivid: ['#34D399', '#10B981', '#059669'] as string[],
  ai: ['#8B5CF6', '#6366F1'] as [string, string],
  premium: ['#F59E0B', '#FBBF24'] as [string, string],
  hero: ['rgba(16,185,129,0.18)', 'rgba(99,102,241,0.12)', 'rgba(251,191,36,0.06)', 'rgba(11,17,32,0)'] as string[],
  heroMesh: ['rgba(16,185,129,0.20)', 'rgba(99,102,241,0.14)', 'rgba(11,17,32,0)'] as string[],
  cardShine: ['rgba(255,255,255,0.10)', 'rgba(255,255,255,0.03)', 'transparent'] as string[],
  cardShineStrong: ['rgba(255,255,255,0.15)', 'rgba(255,255,255,0.05)', 'transparent'] as string[],
  imageOverlay: ['transparent', 'rgba(11,17,32,0.7)'] as [string, string],
  darkFade: ['rgba(11,17,32,0)', 'rgba(11,17,32,0.95)'] as [string, string],
  // v3 radical redesign tokens
  meshA: ['rgba(16,185,129,0.25)', 'rgba(16,185,129,0.05)', 'transparent'] as string[],
  meshB: ['rgba(99,102,241,0.20)', 'rgba(99,102,241,0.05)', 'transparent'] as string[],
  meshC: ['rgba(251,191,36,0.15)', 'rgba(251,191,36,0.03)', 'transparent'] as string[],
  surfaceCard: ['rgba(15,23,42,0.80)', 'rgba(15,23,42,0.60)'] as [string, string],
  dividerGlow: ['transparent', 'rgba(16,185,129,0.3)', 'transparent'] as string[],
};

export const Glass = {
  card: {
    backgroundColor: 'rgba(15,23,42,0.55)',
    borderWidth: 1,
    borderColor: 'rgba(148,163,184,0.15)',
    ...Platform.select({
      web: { backdropFilter: 'blur(24px)', WebkitBackdropFilter: 'blur(24px)' } as any,
      default: {},
    }),
  },
  cardLight: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  strong: {
    backgroundColor: 'rgba(15,23,42,0.70)',
    borderWidth: 1,
    borderColor: 'rgba(148,163,184,0.22)',
    ...Platform.select({
      web: { backdropFilter: 'blur(32px)', WebkitBackdropFilter: 'blur(32px)' } as any,
      default: {},
    }),
  },
  subtle: {
    backgroundColor: 'rgba(15,23,42,0.35)',
    borderWidth: 1,
    borderColor: 'rgba(148,163,184,0.10)',
    ...Platform.select({
      web: { backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)' } as any,
      default: {},
    }),
  },
  // v3 radical redesign tokens
  hero: {
    backgroundColor: 'rgba(15,23,42,0.40)',
    borderWidth: 1,
    borderColor: 'rgba(148,163,184,0.08)',
    ...Platform.select({
      web: { backdropFilter: 'blur(40px)', WebkitBackdropFilter: 'blur(40px)' } as any,
      default: {},
    }),
  },
  elevated: {
    backgroundColor: 'rgba(15,23,42,0.75)',
    borderWidth: 1,
    borderColor: 'rgba(148,163,184,0.18)',
    ...Platform.select({
      web: { backdropFilter: 'blur(32px)', WebkitBackdropFilter: 'blur(32px)' } as any,
      default: {},
    }),
  },
  // Light mode variants for dark Glass tokens
  strongLight: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#CBD5E1',
  },
  subtleLight: {
    backgroundColor: 'rgba(241,245,249,0.85)',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  heroLight: {
    backgroundColor: 'rgba(255,255,255,0.75)',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  elevatedLight: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#CBD5E1',
  },
};

export const Glow = {
  primary: {
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.45,
    shadowRadius: 20,
    elevation: 12,
  },
  primarySoft: {
    shadowColor: '#10B981',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 16,
    elevation: 8,
  },
  accent: {
    shadowColor: '#FBBF24',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.4,
    shadowRadius: 20,
    elevation: 12,
  },
  ai: {
    shadowColor: '#8B5CF6',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.4,
    shadowRadius: 20,
    elevation: 12,
  },
};

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  '2xl': 32,
  '3xl': 40,
  '4xl': 48,
  '5xl': 64,
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
  '5xl': 42,
  '6xl': 52,
};

export const FontWeight = {
  normal: '400' as const,
  medium: '500' as const,
  semibold: '600' as const,
  bold: '700' as const,
  extrabold: '800' as const,
};

export const BorderRadius = {
  sm: 6,
  md: 8,
  lg: 12,
  xl: 16,
  '2xl': 24,
  '3xl': 32,
  full: 9999,
};

export const Shadow = {
  soft: {
    shadowColor: '#0B1120',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 4,
  },
  medium: {
    shadowColor: '#0B1120',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.25,
    shadowRadius: 24,
    elevation: 8,
  },
  hover: {
    shadowColor: '#0B1120',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.3,
    shadowRadius: 32,
    elevation: 12,
  },
};
