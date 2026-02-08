import React, { useRef, useEffect, useMemo, useCallback } from 'react';
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  StyleSheet,
  Animated,
  Easing,
  Platform,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';

import { StarRating } from './StarRating';
import { RootStackParamList } from '../types';
import { Spacing, BorderRadius, Shadow, FontWeight, Glass, Glow, Gradients, FontSize } from '../constants/theme';
import { ApiProduct } from '../services/api';
import { useTheme } from '../context/ThemeContext';
import { useWishlist } from '../context/WishlistContext';

interface SelectableProductCardProps {
  product: ApiProduct;
  numColumns?: number;
  isSelectionMode: boolean;
  isSelected: boolean;
  onPress: (product: ApiProduct) => void;
  onLongPress: (product: ApiProduct) => void;
}

function imageForCategory(categories?: string[]) {
  const c = (categories && categories.length > 0 ? categories[0] : '').toLowerCase();
  if (c.includes('audio')) return 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80';
  if (c.includes('smart') || c.includes('phone') || c.includes('mobile')) return 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80';
  if (c.includes('camera') || c.includes('photo')) return 'https://images.unsplash.com/photo-1519183071298-a2962be96cdb?w=800&q=80';
  if (c.includes('watch') || c.includes('wear')) return 'https://images.unsplash.com/photo-1523275335684-37898b6baf30e?w=800&q=80';
  if (c.includes('laptop') || c.includes('computer')) return 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80';
  return 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&q=80';
}

export const SelectableProductCard: React.FC<SelectableProductCardProps> = ({
  product,
  numColumns = 2,
  isSelectionMode,
  isSelected,
  onPress,
  onLongPress,
}) => {
  const { colors, colorScheme } = useTheme();

  const imageUri = useMemo(() => {
    const direct = product.imageUrl;
    if (typeof direct === 'string' && direct.trim().length > 0) return direct.trim();
    return imageForCategory(product.categories);
  }, [product]);

  const reviewCount = product.reviewCount ?? 0;
  const avgRating = product.averageRating ?? 0;

  const { isInWishlist, toggleWishlist } = useWishlist();
  const productId = String(product.id ?? '');
  const inWishlist = isInWishlist(productId);

  const heartScale = useRef(new Animated.Value(1)).current;

  const handleWishlistToggle = (e: any) => {
    e.stopPropagation();

    // Bounce animation
    Animated.sequence([
      Animated.spring(heartScale, {
        toValue: 1.35,
        useNativeDriver: true,
        speed: 50,
        bounciness: 12,
      }),
      Animated.spring(heartScale, {
        toValue: 1,
        useNativeDriver: true,
        speed: 50,
        bounciness: 8,
      }),
    ]).start();

    toggleWishlist({
      id: productId,
      name: product.name ?? 'Product',
      price: product.price,
      imageUrl: imageUri,
      categories: product.categories,
      averageRating: avgRating,
    } as any);
  };

  const imageOpacity = useRef(new Animated.Value(0)).current;
  const onImageLoad = useCallback(() => {
    Animated.timing(imageOpacity, {
      toValue: 1,
      duration: 350,
      useNativeDriver: true,
    }).start();
  }, [imageOpacity]);

  const shakeAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (isSelectionMode) {
      const animation = Animated.loop(
        Animated.sequence([
          Animated.timing(shakeAnim, {
            toValue: -2,
            duration: 50,
            easing: Easing.linear,
            useNativeDriver: true,
          }),
          Animated.timing(shakeAnim, {
            toValue: 2,
            duration: 50,
            easing: Easing.linear,
            useNativeDriver: true,
          }),
          Animated.timing(shakeAnim, {
            toValue: 0,
            duration: 50,
            easing: Easing.linear,
            useNativeDriver: true,
          }),
        ])
      );
      animation.start();
      return () => animation.stop();
    } else {
      shakeAnim.setValue(0);
    }
  }, [isSelectionMode, shakeAnim]);

  const rotateInterpolate = shakeAnim.interpolate({
    inputRange: [-2, 2],
    outputRange: ['-2deg', '2deg'],
  });

  let displayCategory = 'Uncategorized';
  if (product.categories && product.categories.length > 0) {
    displayCategory = product.categories[0];
  } else if ((product as any).category) {
    displayCategory = (product as any).category;
  }

  // Aspect ratio based on column count
  const aspectRatio = numColumns === 1 ? 16 / 9 : numColumns >= 3 ? 1 : 3 / 4;
  const showWishlistButton = numColumns < 4;

  return (
    <Animated.View
      style={
        isSelectionMode
          ? { transform: [{ rotate: rotateInterpolate }] }
          : undefined
      }
    >
      <TouchableOpacity
        activeOpacity={0.9}
        style={[
          styles.container,
          { aspectRatio },
          isSelectionMode && styles.cardSelectionMode,
          isSelected && [styles.cardSelected, { borderColor: colors.primary }, Glow.primary],
          // Web hover transition
          Platform.OS === 'web' && ({ transition: 'transform 0.3s ease', cursor: 'pointer' } as any),
        ]}
        onPress={() => onPress(product)}
        onLongPress={() => onLongPress(product)}
        delayLongPress={2250}
        accessibilityLabel={`${product.name ?? 'Product'}, rated ${avgRating.toFixed(1)} stars, $${product.price.toFixed(2)}`}
        accessibilityRole="button"
        accessibilityHint="Double tap to view product details"
      >
        {/* Full-bleed image with fade-in */}
        <Animated.Image
          source={{ uri: imageUri }}
          style={[styles.image, { opacity: imageOpacity }]}
          resizeMode="cover"
          onLoad={onImageLoad}
        />

        {/* Bottom gradient overlay — 65% height */}
        <LinearGradient
          colors={['transparent', 'rgba(11,17,32,0.90)'] as [string, string]}
          style={styles.bottomGradient}
        />

        {/* Wishlist button — top right, glass circle */}
        {!isSelectionMode && showWishlistButton && (
          <TouchableOpacity
            style={[
              styles.wishlistButton,
              colorScheme === 'dark' ? Glass.strong : { backgroundColor: 'rgba(255,255,255,0.9)' },
              numColumns >= 3 && styles.wishlistButtonCompact,
            ]}
            onPress={handleWishlistToggle}
            activeOpacity={0.8}
            accessibilityLabel={inWishlist ? 'Remove from wishlist' : 'Add to wishlist'}
            accessibilityRole="button"
          >
            <Animated.View style={{ transform: [{ scale: heartScale }] }}>
              <Ionicons
                name={inWishlist ? 'heart' : 'heart-outline'}
                size={numColumns >= 3 ? 14 : 18}
                color={inWishlist ? '#F87171' : '#fff'}
              />
            </Animated.View>
          </TouchableOpacity>
        )}

        {/* Selection indicator */}
        {isSelectionMode && (
          <View
            style={[
              styles.selectionIndicator,
              {
                backgroundColor: isSelected ? colors.primary : 'rgba(255,255,255,0.9)',
                borderColor: isSelected ? colors.primary : colors.border,
              },
            ]}
          >
            {isSelected && <Ionicons name="checkmark" size={14} color="#fff" />}
          </View>
        )}

        {/* Overlaid content at bottom */}
        <View style={[styles.overlayContent, numColumns >= 3 && styles.overlayContentCompact]}>
          {/* Category pill */}
          <View style={[styles.categoryPill, numColumns >= 3 && styles.categoryPillCompact]}>
            <Text style={[styles.categoryText, numColumns >= 3 && styles.categoryTextCompact]} numberOfLines={1}>
              {displayCategory}
            </Text>
          </View>

          {/* Product name */}
          <Text
            numberOfLines={numColumns >= 3 ? 1 : 2}
            style={[styles.name, numColumns >= 3 && styles.nameCompact]}
          >
            {product.name ?? 'Product'}
          </Text>

          {/* Rating row */}
          <View style={styles.ratingRow}>
            <StarRating rating={avgRating} size="sm" compact={numColumns >= 3} />
            <Text style={[styles.reviewCount, numColumns >= 3 && styles.reviewCountCompact]}>
              ({reviewCount})
            </Text>
          </View>

          {/* Price + arrow row */}
          <View style={styles.priceRow}>
            <Text style={[styles.price, numColumns >= 3 && styles.priceCompact]}>
              {`$${product.price.toFixed(2)}`}
            </Text>
            {numColumns < 3 && (
              <Ionicons name="arrow-forward" size={16} color="rgba(255,255,255,0.5)" />
            )}
          </View>
        </View>
      </TouchableOpacity>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: '100%',
    borderRadius: BorderRadius['2xl'],
    overflow: 'hidden',
    ...Shadow.medium,
    position: 'relative',
    backgroundColor: '#0B1120',
  },
  cardSelectionMode: {
    transform: [{ scale: 1.05 }],
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  cardSelected: {
    borderWidth: 2,
  },

  image: {
    ...StyleSheet.absoluteFillObject,
    width: '100%',
    height: '100%',
  },

  bottomGradient: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: '65%',
  },

  wishlistButton: {
    position: 'absolute',
    top: Spacing.md,
    right: Spacing.md,
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  wishlistButtonCompact: {
    width: 26,
    height: 26,
    borderRadius: 13,
    top: Spacing.sm,
    right: Spacing.sm,
  },

  selectionIndicator: {
    position: 'absolute',
    top: Spacing.sm,
    right: Spacing.sm,
    width: 24,
    height: 24,
    borderRadius: 12,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadow.soft,
    zIndex: 2,
  },

  overlayContent: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    padding: Spacing.md,
    gap: 4,
  },
  overlayContentCompact: {
    padding: Spacing.sm,
    gap: 2,
  },

  categoryPill: {
    alignSelf: 'flex-start',
    backgroundColor: 'rgba(255,255,255,0.15)',
    paddingHorizontal: Spacing.sm,
    paddingVertical: 2,
    borderRadius: BorderRadius.full,
    marginBottom: 2,
  },
  categoryPillCompact: {
    paddingHorizontal: Spacing.xs,
    paddingVertical: 1,
  },
  categoryText: {
    color: 'rgba(255,255,255,0.85)',
    fontSize: 11,
    fontWeight: FontWeight.medium,
  },
  categoryTextCompact: {
    fontSize: 9,
  },

  name: {
    color: '#fff',
    fontSize: 14,
    fontWeight: FontWeight.bold,
    lineHeight: 18,
  },
  nameCompact: {
    fontSize: 11,
    lineHeight: 14,
  },

  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
  },
  reviewCount: {
    color: 'rgba(255,255,255,0.6)',
    fontSize: 11,
  },
  reviewCountCompact: {
    fontSize: 9,
  },

  priceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  price: {
    color: '#10B981',
    fontSize: 15,
    fontWeight: FontWeight.bold,
  },
  priceCompact: {
    fontSize: 12,
  },
});
