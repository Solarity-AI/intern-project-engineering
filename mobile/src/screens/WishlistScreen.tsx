import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  useWindowDimensions,
  Platform,
  TouchableWithoutFeedback,
} from 'react-native';

import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { SelectableWishlistCard } from '../components/SelectableWishlistCard';
import { LoadMoreCard } from '../components/LoadMoreCard'; // ✨ Added LoadMoreCard

import { useWishlist } from '../context/WishlistContext';
import { useTheme } from '../context/ThemeContext';
import { getWishlistProducts, ApiProduct } from '../services/api'; // ✨ Added API call

import { RootStackParamList } from '../types';
import { BorderRadius, FontSize, FontWeight, Spacing } from '../constants/theme';

type WishlistNavigationProp = NativeStackNavigationProp<RootStackParamList, 'Wishlist'>;

const GRID_STORAGE_KEY = 'wishlist_grid_mode';

export const WishlistScreen = () => {
  const navigation = useNavigation<WishlistNavigationProp>();
  const { colors, colorScheme, toggleTheme } = useTheme();
  const { removeFromWishlist, removeMultipleFromWishlist, clearWishlist, wishlistCount } = useWishlist();

  const { width } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  const webBp = !isWeb ? 'mobile' : width < 720 ? 'narrow' : width < 1100 ? 'medium' : 'wide';

  const headerIconSize = isWeb ? (webBp === 'wide' ? 26 : 24) : 20;
  const headerIconSizeBig = isWeb ? (webBp === 'wide' ? 28 : 26) : 22;

  const [gridMode, setGridMode] = useState<1 | 2 | 3>(2);
  const numColumns = gridMode;

  const gridTouchedRef = useRef(false);

  // ✨ Pagination states
  const [pagedWishlist, setPagedWishlist] = useState<ApiProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);

  const fetchWishlist = useCallback(async (pageNum: number = 0, append: boolean = false) => {
    try {
      if (!append) setLoading(true);
      else setLoadingMore(true);

      const page = await getWishlistProducts({ page: pageNum, size: 10 });

      if (append) {
        setPagedWishlist(prev => [...prev, ...page.content]);
      } else {
        setPagedWishlist(page.content);
      }

      setCurrentPage(pageNum);
      setTotalPages(page.totalPages);
      setHasMore(!page.last);
      setTotalItems(page.totalElements);
    } catch (error) {
      console.error('Error fetching wishlist products:', error);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, []);

  useEffect(() => {
    fetchWishlist(0, false);
  }, [fetchWishlist]);

  const loadMore = () => {
    if (!loadingMore && hasMore) {
      fetchWishlist(currentPage + 1, true);
    }
  };

  // Web auto-grid
  useEffect(() => {
    if (!isWeb) return;
    if (gridTouchedRef.current) return;

    const next: 1 | 2 | 3 = width < 720 ? 1 : width < 1100 ? 2 : 3;
    if (gridMode !== next) setGridMode(next);
  }, [isWeb, width, gridMode]);

  const loadGridPreference = useCallback(async () => {
    try {
      const stored = await AsyncStorage.getItem(GRID_STORAGE_KEY);
      const parsed = stored ? (Number(stored) as 1 | 2 | 3) : null;
      if (parsed === 1 || parsed === 2 || parsed === 3) {
        setGridMode(parsed);
      }
    } catch {
      // no-op
    }
  }, []);

  const saveGridPreference = useCallback(async (value: 1 | 2 | 3) => {
    try {
      await AsyncStorage.setItem(GRID_STORAGE_KEY, String(value));
    } catch {
      // no-op
    }
  }, []);

  useEffect(() => {
    loadGridPreference();
  }, [loadGridPreference]);

  const toggleGridMode = () => {
    gridTouchedRef.current = true;
    setGridMode(prev => {
      const next: 1 | 2 | 3 = prev === 1 ? 2 : prev === 2 ? 3 : 1;
      saveGridPreference(next);
      return next;
    });
  };

  // Multi-select mode
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set());

  const [selectionTick, setSelectionTick] = useState(0);
  const bumpSelectionTick = useCallback(() => setSelectionTick(t => t + 1), []);


  const handleCancelSelection = () => {
    setIsSelectionMode(false);
    setSelectedItems(new Set());
    bumpSelectionTick();
  };

  const handleCardLongPress = (item: ApiProduct) => {
    const id = String(item.id);
    setIsSelectionMode(true);
    setSelectedItems(prev => {
      const next = new Set(prev);
      next.add(id);
      bumpSelectionTick();
      return next;
    });
  };

  const handleCardPress = (item: ApiProduct) => {
    const id = String(item.id);
    if (isSelectionMode) {
      setSelectedItems(prev => {
        const next = new Set(prev);
        if (next.has(id)) next.delete(id);
        else next.add(id);
        if (next.size === 0) setIsSelectionMode(false);
        return next;
      });
      bumpSelectionTick();
      return;
    }
    navigation.navigate('ProductDetails', { productId: id });
  };

  const handleRemoveSelected = useCallback(() => {
    const idsToRemove = Array.from(selectedItems);
    if (idsToRemove.length === 0) return;

    // ✨ Optimistic update: Remove immediately from UI
    setPagedWishlist(prev => {
      const idsSet = new Set(idsToRemove);
      return prev.filter(item => !idsSet.has(String(item.id)));
    });
    setTotalItems(prev => Math.max(0, prev - idsToRemove.length));

    // Exit selection mode first
    handleCancelSelection();

    // Then sync with backend/context (batch operation)
    removeMultipleFromWishlist(idsToRemove);
  }, [selectedItems, removeMultipleFromWishlist]);

  // ✨ Wrapped in useCallback to prevent unnecessary re-renders
  const handleRemoveSingle = useCallback((id: string) => {
    console.log('Removing item:', id);
    // ✨ Optimistic update: Remove immediately from UI
    setPagedWishlist(prev => {
      const filtered = prev.filter(item => String(item.id) !== String(id));
      console.log('Previous length:', prev.length, 'New length:', filtered.length);
      return filtered;
    });
    setTotalItems(prev => Math.max(0, prev - 1));

    // Then sync with backend/context
    removeFromWishlist(id);
  }, [removeFromWishlist]);

  const stats = useMemo(() => {
    const totalPrice = pagedWishlist.reduce((sum, item) => sum + (item.price || 0), 0);
    const avgRating = pagedWishlist.length > 0
      ? pagedWishlist.reduce((sum, item) => sum + (item.averageRating || 0), 0) / pagedWishlist.length
      : 0;
    return { itemCount: totalItems, totalPrice, avgRating };
  }, [pagedWishlist, totalItems]);

  const renderWishlistItem = ({ item, index }: { item: ApiProduct; index: number }) => {
    const id = String(item.id);
    const selected = selectedItems.has(id);
    const isGrid = numColumns > 1;
    const gapSize = Spacing.sm;

    const forceKey =
      Platform.OS === 'android'
        ? `${id}-${isSelectionMode ? 1 : 0}-${selected ? 1 : 0}-${selectionTick}`
        : id;

    return (
      <View
        style={[
          styles.gridItemWrapper,
          isGrid && {
            width: `${100 / numColumns}%`,
            paddingRight: index % numColumns === numColumns - 1 ? 0 : gapSize / 2,
            paddingLeft: index % numColumns === 0 ? 0 : gapSize / 2,
            marginBottom: Spacing.sm,
            flexGrow: 0,
            flexShrink: 0,
          },
          !isGrid && {
            width: '100%',
            marginBottom: Spacing.sm,
          },
        ]}
      >
        <SelectableWishlistCard
          key={forceKey} // ✅ (Android only remount)
          item={item as any}
          numColumns={numColumns}
          isSelectionMode={isSelectionMode}
          isSelected={selected}
          onPress={() => handleCardPress(item)}
          onLongPress={() => handleCardLongPress(item)}
          onRemove={handleRemoveSingle}
        />
      </View>
    );
  };

  const emptyState = (
    <View style={styles.emptyContainer}>
      <View style={[styles.emptyIcon, { backgroundColor: colors.muted }]}>
        <Ionicons name="heart-outline" size={44} color={colors.mutedForeground} />
      </View>
      <Text style={[styles.emptyTitle, { color: colors.foreground }]}>Your wishlist is empty</Text>
      <Text style={[styles.emptySubtitle, { color: colors.mutedForeground }]}>
        Save products you love and find them here later.
      </Text>

      <TouchableOpacity
        style={[styles.emptyButton, { backgroundColor: colors.primary }]}
        onPress={() => navigation.navigate('ProductList')}
        activeOpacity={0.85}
      >
        <Ionicons name="search" size={18} color={colors.primaryForeground} />
        <Text style={[styles.emptyButtonText, { color: colors.primaryForeground }]}>Browse products</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <ScreenWrapper>
      <TouchableWithoutFeedback
        onPress={() => {
          if (isSelectionMode && selectedItems.size > 0) handleCancelSelection();
        }}
      >
        <View style={{ flex: 1 }}>
          {/* Header */}
          <View style={isWeb ? styles.webPageContainer : undefined}>
            <View style={[styles.header, { backgroundColor: colors.background }, isWeb && styles.headerWeb]}>
              <TouchableOpacity
                style={[styles.brand, isWeb && styles.brandWeb]}
                onPress={() => navigation.navigate('ProductList')}
                activeOpacity={0.85}
              >
                <LinearGradient colors={[colors.primary, colors.accent]} style={styles.brandIcon}>
                  <Ionicons name="star" size={16} color={colors.primaryForeground} />
                </LinearGradient>
                <Text style={[styles.brandText, { color: colors.foreground }]}>ProductReview</Text>
              </TouchableOpacity>

              <View style={styles.headerActions}>
                <TouchableOpacity
                  style={[
                    styles.headerButton,
                    isWeb && styles.headerButtonWeb,
                    { backgroundColor: colors.secondary },
                  ]}
                  onPress={toggleTheme}
                  activeOpacity={0.85}
                >
                  <Ionicons
                    name={colorScheme === 'dark' ? 'sunny' : 'moon'}
                    size={headerIconSize}
                    color={colors.foreground}
                  />
                </TouchableOpacity>

                <TouchableOpacity
                  style={[
                    styles.headerButton,
                    isWeb && styles.headerButtonWeb,
                    { backgroundColor: colors.secondary },
                  ]}
                  onPress={toggleGridMode}
                  activeOpacity={0.85}
                >
                  <Ionicons
                    name={gridMode === 1 ? 'list' : gridMode === 2 ? 'grid-outline' : 'grid'}
                    size={headerIconSize}
                    color={colors.foreground}
                  />
                </TouchableOpacity>

                <TouchableOpacity
                  style={[
                    styles.headerButton,
                    isWeb && styles.headerButtonWeb,
                    { backgroundColor: colors.secondary },
                  ]}
                  onPress={async () => {
                    await clearWishlist();
                    setPagedWishlist([]);
                    setTotalItems(0);
                  }}
                  activeOpacity={0.85}
                >
                  <Ionicons name="trash-outline" size={headerIconSizeBig} color={colors.foreground} />
                </TouchableOpacity>
              </View>
            </View>

            {/* Stats */}
            {totalItems > 0 && (
              <View style={[styles.statsSection, { backgroundColor: colors.secondary }, isWeb && styles.statsSectionWeb]}>
                <View style={styles.statsRow}>
                  <View style={styles.statItem}>
                    <LinearGradient colors={[colors.primary, colors.accent]} style={styles.statIcon}>
                      <Ionicons name="heart" size={20} color={colors.primaryForeground} />
                    </LinearGradient>
                    <View>
                      <Text style={[styles.statValue, { color: colors.foreground }]}>{stats.itemCount}</Text>
                      <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Items</Text>
                    </View>
                  </View>

                  <View style={styles.statItem}>
                    <LinearGradient colors={[colors.primary, colors.accent]} style={styles.statIcon}>
                      <Ionicons name="star" size={20} color={colors.primaryForeground} />
                    </LinearGradient>
                    <View>
                      <Text style={[styles.statValue, { color: colors.foreground }]}>{stats.avgRating.toFixed(1)}</Text>
                      <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Avg Rating</Text>
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
                      <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Total</Text>
                    </View>
                  </View>
                </View>
              </View>
            )}
          </View>

          {/* Content */}
          {loading ? (
            <View style={styles.loadingContainer}>
              <ActivityIndicator size="large" color={colors.primary} />
              <Text style={[styles.loadingText, { color: colors.mutedForeground }]}>Loading wishlist...</Text>
            </View>
          ) : pagedWishlist.length === 0 ? (
            emptyState
          ) : (
            <FlatList
              data={pagedWishlist}
              extraData={selectionTick}
              key={numColumns}
              numColumns={numColumns}
              keyExtractor={(item) => String(item.id)}
              renderItem={renderWishlistItem}
              removeClippedSubviews={false}
              showsVerticalScrollIndicator={false}
              keyboardShouldPersistTaps="handled"
              keyboardDismissMode="on-drag"
              contentContainerStyle={[
                styles.listContent,
                isWeb && styles.webListContent,
                !isWeb && { paddingHorizontal: Spacing.lg },
              ]}
              columnWrapperStyle={
                numColumns > 1 ? styles.columnWrapper : undefined
              }
              ListFooterComponent={
                <LoadMoreCard
                  onPress={loadMore}
                  loading={loadingMore}
                  hasMore={hasMore}
                  currentPage={currentPage}
                  totalPages={totalPages}
                />
              }
            />
          )}

          {/* Floating bottom bar for selection mode */}
          {isSelectionMode && selectedItems.size > 0 && (
            <View style={[styles.floatingBar, { backgroundColor: colors.card }]}>
              <View style={isWeb ? styles.floatingBarInnerWeb : undefined}>
                <TouchableOpacity
                  style={[styles.floatingButton, { backgroundColor: colors.destructive }]}
                  onPress={handleRemoveSelected}
                  activeOpacity={0.9}
                >
                  <Ionicons name="trash" size={18} color="#fff" />
                  <Text style={[styles.floatingButtonText, { color: '#fff' }]}>
                    Remove ({selectedItems.size})
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          )}

        </View>
      </TouchableWithoutFeedback>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  webPageContainer: {
    width: '100%',
    maxWidth: 1200,
    alignSelf: 'center',
    paddingHorizontal: Spacing.lg,
  },

  header: {
    paddingTop: Spacing.md,
    paddingBottom: Spacing.md,
    paddingHorizontal: Spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  headerWeb: {
    paddingHorizontal: 0,
  },

  brand: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },

  brandWeb: {
    paddingVertical: 4,
  },

  brandIcon: {
    width: 32,
    height: 32,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    justifyContent: 'center',
  },

  brandText: { fontSize: FontSize.lg, fontWeight: FontWeight.bold },

  headerActions: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },

  headerButton: {
    width: 40,
    height: 40,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
  },

  headerButtonWeb: {
    width: 46,
    height: 46,
  },

  statsSection: {
    marginHorizontal: Spacing.lg,
    marginBottom: Spacing.lg,
    borderRadius: BorderRadius.xl,
    padding: Spacing.lg,
  },

  statsSectionWeb: {
    marginHorizontal: 0,
  },

  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    flexWrap: 'wrap',
  },

  statItem: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },

  statIcon: {
    width: 36,
    height: 36,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
  },

  statValue: { fontSize: FontSize.lg, fontWeight: FontWeight.bold },

  statLabel: { fontSize: FontSize.sm },

  listContent: {
    paddingBottom: Spacing['5xl'] + Spacing.xl,
  },

  webListContent: {
    width: '100%',
    maxWidth: 1200,
    alignSelf: 'center',
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing['5xl'] + Spacing.xl,
  },

  columnWrapper: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },

  gridItemWrapper: {
  },

  listItemWrapper: {
    paddingVertical: Spacing.sm / 2,
  },

  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: Spacing['2xl'],
  },

  loadingText: {
    marginTop: Spacing.md,
    fontSize: FontSize.base,
  },

  emptyContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: Spacing['2xl'],
  },

  emptyIcon: {
    width: 96,
    height: 96,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.lg,
  },

  emptyTitle: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
    marginBottom: Spacing.sm,
    textAlign: 'center',
  },

  emptySubtitle: {
    fontSize: FontSize.base,
    textAlign: 'center',
    marginBottom: Spacing.xl,
    paddingHorizontal: Spacing.lg,
  },

  emptyButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.lg,
  },

  emptyButtonText: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },

  floatingBar: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    padding: Spacing.lg,
    borderTopLeftRadius: BorderRadius.xl,
    borderTopRightRadius: BorderRadius.xl,
  },

  floatingButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.sm,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.lg,
  },

  floatingButtonText: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },
  floatingBarInnerWeb: {
    width: '100%',
    maxWidth: 1200,
    alignSelf: 'center',
    paddingHorizontal: Spacing.lg,
  },

});