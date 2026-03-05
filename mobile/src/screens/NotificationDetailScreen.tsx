// NotificationDetailScreen — v3 Radical Redesign
// Mini gradient hero (120px) with centered type icon, gradient button styles
import React, { useEffect, useMemo } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  useWindowDimensions,
  Platform,
} from 'react-native';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { GradientDivider } from '../components/GradientDivider';
import { useNotifications, NotificationType } from '../context/NotificationContext';
import { useTheme } from '../context/ThemeContext';
import { RootStackParamList } from '../types';
import {
  Spacing,
  FontSize,
  FontWeight,
  BorderRadius,
  Shadow,
  Glass,
  Glow,
  Gradients,
  getDetailMaxWidth,
} from '../constants/theme';

type RouteType = RouteProp<RootStackParamList, 'NotificationDetail'>;

function getNotificationIcon(type: NotificationType): keyof typeof Ionicons.glyphMap {
  switch (type) {
    case 'review': return 'star';
    case 'order': return 'cube';
    case 'system': return 'notifications';
  }
}

function getNotificationGradient(type: NotificationType): [string, string] {
  switch (type) {
    case 'review': return ['#10B981', '#059669'];
    case 'order': return ['#3B82F6', '#2563EB'];
    case 'system': return ['#8B5CF6', '#6366F1'];
  }
}

function getNotificationColor(type: NotificationType): string {
  switch (type) {
    case 'review': return '#10B981';
    case 'order': return '#3B82F6';
    case 'system': return '#8B5CF6';
  }
}

function formatFullDate(date: Date): string {
  return date.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export const NotificationDetailScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<RouteType>();
  const { colors, colorScheme } = useTheme();
  const { notifications, markAsRead, clearNotification } = useNotifications();

  const { width: windowWidth } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  const contentMaxWidth = isWeb ? getDetailMaxWidth(windowWidth) : undefined;

  // contentMaxWidth is passed to ScreenWrapper — no local wrapper View needed

  const notificationId = route.params?.notificationId;

  const notification = useMemo(() => {
    return notifications.find((n) => n.id === notificationId);
  }, [notifications, notificationId]);

  useEffect(() => {
    if (notification && !notification.isRead) {
      markAsRead(notification.id);
    }
  }, [notification, markAsRead]);

  const handleBack = () => {
    if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('Notifications' as any);
    }
  };

  if (!notification) {
    return (
      <ScreenWrapper backgroundColor={colors.background} contentMaxWidth={contentMaxWidth}>
          <View style={styles.header}>
            <TouchableOpacity onPress={handleBack} style={styles.backButton}>
              <Ionicons name="arrow-back" size={22} color={colors.foreground} />
            </TouchableOpacity>
          </View>
          <View style={styles.errorContainer}>
            <Ionicons name="alert-circle-outline" size={48} color={colors.mutedForeground} />
            <Text style={[styles.errorTitle, { color: colors.foreground }]}>
              Notification Not Found
            </Text>
            <Text style={[styles.errorSubtitle, { color: colors.mutedForeground }]}>
              This notification may have been deleted.
            </Text>
          </View>
      </ScreenWrapper>
    );
  }

  const typeColor = getNotificationColor(notification.type);
  const typeGradient = getNotificationGradient(notification.type);

  const handleDelete = () => {
    clearNotification(notification.id);
    handleBack();
  };

  const handleViewProduct = () => {
    if (notification.data?.productId) {
      navigation.navigate('ProductDetails', {
        productId: notification.data.productId,
      } as any);
    }
  };

  return (
    <ScreenWrapper backgroundColor={colors.background} contentMaxWidth={contentMaxWidth}>
        {/* Header bar */}
        <View style={[styles.header, { borderBottomColor: colors.border }]}>
          <TouchableOpacity onPress={handleBack} style={styles.backButton}>
            <Ionicons name="arrow-back" size={22} color={colors.foreground} />
          </TouchableOpacity>

          <TouchableOpacity onPress={handleDelete} style={styles.deleteButton}>
            <Ionicons name="trash-outline" size={20} color={colors.destructive} />
          </TouchableOpacity>
        </View>

        <ScrollView showsVerticalScrollIndicator={false}>
          {/* ===== MINI GRADIENT HERO ===== */}
          <LinearGradient
            colors={typeGradient}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
            style={styles.miniHero}
          >
            <View style={styles.heroIconCircle}>
              <Ionicons name={getNotificationIcon(notification.type)} size={32} color="#fff" />
            </View>
          </LinearGradient>

          <View style={styles.content}>
            {/* Type Badge */}
            <View style={[styles.typeBadge, { backgroundColor: typeColor + '15' }]}>
              <Text style={[styles.typeBadgeText, { color: typeColor }]}>
                {notification.type.charAt(0).toUpperCase() + notification.type.slice(1)}
              </Text>
            </View>

            {/* Title */}
            <Text style={[styles.title, { color: colors.foreground }]}>
              {notification.title}
            </Text>

            {/* Timestamp */}
            <View style={styles.timestampContainer}>
              <Ionicons name="time-outline" size={16} color={colors.mutedForeground} />
              <Text style={[styles.timestamp, { color: colors.mutedForeground }]}>
                {formatFullDate(notification.timestamp)}
              </Text>
            </View>

            <GradientDivider />

            {/* Body */}
            <Text style={[styles.body, { color: colors.foreground }]}>
              {notification.body}
            </Text>

            {/* Additional Info */}
            {notification.data?.productName && (
              <View style={[styles.infoCard, colorScheme === 'dark' ? Glass.elevated : { backgroundColor: colors.secondary }]}>
                <View style={styles.infoRow}>
                  <Ionicons name="cube-outline" size={18} color={colors.mutedForeground} />
                  <Text style={[styles.infoLabel, { color: colors.mutedForeground }]}>
                    Product:
                  </Text>
                  <Text style={[styles.infoValue, { color: colors.foreground }]}>
                    {notification.data.productName}
                  </Text>
                </View>
              </View>
            )}

            {/* Actions */}
            <View style={styles.actions}>
              {notification.data?.productId && (
                <TouchableOpacity
                  onPress={handleViewProduct}
                  activeOpacity={0.8}
                >
                  <LinearGradient
                    colors={Gradients.brand as [string, string]}
                    style={[styles.actionButtonGradient, Glow.primary]}
                  >
                    <Text style={styles.actionButtonGradientText}>View Product</Text>
                    <Ionicons name="arrow-forward" size={18} color="#fff" />
                  </LinearGradient>
                </TouchableOpacity>
              )}

              <TouchableOpacity
                onPress={handleDelete}
                activeOpacity={0.8}
                style={[styles.actionButton, styles.deleteActionButton, { borderColor: colors.border }]}
              >
                <Text style={[styles.actionButtonText, { color: colors.destructive }]}>
                  Delete Notification
                </Text>
                <Ionicons name="trash-outline" size={18} color={colors.destructive} />
              </TouchableOpacity>
            </View>
          </View>
        </ScrollView>
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
  backButton: {
    padding: Spacing.xs,
  },
  deleteButton: {
    padding: Spacing.xs,
  },

  /* Mini gradient hero */
  miniHero: {
    height: 120,
    alignItems: 'center',
    justifyContent: 'center',
  },
  heroIconCircle: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: 'rgba(255,255,255,0.2)',
    alignItems: 'center',
    justifyContent: 'center',
  },

  content: {
    padding: Spacing.lg,
  },

  typeBadge: {
    alignSelf: 'center',
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.xs,
    borderRadius: BorderRadius.full,
    marginBottom: Spacing.lg,
    marginTop: Spacing.md,
  },
  typeBadgeText: {
    fontSize: FontSize.xs,
    fontWeight: FontWeight.semibold,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },

  title: {
    fontSize: FontSize['2xl'],
    fontWeight: FontWeight.bold,
    textAlign: 'center',
    marginBottom: Spacing.md,
    lineHeight: FontSize['2xl'] * 1.3,
  },

  timestampContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.xs,
  },
  timestamp: {
    fontSize: FontSize.sm,
  },

  body: {
    fontSize: FontSize.base,
    lineHeight: FontSize.base * 1.6,
    marginBottom: Spacing.xl,
  },

  infoCard: {
    padding: Spacing.md,
    borderRadius: BorderRadius.lg,
    marginBottom: Spacing.xl,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  infoLabel: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
  },
  infoValue: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
    flex: 1,
  },

  actions: {
    gap: Spacing.md,
  },
  actionButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.sm,
    paddingVertical: Spacing.lg,
    paddingHorizontal: Spacing.xl,
    borderRadius: BorderRadius.xl,
  },
  actionButtonGradientText: {
    color: '#fff',
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },
  actionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.sm,
    paddingVertical: Spacing.lg,
    paddingHorizontal: Spacing.xl,
    borderRadius: BorderRadius.xl,
  },
  deleteActionButton: {
    backgroundColor: 'transparent',
    borderWidth: 1,
  },
  actionButtonText: {
    fontSize: FontSize.base,
    fontWeight: FontWeight.semibold,
  },

  errorContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: Spacing['2xl'],
    gap: Spacing.md,
  },
  errorTitle: {
    fontSize: FontSize.xl,
    fontWeight: FontWeight.bold,
    textAlign: 'center',
  },
  errorSubtitle: {
    fontSize: FontSize.base,
    textAlign: 'center',
  },
});
