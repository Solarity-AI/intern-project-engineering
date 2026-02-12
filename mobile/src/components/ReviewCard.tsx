// ReviewCard — v3 Radical Redesign
// 2px left accent line colored by rating

import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import { StarRating } from './StarRating';
import { Review } from '../types';
import { Spacing, FontSize, BorderRadius, FontWeight, Glass, Shadow, Gradients } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

interface ReviewCardProps {
  review: Review;
  onHelpfulPress?: (reviewId: string) => void;
  isHelpful?: boolean;
}

function getRatingAccentColor(rating: number): string {
  if (rating >= 4) return '#10B981'; // emerald
  if (rating === 3) return '#FBBF24'; // gold
  return '#F87171'; // red
}

export const ReviewCard: React.FC<ReviewCardProps> = ({ review, onHelpfulPress, isHelpful = false }) => {
  const { colors, colorScheme } = useTheme();

  const formattedDate = new Date(review.createdAt).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  const helpfulCount = review.helpfulCount ?? review.helpful ?? 0;
  const accentColor = getRatingAccentColor(review.rating);

  return (
    <View style={[
      styles.container,
      colorScheme === 'dark' ? Glass.card : Glass.cardLight,
    ]}>
      {/* 2px left accent line */}
      <View style={[styles.accentLine, { backgroundColor: accentColor }]} />

      {/* Header */}
      <View style={styles.header}>
        <View style={styles.userInfo}>
          <LinearGradient
            colors={colorScheme === 'dark' ? ['rgba(16,185,129,0.2)', 'rgba(99,102,241,0.15)'] as [string, string] : [colors.secondary, colors.secondary] as [string, string]}
            style={styles.avatar}
          >
            <Ionicons name="person" size={18} color={colorScheme === 'dark' ? '#94A3B8' : colors.mutedForeground} />
          </LinearGradient>
          <View>
            <Text style={[styles.userName, { color: colors.foreground }]}>
              {review.reviewerName || review.userName}
            </Text>
            <Text style={[styles.date, { color: colors.mutedForeground }]}>
              {formattedDate}
            </Text>
          </View>
        </View>
        <StarRating rating={review.rating} size="sm" />
      </View>

      {/* Comment */}
      <Text style={[styles.comment, { color: colors.foreground }]}>
        {review.comment}
      </Text>

      {/* Footer */}
      <TouchableOpacity
        style={[styles.helpfulButton, isHelpful && styles.helpfulButtonActive]}
        activeOpacity={0.7}
        onPress={() => onHelpfulPress && onHelpfulPress(review.id)}
        accessibilityLabel={`Mark as helpful, ${helpfulCount} people found this helpful`}
        accessibilityRole="button"
      >
        <Ionicons
          name={isHelpful ? "thumbs-up" : "thumbs-up-outline"}
          size={16}
          color={isHelpful ? colors.primary : colors.mutedForeground}
        />
        <Text style={[
          styles.helpfulText,
          { color: isHelpful ? colors.primary : colors.mutedForeground }
        ]}>
          Helpful ({helpfulCount})
        </Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    paddingLeft: Spacing.xl + 6, // extra space for accent line
    paddingRight: Spacing.xl,
    paddingVertical: Spacing.xl,
    borderRadius: BorderRadius['2xl'],
    marginBottom: Spacing.md,
    overflow: 'hidden',
    position: 'relative',
    ...Shadow.soft,
  },
  accentLine: {
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    width: 2,
    borderTopLeftRadius: BorderRadius['2xl'],
    borderBottomLeftRadius: BorderRadius['2xl'],
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: Spacing.md,
  },
  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
  },
  userName: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },
  date: {
    fontSize: FontSize.sm,
  },
  comment: {
    fontSize: FontSize.base,
    lineHeight: FontSize.base * 1.5,
    marginBottom: Spacing.lg,
  },
  helpfulButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
    paddingVertical: 4,
    paddingHorizontal: 8,
    borderRadius: BorderRadius.md,
  },
  helpfulButtonActive: {
    backgroundColor: 'rgba(16, 185, 129, 0.12)',
  },
  helpfulText: {
    fontSize: FontSize.sm,
  },
});
