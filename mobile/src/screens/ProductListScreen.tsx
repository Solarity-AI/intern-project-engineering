// React Native ProductListScreen — v3 Radical Redesign
// Full-bleed hero, floating stats, image-overlay cards, gradient dividers
import React, { useCallback, useEffect, useMemo, useState, useRef } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { getProducts, getGlobalStats, ApiProduct, GlobalStats, getUserMessage, clearApiCache } from '../services/api';
import { TouchableWithoutFeedback, DeviceEventEmitter } from 'react-native';

const SORT_STORAGE_KEY = 'user_sort_preference';

import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  Animated,
  useWindowDimensions,
  Platform,
  Vibration,
  Image,
  RefreshControl,
} from 'react-native';

import { useNavigation, useFocusEffect, useRoute, RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { SelectableProductCard } from '../components/SelectableProductCard';
import { SearchBar } from '../components/SearchBar';
import { LoadMoreCard } from '../components/LoadMoreCard';
import { OfflineBanner } from '../components/OfflineBanner';
import { CategoryFilter } from '../components/CategoryFilter';
import { SortFilter } from '../components/SortFilter';
import { GradientDivider } from '../components/GradientDivider';
import { ProductCardSkeleton } from '../components/ProductCardSkeleton';
import { useNotifications } from '../context/NotificationContext';
import { useWishlist } from '../context/WishlistContext';
import { useTheme } from '../context/ThemeContext';
import { useSearch } from '../context/SearchContext';
import { useNetwork } from '../context/NetworkContext';

import { RootStackParamList } from '../types';
import { BorderRadius, FontSize, FontWeight, Spacing, Gradients, Glass, Glow, Shadow } from '../constants/theme';
import { useDebounce } from '../hooks/useDebounce';

type ProductListNavigationProp = NativeStackNavigationProp<RootStackParamList, 'ProductList'>;

// Helper to get category image
function imageForCategory(categories?: string[]) {
  const c = (categories && categories.length > 0 ? categories[0] : '').toLowerCase();
  if (c.includes('audio')) return 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80';
  if (c.includes('smart') || c.includes('phone') || c.includes('mobile')) return 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80';
  if (c.includes('camera') || c.includes('photo')) return 'https://images.unsplash.com/photo-1519183071298-a2962be96cdb?w=800&q=80';
  if (c.includes('watch') || c.includes('wear')) return 'https://images.unsplash.com/photo-1523275335684-37898b6baf30e?w=800&q=80';
  if (c.includes('laptop') || c.includes('computer')) return 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80';
  return 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&q=80';
}

export const ProductListScreen = () => {
  const navigation = useNavigation<ProductListNavigationProp>();
  const route = useRoute<RouteProp<RootStackParamList, 'ProductList'>>();
  const { colors, colorScheme, toggleTheme } = useTheme();
  const { unreadCount } = useNotifications();
  const { wishlistCount, addMultipleToWishlist, isInWishlist } = useWishlist();
  const { addSearchTerm } = useSearch();
  const { isConnected, isInternetReachable, checkConnection } = useNetwork();

  const { width } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  const webBp = !isWeb ? 'mobile' : width < 720 ? 'narrow' : width < 1100 ? 'medium' : 'wide';

  const containerMaxWidth =
    !isWeb ? undefined : webBp === 'wide' ? 1200 : webBp === 'medium' ? 1040 : 900;

  const headerIconSize = isWeb ? 20 : 18;
  const headerIconSizeBig = isWeb ? 22 : 20;

  // Offline
  const isOffline = !isConnected || isInternetReachable === false;

  // Refs to prevent double fetching and handle race conditions
  const abortControllerRef = useRef<AbortController | null>(null);
  const fetchIdRef = useRef(0);

  // Grid mode: 1 / 2 / 3
  const [gridMode, setGridMode] = useState<1 | 2 | 3>(2);
  const numColumns = gridMode;
  const gridTouchedRef = useRef(false);

  useEffect(() => {
    if (!isWeb) return;
    if (gridTouchedRef.current) return;

    const next: 1 | 2 | 3 = width < 720 ? 1 : width < 1100 ? 2 : 3;
    if (gridMode !== next) setGridMode(next);
  }, [isWeb, width, gridMode]);

  // Multi-select mode
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set());

  const [selectionTick, setSelectionTick] = useState(0);
  const bumpSelectionTick = useCallback(() => setSelectionTick(t => t + 1), []);

  const [apiProducts, setApiProducts] = useState<ApiProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Pagination states
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  // Search / Sort / Filter
  const [searchQuery, setSearchQuery] = useState((route.params as any)?.search ?? '');
  const [submittedSearchQuery, setSubmittedSearchQuery] = useState((route.params as any)?.search ?? '');
  const [selectedCategory, setSelectedCategory] = useState((route.params as any)?.category ?? 'All');
  const [sortBy, setSortBy] = useState('name,asc');
  const [sortLoaded, setSortLoaded] = useState(false);

  // Global stats from backend
  const [globalStats, setGlobalStats] = useState<GlobalStats | null>(null);

  const filteredProducts = apiProducts;

  const loadSortPreference = useCallback(async () => {
    try {
      const storedSort = await AsyncStorage.getItem(SORT_STORAGE_KEY);
      if (storedSort) setSortBy(storedSort);
    } finally {
      setSortLoaded(true);
    }
  }, []);

  useEffect(() => {
    loadSortPreference();
  }, [loadSortPreference]);

  // Fetch global stats from backend
  useEffect(() => {
    const fetchStats = async () => {
      try {
        const stats = await getGlobalStats({
          category: selectedCategory === 'All' ? undefined : selectedCategory,
          search: submittedSearchQuery?.trim() || undefined,
        });
        setGlobalStats(stats);
      } catch (err) {
        console.error('Failed to fetch global stats:', err);
      }
    };
    fetchStats();
  }, [selectedCategory, submittedSearchQuery]);

  const toggleGridMode = () => {
    gridTouchedRef.current = true;
    setGridMode(prev => (prev === 1 ? 2 : prev === 2 ? 3 : 1));
  };

  const handleCancelSelection = () => {
    setIsSelectionMode(false);
    setSelectedItems(new Set());
    bumpSelectionTick();
  };

  const handleAddSelectedToWishlist = () => {
    const selectedProducts = filteredProducts.filter(p => {
      const id = String((p as any)?.id ?? '');
      return selectedItems.has(id) && !isInWishlist(id);
    });

    if (selectedProducts.length === 0) {
      handleCancelSelection();
      return;
    }

    requestAnimationFrame(() => {
      addMultipleToWishlist(selectedProducts.map(p => ({
        id: String((p as any).id),
        name: p.name,
        price: p.price,
        imageUrl: p.imageUrl,
        categories: p.categories,
        averageRating: p.averageRating,
      })) as any);
      handleCancelSelection();
    });
  };

  const handleCardLongPress = (product: ApiProduct) => {
    const id = String((product as any)?.id ?? '');
    if (!id) return;

    if (Platform.OS === 'android') {
      Vibration.vibrate(50);
    }

    setIsSelectionMode(true);
    setSelectedItems(prev => new Set(prev).add(id));
    bumpSelectionTick();
  };

  const handleCardPress = (product: ApiProduct) => {
    const id = String((product as any)?.id ?? '');
    if (!id) return;

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

    navigation.navigate('ProductDetails', { productId: (product as any)?.id });
  };

  const fetchProducts = useCallback(
    async (page: number, append: boolean, searchOverride?: string, categoryOverride?: string, sortOverride?: string) => {
      if (!sortLoaded) return;

      const effectiveSearch = searchOverride !== undefined ? searchOverride : submittedSearchQuery;
      const effectiveCategory = categoryOverride !== undefined ? categoryOverride : selectedCategory;
      const effectiveSort = sortOverride !== undefined ? sortOverride : sortBy;

      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }

      abortControllerRef.current = new AbortController();
      fetchIdRef.current += 1;
      const currentFetchId = fetchIdRef.current;

      console.log(`[Search] Fetching with: search="${effectiveSearch}", category="${effectiveCategory}", sort="${effectiveSort}", page=${page}`);

      try {
        if (page === 0) setLoading(true);
        else setLoadingMore(true);

        setError(null);

        if (isOffline) {
          setLoading(false);
          setLoadingMore(false);
          return;
        }

        const res = await getProducts({
          page,
          size: 20,
          category: effectiveCategory === 'All' ? undefined : effectiveCategory,
          search: effectiveSearch?.trim() ? effectiveSearch.trim() : undefined,
          sort: effectiveSort,
        });

        if (currentFetchId !== fetchIdRef.current) {
          console.log(`[Search] Ignoring stale response for fetchId=${currentFetchId}, current=${fetchIdRef.current}`);
          return;
        }

        const items = (res as any)?.content ?? (res as any)?.items ?? [];
        const totalPagesFromApi = (res as any)?.totalPages ?? 0;
        const totalElementsFromApi = (res as any)?.totalElements ?? 0;

        console.log(`[Search] Received ${items.length} items for search="${effectiveSearch}"`);

        setTotalPages(totalPagesFromApi);
        setTotalElements(totalElementsFromApi);
        setCurrentPage(page);
        setHasMore(page < totalPagesFromApi - 1);
        setApiProducts(prev => (append ? [...prev, ...items] : items));
      } catch (err: any) {
        if (err.name === 'AbortError') {
          console.log('[Search] Request aborted');
          return;
        }
        if (currentFetchId !== fetchIdRef.current) return;
        setError(getUserMessage(err));
      } finally {
        if (currentFetchId === fetchIdRef.current) {
          setLoading(false);
          setLoadingMore(false);
        }
      }
    },
    [sortLoaded, isOffline, submittedSearchQuery, selectedCategory, sortBy]
  );

  // Initial load and filter changes
  useEffect(() => {
    if (!sortLoaded) return;
    fetchProducts(0, false);
  }, [selectedCategory, submittedSearchQuery, sortBy, sortLoaded]);

  // Listen for review updates
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener('reviewAdded', (event) => {
      console.log('Review added event received:', event);
      fetchProducts(0, false);

      getGlobalStats({
        category: selectedCategory === 'All' ? undefined : selectedCategory,
        search: submittedSearchQuery?.trim() || undefined,
      }).then(setGlobalStats).catch(console.error);
    });

    return () => {
      subscription.remove();
    };
  }, [fetchProducts, selectedCategory, submittedSearchQuery]);

  useEffect(() => {
    const category = (route.params as any)?.category;
    const search = (route.params as any)?.search;
    if (category) setSelectedCategory(category);
    if (search !== undefined) {
      setSearchQuery(search);
      setSubmittedSearchQuery(search);
    }
  }, [route.params]);

  const loadMoreProducts = () => {
    if (loadingMore || loading || !hasMore) return;
    fetchProducts(currentPage + 1, true);
  };

  const stats = useMemo(() => {
    if (globalStats) {
      return {
        productCount: globalStats.totalProducts,
        totalReviews: globalStats.totalReviews,
        avgRating: globalStats.averageRating,
      };
    }
    const productCount = totalElements;
    const totalReviews = apiProducts.reduce((sum, p) => sum + ((p as any)?.reviewCount || 0), 0);
    const avgRating = apiProducts.length > 0
      ? apiProducts.reduce((sum, p) => sum + ((p as any)?.averageRating || 0), 0) / apiProducts.length
      : 0;
    return { productCount, totalReviews, avgRating };
  }, [globalStats, totalElements, apiProducts]);

  const handleReset = useCallback(() => {
    setSearchQuery('');
    setSubmittedSearchQuery('');
    setSelectedCategory('All');
    setSortBy('name,asc');
    if (Platform.OS === 'web') navigation.setParams({ category: 'All', search: '' } as any);
  }, [navigation]);

  // Debounce search query with useDebounce hook
  const debouncedSearchQuery = useDebounce(searchQuery, 500);
  const prevDebouncedRef = useRef(debouncedSearchQuery);

  useEffect(() => {
    const prev = prevDebouncedRef.current;
    prevDebouncedRef.current = debouncedSearchQuery;

    // Only reset when the user actively clears the search field
    // (debounced value transitions from non-empty to empty)
    if (prev.trim().length > 0 && debouncedSearchQuery.trim().length === 0) {
      handleReset();
    }

    if (Platform.OS === 'web') {
      navigation.setParams({
        category: selectedCategory,
        search: debouncedSearchQuery,
      } as any);
    }
  }, [debouncedSearchQuery, selectedCategory, navigation, handleReset]);

  // Pull-to-refresh
  const [refreshing, setRefreshing] = useState(false);
  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    clearApiCache();
    await fetchProducts(0, false);
    try {
      const stats = await getGlobalStats({
        category: selectedCategory === 'All' ? undefined : selectedCategory,
        search: submittedSearchQuery?.trim() || undefined,
      });
      setGlobalStats(stats);
    } catch {}
    setRefreshing(false);
  }, [fetchProducts, selectedCategory, submittedSearchQuery]);

  const handleSearchSubmit = (search: string) => {
    if (search.trim().length > 0) {
      addSearchTerm(search);
      setSubmittedSearchQuery(search);
      if (Platform.OS === 'web') navigation.setParams({ search } as any);
    } else {
      handleReset();
    }
  };

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
    if (Platform.OS === 'web') navigation.setParams({ category } as any);
  };

  const handleSortChange = async (sort: string) => {
    setSortBy(sort);
    try {
      await AsyncStorage.setItem(SORT_STORAGE_KEY, sort);
    } catch (error) {
      console.error('Failed to save sort preference:', error);
    }
  };

  const handleRetry = useCallback(async () => {
    const online = await checkConnection();
    if (online) {
      setError(null);
      fetchProducts(0, false);
    }
  }, [checkConnection, fetchProducts]);

  // Featured product = first product (excluded from grid)
  const featuredProduct = useMemo(() => {
    if (!loading && !error && filteredProducts.length > 0) return filteredProducts[0];
    return null;
  }, [loading, error, filteredProducts]);

  const gridProducts = useMemo(() => {
    if (featuredProduct) return filteredProducts.slice(1);
    return filteredProducts;
  }, [featuredProduct, filteredProducts]);

  const featuredImageUri = useMemo(() => {
    if (!featuredProduct) return '';
    const direct = featuredProduct.imageUrl;
    if (typeof direct === 'string' && direct.trim().length > 0) return direct.trim();
    return imageForCategory(featuredProduct.categories);
  }, [featuredProduct]);

  const featuredImageOpacity = useRef(new Animated.Value(0)).current;
  const onFeaturedImageLoad = useCallback(() => {
    Animated.timing(featuredImageOpacity, {
      toValue: 1,
      duration: 400,
      useNativeDriver: false,
    }).start();
  }, [featuredImageOpacity]);

  // Reset opacity when featured product changes
  useEffect(() => {
    featuredImageOpacity.setValue(0);
  }, [featuredImageUri]);

  // ListHeaderComponent — Hero, Search, Filter
  const listHeaderContent = useMemo(() => (
    <>
      {/* ===== FULL-BLEED IMMERSIVE HERO ===== */}
      <View style={styles.heroWrapper}>
        {/* Mesh gradient layers */}
        <LinearGradient
          colors={Gradients.meshA as [string, string, ...string[]]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={StyleSheet.absoluteFill}
        />
        <LinearGradient
          colors={Gradients.meshB as [string, string, ...string[]]}
          start={{ x: 1, y: 0 }}
          end={{ x: 0, y: 1 }}
          style={StyleSheet.absoluteFill}
        />
        <LinearGradient
          colors={Gradients.meshC as [string, string, ...string[]]}
          start={{ x: 0.5, y: 0 }}
          end={{ x: 0.5, y: 1 }}
          style={StyleSheet.absoluteFill}
        />

        {/* Decorative blur orbs */}
        {colorScheme === 'dark' && (
          <>
            <View style={styles.heroOrbGreen} />
            <View style={styles.heroOrbPurple} />
            <View style={styles.heroOrbGold} />
          </>
        )}

        {/* Top bar — logo + nav INSIDE the hero */}
        <View style={[styles.topBar, isWeb && styles.topBarWeb, isWeb && { maxWidth: containerMaxWidth }]}>
          <TouchableOpacity
            onPress={handleReset}
            style={styles.logoContainer}
            accessibilityLabel="Solarity Review, go to home"
            accessibilityRole="button"
          >
            <LinearGradient colors={Gradients.brandVivid as [string, string, ...string[]]} style={styles.logoIcon}>
              <Ionicons name="flash" size={18} color="#fff" />
            </LinearGradient>
            <Text style={styles.logoText}>Solarity</Text>
            <Text style={styles.logoTextAccent}>Review</Text>
          </TouchableOpacity>

          <View style={styles.headerButtons}>
            <TouchableOpacity
              style={[styles.headerIconButton, isWeb && styles.headerIconButtonWeb]}
              onPress={toggleTheme}
              activeOpacity={0.8}
              accessibilityLabel={colorScheme === 'dark' ? 'Toggle light mode' : 'Toggle dark mode'}
              accessibilityRole="button"
            >
              <Ionicons
                name={colorScheme === 'dark' ? 'sunny' : 'moon'}
                size={headerIconSize}
                color={colorScheme === 'dark' ? '#FBBF24' : '#fff'}
              />
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.headerIconButton, isWeb && styles.headerIconButtonWeb]}
              onPress={toggleGridMode}
              activeOpacity={0.8}
              accessibilityLabel="Change grid layout"
              accessibilityRole="button"
            >
              <Ionicons
                name={gridMode === 1 ? 'list' : gridMode === 2 ? 'grid-outline' : 'grid'}
                size={headerIconSize}
                color="#fff"
              />
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.headerIconButton, isWeb && styles.headerIconButtonWeb]}
              onPress={() => navigation.navigate('Wishlist')}
              activeOpacity={0.8}
              accessibilityLabel={`View wishlist, ${wishlistCount} items`}
              accessibilityRole="button"
            >
              <Ionicons name="heart" size={headerIconSizeBig} color="#F87171" />
              {wishlistCount > 0 && (
                <View style={styles.badge}>
                  <Text style={styles.badgeText}>{wishlistCount}</Text>
                </View>
              )}
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.headerIconButton, isWeb && styles.headerIconButtonWeb]}
              onPress={() => navigation.navigate('Notifications')}
              activeOpacity={0.8}
              accessibilityLabel={`View notifications, ${unreadCount} unread`}
              accessibilityRole="button"
            >
              <Ionicons name="notifications" size={headerIconSizeBig} color="#FBBF24" />
              {unreadCount > 0 && (
                <View style={[styles.badge, { backgroundColor: colors.destructive }]}>
                  <Text style={styles.badgeText}>{unreadCount}</Text>
                </View>
              )}
            </TouchableOpacity>
          </View>
        </View>

        {/* Hero text — LEFT-ALIGNED, massive tight headlines */}
        <View style={[styles.heroContent, isWeb && { maxWidth: containerMaxWidth, alignSelf: 'center', width: '100%' }]}>
          <Text style={[styles.heroTitle, isWeb && styles.heroTitleWeb]}>
            Discover{'\n'}Products You'll{' '}
            <Text style={{ color: '#10B981' }}>Love</Text>
          </Text>
          <Text style={styles.heroSubtitle}>
            AI-powered insights from real reviews
          </Text>
        </View>
      </View>

      {/* ===== FLOATING STATS BAR — overlaps hero bottom ===== */}
      <View style={[styles.floatingStatsContainer, isWeb && { maxWidth: containerMaxWidth, alignSelf: 'center', width: '100%' }]}>
        <View style={[
          styles.floatingStats,
          colorScheme === 'dark' ? Glass.elevated : { backgroundColor: colors.card, ...Shadow.medium },
        ]}>
          <View style={styles.statItem}>
            <Text style={[styles.statNumber, { color: colors.foreground }]}>{stats.avgRating.toFixed(1)}</Text>
            <Text style={styles.statUpperLabel}>AVG RATING</Text>
          </View>
          <View style={[styles.statSeparator, { backgroundColor: colors.border }]} />
          <View style={styles.statItem}>
            <Text style={[styles.statNumber, { color: colors.foreground }]}>{String(stats.totalReviews)}</Text>
            <Text style={styles.statUpperLabel}>REVIEWS</Text>
          </View>
          <View style={[styles.statSeparator, { backgroundColor: colors.border }]} />
          <View style={styles.statItem}>
            <Text style={[styles.statNumber, { color: colors.foreground }]}>{String(stats.productCount)}</Text>
            <Text style={styles.statUpperLabel}>PRODUCTS</Text>
          </View>
        </View>
      </View>

      {/* ===== SEARCH — pill-shaped ===== */}
      <View style={[styles.searchSection, isWeb && styles.searchSectionWeb, isWeb && { maxWidth: containerMaxWidth }]}>
        <SearchBar value={searchQuery} onChangeText={setSearchQuery} onSearchSubmit={handleSearchSubmit} />
      </View>

      {/* ===== COMBINED FILTER TOOLBAR — no "Explore Products" or "Sort by:" labels ===== */}
      <View style={[styles.filterSection, isWeb && styles.filterSectionWeb, isWeb && { maxWidth: containerMaxWidth }]}>
        <CategoryFilter
          selectedCategory={selectedCategory}
          onCategoryChange={handleCategoryChange}
        />
        <SortFilter
          selectedSort={sortBy}
          onSortChange={handleSortChange}
        />
      </View>

      {/* ===== FEATURED PRODUCT SPOTLIGHT ===== */}
      {featuredProduct && (
        <View style={[styles.featuredContainer, isWeb && { maxWidth: containerMaxWidth, alignSelf: 'center', width: '100%' }]}>
          <TouchableOpacity
            activeOpacity={0.9}
            style={[
              styles.featuredCard,
              colorScheme === 'dark'
                ? Glass.elevated
                : { backgroundColor: '#0F172A', ...Shadow.medium },
            ]}
            onPress={() => handleCardPress(featuredProduct)}
          >
            <Animated.Image
              source={{ uri: featuredImageUri }}
              style={[styles.featuredImage, { opacity: featuredImageOpacity }]}
              resizeMode="cover"
              onLoad={onFeaturedImageLoad}
            />
            <LinearGradient
              colors={['transparent', 'rgba(11,17,32,0.85)'] as [string, string]}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 0 }}
              style={styles.featuredImageOverlay}
            />
            <View style={styles.featuredContent}>
              <Text style={styles.featuredLabel}>FEATURED</Text>
              <Text style={styles.featuredName} numberOfLines={2}>
                {featuredProduct.name ?? 'Product'}
              </Text>
              <View style={styles.featuredRatingRow}>
                <Ionicons name="star" size={14} color="#FBBF24" />
                <Text style={styles.featuredRating}>
                  {(featuredProduct.averageRating ?? 0).toFixed(1)}
                </Text>
                <Text style={styles.featuredReviewCount}>
                  ({featuredProduct.reviewCount ?? 0})
                </Text>
              </View>
              <View style={styles.featuredPriceRow}>
                <Text style={styles.featuredPrice}>
                  ${featuredProduct.price?.toFixed(2) ?? 'N/A'}
                </Text>
                <Ionicons name="arrow-forward" size={18} color="rgba(255,255,255,0.5)" />
              </View>
            </View>
          </TouchableOpacity>
        </View>
      )}

      {/* ===== GRADIENT DIVIDER before grid ===== */}
      <View style={[isWeb && { maxWidth: containerMaxWidth, alignSelf: 'center', width: '100%' }, { paddingHorizontal: Spacing.lg }]}>
        <GradientDivider label="All Products" />
      </View>
    </>
  ), [
    isWeb, containerMaxWidth, webBp, colors, colorScheme,
    handleReset, toggleTheme, toggleGridMode, gridMode,
    headerIconSize, headerIconSizeBig, wishlistCount, unreadCount,
    stats, searchQuery, setSearchQuery, handleSearchSubmit,
    selectedCategory, handleCategoryChange, sortBy, handleSortChange,
    featuredProduct, featuredImageUri, featuredImageOpacity, onFeaturedImageLoad, handleCardPress,
  ]);

  useFocusEffect(
    useCallback(() => {
      return () => { };
    }, [])
  );

  const newToWishlistCount = useMemo(() => {
    let count = 0;
    selectedItems.forEach(id => {
      if (!isInWishlist(id)) count++;
    });
    return count;
  }, [selectedItems, isInWishlist]);

  return (
    <ScreenWrapper>
      <TouchableWithoutFeedback
        onPress={() => {
          if (isSelectionMode && selectedItems.size > 0) handleCancelSelection();
        }}
      >
        <View style={{ flex: 1 }}>
          {isOffline && <OfflineBanner onRetry={handleRetry} />}

          <FlatList
            data={loading || error ? [] : gridProducts}
            extraData={selectionTick}
            key={numColumns}
            numColumns={numColumns}
            columnWrapperStyle={
              numColumns > 1 ? styles.columnWrap : undefined
            }
            removeClippedSubviews={false}
            keyExtractor={(item: any) => String(item?.id ?? '')}
            contentContainerStyle={[
              styles.listContent,
              isWeb && styles.webListContent,
              isWeb && { maxWidth: containerMaxWidth },
              !isWeb && { paddingHorizontal: Spacing.lg },
            ]}
            ListHeaderComponent={listHeaderContent}
            ListEmptyComponent={
              loading ? (
                <View style={[styles.skeletonGrid, numColumns > 1 && styles.skeletonGridMultiCol]}>
                  {Array.from({ length: numColumns === 1 ? 4 : 6 }).map((_, i) => (
                    <View
                      key={`skel-${i}`}
                      style={[
                        numColumns > 1 && {
                          width: `${100 / numColumns}%` as any,
                          paddingRight: i % numColumns === numColumns - 1 ? 0 : Spacing.lg / 2,
                          paddingLeft: i % numColumns === 0 ? 0 : Spacing.lg / 2,
                          marginBottom: Spacing.lg,
                        },
                        numColumns === 1 && {
                          width: '100%',
                          marginBottom: Spacing.lg,
                        },
                      ]}
                    >
                      <ProductCardSkeleton numColumns={numColumns} />
                    </View>
                  ))}
                </View>
              ) : error && !isOffline ? (
                <View style={styles.errorContainer}>
                  <Ionicons name="alert-circle-outline" size={24} color={colors.destructive} />
                  <Text style={{ color: colors.destructive, marginLeft: Spacing.sm, flex: 1 }}>{error}</Text>
                  <TouchableOpacity onPress={handleRetry} style={styles.retryTextButton}>
                    <Text style={{ color: colors.primary, fontWeight: FontWeight.semibold }}>Retry</Text>
                  </TouchableOpacity>
                </View>
              ) : isOffline ? (
                <View style={styles.offlineContainer}>
                  <Ionicons name="cloud-offline-outline" size={48} color={colors.mutedForeground} />
                  <Text style={[styles.offlineText, { color: colors.mutedForeground }]}>
                    Waiting for connection...
                  </Text>
                </View>
              ) : (
                <View style={styles.emptyContainer}>
                  <View style={[styles.emptyIcon, { backgroundColor: colors.muted }]}>
                    <Ionicons name="search-outline" size={44} color={colors.mutedForeground} />
                  </View>
                  <Text style={[styles.emptyTitle, { color: colors.foreground }]}>No products found</Text>
                  <Text style={[styles.emptySubtitle, { color: colors.mutedForeground }]}>
                    Try adjusting your search or filters to find what you're looking for.
                  </Text>
                  <TouchableOpacity
                    style={[styles.emptyButton, { backgroundColor: colors.primary }]}
                    onPress={handleReset}
                    activeOpacity={0.8}
                  >
                    <Text style={[styles.emptyButtonText, { color: colors.primaryForeground }]}>Clear all filters</Text>
                  </TouchableOpacity>
                </View>
              )
            }
            renderItem={({ item, index }) => {
              const isGrid = numColumns > 1;
              const gapSize = Platform.OS === 'android' ? Spacing.md : Spacing.lg;

              const id = String((item as any)?.id ?? '');
              const selected = selectedItems.has(id);

              const forceKey =
                Platform.OS === 'android'
                  ? `${id}-${isSelectionMode ? 1 : 0}-${selected ? 1 : 0}-${selectionTick}`
                  : id;

              return (
                <View
                  style={[
                    isGrid && {
                      width: `${100 / numColumns}%`,
                      paddingRight: index % numColumns === numColumns - 1 ? 0 : gapSize / 2,
                      paddingLeft: index % numColumns === 0 ? 0 : gapSize / 2,
                      marginBottom: Spacing.lg,
                      flexGrow: 0,
                      flexShrink: 0,
                    },
                    !isGrid && {
                      width: '100%',
                      marginBottom: Spacing.lg,
                    },
                  ]}
                  collapsable={false}
                >
                  <SelectableProductCard
                    key={forceKey}
                    product={item}
                    numColumns={numColumns}
                    isSelectionMode={isSelectionMode}
                    isSelected={selected}
                    onPress={handleCardPress}
                    onLongPress={handleCardLongPress}
                  />
                </View>
              );
            }}

            showsVerticalScrollIndicator={false}
            keyboardShouldPersistTaps="handled"
            keyboardDismissMode="on-drag"
            maxToRenderPerBatch={Platform.OS === 'android' ? 10 : 15}
            updateCellsBatchingPeriod={Platform.OS === 'android' ? 50 : 30}
            initialNumToRender={8}
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
            ListFooterComponent={
              !loading && !error && gridProducts.length > 0 ? (
                <View style={styles.footerWrap}>
                  <LoadMoreCard
                    onPress={loadMoreProducts}
                    loading={loadingMore}
                    hasMore={hasMore}
                    currentPage={currentPage}
                    totalPages={totalPages}
                  />
                </View>
              ) : null
            }
            onEndReachedThreshold={0.4}
            onEndReached={() => {
              if (hasMore && !loadingMore && !loading && !error) loadMoreProducts();
            }}
          />

          {isSelectionMode && selectedItems.size > 0 && (
            <View style={[styles.floatingBar, colorScheme === 'dark' ? Glass.card : { backgroundColor: colors.card }]}>
              <TouchableOpacity
                style={[
                  styles.floatingButton,
                  { backgroundColor: newToWishlistCount > 0 ? colors.primary : colors.muted }
                ]}
                onPress={handleAddSelectedToWishlist}
                activeOpacity={0.85}
                disabled={newToWishlistCount === 0}
              >
                <Ionicons name="heart" size={18} color="#fff" />
                <Text style={[styles.floatingButtonText, { color: '#fff' }]}>
                  {newToWishlistCount > 0
                    ? `Add to Wishlist (${newToWishlistCount})`
                    : 'Already in Wishlist'
                  }
                </Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      </TouchableWithoutFeedback>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  /* ===== FULL-BLEED HERO ===== */
  heroWrapper: {
    width: '100%',
    minHeight: 280,
    backgroundColor: '#0B1120',
    position: 'relative',
    overflow: 'hidden',
    // NO borderRadius, NO margin — full-bleed
  },
  heroOrbGreen: {
    position: 'absolute',
    top: -40,
    right: -30,
    width: 250,
    height: 250,
    borderRadius: 125,
    backgroundColor: 'rgba(16,185,129,0.18)',
    ...Platform.select({
      web: { filter: 'blur(60px)' } as any,
      default: { opacity: 0.5 },
    }),
  },
  heroOrbPurple: {
    position: 'absolute',
    bottom: 20,
    left: -40,
    width: 200,
    height: 200,
    borderRadius: 100,
    backgroundColor: 'rgba(99,102,241,0.15)',
    ...Platform.select({
      web: { filter: 'blur(60px)' } as any,
      default: { opacity: 0.4 },
    }),
  },
  heroOrbGold: {
    position: 'absolute',
    top: 40,
    left: '35%',
    width: 150,
    height: 150,
    borderRadius: 75,
    backgroundColor: 'rgba(251,191,36,0.10)',
    ...Platform.select({
      web: { filter: 'blur(60px)' } as any,
      default: { opacity: 0.3 },
    }),
  },

  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.lg,
    paddingBottom: Spacing.sm,
    zIndex: 2,
  },
  topBarWeb: {
    alignSelf: 'center',
    width: '100%',
  },

  logoContainer: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  logoIcon: {
    width: 36,
    height: 36,
    borderRadius: BorderRadius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadow.soft,
  },
  logoText: { fontSize: FontSize.xl, fontWeight: FontWeight.bold, color: '#F1F5F9' },
  logoTextAccent: { fontSize: FontSize.xl, fontWeight: FontWeight.bold, marginLeft: -2, color: '#10B981' },

  headerButtons: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm },
  headerIconButton: {
    borderRadius: BorderRadius.full,
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.08)',
  },
  headerIconButtonWeb: {
    width: 44,
    height: 44,
  },

  badge: {
    position: 'absolute',
    top: -3,
    right: -3,
    borderRadius: BorderRadius.full,
    minWidth: 18,
    height: 18,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 4,
    backgroundColor: '#10B981',
  },
  badgeText: { color: '#fff', fontSize: 10, fontWeight: '700' },

  heroContent: {
    paddingHorizontal: Spacing.xl,
    paddingTop: Spacing.lg,
    paddingBottom: Spacing['5xl'],
    zIndex: 2,
  },
  heroTitle: {
    fontSize: FontSize['5xl'],
    fontWeight: FontWeight.extrabold,
    color: '#F1F5F9',
    lineHeight: FontSize['5xl'] * 1.1,
    letterSpacing: -1,
    textAlign: 'left',
  },
  heroTitleWeb: {
    fontSize: FontSize['6xl'],
    lineHeight: FontSize['6xl'] * 1.05,
  },
  heroSubtitle: {
    fontSize: FontSize.base,
    color: 'rgba(148,163,184,0.8)',
    marginTop: Spacing.sm,
    letterSpacing: 0.3,
    textAlign: 'left',
  },

  /* ===== FLOATING STATS BAR ===== */
  floatingStatsContainer: {
    paddingHorizontal: Spacing.lg,
    marginTop: -40, // overlaps hero bottom edge
    zIndex: 3,
  },
  floatingStats: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderRadius: BorderRadius['2xl'],
    paddingVertical: Spacing.lg,
    paddingHorizontal: Spacing.md,
  },
  statItem: {
    alignItems: 'center',
    flex: 1,
  },
  statNumber: {
    fontSize: FontSize['2xl'],
    fontWeight: FontWeight.bold,
  },
  statUpperLabel: {
    fontSize: 11,
    fontWeight: FontWeight.semibold,
    letterSpacing: 1,
    color: 'rgba(148,163,184,0.7)',
    marginTop: 2,
  },
  statSeparator: {
    width: 1,
    height: 32,
    opacity: 0.3,
  },

  /* ===== SEARCH ===== */
  searchSection: {
    paddingVertical: Spacing.md,
    zIndex: 9999,
    elevation: 20,
    paddingHorizontal: Spacing.lg,
  },
  searchSectionWeb: {
    width: '100%',
    alignSelf: 'center',
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.md,
    paddingBottom: Spacing.sm,
  },

  /* ===== FILTER SECTION ===== */
  filterSection: {
    paddingHorizontal: 0,
    paddingTop: Spacing.xs,
    paddingBottom: Spacing.md,
    gap: Spacing.sm,
  },
  filterSectionWeb: {
    width: '100%',
    alignSelf: 'center',
    paddingHorizontal: 0,
  },

  /* ===== FEATURED PRODUCT SPOTLIGHT ===== */
  featuredContainer: {
    paddingHorizontal: Spacing.lg,
    marginBottom: Spacing.sm,
  },
  featuredCard: {
    flexDirection: 'row',
    borderRadius: BorderRadius['2xl'],
    overflow: 'hidden',
    height: 200,
  },
  featuredImage: {
    width: '45%',
    height: '100%',
  },
  featuredImageOverlay: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    left: '25%',
    width: '35%',
  },
  featuredContent: {
    flex: 1,
    padding: Spacing.lg,
    justifyContent: 'center',
    gap: Spacing.xs,
  },
  featuredLabel: {
    fontSize: 11,
    fontWeight: FontWeight.bold,
    letterSpacing: 2,
    color: '#10B981',
  },
  featuredName: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
    color: '#F1F5F9',
    lineHeight: FontSize.lg * 1.3,
  },
  featuredRatingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  featuredRating: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.bold,
    color: '#FBBF24',
  },
  featuredReviewCount: {
    fontSize: FontSize.xs,
    color: 'rgba(148,163,184,0.7)',
  },
  featuredPriceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: Spacing.xs,
  },
  featuredPrice: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
    color: '#10B981',
  },

  /* ===== LIST ===== */
  listContent: {
    flexGrow: 1,
    paddingBottom: Spacing.lg,
  },
  webListContent: {
    width: '100%',
    alignSelf: 'center',
    paddingHorizontal: Spacing.lg,
    flexGrow: 1,
    paddingBottom: Spacing.lg,
  },
  columnWrap: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },

  skeletonGrid: {
    paddingTop: Spacing.md,
  },
  skeletonGridMultiCol: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  errorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: Spacing.lg,
    margin: Spacing.lg,
    borderRadius: BorderRadius.lg,
  },
  retryTextButton: {
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    borderRadius: BorderRadius.md,
  },
  footerWrap: {
    width: '100%',
    paddingTop: Spacing.md,
    paddingBottom: Spacing['3xl'],
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

  offlineContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: Spacing['2xl'],
    marginTop: Spacing['3xl'],
  },
  offlineText: {
    fontSize: FontSize.base,
    marginTop: Spacing.md,
    textAlign: 'center',
  },

  emptyContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: Spacing['2xl'],
    marginTop: Spacing['5xl'],
  },
  emptyIcon: {
    width: 80,
    height: 80,
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.lg,
  },
  emptyTitle: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
    marginBottom: Spacing.sm,
    textAlign: 'center',
  },
  emptySubtitle: {
    fontSize: FontSize.base,
    textAlign: 'center',
    marginBottom: Spacing['2xl'],
    paddingHorizontal: Spacing.xl,
  },
  emptyButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.full,
  },
  emptyButtonText: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },
});
