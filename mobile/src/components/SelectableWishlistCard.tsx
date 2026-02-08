import React, { useRef, useEffect, useCallback, memo } from 'react';
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  Pressable,
  StyleSheet,
  Animated,
  Easing,
  Platform,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';

import { StarRating } from './StarRating';
import { useTheme } from '../context/ThemeContext';
import { WishlistItem } from '../context/WishlistContext';
import { Spacing, BorderRadius, Shadow, FontSize, FontWeight, Glass, Glow } from '../constants/theme';

interface SelectableWishlistCardProps {
  item: WishlistItem;
  isSelectionMode: boolean;
  isSelected: boolean;
  onPress: (item: WishlistItem) => void;
  onLongPress: (item: WishlistItem) => void;
  onRemove: (id: string) => void;
  width?: string;
  numColumns?: 1 | 2 | 3;
}

function SelectableWishlistCardComponent({
  item,
  isSelectionMode,
  isSelected,
  onPress,
  onLongPress,
  onRemove,
  numColumns,
}: SelectableWishlistCardProps) {

  const { colors, colorScheme } = useTheme();
  const [imageError, setImageError] = React.useState(false);
  const [imageKey, setImageKey] = React.useState(0);

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
            toValue: -1.2,
            duration: 85,
            easing: Easing.linear,
            useNativeDriver: true,
          }),
          Animated.timing(shakeAnim, {
            toValue: 1.2,
            duration: 85,
            easing: Easing.linear,
            useNativeDriver: true,
          }),
          Animated.timing(shakeAnim, {
            toValue: 0,
            duration: 85,
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
    inputRange: [-1.2, 1.2],
    outputRange: ['-1.2deg', '1.2deg'],
  });

  const isCompact = numColumns !== undefined && numColumns >= 3;
  const aspectRatio = numColumns === 1 ? 16 / 9 : isCompact ? 1 : 3 / 4;

  return (
    <View style={{ zIndex: isSelectionMode ? (isSelected ? 2 : 1) : 1 }}>
      <Animated.View
        style={
          isSelectionMode
            ? [
                {
                  transform: [{ rotate: rotateInterpolate }],
                  marginHorizontal: 2,
                  elevation: isSelected ? 4 : 2,
                },
              ]
            : []
        }
        collapsable={false}
      >
        <TouchableOpacity
          activeOpacity={0.9}
          style={[
            styles.card,
            { aspectRatio },
            isSelectionMode && styles.cardSelectionMode,
            isSelected && [styles.cardSelected, { borderColor: colors.primary }, Glow.primary],
          ]}
          onPress={() => onPress(item)}
          onLongPress={() => onLongPress(item)}
          delayLongPress={2250}
          accessibilityLabel={`${item.name}, $${item.price?.toFixed(2) ?? '0.00'}`}
          accessibilityRole="button"
          accessibilityHint="Double tap to view product details"
        >
          {/* Full-bleed image with fade-in */}
          {item.imageUrl && !imageError ? (
            <Animated.Image
              key={`${item.id}-${imageKey}`}
              source={{ uri: item.imageUrl }}
              style={[styles.image, { opacity: imageOpacity }]}
              resizeMode="cover"
              onLoad={onImageLoad}
              onError={() => {
                setImageError(true);
                setImageKey(prev => prev + 1);
              }}
            />
          ) : (
            <View style={[styles.imagePlaceholder, { backgroundColor: colors.muted }]}>
              <Ionicons name="image-outline" size={32} color={colors.mutedForeground} />
            </View>
          )}

          {/* Bottom gradient overlay */}
          <LinearGradient
            colors={['transparent', 'rgba(11,17,32,0.90)'] as [string, string]}
            style={styles.bottomGradient}
          />

          {/* Emerald heart icon — top left */}
          {!isSelectionMode && (
            <View style={[styles.heartBadge, isCompact && styles.heartBadgeCompact]}>
              <Ionicons name="heart" size={isCompact ? 12 : 14} color="#10B981" />
            </View>
          )}

          {/* Quick delete button — top right */}
          {/* Using Pressable instead of TouchableOpacity to avoid nested button issue on web */}
          {!isSelectionMode && (
            <Pressable
              style={[
                styles.deleteButton,
                colorScheme === 'dark' ? Glass.strong : { backgroundColor: 'rgba(255,255,255,0.9)' },
                isCompact && styles.deleteButtonCompact,
              ]}
              onPress={() => onRemove(item.id)}
              hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
              accessibilityLabel="Remove from wishlist"
              accessibilityRole="button"
            >
              <Ionicons
                name="close"
                size={isCompact ? 14 : 18}
                color="#fff"
              />
            </Pressable>
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
          <View style={[styles.overlayContent, isCompact && styles.overlayContentCompact]}>
            {item.category && (
              <View style={[styles.categoryPill, isCompact && styles.categoryPillCompact]}>
                <Text style={[styles.categoryText, isCompact && styles.categoryTextCompact]}>
                  {item.category}
                </Text>
              </View>
            )}

            <Text numberOfLines={2} style={[styles.name, isCompact && styles.nameCompact]}>
              {item.name}
            </Text>

            {item.averageRating !== undefined && (
              <View style={styles.ratingRow}>
                <StarRating rating={item.averageRating} size="sm" compact={isCompact} />
                <Text style={[styles.reviewCount, isCompact && styles.reviewCountCompact]}>
                  ({(item as any).reviewCount ?? 0})
                </Text>
              </View>
            )}

            {item.price !== undefined && (
              <Text style={[styles.price, isCompact && styles.priceCompact]}>
                ${item.price.toFixed(2)}
              </Text>
            )}
          </View>
        </TouchableOpacity>
      </Animated.View>
    </View>
  );
}

export const SelectableWishlistCard = memo(SelectableWishlistCardComponent);

const styles = StyleSheet.create({
  card: {
    borderRadius: BorderRadius['2xl'],
    overflow: 'hidden',
    ...Shadow.medium,
    position: 'relative',
    backgroundColor: '#0B1120',
  },
  cardSelectionMode: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.4,
    shadowRadius: 12,
    elevation: 12,
  },
  cardSelected: {
    borderWidth: 2,
  },

  image: {
    ...StyleSheet.absoluteFillObject,
    width: '100%',
    height: '100%',
  },
  imagePlaceholder: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
  },

  bottomGradient: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: '65%',
  },

  heartBadge: {
    position: 'absolute',
    top: Spacing.sm,
    left: Spacing.sm,
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: 'rgba(15,23,42,0.7)',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  heartBadgeCompact: {
    width: 22,
    height: 22,
    borderRadius: 11,
    top: Spacing.xs,
    left: Spacing.xs,
  },

  deleteButton: {
    position: 'absolute',
    top: Spacing.sm,
    right: Spacing.sm,
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10,
  },
  deleteButtonCompact: {
    width: 24,
    height: 24,
    borderRadius: 12,
    top: Spacing.xs,
    right: Spacing.xs,
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
    zIndex: 11,
    ...Shadow.soft,
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
    fontSize: 10,
    fontWeight: FontWeight.medium,
  },
  categoryTextCompact: {
    fontSize: 8,
  },

  name: {
    color: '#fff',
    fontSize: FontSize.xs,
    fontWeight: FontWeight.bold,
    lineHeight: 16,
  },
  nameCompact: {
    fontSize: 10,
    lineHeight: 13,
  },

  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  reviewCount: {
    color: 'rgba(255,255,255,0.6)',
    fontSize: 10,
  },
  reviewCountCompact: {
    fontSize: 9,
  },

  price: {
    color: '#10B981',
    fontSize: FontSize.sm,
    fontWeight: FontWeight.bold,
  },
  priceCompact: {
    fontSize: 11,
  },
});
