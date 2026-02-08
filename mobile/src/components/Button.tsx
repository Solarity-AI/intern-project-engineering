// React Native Button Component
// Cross-platform with multiple variants + press feedback animation

import React, { useRef, useCallback } from 'react';
import {
  TouchableOpacity,
  Text,
  Animated,
  ViewStyle,
  TextStyle,
  ActivityIndicator,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Spacing, FontSize, BorderRadius, Shadow, Gradients, Glow } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

type ButtonVariant = 'default' | 'secondary' | 'outline' | 'ghost' | 'premium';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps {
  children: React.ReactNode;
  onPress: () => void;
  variant?: ButtonVariant;
  size?: ButtonSize;
  disabled?: boolean;
  loading?: boolean;
  fullWidth?: boolean;
  style?: ViewStyle;
  textStyle?: TextStyle;
  icon?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  onPress,
  variant = 'default',
  size = 'md',
  disabled = false,
  loading = false,
  fullWidth = false,
  style,
  textStyle,
  icon,
}) => {
  const { colors } = useTheme();
  const scaleAnim = useRef(new Animated.Value(1)).current;

  const handlePressIn = useCallback(() => {
    Animated.spring(scaleAnim, {
      toValue: 0.96,
      useNativeDriver: true,
      speed: 50,
      bounciness: 4,
    }).start();
  }, [scaleAnim]);

  const handlePressOut = useCallback(() => {
    Animated.spring(scaleAnim, {
      toValue: 1,
      useNativeDriver: true,
      speed: 50,
      bounciness: 4,
    }).start();
  }, [scaleAnim]);

  const sizeStyles: Record<ButtonSize, { height: number; paddingHorizontal: number; fontSize: number }> = {
    sm: { height: 36, paddingHorizontal: Spacing.md, fontSize: FontSize.sm },
    md: { height: 44, paddingHorizontal: Spacing.lg, fontSize: FontSize.base },
    lg: { height: 52, paddingHorizontal: Spacing['2xl'], fontSize: FontSize.lg },
  };

  const getVariantStyles = (): { container: ViewStyle; text: TextStyle } => {
    switch (variant) {
      case 'secondary':
        return {
          container: {
            backgroundColor: colors.secondary,
          },
          text: { color: colors.secondaryForeground },
        };
      case 'outline':
        return {
          container: {
            backgroundColor: 'transparent',
            borderWidth: 1,
            borderColor: colors.border,
          },
          text: { color: colors.foreground },
        };
      case 'ghost':
        return {
          container: {
            backgroundColor: 'transparent',
          },
          text: { color: colors.foreground },
        };
      case 'premium':
        return {
          container: {},
          text: { color: colors.primaryForeground },
        };
      default:
        return {
          container: {
            backgroundColor: colors.primary,
            ...Shadow.medium,
          },
          text: { color: colors.primaryForeground },
        };
    }
  };

  const variantStyles = getVariantStyles();
  const currentSize = sizeStyles[size];

  const containerStyle: ViewStyle = {
    height: currentSize.height,
    paddingHorizontal: currentSize.paddingHorizontal,
    borderRadius: BorderRadius.xl,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.sm,
    opacity: disabled ? 0.5 : 1,
    ...(fullWidth && { width: '100%' }),
    ...variantStyles.container,
    ...style,
  };

  const textStyleCombined: TextStyle = {
    fontSize: currentSize.fontSize,
    fontWeight: '600',
    ...variantStyles.text,
    ...textStyle,
  };

  const content = (
    <>
      {loading ? (
        <ActivityIndicator color={variantStyles.text.color} size="small" />
      ) : (
        <>
          {icon}
          <Text style={textStyleCombined}>{children}</Text>
        </>
      )}
    </>
  );

  if (variant === 'premium') {
    return (
      <Animated.View style={{ transform: [{ scale: scaleAnim }] }}>
        <TouchableOpacity
          onPress={onPress}
          onPressIn={handlePressIn}
          onPressOut={handlePressOut}
          disabled={disabled || loading}
          activeOpacity={0.8}
          accessibilityRole="button"
        >
          <LinearGradient
            colors={Gradients.brand}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
            style={[containerStyle, Glow.primary]}
          >
            {content}
          </LinearGradient>
        </TouchableOpacity>
      </Animated.View>
    );
  }

  return (
    <Animated.View style={{ transform: [{ scale: scaleAnim }] }}>
      <TouchableOpacity
        onPress={onPress}
        onPressIn={handlePressIn}
        onPressOut={handlePressOut}
        disabled={disabled || loading}
        activeOpacity={0.8}
        accessibilityRole="button"
        style={containerStyle}
      >
        {content}
      </TouchableOpacity>
    </Animated.View>
  );
};
