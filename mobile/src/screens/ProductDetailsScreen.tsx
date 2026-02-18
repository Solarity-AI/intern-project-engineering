// ProductDetailsScreen.tsx — v3 Radical Redesign
// Cinematic hero image, overlapping info bar, AI banner, accent lines

import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  View,
  Text,
  Image,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Animated,
  useWindowDimensions,
  Platform,
  DeviceEventEmitter,
} from 'react-native';
import { useRoute, useNavigation, RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { StarRating } from '../components/StarRating';
import { RatingBreakdown } from '../components/RatingBreakdown';
import { ReviewCard } from '../components/ReviewCard';
import { Button } from '../components/Button';
import { AddReviewModal } from '../components/AddReviewModal';
import { AISummaryCard } from '../components/AISummaryCard';
import { LoadMoreCard } from '../components/LoadMoreCard';
import { GradientDivider } from '../components/GradientDivider';
import { SectionHeader } from '../components/SectionHeader';
import { SkeletonLoader } from '../components/SkeletonLoader';

import { useWishlist } from '../context/WishlistContext';
import { useNotifications } from '../context/NotificationContext';
import { useTheme } from '../context/ThemeContext';
import { ToastProvider, useToast } from '../context/ToastContext';

import { RootStackParamList, Review } from '../types';
import { Spacing, FontSize, FontWeight, BorderRadius, Shadow, Glass, Gradients, Glow } from '../constants/theme';
import { getProduct, postReview, getReviews, markReviewAsHelpful, getUserVotedReviews, ApiReview, getUserMessage } from '../services/api';

type RouteType = RouteProp<RootStackParamList, 'ProductDetails'>;

const mapApiReviewToReview = (apiReview: ApiReview, productId: string): Review => ({
  id: String(apiReview.id ?? Date.now()),
  productId: productId,
  userName: apiReview.reviewerName || 'Anonymous',
  rating: apiReview.rating,
  comment: apiReview.comment,
  createdAt: apiReview.createdAt || new Date().toISOString(),
  helpfulCount: apiReview.helpfulCount ?? 0,
});

const ProductDetailsContent: React.FC = () => {
  const route = useRoute<RouteType>();
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { colors, colorScheme } = useTheme();
  const { showToast } = useToast();
  const { isInWishlist, toggleWishlist } = useWishlist();
  const { addNotification } = useNotifications();

  const { width: windowWidth } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  const MAX_CONTENT_WIDTH = 600;
  const isWideScreen = windowWidth > MAX_CONTENT_WIDTH;

  const contentWidth = isWeb && isWideScreen ? MAX_CONTENT_WIDTH : windowWidth;
  const horizontalPadding = isWeb && isWideScreen ? (windowWidth - MAX_CONTENT_WIDTH) / 2 : 0;

  const scrollViewRef = useRef<ScrollView>(null);
  const reviewsSectionRef = useRef<View>(null);
  const heroImageOpacity = useRef(new Animated.Value(0)).current;
  const onHeroImageLoad = useCallback(() => {
    Animated.timing(heroImageOpacity, {
      toValue: 1,
      duration: 400,
      useNativeDriver: Platform.OS !== 'web',
    }).start();
  }, [heroImageOpacity]);

  const productId = route.params?.productId ?? '';
  const routeImageUrl = route.params?.imageUrl;
  const routeName = route.params?.name;

  const [product, setProduct] = useState<any>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [helpfulReviews, setHelpfulReviews] = useState<string[]>([]);
  const [selectedRating, setSelectedRating] = useState<number | null>(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [totalPages, setTotalPages] = useState(0);

  const inWishlist = isInWishlist(productId);

  useEffect(() => {
    fetchProduct();
    fetchUserVotes();
    fetchReviews(0, false);
  }, [productId, selectedRating]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const data = await getProduct(productId);
      setProduct(data);
    } catch (error: unknown) {
      showToast({
        type: 'error',
        title: 'Error',
        message: getUserMessage(error),
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchReviews = async (pageNum: number = 0, append: boolean = false) => {
    try {
      if (append) {
        setLoadingMore(true);
      }

      const reviewsData = await getReviews(productId, {
        page: pageNum,
        size: 10,
        rating: selectedRating
      });

      const newReviews: Review[] = (reviewsData.content || []).map(
        (apiReview: ApiReview) => mapApiReviewToReview(apiReview, productId)
      );

      if (append) {
        setReviews(prev => [...prev, ...newReviews]);
      } else {
        setReviews(newReviews);
      }

      setCurrentPage(pageNum);
      setTotalPages(reviewsData.totalPages);
      setHasMore(!reviewsData.last);
    } catch (error: any) {
      console.error('Error fetching reviews:', error);
    } finally {
      setLoadingMore(false);
    }
  };

  const loadMoreReviews = () => {
    if (!loadingMore && hasMore) {
      fetchReviews(currentPage + 1, true);
    }
  };

  const fetchUserVotes = async () => {
    try {
      const votedIds = await getUserVotedReviews();
      setHelpfulReviews(votedIds.map(String));
    } catch (error) {
      console.error('Error fetching user votes:', error);
    }
  };

  const handleSubmitReview = async (reviewData: {
    userName: string;
    rating: number;
    comment: string;
  }) => {
    try {
      await postReview(productId, {
        reviewerName: reviewData.userName,
        rating: reviewData.rating,
        comment: reviewData.comment,
      });

      showToast({
        type: 'success',
        title: 'Review submitted!',
        message: 'Thank you for your feedback.',
      });

      addNotification({
        type: 'review',
        title: 'Review Posted',
        body: `Your review for ${displayName} has been published.`,
        data: { productId, productName: displayName },
      });

      DeviceEventEmitter.emit('reviewAdded', { productId });

      await fetchProduct();
      await fetchReviews(0, false);

    } catch (error: unknown) {
      showToast({
        type: 'error',
        title: 'Error',
        message: getUserMessage(error),
      });
    }
  };

  const handleHelpfulPress = useCallback(async (reviewId: string) => {
    const reviewIdStr = String(reviewId);
    const isAlreadyHelpful = helpfulReviews.includes(reviewIdStr);

    if (isAlreadyHelpful) {
      setHelpfulReviews(prev => prev.filter(id => id !== reviewIdStr));
      setReviews(prev => prev.map(r =>
        String(r.id) === reviewIdStr
          ? { ...r, helpfulCount: Math.max(0, (r.helpfulCount || 0) - 1) }
          : r
      ));
    } else {
      setHelpfulReviews(prev => [...prev, reviewIdStr]);
      setReviews(prev => prev.map(r =>
        String(r.id) === reviewIdStr
          ? { ...r, helpfulCount: (r.helpfulCount || 0) + 1 }
          : r
      ));
    }

    try {
      await markReviewAsHelpful(reviewIdStr);
    } catch (error) {
      console.error('Error toggling review vote:', error);
      if (isAlreadyHelpful) {
        setHelpfulReviews(prev => [...prev, reviewIdStr]);
        setReviews(prev => prev.map(r =>
          String(r.id) === reviewIdStr
            ? { ...r, helpfulCount: (r.helpfulCount || 0) + 1 }
            : r
        ));
      } else {
        setHelpfulReviews(prev => prev.filter(id => id !== reviewIdStr));
        setReviews(prev => prev.map(r =>
          String(r.id) === reviewIdStr
            ? { ...r, helpfulCount: Math.max(0, (r.helpfulCount || 0) - 1) }
            : r
        ));
      }
    }
  }, [helpfulReviews]);

  const handleWishlistToggle = () => {
    if (!product) return;

    toggleWishlist({
      id: productId,
      name: product.name || 'Product',
      price: product.price,
      imageUrl: product.imageUrl || routeImageUrl,
      categories: product.categories,
      averageRating: product.averageRating,
    } as any);

    showToast({
      type: inWishlist ? 'info' : 'success',
      title: inWishlist ? 'Removed from wishlist' : 'Added to wishlist',
      message: inWishlist
        ? `${product.name || 'Product'} removed from your wishlist`
        : `${product.name || 'Product'} added to your wishlist`,
    });
  };

  const handleAIAssistant = () => {
    navigation.navigate('AIAssistant' as any, {
      productName: displayName,
      productId: productId,
      reviews: reviews,
    });
  };

  const handleBack = () => {
    if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('ProductList' as any);
    }
  };

  if (loading && !product) {
    return (
      <ScreenWrapper backgroundColor={colors.background}>
        <ScrollView showsVerticalScrollIndicator={false}>
          {/* Hero image skeleton */}
          <SkeletonLoader width="100%" height={400} borderRadius={0} />

          {/* Info bar skeleton */}
          <View style={styles.infoBarContainer}>
            <View style={[
              styles.infoBar,
              colorScheme === 'dark' ? Glass.elevated : { backgroundColor: colors.card, ...Shadow.medium },
            ]}>
              {[0, 1, 2].map(i => (
                <React.Fragment key={i}>
                  {i > 0 && <View style={[styles.infoBarSep, { backgroundColor: colors.border }]} />}
                  <View style={styles.infoBarItem}>
                    <SkeletonLoader width={18} height={18} borderRadius={9} />
                    <SkeletonLoader width={40} height={20} borderRadius={BorderRadius.sm} />
                    <SkeletonLoader width={48} height={12} borderRadius={BorderRadius.sm} />
                  </View>
                </React.Fragment>
              ))}
            </View>
          </View>

          {/* Description skeleton */}
          <View style={styles.section}>
            <SkeletonLoader width="100%" height={14} borderRadius={BorderRadius.sm} />
            <SkeletonLoader width="90%" height={14} borderRadius={BorderRadius.sm} />
            <SkeletonLoader width="70%" height={14} borderRadius={BorderRadius.sm} />
          </View>

          {/* AI banner skeleton */}
          <View style={styles.section}>
            <SkeletonLoader width="100%" height={64} borderRadius={BorderRadius.xl} />
          </View>

          {/* Reviews skeleton */}
          <View style={styles.section}>
            <SkeletonLoader width={150} height={22} borderRadius={BorderRadius.sm} />
            {[0, 1].map(i => (
              <View key={i} style={{ gap: Spacing.sm, marginTop: Spacing.md }}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: Spacing.sm }}>
                  <SkeletonLoader width={36} height={36} borderRadius={18} />
                  <View style={{ flex: 1, gap: 4 }}>
                    <SkeletonLoader width={100} height={14} borderRadius={BorderRadius.sm} />
                    <SkeletonLoader width={80} height={12} borderRadius={BorderRadius.sm} />
                  </View>
                </View>
                <SkeletonLoader width="100%" height={14} borderRadius={BorderRadius.sm} />
                <SkeletonLoader width="80%" height={14} borderRadius={BorderRadius.sm} />
              </View>
            ))}
          </View>
        </ScrollView>
      </ScreenWrapper>
    );
  }

  if (!product) {
    return (
      <ScreenWrapper backgroundColor={colors.background}>
        <View style={styles.errorContainer}>
          <Ionicons name="alert-circle-outline" size={64} color={colors.mutedForeground} />
          <Text style={[styles.errorText, { color: colors.foreground }]}>
            Product not found
          </Text>
          <Button onPress={handleBack}>Go Back</Button>
        </View>
      </ScreenWrapper>
    );
  }

  const displayName = product.name ?? routeName ?? 'Product';
  const imageUrl = product.imageUrl || routeImageUrl;

  const responsiveContainerStyle = {
    width: '100%' as const,
    maxWidth: isWeb ? MAX_CONTENT_WIDTH : undefined,
    alignSelf: 'center' as const,
  };

  return (
    <ScreenWrapper backgroundColor={colors.background}>
      <ScrollView
        ref={scrollViewRef}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={isWeb && isWideScreen ? { alignItems: 'center' } : undefined}
      >
        <View style={responsiveContainerStyle}>
          {/* ===== CINEMATIC HERO IMAGE ===== */}
          {imageUrl && (
            <View style={[
              styles.heroImage,
              isWeb && { borderRadius: 0 },
            ]}>
              <Animated.Image
                source={{ uri: imageUrl }}
                style={[styles.heroImg, { opacity: heroImageOpacity }]}
                resizeMode="cover"
                onLoad={onHeroImageLoad}
              />
              {/* Top gradient for buttons */}
              <LinearGradient
                colors={['rgba(11,17,32,0.6)', 'transparent'] as [string, string]}
                style={styles.heroTopGradient}
              />
              {/* Bottom 50% gradient for info */}
              <LinearGradient
                colors={['transparent', 'rgba(11,17,32,0.85)'] as [string, string]}
                style={styles.heroBottomGradient}
              />

              {/* Back button — top left glass circle */}
              <TouchableOpacity
                style={[styles.heroBackButton, Glass.strong]}
                onPress={handleBack}
              >
                <Ionicons name="chevron-back" size={22} color="#fff" />
              </TouchableOpacity>

              {/* Wishlist button — top right glass circle */}
              <TouchableOpacity
                onPress={handleWishlistToggle}
                style={[styles.heroWishlistButton, Glass.strong]}
              >
                <Ionicons
                  name={inWishlist ? 'heart' : 'heart-outline'}
                  size={24}
                  color={inWishlist ? '#F87171' : '#fff'}
                />
              </TouchableOpacity>

              {/* Overlaid product info at bottom */}
              <View style={styles.heroOverlayInfo}>
                {product.categories && product.categories.length > 0 && (
                  <View style={styles.heroCategoryRow}>
                    {product.categories.map((cat: string) => (
                      <View key={cat} style={styles.heroCategoryPill}>
                        <Text style={styles.heroCategoryText}>{cat}</Text>
                      </View>
                    ))}
                  </View>
                )}
                <Text style={styles.heroProductName}>{displayName}</Text>
                <View style={styles.heroPriceRatingRow}>
                  <Text style={styles.heroPrice}>${(product.price ?? 0).toFixed(2)}</Text>
                  {product.averageRating !== undefined && (
                    <View style={styles.heroRatingChip}>
                      <Ionicons name="star" size={14} color="#FBBF24" />
                      <Text style={styles.heroRatingText}>{product.averageRating.toFixed(1)}</Text>
                    </View>
                  )}
                </View>
              </View>
            </View>
          )}

          {/* No image — show back button normally */}
          {!imageUrl && (
            <TouchableOpacity style={styles.plainBackButton} onPress={handleBack}>
              <Ionicons name="chevron-back" size={20} color={colors.foreground} />
              <Text style={[styles.plainBackText, { color: colors.foreground }]}>Back</Text>
            </TouchableOpacity>
          )}

          {/* ===== OVERLAPPING INFO BAR ===== */}
          <View style={styles.infoBarContainer}>
            <View style={[
              styles.infoBar,
              colorScheme === 'dark' ? Glass.elevated : { backgroundColor: colors.card, ...Shadow.medium },
            ]}>
              <View style={styles.infoBarItem}>
                <Ionicons name="star" size={18} color="#FBBF24" />
                <Text style={[styles.infoBarValue, { color: colors.foreground }]}>
                  {(product.averageRating ?? 0).toFixed(1)}
                </Text>
                <Text style={styles.infoBarLabel}>Rating</Text>
              </View>
              <View style={[styles.infoBarSep, { backgroundColor: colors.border }]} />
              <View style={styles.infoBarItem}>
                <Ionicons name="chatbubbles" size={18} color="#10B981" />
                <Text style={[styles.infoBarValue, { color: colors.foreground }]}>
                  {product.reviewCount || 0}
                </Text>
                <Text style={styles.infoBarLabel}>Reviews</Text>
              </View>
              <View style={[styles.infoBarSep, { backgroundColor: colors.border }]} />
              <View style={styles.infoBarItem}>
                <Ionicons name="pricetag" size={18} color="#10B981" />
                <Text style={[styles.infoBarValue, { color: colors.foreground }]}>
                  ${(product.price ?? 0).toFixed(0)}
                </Text>
                <Text style={styles.infoBarLabel}>Price</Text>
              </View>
            </View>
          </View>

          {/* Description */}
          {product.description && (
            <View style={styles.section}>
              <Text style={[styles.description, { color: colors.foreground }]}>
                {product.description}
              </Text>
            </View>
          )}

          {/* ===== AI ASSISTANT BANNER ===== */}
          <View style={styles.section}>
            <TouchableOpacity
              onPress={handleAIAssistant}
              activeOpacity={0.85}
              style={styles.aiBanner}
            >
              <LinearGradient
                colors={Gradients.ai as [string, string]}
                start={{ x: 0, y: 0 }}
                end={{ x: 1, y: 0 }}
                style={[styles.aiBannerGradient, Glow.ai]}
              >
                <Ionicons name="sparkles" size={22} color="#fff" />
                <Text style={styles.aiBannerText}>Ask AI about this product</Text>
                <Ionicons name="arrow-forward" size={20} color="rgba(255,255,255,0.7)" />
              </LinearGradient>
            </TouchableOpacity>
          </View>

          {/* AI Summary Section */}
          {product.aiSummary && (
            <View style={styles.section}>
              <AISummaryCard summary={product.aiSummary} />
            </View>
          )}

          <GradientDivider />

          {/* Rating Breakdown */}
          <View style={styles.section}>
            <SectionHeader title="Rating Breakdown" accentColor="#FBBF24" />
            <View style={{ marginTop: Spacing.md }}>
              <RatingBreakdown
                breakdown={product.ratingBreakdown}
                totalCount={product.reviewCount}
                selectedRating={selectedRating}
                onSelectRating={setSelectedRating}
              />
            </View>
          </View>

          <GradientDivider />

          {/* Reviews Section */}
          <View ref={reviewsSectionRef} style={styles.section} collapsable={false}>
            <View style={styles.reviewsHeader}>
              <SectionHeader
                title={`Reviews ${selectedRating !== null ? `(${selectedRating}★)` : ''}`}
                accentColor="#10B981"
              />
              <Button variant="premium" onPress={() => setIsReviewModalOpen(true)}>
                Add Review
              </Button>
            </View>

            {reviews.length === 0 ? (
              <Text style={{ color: colors.mutedForeground, marginTop: 8 }}>
                {selectedRating !== null
                  ? `No ${selectedRating}★ reviews found.`
                  : 'No reviews yet. Be the first to review!'}
              </Text>
            ) : (
              <View style={{ marginTop: Spacing.md, gap: Spacing.md }}>
                {reviews.map((r) => (
                  <ReviewCard
                    key={r.id}
                    review={r}
                    onHelpfulPress={handleHelpfulPress}
                    isHelpful={helpfulReviews.includes(String(r.id))}
                  />
                ))}

                <LoadMoreCard
                  onPress={loadMoreReviews}
                  loading={loadingMore}
                  hasMore={hasMore}
                  currentPage={currentPage}
                  totalPages={totalPages}
                />
              </View>
            )}
          </View>
        </View>
      </ScrollView>

      <AddReviewModal
        visible={isReviewModalOpen}
        onClose={() => setIsReviewModalOpen(false)}
        productName={displayName}
        onSubmit={handleSubmitReview}
      />
    </ScreenWrapper>
  );
};

export const ProductDetailsScreen: React.FC = () => {
  return (
    <ToastProvider>
      <ProductDetailsContent />
    </ToastProvider>
  );
};

const styles = StyleSheet.create({
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: Spacing.lg,
    paddingHorizontal: Spacing['2xl'],
  },
  errorText: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
  },

  /* ===== CINEMATIC HERO ===== */
  heroImage: {
    position: 'relative',
    width: '100%',
    aspectRatio: 4 / 5,
    maxHeight: 500,
    overflow: 'hidden',
  },
  heroImg: {
    width: '100%',
    height: '100%',
  },
  heroTopGradient: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: 120,
  },
  heroBottomGradient: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: '50%',
  },
  heroBackButton: {
    position: 'absolute',
    top: Spacing.lg,
    left: Spacing.lg,
    width: 44,
    height: 44,
    borderRadius: 22,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  heroWishlistButton: {
    position: 'absolute',
    top: Spacing.lg,
    right: Spacing.lg,
    width: 44,
    height: 44,
    borderRadius: 22,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  heroOverlayInfo: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    padding: Spacing.xl,
    gap: Spacing.sm,
  },
  heroCategoryRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.xs,
  },
  heroCategoryPill: {
    backgroundColor: 'rgba(255,255,255,0.15)',
    paddingHorizontal: Spacing.sm,
    paddingVertical: 3,
    borderRadius: BorderRadius.full,
  },
  heroCategoryText: {
    color: 'rgba(255,255,255,0.85)',
    fontSize: 11,
    fontWeight: FontWeight.semibold,
    textTransform: 'uppercase',
  },
  heroProductName: {
    color: '#fff',
    fontSize: FontSize['4xl'],
    fontWeight: FontWeight.bold,
    lineHeight: FontSize['4xl'] * 1.1,
  },
  heroPriceRatingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
  },
  heroPrice: {
    color: '#10B981',
    fontSize: FontSize['2xl'],
    fontWeight: FontWeight.bold,
  },
  heroRatingChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: 'rgba(255,255,255,0.12)',
    paddingHorizontal: Spacing.sm,
    paddingVertical: 4,
    borderRadius: BorderRadius.full,
  },
  heroRatingText: {
    color: '#FBBF24',
    fontSize: FontSize.sm,
    fontWeight: FontWeight.bold,
  },

  plainBackButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
  },
  plainBackText: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.medium,
  },

  /* ===== OVERLAPPING INFO BAR ===== */
  infoBarContainer: {
    paddingHorizontal: Spacing.lg,
    marginTop: -20,
    zIndex: 3,
  },
  infoBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderRadius: BorderRadius['2xl'],
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
  },
  infoBarItem: {
    alignItems: 'center',
    flex: 1,
    gap: 2,
  },
  infoBarValue: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
  },
  infoBarLabel: {
    fontSize: 11,
    color: 'rgba(148,163,184,0.7)',
    fontWeight: FontWeight.medium,
  },
  infoBarSep: {
    width: 1,
    height: 28,
    opacity: 0.3,
  },

  description: {
    fontSize: FontSize.base,
    lineHeight: FontSize.base * 1.6,
  },

  section: {
    padding: Spacing.lg,
    gap: Spacing.md,
  },

  /* ===== AI BANNER ===== */
  aiBanner: {
    width: '100%',
  },
  aiBannerGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.md,
    height: 64,
    borderRadius: BorderRadius.xl,
  },
  aiBannerText: {
    color: '#fff',
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
    flex: 1,
  },

  reviewsHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing.md,
  },
});
