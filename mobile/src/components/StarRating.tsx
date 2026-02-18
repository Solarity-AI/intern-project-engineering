// React Native StarRating Component
// Compatible with iOS and Android — with tap animation

import React, { useRef, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ViewStyle,
  Animated,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Spacing, FontSize } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

interface StarRatingProps {
  rating: number;
  maxRating?: number;
  size?: 'sm' | 'md' | 'lg';
  interactive?: boolean;
  onRatingChange?: (rating: number) => void;
  showValue?: boolean;
  style?: ViewStyle;
  compact?: boolean;
}

const sizeMap = {
  sm: 16,
  md: 20,
  lg: 24,
};

const AnimatedStar: React.FC<{
  index: number;
  iconName: 'star' | 'star-half' | 'star-outline';
  iconSize: number;
  color: string;
  interactive: boolean;
  onPress: (index: number) => void;
}> = ({ index, iconName, iconSize, color, interactive, onPress }) => {
  const scaleAnim = useRef(new Animated.Value(1)).current;

  const handlePress = useCallback(() => {
    if (!interactive) return;

    Animated.sequence([
      Animated.spring(scaleAnim, {
        toValue: 1.4,
        useNativeDriver: false,
        speed: 50,
        bounciness: 15,
      }),
      Animated.spring(scaleAnim, {
        toValue: 1,
        useNativeDriver: false,
        speed: 50,
        bounciness: 8,
      }),
    ]).start();

    onPress(index);
  }, [interactive, index, onPress, scaleAnim]);

  const StarComponent = interactive ? TouchableOpacity : View;

  return (
    <StarComponent
      onPress={handlePress}
      style={styles.starContainer}
      activeOpacity={0.7}
      accessibilityLabel={`${index + 1} star${index > 0 ? 's' : ''}`}
      accessibilityRole={interactive ? 'button' : undefined}
    >
      <Animated.View style={{ transform: [{ scale: scaleAnim }] }}>
        <Ionicons name={iconName} size={iconSize} color={color} />
      </Animated.View>
    </StarComponent>
  );
};

export const StarRating: React.FC<StarRatingProps> = ({
  rating,
  maxRating = 5,
  size = 'md',
  interactive = false,
  onRatingChange,
  showValue = false,
  style,
  compact = false,
}) => {
  const { colors } = useTheme();
  const iconSize = compact ? 12 : sizeMap[size];

  const handlePress = useCallback((index: number) => {
    if (interactive && onRatingChange) {
      onRatingChange(index + 1);
    }
  }, [interactive, onRatingChange]);

  const renderStar = (index: number) => {
    const floorRating = Math.floor(rating);
    const isFull = index < floorRating;
    const isHalf = index === floorRating && rating % 1 >= 0.2 && rating % 1 < 0.8;
    const isActuallyFull = index === floorRating && rating % 1 >= 0.8;

    let iconName: 'star' | 'star-half' | 'star-outline' = 'star-outline';
    if (isFull || isActuallyFull) {
      iconName = 'star';
    } else if (isHalf) {
      iconName = 'star-half';
    }

    const isFilled = iconName !== 'star-outline';

    return (
      <AnimatedStar
        key={index}
        index={index}
        iconName={iconName}
        iconSize={iconSize}
        color={isFilled ? colors.starFilled : colors.starEmpty}
        interactive={interactive}
        onPress={handlePress}
      />
    );
  };

  return (
    <View style={[styles.container, style]}>
      <View style={styles.starsRow}>
        {Array.from({ length: maxRating }).map((_, index) => renderStar(index))}
      </View>
      {showValue && (
        <Text style={[styles.valueText, { color: colors.foreground }]}>
          {rating.toFixed(1)}
        </Text>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  starsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  starContainer: {
    padding: 2,
  },
  valueText: {
    marginLeft: Spacing.sm,
    fontSize: FontSize.sm,
    fontWeight: '600',
  },
});
