// React Native AddReviewModal Component
// Modal for submitting reviews (toast validation must be visible INSIDE the modal)
// ✨ Updated: Responsive design for Web

import React, { useMemo, useState } from 'react';
import {
  View,
  Text,
  TextInput,
  Modal,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  useWindowDimensions,
  Pressable,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { StarRating } from './StarRating';
import { Button } from './Button';
import { Colors, Spacing, FontSize, BorderRadius, FontWeight, Shadow, Glass } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

// ✅ IMPORTANT: ToastProvider MUST be inside <Modal> to render in the same native layer.
import { ToastProvider, useToast } from '../context/ToastContext';

interface AddReviewModalProps {
  visible: boolean;
  onClose: () => void;
  productName: string;
  onSubmit: (review: { userName: string; rating: number; comment: string }) => void;
}

// --- Frontend validation rules (match backend if possible) ---
const MIN_REVIEW_LEN = 10;
const MAX_REVIEW_LEN = 500;

const MIN_NAME_LEN = 2; // applied only if name is provided
const MAX_NAME_LEN = 50;

const AddReviewModalContent: React.FC<Omit<AddReviewModalProps, 'visible'> & { onBackdropPress?: () => void }> = ({
  onClose,
  productName,
  onSubmit,
  onBackdropPress,
}) => {
  const { colors, colorScheme } = useTheme();
  const { showToast } = useToast();
  const { width } = useWindowDimensions();
  
  // ✨ Responsive breakpoints
  const isWeb = Platform.OS === 'web';
  const isNarrow = width < 600;
  const maxFormWidth = isWeb ? 520 : undefined;

  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [userName, setUserName] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const trimmedComment = useMemo(() => comment.trim(), [comment]);
  const trimmedName = useMemo(() => userName.trim(), [userName]);

  const commentLen = trimmedComment.length;
  const nameLen = trimmedName.length;

  const handleSubmit = async () => {
    // ❗ validation: show toast INSIDE modal layer
    if (rating === 0) {
      showToast({
        type: 'error',
        title: 'Rating required',
        message: 'Please select at least one star before submitting your review.',
      });
      return;
    }

    // --- comment validation (required) ---
    if (commentLen < MIN_REVIEW_LEN) {
      showToast({
        type: 'error',
        title: 'Review too short',
        message: `Your review must be at least ${MIN_REVIEW_LEN} characters long.`,
      });
      return;
    }

    if (commentLen > MAX_REVIEW_LEN) {
      showToast({
        type: 'error',
        title: 'Review too long',
        message: `Your review cannot exceed ${MAX_REVIEW_LEN} characters.`,
      });
      return;
    }

    // --- name validation (optional; validate only if provided) ---
    if (nameLen > 0 && nameLen < MIN_NAME_LEN) {
      showToast({
        type: 'error',
        title: 'Name too short',
        message: `Your name must be at least ${MIN_NAME_LEN} characters long (or leave it empty).`,
      });
      return;
    }

    if (nameLen > MAX_NAME_LEN) {
      showToast({
        type: 'error',
        title: 'Name too long',
        message: `Your name cannot exceed ${MAX_NAME_LEN} characters.`,
      });
      return;
    }

    setIsSubmitting(true);

    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 800));

    // Success toast is handled by the parent screen (after modal closes)
    onSubmit({
      userName: nameLen > 0 ? trimmedName : 'Anonymous',
      rating,
      comment: trimmedComment, // send trimmed comment to avoid leading/trailing spaces
    });

    // Reset form
    setRating(0);
    setComment('');
    setUserName('');
    setIsSubmitting(false);
    onClose();
  };

  const handleClose = () => {
    setRating(0);
    setComment('');
    setUserName('');
    onClose();
  };

  // ✨ Web: Centered card layout with backdrop
  if (isWeb) {
    return (
      <Pressable 
        style={styles.webBackdrop} 
        onPress={onBackdropPress}
      >
        <Pressable
          style={[
            styles.webCard,
            colorScheme === 'dark' ? Glass.card : Glass.cardLight,
            {
              maxWidth: maxFormWidth,
              width: isNarrow ? '95%' : '90%',
            }
          ]}
          onPress={(e) => e.stopPropagation()}
        >
          <ScrollView 
            showsVerticalScrollIndicator={false}
            contentContainerStyle={styles.webScrollContent}
          >
            {/* Header */}
            <View style={styles.header}>
              <View style={{ flex: 1 }}>
                <Text style={[styles.title, { color: colors.foreground }]}>Write a Review</Text>
                <Text style={[styles.subtitle, { color: colors.mutedForeground }]} numberOfLines={1}>
                  {productName}
                </Text>
              </View>

              <TouchableOpacity
                onPress={handleClose}
                style={[styles.closeButton, { backgroundColor: colors.secondary }]}
              >
                <Ionicons name="close" size={20} color={colors.mutedForeground} />
              </TouchableOpacity>
            </View>

            {/* Form */}
            <View style={styles.formWeb}>
              {/* Rating */}
              <View style={styles.field}>
                <Text style={[styles.label, { color: colors.foreground }]}>Your Rating</Text>
                <StarRating rating={rating} size="lg" interactive onRatingChange={setRating} />
              </View>

              {/* Name */}
              <View style={styles.field}>
                <Text style={[styles.label, { color: colors.foreground }]}>Your Name (optional)</Text>
                <TextInput
                  style={[styles.textInput, { backgroundColor: colors.secondary, color: colors.foreground }]}
                  value={userName}
                  onChangeText={setUserName}
                  placeholder="Enter your name"
                  placeholderTextColor={colors.mutedForeground}
                  maxLength={MAX_NAME_LEN}
                />
                <Text style={[styles.charCount, { color: colors.mutedForeground }]}>
                  {nameLen}/{MAX_NAME_LEN}
                </Text>
              </View>

              {/* Comment */}
              <View style={styles.field}>
                <Text style={[styles.label, { color: colors.foreground }]}>Your Review</Text>
                <TextInput
                  style={[styles.textArea, { backgroundColor: colors.secondary, color: colors.foreground }]}
                  value={comment}
                  onChangeText={setComment}
                  placeholder="Share your experience with this product..."
                  placeholderTextColor={colors.mutedForeground}
                  multiline
                  numberOfLines={4}
                  textAlignVertical="top"
                  maxLength={MAX_REVIEW_LEN + 50}
                />
                <Text style={[styles.charCount, { color: colors.mutedForeground }]}>
                  Minimum {MIN_REVIEW_LEN} characters ({commentLen}/{MAX_REVIEW_LEN})
                </Text>
              </View>

              {/* Buttons */}
              <View style={styles.webButtons}>
                <Button onPress={handleClose} variant="outline" style={styles.webButton}>
                  Cancel
                </Button>
                <Button onPress={handleSubmit} variant="premium" loading={isSubmitting} style={styles.webButton}>
                  Submit Review
                </Button>
              </View>
            </View>
          </ScrollView>
        </Pressable>
      </Pressable>
    );
  }

  // ✨ Mobile: Full screen layout (original)
  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={[styles.container, { backgroundColor: colors.background }]}
    >
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* Header */}
        <View style={styles.header}>
          <View>
            <Text style={[styles.title, { color: colors.foreground }]}>Write a Review</Text>
            <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>{productName}</Text>
          </View>

          <TouchableOpacity
            onPress={handleClose}
            style={[styles.closeButton, { backgroundColor: colors.secondary }]}
          >
            <Ionicons name="close" size={20} color={colors.mutedForeground} />
          </TouchableOpacity>
        </View>

        {/* Form */}
        <View style={styles.form}>
          {/* Rating */}
          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>Your Rating</Text>
            <StarRating rating={rating} size="lg" interactive onRatingChange={setRating} />
          </View>

          {/* Name */}
          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>Your Name (optional)</Text>
            <TextInput
              style={[styles.textInput, { backgroundColor: colors.secondary, color: colors.foreground }]}
              value={userName}
              onChangeText={setUserName}
              placeholder="Enter your name"
              placeholderTextColor={colors.mutedForeground}
              maxLength={MAX_NAME_LEN}
            />
            <Text style={[styles.charCount, { color: colors.mutedForeground }]}>
              {nameLen}/{MAX_NAME_LEN}
            </Text>
          </View>

          {/* Comment */}
          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>Your Review</Text>
            <TextInput
              style={[styles.textArea, { backgroundColor: colors.secondary, color: colors.foreground }]}
              value={comment}
              onChangeText={setComment}
              placeholder="Share your experience with this product..."
              placeholderTextColor={colors.mutedForeground}
              multiline
              numberOfLines={4}
              textAlignVertical="top"
              maxLength={MAX_REVIEW_LEN + 50}
            />
            <Text style={[styles.charCount, { color: colors.mutedForeground }]}>
              Minimum {MIN_REVIEW_LEN} characters ({commentLen}/{MAX_REVIEW_LEN})
            </Text>
          </View>

          {/* Buttons */}
          <View style={styles.buttons}>
            <Button onPress={handleClose} variant="outline" style={styles.button}>
              Cancel
            </Button>
            <Button onPress={handleSubmit} variant="premium" loading={isSubmitting} style={styles.button}>
              Submit Review
            </Button>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

export const AddReviewModal: React.FC<AddReviewModalProps> = ({
  visible,
  onClose,
  productName,
  onSubmit,
}) => {
  const { colors } = useTheme();
  const isWeb = Platform.OS === 'web';

  // ✅ Provider INSIDE Modal = toast shows on the review screen (not behind it)
  return (
    <Modal
      visible={visible}
      animationType={isWeb ? 'fade' : 'slide'}
      presentationStyle={isWeb ? 'overFullScreen' : 'pageSheet'}
      transparent={isWeb}
      onRequestClose={onClose}
    >
      <ToastProvider>
        <View style={[
          styles.modalContainer,
          isWeb && { backgroundColor: 'rgba(11, 17, 32, 0.85)' },
          !isWeb && { backgroundColor: colors.background },
        ]}>
          <AddReviewModalContent 
            onClose={onClose} 
            productName={productName} 
            onSubmit={onSubmit}
            onBackdropPress={onClose}
          />
        </View>
      </ToastProvider>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalContainer: {
    flex: 1,
  },
  
  // ✨ Web styles
  webBackdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: Spacing.lg,
  },
  webCard: {
    borderRadius: BorderRadius['2xl'],
    maxHeight: '90%',
    ...Platform.select({
      web: { boxShadow: '0px 16px 32px rgba(0, 0, 0, 0.35)' } as any,
      default: { shadowColor: '#000', shadowOffset: { width: 0, height: 16 }, shadowOpacity: 0.35, shadowRadius: 32, elevation: 20 },
    }),
  },
  webScrollContent: {
    padding: Spacing['2xl'],
  },
  formWeb: {
    gap: Spacing.lg,
  },
  webButtons: {
    flexDirection: 'row',
    gap: Spacing.md,
    marginTop: Spacing.lg,
  },
  webButton: {
    flex: 1,
    minWidth: 120,
  },

  // Mobile styles (original)
  container: { 
    flex: 1, 
    paddingTop: Spacing['3xl'],
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    paddingHorizontal: Spacing.lg,
    marginBottom: Spacing['2xl'],
  },
  title: {
    fontSize: FontSize['2xl'],
    fontWeight: FontWeight.bold,
    marginBottom: Spacing.xs,
  },
  subtitle: { 
    fontSize: FontSize.base,
  },
  closeButton: {
    width: 36,
    height: 36,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
  },
  form: { 
    paddingHorizontal: Spacing.lg, 
    gap: Spacing.xl,
  },
  field: { 
    gap: Spacing.sm,
  },
  label: { 
    fontSize: FontSize.sm, 
    fontWeight: FontWeight.medium,
  },
  textInput: {
    height: 48,
    borderRadius: BorderRadius.lg,
    paddingHorizontal: Spacing.lg,
    fontSize: FontSize.base,
    borderWidth: 1,
    borderColor: 'rgba(148,163,184,0.12)',
  },
  textArea: {
    height: 120,
    borderRadius: BorderRadius.lg,
    paddingHorizontal: Spacing.lg,
    paddingTop: Spacing.md,
    fontSize: FontSize.base,
    borderWidth: 1,
    borderColor: 'rgba(148,163,184,0.12)',
  },
  charCount: { 
    fontSize: FontSize.xs, 
    marginTop: Spacing.xs,
  },
  buttons: {
    flexDirection: 'row',
    gap: Spacing.md,
    marginTop: Spacing.lg,
    paddingBottom: Spacing['3xl'],
  },
  button: { 
    flex: 1,
  },
});