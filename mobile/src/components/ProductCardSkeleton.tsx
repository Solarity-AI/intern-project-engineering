import React from 'react';
import { View, StyleSheet } from 'react-native';
import { SkeletonLoader } from './SkeletonLoader';
import { BorderRadius, Spacing, Shadow } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

interface ProductCardSkeletonProps {
  numColumns?: number;
}

export const ProductCardSkeleton: React.FC<ProductCardSkeletonProps> = ({
  numColumns = 2,
}) => {
  const { colorScheme } = useTheme();
  const aspectRatio = numColumns === 1 ? 16 / 9 : numColumns >= 3 ? 1 : 3 / 4;

  const baseBg = colorScheme === 'dark'
    ? 'rgba(15,23,42,0.75)'
    : 'rgba(241,245,249,1)';

  return (
    <View style={[styles.container, { aspectRatio, backgroundColor: baseBg }]}>
      {/* Full image area shimmer */}
      <SkeletonLoader
        width="100%"
        height="100%"
        borderRadius={0}
        style={StyleSheet.absoluteFillObject}
      />

      {/* Bottom content overlay */}
      <View style={[styles.overlayContent, numColumns >= 3 && styles.overlayContentCompact]}>
        {/* Category pill */}
        <SkeletonLoader
          width={60}
          height={18}
          borderRadius={BorderRadius.full}
        />

        {/* Name line */}
        <SkeletonLoader
          width="75%"
          height={numColumns >= 3 ? 14 : 16}
          borderRadius={BorderRadius.sm}
        />

        {/* Rating row */}
        <View style={styles.ratingRow}>
          <SkeletonLoader width={80} height={12} borderRadius={BorderRadius.sm} />
          <SkeletonLoader width={24} height={12} borderRadius={BorderRadius.sm} />
        </View>

        {/* Price */}
        <SkeletonLoader
          width={70}
          height={numColumns >= 3 ? 14 : 18}
          borderRadius={BorderRadius.sm}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
    borderRadius: BorderRadius['2xl'],
    overflow: 'hidden',
    ...Shadow.medium,
    position: 'relative',
  },
  overlayContent: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    padding: Spacing.md,
    gap: 6,
  },
  overlayContentCompact: {
    padding: Spacing.sm,
    gap: 4,
  },
  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
  },
});
