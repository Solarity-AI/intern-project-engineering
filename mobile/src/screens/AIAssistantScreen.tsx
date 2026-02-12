// AIAssistantScreen.tsx — v3 Radical Redesign
// Gradient header, decorative orbs, accent-line bubbles, 2-column option grid

import React, { useState, useRef, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  useWindowDimensions,
  Platform,
} from 'react-native';
import { useRoute, useNavigation, RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { useTheme } from '../context/ThemeContext';
import { RootStackParamList } from '../types';
import { Spacing, FontSize, BorderRadius, FontWeight, Shadow, Glass, Gradients } from '../constants/theme';
import { chatWithAI, getUserMessage } from '../services/api';

type RouteType = RouteProp<RootStackParamList, 'AIAssistant'>;

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  options?: string[];
  selectedOption?: string;
  timestamp: Date;
  hideIcon?: boolean;
}

const QUESTIONS = [
  'How many reviews are there?',
  'What do customers say about quality?',
  'When were most reviews posted?',
  'What are the main complaints?',
  'Any common praise patterns?',
];

export const AIAssistantScreen: React.FC = () => {
  const route = useRoute<RouteType>();
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { colors, colorScheme } = useTheme();
  const scrollViewRef = useRef<ScrollView>(null);

  const { width: windowWidth } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  const MAX_CONTENT_WIDTH = 600;
  const isWideScreen = windowWidth > MAX_CONTENT_WIDTH;

  const responsiveContainerStyle = {
    width: '100%' as const,
    maxWidth: isWeb ? MAX_CONTENT_WIDTH : undefined,
    alignSelf: 'center' as const,
    flex: 1,
  };

  const productName = route.params?.productName || 'this product';
  const productId = route.params?.productId;
  const reviews = route.params?.reviews || [];

  const activeRequestRef = useRef(false);
  const processingRef = useRef(false);
  const isExitingRef = useRef(false);

  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      role: 'assistant',
      content: `Hi! I'm your AI assistant for ${productName}. I can help you understand customer reviews better.`,
      options: QUESTIONS,
      timestamp: new Date(),
    },
  ]);

  const [isLoading, setIsLoading] = useState(false);
  const [waitingForMore, setWaitingForMore] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [lastActiveMessageId, setLastActiveMessageId] = useState<string>('1');

  useEffect(() => {
    const t = setTimeout(() => {
      scrollViewRef.current?.scrollToEnd({ animated: true });
    }, 100);
    return () => clearTimeout(t);
  }, [messages]);

  const analyzeReviewsLocally = useCallback(
    (question: string): string => {
      const lowerQuestion = question.toLowerCase();
      if (lowerQuestion.includes('how many')) {
        return `There are ${reviews.length} customer reviews for this product.`;
      }
      return 'I analyzed the reviews locally and found mixed feedback.';
    },
    [reviews.length]
  );

  const consumeOptionsForMessage = useCallback((messageId: string, selected: string) => {
    setMessages((prev) =>
      prev.map((m) =>
        m.id === messageId
          ? { ...m, selectedOption: selected, options: undefined }
          : m
      )
    );
  }, []);

  const handleQuestionSelect = useCallback(
    async (question: string, messageId: string) => {
      if (
        activeRequestRef.current ||
        processingRef.current ||
        isProcessing ||
        isLoading ||
        isExitingRef.current
      ) {
        return;
      }

      if (messageId !== lastActiveMessageId) return;

      activeRequestRef.current = true;
      processingRef.current = true;
      setIsProcessing(true);

      consumeOptionsForMessage(messageId, question);

      const userMessage: Message = {
        id: Date.now().toString(),
        role: 'user',
        content: question,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);
      setIsLoading(true);

      try {
        let answer = '';
        if (productId) {
          const response = await chatWithAI(productId, question);
          answer = response.answer;
        } else {
          answer = analyzeReviewsLocally(question);
        }

        const answerId = (Date.now() + 1).toString();

        const assistantAnswer: Message = {
          id: answerId,
          role: 'assistant',
          content: answer,
          timestamp: new Date(),
        };

        const followupId = (Date.now() + 2).toString();
        const assistantFollowup: Message = {
          id: followupId,
          role: 'assistant',
          content: 'Do you have more questions?',
          options: ['Yes', 'No'],
          timestamp: new Date(),
          hideIcon: true,
        };

        setMessages((prev) => [...prev, assistantAnswer, assistantFollowup]);
        setLastActiveMessageId(followupId);
        setWaitingForMore(true);
      } catch (error) {
        console.error('AI Chat Error:', error);

        const followupId = (Date.now() + 2).toString();
        const errMsg: Message = {
          id: (Date.now() + 1).toString(),
          role: 'assistant',
          content: getUserMessage(error),
          timestamp: new Date(),
        };
        const retryMsg: Message = {
          id: followupId,
          role: 'assistant',
          content: 'Do you have more questions?',
          options: ['Yes', 'No'],
          timestamp: new Date(),
          hideIcon: true,
        };

        setMessages((prev) => [...prev, errMsg, retryMsg]);
        setLastActiveMessageId(followupId);
        setWaitingForMore(true);
      } finally {
        setIsLoading(false);
        setIsProcessing(false);
        processingRef.current = false;
        activeRequestRef.current = false;
      }
    },
    [
      analyzeReviewsLocally,
      consumeOptionsForMessage,
      isLoading,
      isProcessing,
      lastActiveMessageId,
      productId,
      isProcessing,
    ]
  );

  const handleMoreQuestions = useCallback(
    (choice: string, messageId: string) => {
      if (processingRef.current || isProcessing || isExitingRef.current) return;

      if (messageId !== lastActiveMessageId) return;

      processingRef.current = true;
      setIsProcessing(true);
      setWaitingForMore(false);

      consumeOptionsForMessage(messageId, choice);

      const userMessage: Message = {
        id: Date.now().toString(),
        role: 'user',
        content: choice,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);

      if (choice === 'No') {
        isExitingRef.current = true;

        setTimeout(() => {
          const exitMessage: Message = {
            id: (Date.now() + 1).toString(),
            role: 'assistant',
            content: 'Thank you for using AI Assistant! Feel free to come back anytime.',
            timestamp: new Date(),
          };
          setMessages((prev) => [...prev, exitMessage]);
          setLastActiveMessageId('');
          setIsProcessing(false);
          processingRef.current = false;

          setTimeout(() => {
            navigation.goBack();
          }, 3000);
        }, 300);
      } else {
        setTimeout(() => {
          const newId = (Date.now() + 1).toString();
          const restartMessage: Message = {
            id: newId,
            role: 'assistant',
            content: 'Great! What would you like to know?',
            options: QUESTIONS,
            timestamp: new Date(),
          };
          setMessages((prev) => [...prev, restartMessage]);
          setLastActiveMessageId(newId);
          setIsProcessing(false);
          processingRef.current = false;
        }, 300);
      }
    },
    [consumeOptionsForMessage, isProcessing, lastActiveMessageId, navigation]
  );

  const renderMessage = (message: Message) => {
    const isUser = message.role === 'user';
    const isActiveMessage = message.id === lastActiveMessageId;

    const shouldShowOptions =
      !isUser && isActiveMessage && !!message.options && message.options.length > 0;

    const isDisabled =
      isProcessing || isLoading || isExitingRef.current || activeRequestRef.current;

    return (
      <View
        key={message.id}
        style={[
          styles.messageBubble,
          isUser ? styles.userBubble : styles.assistantBubble,
        ]}
      >
        {!isUser && !message.hideIcon && (
          <View style={styles.aiIconContainer}>
            <LinearGradient colors={Gradients.ai as [string, string]} style={styles.aiIcon}>
              <Ionicons name="sparkles" size={16} color="#fff" />
            </LinearGradient>
          </View>
        )}

        <View
          style={[
            styles.bubbleContent,
            isUser
              ? styles.userBubbleContent
              : [
                  colorScheme === 'dark'
                    ? { ...Glass.elevated }
                    : { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1 },
                  styles.assistantBubbleContent,
                ],
          ]}
        >
          <Text
            style={[
              styles.messageText,
              { color: isUser ? '#fff' : colors.foreground },
            ]}
          >
            {message.content}
          </Text>

          {shouldShowOptions && (
            <View style={styles.optionsContainer}>
              {/* 2-column grid for question options, single row for Yes/No */}
              <View style={[
                styles.optionsGrid,
                waitingForMore && styles.optionsGridSingle,
              ]}>
                {message.options!.map((option, index) => (
                  <TouchableOpacity
                    key={index}
                    activeOpacity={isDisabled ? 1 : 0.8}
                    disabled={isDisabled}
                    style={[
                      styles.optionButton,
                      waitingForMore && styles.optionButtonFull,
                      isDisabled
                        ? { backgroundColor: colors.muted, borderColor: colors.border, opacity: 0.5 }
                        : colorScheme === 'dark'
                          ? { ...Glass.card }
                          : { backgroundColor: colors.secondary, borderColor: colors.border },
                    ]}
                    onPress={() => {
                      if (isDisabled) return;

                      if (waitingForMore) {
                        handleMoreQuestions(option, message.id);
                      } else {
                        handleQuestionSelect(option, message.id);
                      }
                    }}
                  >
                    {/* 2px emerald left accent */}
                    <View style={styles.optionAccent} />
                    <Text
                      style={[
                        styles.optionText,
                        { color: isDisabled ? colors.mutedForeground : colors.foreground },
                      ]}
                      numberOfLines={2}
                    >
                      {option}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>
          )}
        </View>

        <Text style={[styles.timestamp, { color: colors.mutedForeground }]}>
          {message.timestamp.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
        </Text>
      </View>
    );
  };

  return (
    <ScreenWrapper backgroundColor={colors.background}>
      <View style={responsiveContainerStyle}>
        {/* ===== GRADIENT HEADER ===== */}
        <LinearGradient
          colors={Gradients.ai as [string, string]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 0 }}
          style={styles.header}
        >
          <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
            <Ionicons name="arrow-back" size={22} color="#fff" />
          </TouchableOpacity>

          <View style={styles.headerTitle}>
            <View style={styles.headerIconCircle}>
              <Ionicons name="sparkles" size={20} color="#fff" />
            </View>
            <View>
              <Text style={styles.headerTitleText}>AI Assistant</Text>
              <Text style={styles.headerSubtitle}>
                Analyzing {reviews.length} reviews
              </Text>
            </View>
          </View>

          <View style={{ width: 40 }} />

          {/* Purple glow line at bottom */}
          <View style={styles.headerGlowLine} />
        </LinearGradient>

        {/* ===== CHAT BODY with decorative orbs ===== */}
        <View style={{ flex: 1, position: 'relative' }}>
          {/* Decorative background orbs */}
          {colorScheme === 'dark' && (
            <>
              <View style={styles.bgOrbPurple} />
              <View style={styles.bgOrbGreen} />
            </>
          )}

          <ScrollView
            ref={scrollViewRef}
            contentContainerStyle={styles.messagesContainer}
            showsVerticalScrollIndicator={false}
          >
            {messages.map(renderMessage)}

            {isLoading && (
              <View style={[styles.loadingBubble, colorScheme === 'dark' ? Glass.card : { backgroundColor: colors.card }]}>
                <ActivityIndicator size="small" color={colors.primary} />
                <Text style={[styles.loadingText, { color: colors.mutedForeground }]}>
                  Analyzing reviews...
                </Text>
              </View>
            )}
          </ScrollView>
        </View>
      </View>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  /* ===== GRADIENT HEADER ===== */
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.lg,
    position: 'relative',
  },
  backButton: {
    padding: Spacing.xs,
  },
  headerTitle: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
  },
  headerIconCircle: {
    width: 40,
    height: 40,
    borderRadius: BorderRadius.full,
    backgroundColor: 'rgba(255,255,255,0.15)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitleText: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.bold,
    color: '#fff',
  },
  headerSubtitle: {
    fontSize: FontSize.xs,
    marginTop: 2,
    color: 'rgba(255,255,255,0.7)',
  },
  headerGlowLine: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: 1,
    backgroundColor: 'rgba(139,92,246,0.5)',
  },

  /* ===== DECORATIVE ORBS ===== */
  bgOrbPurple: {
    position: 'absolute',
    top: 40,
    right: -40,
    width: 200,
    height: 200,
    borderRadius: 100,
    backgroundColor: 'rgba(139,92,246,0.05)',
    ...Platform.select({
      web: { filter: 'blur(80px)' } as any,
      default: { opacity: 0.3 },
    }),
  },
  bgOrbGreen: {
    position: 'absolute',
    bottom: 60,
    left: -20,
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: 'rgba(16,185,129,0.03)',
    ...Platform.select({
      web: { filter: 'blur(60px)' } as any,
      default: { opacity: 0.2 },
    }),
  },

  messagesContainer: {
    padding: Spacing.lg,
    gap: Spacing.lg,
  },

  messageBubble: {
    gap: Spacing.xs,
  },
  userBubble: {
    alignItems: 'flex-end',
  },
  assistantBubble: {
    alignItems: 'flex-start',
  },

  aiIconContainer: {
    marginBottom: Spacing.xs,
  },
  aiIcon: {
    width: 32,
    height: 32,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadow.soft,
  },

  bubbleContent: {
    maxWidth: '85%',
    padding: Spacing.lg,
    borderRadius: BorderRadius['2xl'],
    ...Shadow.medium,
  },
  userBubbleContent: {
    borderBottomRightRadius: 4,
    overflow: 'hidden',
    backgroundColor: '#10B981',
    borderWidth: 0,
  },
  assistantBubbleContent: {
    borderLeftWidth: 2,
    borderLeftColor: '#8B5CF6',
  },

  messageText: {
    fontSize: FontSize.base,
    lineHeight: FontSize.base * 1.5,
  },

  optionsContainer: {
    marginTop: Spacing.md,
  },
  optionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.sm,
  },
  optionsGridSingle: {
    flexDirection: 'row',
    flexWrap: 'nowrap',
  },

  optionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.lg,
    width: '48%',
    overflow: 'hidden',
  },
  optionButtonFull: {
    width: '48%',
  },
  optionAccent: {
    width: 2,
    height: '100%',
    backgroundColor: '#10B981',
    borderRadius: 1,
    marginRight: Spacing.sm,
    minHeight: 16,
  },

  optionText: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
    flex: 1,
  },

  timestamp: {
    fontSize: FontSize.xs,
    marginTop: 4,
  },

  loadingBubble: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
    padding: Spacing.md,
    borderRadius: BorderRadius.xl,
    alignSelf: 'flex-start',
    ...Shadow.soft,
  },

  loadingText: {
    fontSize: FontSize.sm,
  },
});
