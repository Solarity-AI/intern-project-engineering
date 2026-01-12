// ProductDetailsScreen.tsx
// Product details with reviews, ratings, AI Summary, and AI Assistant navigation

import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  View,
  Text,
  Image,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
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

import { useWishlist } from '../context/WishlistContext';
import { useNotifications } from '../context/NotificationContext';
import { useTheme } from '../context/ThemeContext';
import { ToastProvider, useToast } from '../context/ToastContext';

import { RootStackParamList, Review } from '../types';
import { Spacing, FontSize, FontWeight, BorderRadius, Shadow } from '../constants/theme';
import { getProduct, postReview, getReviews, markReviewAsHelpful, getUserVotedReviews } from '../services/api';

type RouteType = RouteProp<RootStackParamList, 'ProductDetails'>;

const ProductDetailsContent: React.FC = () => {
  const route = useRoute<RouteType>();
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { colors, colorScheme } = useTheme();
  const { showToast } = useToast();
  const { isInWishlist, toggleWishlist } = useWishlist();
  const { addNotification } = useNotifications();

  const scrollViewRef = useRef<ScrollView>(null);
  const reviewsSectionRef = useRef<View>(null);

  const productId = route.params?.productId ?? '';
  const routeImageUrl = route.params?.imageUrl;
  const routeName = route.params?.name;

  const [product, setProduct] = useState<any>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [helpfulReviews, setHelpfulReviews] = useState<string[]>([]);
  const [selectedRating, setSelectedRating] = useState<number | null>(null);

  const inWishlist = isInWishlist(productId);
  // Wishlist button styling - theme-aware
  const wishlistButtonBg = inWishlist
    ? colors.primary
    : colorScheme === 'dark'
      ? 'rgba(28, 25, 23, 0.9)'
      : 'rgba(255, 255, 255, 0.9)';
  const wishlistIconColor = inWishlist ? '#fff' : colors.foreground;

  useEffect(() => {
    fetchProduct();
    fetchUserVotes();
  }, [productId]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const data = await getProduct(productId);
      setProduct(data);
      
      // Fetch reviews separately
      const reviewsData = await getReviews(productId);
      setReviews(reviewsData.content || []);
    } catch (error: any) {
      showToast({
        type: 'error',
        title: 'Error',
        message: error.message || 'Failed to load product',
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchUserVotes = async () => {
    try {
      console.log('Fetching user votes...');
      const votedIds = await getUserVotedReviews();
      console.log('Voted IDs from backend:', votedIds);
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
      const newReview = await postReview(productId, {
        reviewerName: reviewData.userName,
        rating: reviewData.rating,
        comment: reviewData.comment,
      });
      
      setReviews(prev => [newReview as any, ...prev]);
      
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

      await fetchProduct();
    } catch (error: any) {
      showToast({
        type: 'error',
        title: 'Error',
        message: error.message || 'Failed to submit review',
      });
    }
  };

  const handleHelpfulPress = useCallback(async (reviewId: string) => {
    // Optimistic update
    const isAlreadyHelpful = helpfulReviews.includes(reviewId);
    
    if (isAlreadyHelpful) {
      // If already voted, we don't support un-voting in UI yet (backend doesn't support it either)
      return;
    }

    setHelpfulReviews(prev => [...prev, reviewId]);
    
    // Update review count locally
    setReviews(prev => prev.map(r => 
      r.id === reviewId 
        ? { ...r, helpfulCount: (r.helpfulCount || 0) + 1 } 
        : r
    ));

    try {
      await markReviewAsHelpful(reviewId);
      console.log('Marked review as helpful:', reviewId);
    } catch (error) {
      console.error('Error marking review as helpful:', error);
      // Revert on error
      setHelpfulReviews(prev => prev.filter(id => id !== reviewId));
      setReviews(prev => prev.map(r => 
        r.id === reviewId 
          ? { ...r, helpfulCount: (r.helpfulCount || 0) - 1 } 
          : r
      ));
    }
  }, [helpfulReviews]);

  const handleWishlistToggle = () => {
    toggleWishlist({
      id: productId,
      name: product?.name || 'Product',
      price: product?.price,
      imageUrl: product?.imageUrl || routeImageUrl,
      category: product?.category,
      averageRating: product?.averageRating,
    });

    showToast({
      type: inWishlist ? 'info' : 'success',
      title: inWishlist ? 'Removed from wishlist' : 'Added to wishlist',
      message: inWishlist
        ? `${product?.name || 'Product'} removed from your wishlist`
        : `${product?.name || 'Product'} added to your wishlist`,
    });
  };

  const handleAIAssistant = () => {
    navigation.navigate('AIAssistant' as any, {
      productName: displayName,
      productId: productId,
      reviews: reviews,
    });
  };

  const filteredReviews = useMemo(() => {
    if (selectedRating === null) return reviews;
    return reviews.filter(r => Math.floor(r.rating) === selectedRating);
  }, [reviews, selectedRating]);

  if (loading) {
    return (
      <ScreenWrapper backgroundColor={colors.background}>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
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
          <Button onPress={() => navigation.goBack()}>Go Back</Button>
        </View>
      </ScreenWrapper>
    );
  }

  const displayName = product.name ?? routeName ?? 'Product';
  const imageUrl = product.imageUrl || routeImageUrl;

  return (
    <ScreenWrapper backgroundColor={colors.background}>
      <ScrollView ref={scrollViewRef} showsVerticalScrollIndicator={false}>
        {/* Back Button */}
        <TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
          <Ionicons name="chevron-back" size={20} color={colors.foreground} />
          <Text style={[styles.backButtonText, { color: colors.foreground }]}>Back</Text>
        </TouchableOpacity>

        {/* Product Image */}
        {imageUrl && (
          <View style={styles.imageContainer}>
            <Image source={{ uri: imageUrl }} style={styles.image} resizeMode="cover" />
            
            {/* Wishlist Button */}
            <TouchableOpacity
              onPress={handleWishlistToggle}
              style={[styles.wishlistButton, {
                backgroundColor: wishlistButtonBg,
              }]}
            >
              <Ionicons
                name={inWishlist ? 'heart' : 'heart-outline'}
                size={24}
                color={wishlistIconColor}
              />
            </TouchableOpacity>
          </View>
        )}

        {/* Product Info */}
        <View style={styles.infoSection}>
          {product.category && (
            <Text style={[styles.category, { color: colors.mutedForeground }]}>
              {product.category}
            </Text>
          )}
          
          <Text style={[styles.productName, { color: colors.foreground }]}>
            {displayName}
          </Text>

          {product.price !== undefined && (
            <Text style={[styles.price, { color: colors.primary }]}>
              ${product.price.toFixed(2)}
            </Text>
          )}

          {/* Rating Summary */}
          {product.averageRating !== undefined && (
            <View style={styles.ratingRow}>
              <StarRating rating={product.averageRating} size="md" />
              <Text style={[styles.ratingText, { color: colors.foreground }]}>
                {product.averageRating.toFixed(1)}
              </Text>
              <Text style={[styles.reviewCountText, { color: colors.mutedForeground }]}>
                ({product.reviewCount || 0} reviews)
              </Text>
            </View>
          )}

          {/* Description */}
          {product.description && (
            <Text style={[styles.description, { color: colors.foreground }]}>
              {product.description}
            </Text>
          )}
        </View>

        {/* ✨ AI Summary Section */}
        {product.aiSummary && (
          <View style={styles.section}>
            <AISummaryCard summary={product.aiSummary} />
          </View>
        )}

        {/* Rating Breakdown */}
        {reviews.length > 0 && (
          <View style={styles.section}>
            <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
              Rating Breakdown
            </Text>
            <RatingBreakdown
              reviews={reviews}
              selectedRating={selectedRating}
              onSelectRating={setSelectedRating}
            />
          </View>
        )}

        {/* Reviews Section */}
        <View ref={reviewsSectionRef} style={styles.section} collapsable={false}>
          <View style={styles.reviewsHeader}>
            <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
              Reviews {selectedRating !== null ? `(${selectedRating}★)` : ''}
            </Text>

            <View style={styles.reviewActions}>
              {/* AI Assistant Button */}
              <TouchableOpacity
                onPress={handleAIAssistant}
                style={styles.aiChatButton}
                activeOpacity={0.8}
              >
                <LinearGradient
                  colors={['#8B5CF6', '#6366F1']}
                  style={styles.aiChatGradient}
                >
                  <Ionicons name="chatbubbles" size={20} color="#fff" />
                </LinearGradient>
              </TouchableOpacity>

              {/* Add Review Button */}
              <Button variant="premium" onPress={() => setIsReviewModalOpen(true)}>
                Add Review
              </Button>
            </View>
          </View>

          {filteredReviews.length === 0 ? (
            <Text style={{ color: colors.mutedForeground, marginTop: 8 }}>
              {selectedRating !== null
                ? `No ${selectedRating}★ reviews found.`
                : 'No reviews yet. Be the first to review!'}
            </Text>
          ) : (
            <View style={{ marginTop: Spacing.md, gap: Spacing.md }}>
              {filteredReviews.map((r) => (
                <ReviewCard
                  key={r.id}
                  review={r}
                  onHelpfulPress={handleHelpfulPress}
                  isHelpful={helpfulReviews.includes(r.id)}
                />
              ))}
            </View>
          )}
        </View>
      </ScrollView>

      {/* Add Review Modal */}
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
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },

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

  backButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
  },

  backButtonText: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.medium,
  },

  imageContainer: {
    position: 'relative',
    width: '100%',
    aspectRatio: 1,
    maxHeight: 400,
  },

  image: {
    width: '100%',
    height: '100%',
  },

  wishlistButton: {
    position: 'absolute',
    top: Spacing.lg,
    right: Spacing.lg,
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadow.soft,
  },

  infoSection: {
    padding: Spacing.lg,
    gap: Spacing.sm,
  },

  category: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    textTransform: 'uppercase',
  },

  productName: {
    fontSize: FontSize['2xl'],
    fontWeight: FontWeight.bold,
    lineHeight: FontSize['2xl'] * 1.3,
  },

  price: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
  },

  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
    marginTop: Spacing.sm,
  },

  ratingText: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
  },

  reviewCountText: {
    fontSize: FontSize.base,
  },

  description: {
    fontSize: FontSize.base,
    lineHeight: FontSize.base * 1.6,
    marginTop: Spacing.md,
  },

  section: {
    padding: Spacing.lg,
    gap: Spacing.md,
  },

  sectionTitle: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
  },

  reviewsHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: Spacing.md,
  },

  reviewActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },

  aiChatButton: {
    ...Shadow.soft,
  },

  aiChatGradient: {
    width: 44,
    height: 44,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
