// WishlistScreen - Display user's favorite products
import React, { useMemo, useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  Image,
  useWindowDimensions,
  Platform,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { StarRating } from '../components/StarRating';
import { useWishlist, WishlistItem } from '../context/WishlistContext';
import { useTheme } from '../context/ThemeContext';
import { RootStackParamList } from '../types';
import {
  Spacing,
  FontSize,
  FontWeight,
  BorderRadius,
  Shadow,
} from '../constants/theme';

export const WishlistScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { colors } = useTheme();
  const { wishlist, removeFromWishlist, clearWishlist } = useWishlist();

  const { width } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';

  // FIX: Grid mode toggle (same as ProductListScreen)
  const [gridMode, setGridMode] = useState<1 | 2 | 4>(2);
  
  const numColumns = gridMode;

  // Load saved grid mode on mount
  useEffect(() => {
    loadGridMode();
  }, []);

  const loadGridMode = async () => {
    try {
      const saved = await AsyncStorage.getItem('wishlist_grid_mode');
      if (saved === '1' || saved === '2' || saved === '4') {
        setGridMode(parseInt(saved) as 1 | 2 | 4);
      }
    } catch (error) {
      console.error('Error loading grid mode:', error);
    }
  };

  const saveGridMode = async (mode: 1 | 2 | 4) => {
    try {
      await AsyncStorage.setItem('wishlist_grid_mode', String(mode));
    } catch (error) {
      console.error('Error saving grid mode:', error);
    }
  };

  // Toggle grid: 1 → 2 → 4 → 1
  const toggleGridMode = () => {
    setGridMode(prev => {
      const next = prev === 1 ? 2 : prev === 2 ? 4 : 1;
      saveGridMode(next);
      return next;
    });
  };

  // Get icon for current grid mode
  const getGridIcon = (): keyof typeof Ionicons.glyphMap => {
    if (gridMode === 1) return 'list';
    if (gridMode === 2) return 'grid';
    return 'apps'; // 4 columns
  };

  const stats = useMemo(() => {
    const totalPrice = wishlist.reduce((sum, item) => sum + (item.price || 0), 0);
    const avgRating = wishlist.length > 0
      ? wishlist.reduce((sum, item) => sum + (item.averageRating || 0), 0) / wishlist.length
      : 0;

    return {
      count: wishlist.length,
      totalPrice,
      avgRating,
    };
  }, [wishlist]);

  const handleProductPress = (item: WishlistItem) => {
    navigation.navigate('ProductDetails', {
      productId: item.id,
      imageUrl: item.imageUrl,
      name: item.name,
    } as any);
  };

  const renderWishlistItem = ({ item }: { item: WishlistItem }) => (
    <TouchableOpacity
      activeOpacity={0.9}
      style={[
        styles.card,
        {
          backgroundColor: colors.card,
          width: numColumns > 1 ? `${100 / numColumns - 2}%` : '100%',
        },
      ]}
      onPress={() => handleProductPress(item)}
    >
      <View style={styles.imageContainer}>
        {item.imageUrl ? (
          <Image source={{ uri: item.imageUrl }} style={styles.image} resizeMode="cover" />
        ) : (
          <View style={[styles.imagePlaceholder, { backgroundColor: colors.muted }]}>
            <Ionicons name="image-outline" size={32} color={colors.mutedForeground} />
          </View>
        )}

        {/* Remove button */}
        <TouchableOpacity
          style={[styles.removeButton, { backgroundColor: colors.destructive }]}
          onPress={() => removeFromWishlist(item.id)}
          activeOpacity={0.8}
        >
          <Ionicons name="close" size={16} color="#fff" />
        </TouchableOpacity>
      </View>

      <View style={styles.content}>
        <Text numberOfLines={2} style={[styles.name, { color: colors.foreground }]}>
          {item.name}
        </Text>

        {item.averageRating !== undefined && (
          <StarRating rating={item.averageRating} size="sm" />
        )}

        {item.price !== undefined && (
          <Text style={[styles.price, { color: colors.primary }]}>
            ${item.price.toFixed(2)}
          </Text>
        )}

        {item.category && (
          <Text style={[styles.category, { color: colors.mutedForeground }]}>
            {item.category}
          </Text>
        )}
      </View>
    </TouchableOpacity>
  );

  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <View style={[styles.emptyIconContainer, { backgroundColor: colors.muted }]}>
        <Ionicons name="heart-outline" size={64} color={colors.mutedForeground} />
      </View>
      <Text style={[styles.emptyTitle, { color: colors.foreground }]}>Your wishlist is empty</Text>
      <Text style={[styles.emptySubtitle, { color: colors.mutedForeground }]}>
        Start adding your favorite products to see them here
      </Text>
      <TouchableOpacity
        style={[styles.emptyButton, { backgroundColor: colors.primary }]}
        onPress={() => navigation.navigate('ProductList')}
        activeOpacity={0.8}
      >
        <Text style={[styles.emptyButtonText, { color: colors.primaryForeground }]}>
          Browse Products
        </Text>
      </TouchableOpacity>
    </View>
  );

  const renderStatsHeader = () => (
    <View>
      {/* Stats */}
      {wishlist.length > 0 && (
        <View style={[styles.statsCard, { backgroundColor: colors.secondary }]}>
          <View style={styles.statRow}>
            <View style={styles.statItem}>
              <LinearGradient colors={[colors.primary, colors.accent]} style={styles.statIcon}>
                <Ionicons name="heart" size={20} color={colors.primaryForeground} />
              </LinearGradient>
              <View>
                <Text style={[styles.statValue, { color: colors.foreground }]}>
                  {stats.count}
                </Text>
                <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>
                  Items
                </Text>
              </View>
            </View>

            <View style={styles.statItem}>
              <LinearGradient colors={[colors.primary, colors.accent]} style={styles.statIcon}>
                <Ionicons name="star" size={20} color={colors.primaryForeground} />
              </LinearGradient>
              <View>
                <Text style={[styles.statValue, { color: colors.foreground }]}>
                  {stats.avgRating.toFixed(1)}
                </Text>
                <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>
                  Avg Rating
                </Text>
              </View>
            </View>

            <View style={styles.statItem}>
              <LinearGradient colors={[colors.primary, colors.accent]} style={styles.statIcon}>
                <Ionicons name="cash" size={20} color={colors.primaryForeground} />
              </LinearGradient>
              <View>
                <Text style={[styles.statValue, { color: colors.foreground }]}>
                  ${stats.totalPrice.toFixed(0)}
                </Text>
                <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>
                  Total
                </Text>
              </View>
            </View>
          </View>
        </View>
      )}
    </View>
  );

  // FIX: Combined header with grid toggle
  const renderListHeader = () => (
    <View>
      {/* Header */}
      <View style={[styles.header, { borderBottomColor: colors.border }]}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
            <Ionicons name="arrow-back" size={22} color={colors.foreground} />
          </TouchableOpacity>
          <View>
            <Text style={[styles.headerTitle, { color: colors.foreground }]}>My Wishlist</Text>
            {wishlist.length > 0 && (
              <Text style={[styles.headerSubtitle, { color: colors.mutedForeground }]}>
                {wishlist.length} {wishlist.length === 1 ? 'item' : 'items'}
              </Text>
            )}
          </View>
        </View>

        {/* FIX: Header actions (grid toggle + clear all) */}
        <View style={styles.headerActions}>
          {/* Grid Toggle Button */}
          {wishlist.length > 0 && (
            <TouchableOpacity
              onPress={toggleGridMode}
              style={[styles.gridToggleButton, { backgroundColor: colors.secondary }]}
              activeOpacity={0.8}
            >
              <Ionicons name={getGridIcon()} size={20} color={colors.foreground} />
            </TouchableOpacity>
          )}

          {/* Clear all button */}
          {wishlist.length > 0 && (
            <TouchableOpacity 
              onPress={clearWishlist} 
              activeOpacity={0.8}
              style={[styles.clearAllButton, { backgroundColor: colors.destructive }]}
            >
              <Ionicons name="trash-outline" size={16} color="#fff" />
              <Text style={styles.clearAllText}>Clear all</Text>
            </TouchableOpacity>
          )}
        </View>
      </View>

      {renderStatsHeader()}
    </View>
  );

  return (
    <ScreenWrapper backgroundColor={colors.background}>
      <FlatList
        data={wishlist}
        key={numColumns}
        numColumns={numColumns}
        keyExtractor={(item) => item.id}
        renderItem={renderWishlistItem}
        ListHeaderComponent={renderListHeader}
        ListEmptyComponent={renderEmpty}
        contentContainerStyle={[
          styles.listContent,
          isWeb && styles.webMaxWidth,
        ]}
        columnWrapperStyle={numColumns > 1 ? styles.columnWrapper : undefined}
        showsVerticalScrollIndicator={false}
      />
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    borderBottomWidth: 1,
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
  },
  backButton: {
    padding: Spacing.xs,
  },
  headerTitle: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
  },
  headerSubtitle: {
    fontSize: FontSize.xs,
    marginTop: 2,
  },
  
  // FIX: Header actions container
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },

  // FIX: Grid toggle button
  gridToggleButton: {
    width: 36,
    height: 36,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  
  // Clear all button
  clearAllButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    borderRadius: BorderRadius.md,
    ...Shadow.soft,
  },
  clearAllText: {
    color: '#fff',
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },

  statsCard: {
    marginHorizontal: Spacing.lg,
    marginVertical: Spacing.lg,
    padding: Spacing.lg,
    borderRadius: BorderRadius.xl,
  },
  statRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    gap: Spacing.md,
  },
  statItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  statIcon: {
    width: 40,
    height: 40,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
  },
  statValue: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
  },
  statLabel: {
    fontSize: FontSize.xs,
  },

  listContent: {
    paddingBottom: Spacing['3xl'],
  },
  columnWrapper: {
    paddingHorizontal: Spacing.lg,
    gap: Spacing.lg,
    marginTop: Spacing.md,
  },

  card: {
    borderRadius: BorderRadius.xl,
    overflow: 'hidden',
    marginBottom: Spacing.md,
    ...Shadow.soft,
  },
  
  imageContainer: {
    position: 'relative',
    width: '100%',
    aspectRatio: 1, // Square images
    overflow: 'hidden',
  },
  image: {
    width: '100%',
    height: '100%',
  },
  imagePlaceholder: {
    width: '100%',
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  removeButton: {
    position: 'absolute',
    top: Spacing.sm,
    right: Spacing.sm,
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadow.soft,
  },

  content: {
    padding: Spacing.sm, // Reduced from md for better fit
    gap: Spacing.xs,
  },
  name: {
    fontSize: FontSize.xs, // Smaller for 4-column
    fontWeight: FontWeight.semibold,
    marginBottom: 4,
  },
  price: {
    fontSize: FontSize.sm, // Smaller for 4-column
    fontWeight: FontWeight.bold,
  },
  category: {
    fontSize: 10, // Even smaller
  },

  emptyContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing['5xl'],
    paddingHorizontal: Spacing['2xl'],
    gap: Spacing.md,
  },
  emptyIconContainer: {
    width: 120,
    height: 120,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.md,
  },
  emptyTitle: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
    textAlign: 'center',
  },
  emptySubtitle: {
    fontSize: FontSize.base,
    textAlign: 'center',
    marginBottom: Spacing.lg,
  },
  emptyButton: {
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.lg,
    ...Shadow.soft,
  },
  emptyButtonText: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },

  webMaxWidth: {
    width: '100%',
    maxWidth: 1200,
    alignSelf: 'center',
  },
});