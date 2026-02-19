// WishlistScreen — v3 Radical Redesign
// Page title, bento stats (2+1 layout), image-overlay cards
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  useWindowDimensions,
  Platform,
  TouchableWithoutFeedback,
  RefreshControl,
} from 'react-native';

import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { SelectableWishlistCard } from '../components/SelectableWishlistCard';
import { LoadMoreCard } from '../components/LoadMoreCard';
import { OfflineBanner } from '../components/OfflineBanner';
import { GradientDivider } from '../components/GradientDivider';
import { ProductCardSkeleton } from '../components/ProductCardSkeleton';

import { useWishlist } from '../context/WishlistContext';
import { useTheme } from '../context/ThemeContext';
import { useNetwork } from '../context/NetworkContext';
import { getWishlistProducts, ApiProduct, getUserMessage } from '../services/api';

import { RootStackParamList } from '../types';
import { BorderRadius, FontSize, FontWeight, Spacing, Gradients, Glass, Shadow, Glow } from '../constants/theme';
import { getWishlistHeaderToggleSizing } from './wishlistHeaderToggleSizing';

type WishlistNavigationProp = NativeStackNavigationProp<RootStackParamList, 'Wishlist'>;

const GRID_STORAGE_KEY = 'wishlist_grid_mode';

export const WishlistScreen = () => {
  const navigation = useNavigation<WishlistNavigationProp>();
  const { colors, colorScheme, toggleTheme } = useTheme();
  const { removeFromWishlist, removeMultipleFromWishlist, clearWishlist, wishlistCount } = useWishlist();
  const { isConnected, isInternetReachable } = useNetwork();

  const isOffline = !isConnected || isInternetReachable === false;

  const { width } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  const webBp = !isWeb ? 'mobile' : width < 720 ? 'narrow' : width < 1100 ? 'medium' : 'wide';

  const [gridMode, setGridMode] = useState<1 | 2 | 3>(2);
  const numColumns = gridMode;
  const headerToggleSizing = getWishlistHeaderToggleSizing({
    isWeb,
    breakpoint: webBp,
    numColumns,
  });

  const gridTouchedRef = useRef(false);

  const [pagedWishlist, setPagedWishlist] = useState<ApiProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);

  const fetchWishlist = useCallback(async (pageNum: number = 0, append: boolean = false) => {
    try {
      if (!append) setLoading(true);
      else setLoadingMore(true);
      setError(null);

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
    } catch (err) {
      console.error('Error fetching wishlist products:', err);
      setError(getUserMessage(err));
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

  const [refreshing, setRefreshing] = useState(false);
  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await fetchWishlist(0, false);
    setRefreshing(false);
  }, [fetchWishlist]);

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
      return next;
    });
    bumpSelectionTick();
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

    setPagedWishlist(prev => {
      const idsSet = new Set(idsToRemove);
      return prev.filter(item => !idsSet.has(String(item.id)));
    });
    setTotalItems(prev => Math.max(0, prev - idsToRemove.length));

    handleCancelSelection();
    removeMultipleFromWishlist(idsToRemove);
  }, [selectedItems, removeMultipleFromWishlist]);

  const handleRemoveSingle = useCallback((id: string) => {
    console.log('Removing item:', id);
    setPagedWishlist(prev => {
      const filtered = prev.filter(item => String(item.id) !== String(id));
      console.log('Previous length:', prev.length, 'New length:', filtered.length);
      return filtered;
    });
    setTotalItems(prev => Math.max(0, prev - 1));
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
          key={forceKey}
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
          {isOffline && <OfflineBanner onRetry={() => fetchWishlist(0, false)} />}

          {/* Header */}
          <View style={isWeb ? styles.webPageContainer : undefined}>
            <View style={[styles.header, { backgroundColor: colors.background }, isWeb && styles.headerWeb]}>
              <TouchableOpacity
                style={[styles.brand, isWeb && styles.brandWeb]}
                onPress={() => navigation.navigate('ProductList')}
                activeOpacity={0.85}
              >
                <LinearGradient colors={Gradients.brandVivid as [string, string, ...string[]]} style={styles.brandIcon}>
                  <Ionicons name="flash" size={18} color="#fff" />
                </LinearGradient>
                <Text style={[styles.brandText, { color: colors.foreground }]}>Solarity</Text>
                <Text style={[styles.brandTextAccent, { color: colors.primary }]}>Review</Text>
              </TouchableOpacity>

              <View style={[styles.headerActions, { gap: headerToggleSizing.actionGap }]}>
                <TouchableOpacity
                  style={[
                    styles.headerButton,
                    {
                      width: headerToggleSizing.buttonSize,
                      height: headerToggleSizing.buttonSize,
                      borderRadius: headerToggleSizing.buttonSize / 2,
                    },
                    colorScheme === 'dark' ? Glass.subtle : { backgroundColor: colors.secondary },
                  ]}
                  onPress={toggleTheme}
                  activeOpacity={0.85}
                >
                  <Ionicons
                    name={colorScheme === 'dark' ? 'sunny' : 'moon'}
                    size={headerToggleSizing.iconSize}
                    color={colors.foreground}
                  />
                </TouchableOpacity>

                <TouchableOpacity
                  style={[
                    styles.headerButton,
                    {
                      width: headerToggleSizing.buttonSize,
                      height: headerToggleSizing.buttonSize,
                      borderRadius: headerToggleSizing.buttonSize / 2,
                    },
                    colorScheme === 'dark' ? Glass.subtle : { backgroundColor: colors.secondary },
                  ]}
                  onPress={toggleGridMode}
                  activeOpacity={0.85}
                >
                  <Ionicons
                    name={gridMode === 1 ? 'list' : gridMode === 2 ? 'grid-outline' : 'grid'}
                    size={headerToggleSizing.iconSize}
                    color={colors.foreground}
                  />
                </TouchableOpacity>

                <TouchableOpacity
                  style={[
                    styles.headerButton,
                    {
                      width: headerToggleSizing.buttonSize,
                      height: headerToggleSizing.buttonSize,
                      borderRadius: headerToggleSizing.buttonSize / 2,
                    },
                    colorScheme === 'dark' ? Glass.subtle : { backgroundColor: colors.secondary },
                  ]}
                  onPress={async () => {
                    await clearWishlist();
                    setPagedWishlist([]);
                    setTotalItems(0);
                  }}
                  activeOpacity={0.85}
                >
                  <Ionicons
                    name="trash-outline"
                    size={headerToggleSizing.emphasisIconSize}
                    color={colors.foreground}
                  />
                </TouchableOpacity>
              </View>
            </View>

            {/* Page title */}
            <Text style={[styles.pageTitle, { color: colors.foreground }, isWeb && styles.pageTitleWeb]}>
              My Wishlist
            </Text>

            {/* Bento Stats — 2+1 layout */}
            {totalItems > 0 && (
              <View style={[styles.bentoContainer, isWeb && styles.bentoContainerWeb]}>
                <View style={styles.bentoTopRow}>
                  <View style={[
                    styles.bentoCard,
                    colorScheme === 'dark' ? Glass.elevated : { backgroundColor: colors.card, ...Shadow.soft },
                  ]}>
                    <Ionicons name="heart" size={20} color="#F87171" />
                    <Text style={[styles.bentoValue, { color: colors.foreground }]}>{stats.itemCount}</Text>
                    <Text style={styles.bentoLabel}>Items</Text>
                  </View>

                  <View style={[
                    styles.bentoCard,
                    colorScheme === 'dark' ? Glass.elevated : { backgroundColor: colors.card, ...Shadow.soft },
                  ]}>
                    <Ionicons name="star" size={20} color="#FBBF24" />
                    <Text style={[styles.bentoValue, { color: colors.foreground }]}>{stats.avgRating.toFixed(1)}</Text>
                    <Text style={styles.bentoLabel}>Avg Rating</Text>
                  </View>
                </View>

                <View style={[
                  styles.bentoCardFull,
                  colorScheme === 'dark' ? Glass.elevated : { backgroundColor: colors.card, ...Shadow.soft },
                ]}>
                  <Ionicons name="cash" size={20} color="#10B981" />
                  <Text style={[styles.bentoValue, { color: colors.foreground }]}>
                    ${stats.totalPrice.toFixed(0)}
                  </Text>
                  <Text style={styles.bentoLabel}>Total Value</Text>
                </View>
              </View>
            )}

            <GradientDivider />
          </View>

          {/* Content */}
          {loading ? (
            <View style={[
              styles.skeletonGrid,
              numColumns > 1 && styles.skeletonGridMultiCol,
              isWeb ? styles.webListContent : { paddingHorizontal: Spacing.lg },
            ]}>
              {Array.from({ length: numColumns === 1 ? 3 : 4 }).map((_, i) => (
                <View
                  key={`wskel-${i}`}
                  style={[
                    numColumns > 1 && {
                      width: `${100 / numColumns}%` as any,
                      paddingRight: i % numColumns === numColumns - 1 ? 0 : Spacing.sm / 2,
                      paddingLeft: i % numColumns === 0 ? 0 : Spacing.sm / 2,
                      marginBottom: Spacing.sm,
                    },
                    numColumns === 1 && {
                      width: '100%',
                      marginBottom: Spacing.sm,
                    },
                  ]}
                >
                  <ProductCardSkeleton numColumns={numColumns} />
                </View>
              ))}
            </View>
          ) : error ? (
            <View style={styles.errorContainer}>
              <Ionicons name="alert-circle-outline" size={48} color={colors.destructive} />
              <Text style={[styles.errorText, { color: colors.destructive }]}>{error}</Text>
              <TouchableOpacity
                style={[styles.emptyButton, { backgroundColor: colors.primary }]}
                onPress={() => fetchWishlist(0, false)}
                activeOpacity={0.85}
              >
                <Text style={[styles.emptyButtonText, { color: colors.primaryForeground }]}>Retry</Text>
              </TouchableOpacity>
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
              windowSize={5}
              refreshControl={
                <RefreshControl
                  refreshing={refreshing}
                  onRefresh={handleRefresh}
                  tintColor={colors.primary}
                  colors={['#10B981']}
                  progressBackgroundColor={colorScheme === 'dark' ? '#1E293B' : '#FFFFFF'}
                />
              }
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
            <View style={[styles.floatingBar, colorScheme === 'dark' ? Glass.card : { backgroundColor: colors.card }]}>
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
    paddingBottom: Spacing.sm,
    paddingHorizontal: Spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerWeb: {
    paddingHorizontal: 0,
  },

  brand: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  brandWeb: { paddingVertical: 4 },
  brandIcon: {
    width: 36,
    height: 36,
    borderRadius: BorderRadius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadow.soft,
  },
  brandText: { fontSize: FontSize.xl, fontWeight: FontWeight.bold },
  brandTextAccent: { fontSize: FontSize.xl, fontWeight: FontWeight.bold, marginLeft: -2 },

  headerActions: { flexDirection: 'row', alignItems: 'center', flexShrink: 0 },
  headerButton: {
    width: 40,
    height: 40,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
  },

  /* Page title */
  pageTitle: {
    fontSize: FontSize['3xl'],
    fontWeight: FontWeight.bold,
    paddingHorizontal: Spacing.lg,
    marginTop: Spacing.sm,
    marginBottom: Spacing.md,
  },
  pageTitleWeb: {
    paddingHorizontal: 0,
  },

  /* Bento stats */
  bentoContainer: {
    paddingHorizontal: Spacing.lg,
    gap: Spacing.sm,
    marginBottom: Spacing.sm,
  },
  bentoContainerWeb: {
    paddingHorizontal: 0,
  },
  bentoTopRow: {
    flexDirection: 'row',
    gap: Spacing.sm,
  },
  bentoCard: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.xl,
  },
  bentoCardFull: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.xl,
  },
  bentoValue: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
  },
  bentoLabel: {
    fontSize: 11,
    color: 'rgba(148,163,184,0.7)',
    fontWeight: FontWeight.medium,
  },

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

  skeletonGrid: {
    paddingTop: Spacing.md,
  },
  skeletonGridMultiCol: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  errorContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: Spacing['2xl'],
    gap: Spacing.md,
  },
  errorText: {
    fontSize: FontSize.base,
    textAlign: 'center',
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
    borderTopLeftRadius: BorderRadius['2xl'],
    borderTopRightRadius: BorderRadius['2xl'],
    ...Shadow.medium,
  },
  floatingButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.sm,
    paddingVertical: Spacing.lg,
    borderRadius: BorderRadius.xl,
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
