// NotificationsScreen — v3 Radical Redesign
// 3px left accent lines, elevated glass cards, type-colored glow for unread
import React, { useMemo, useState } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  useWindowDimensions,
  Platform,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { useNotifications, Notification, NotificationType } from '../context/NotificationContext';
import { useTheme } from '../context/ThemeContext';
import { RootStackParamList } from '../types';
import {
  Spacing,
  FontSize,
  FontWeight,
  BorderRadius,
  Shadow,
  Glass,
  getDetailMaxWidth,
} from '../constants/theme';

type FilterType = 'all' | NotificationType;

const FILTERS: { key: FilterType; label: string }[] = [
  { key: 'all', label: 'All' },
  { key: 'review', label: 'Reviews' },
  { key: 'order', label: 'Orders' },
  { key: 'system', label: 'System' },
];

function getTimeAgo(date: Date): string {
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m`;
  if (diffHours < 24) return `${diffHours}h`;
  if (diffDays === 1) return 'Yesterday';
  if (diffDays < 7) return `${diffDays}d`;
  return date.toLocaleDateString();
}

function getNotificationIcon(type: NotificationType): keyof typeof Ionicons.glyphMap {
  switch (type) {
    case 'review': return 'star';
    case 'order': return 'cube';
    case 'system': return 'notifications';
  }
}

function getNotificationAccentColor(type: NotificationType): string {
  switch (type) {
    case 'review': return '#10B981'; // emerald
    case 'order': return '#3B82F6'; // blue
    case 'system': return '#8B5CF6'; // purple
  }
}

export const NotificationsScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { colors, colorScheme } = useTheme();
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotifications();

  const [selectedFilter, setSelectedFilter] = useState<FilterType>('all');

  const { width: windowWidth } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  // Passed to ScreenWrapper.contentMaxWidth — centering is handled there
  const contentMaxWidth = isWeb ? getDetailMaxWidth(windowWidth) : undefined;

  const filteredNotifications = useMemo(() => {
    if (selectedFilter === 'all') return notifications;
    return notifications.filter((n) => n.type === selectedFilter);
  }, [notifications, selectedFilter]);

  const handleNotificationPress = (notification: Notification) => {
    markAsRead(notification.id);
    navigation.navigate('NotificationDetail', { notificationId: notification.id } as any);
  };

  const handleBack = () => {
    if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('ProductList' as any);
    }
  };

  const renderFilterChip = ({ key, label }: { key: FilterType; label: string }) => {
    const isSelected = selectedFilter === key;
    return (
      <TouchableOpacity
        key={key}
        onPress={() => setSelectedFilter(key)}
        activeOpacity={0.8}
        style={[
          styles.filterChip,
          {
            backgroundColor: isSelected ? colors.foreground : colors.card,
            borderColor: isSelected ? colors.foreground : colors.border,
          },
        ]}
      >
        <Text
          style={[
            styles.filterChipText,
            { color: isSelected ? colors.card : colors.foreground },
          ]}
        >
          {label}
        </Text>
      </TouchableOpacity>
    );
  };

  const renderNotification = ({ item }: { item: Notification }) => {
    const accentColor = getNotificationAccentColor(item.type);

    return (
      <TouchableOpacity
        onPress={() => handleNotificationPress(item)}
        activeOpacity={0.7}
        style={[
          styles.notificationCard,
          isWeb && styles.notificationCardWeb,
          colorScheme === 'dark'
            ? Glass.elevated
            : { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1 },
          // Unread: type-colored glow
          !item.isRead && Platform.select({
            web: { boxShadow: `0px 0px 12px ${accentColor}4D` } as any,
            default: { shadowColor: accentColor, shadowOffset: { width: 0, height: 0 }, shadowOpacity: 0.3, shadowRadius: 12, elevation: 8 },
          }),
        ]}
      >
        {/* 3px left accent line */}
        <View style={[styles.accentLine, { backgroundColor: accentColor }]} />

        {/* Icon */}
        <View style={[styles.iconContainer, { backgroundColor: accentColor + '15' }]}>
          <Ionicons name={getNotificationIcon(item.type)} size={20} color={accentColor} />
        </View>

        {/* Content */}
        <View style={styles.contentContainer}>
          <Text
            style={[
              styles.notificationTitle,
              {
                color: colors.foreground,
                fontWeight: item.isRead ? FontWeight.medium : FontWeight.bold,
              },
            ]}
            numberOfLines={1}
          >
            {item.title}
          </Text>
          <Text
            style={[styles.notificationBody, { color: colors.mutedForeground }]}
            numberOfLines={2}
          >
            {item.body}
          </Text>
        </View>

        {/* Timestamp */}
        <View style={styles.timestampContainer}>
          <Text style={[styles.timestamp, { color: colors.mutedForeground }]}>
            {getTimeAgo(item.timestamp)}
          </Text>
          <Ionicons name="chevron-forward" size={16} color={colors.mutedForeground} />
        </View>
      </TouchableOpacity>
    );
  };

  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <View style={[styles.emptyIconContainer, { backgroundColor: colors.muted }]}>
        <Ionicons name="notifications-outline" size={48} color={colors.mutedForeground} />
      </View>
      <Text style={[styles.emptyTitle, { color: colors.foreground }]}>No notifications yet</Text>
      <Text style={[styles.emptySubtitle, { color: colors.mutedForeground }]}>
        When you get notifications, they'll show up here
      </Text>
    </View>
  );

  const renderHeader = () => (
    <View>
      <View style={styles.filtersContainer}>
        {FILTERS.map(renderFilterChip)}
      </View>
    </View>
  );

  return (
    <ScreenWrapper backgroundColor={colors.background} contentMaxWidth={contentMaxWidth}>
        {/* Header */}
        <View style={[styles.header, { borderBottomColor: colors.border }]}>
          <View style={styles.headerLeft}>
            <TouchableOpacity onPress={handleBack} style={styles.backButton}>
              <Ionicons name="arrow-back" size={22} color={colors.foreground} />
            </TouchableOpacity>
            <View>
              <Text style={[styles.headerTitle, { color: colors.foreground }]}>Notifications</Text>
              {unreadCount > 0 && (
                <Text style={[styles.headerSubtitle, { color: colors.mutedForeground }]}>
                  {unreadCount} unread
                </Text>
              )}
            </View>
          </View>

          {unreadCount > 0 && (
            <TouchableOpacity onPress={markAllAsRead} activeOpacity={0.7}>
              <Text style={[styles.markAllRead, { color: colors.primary }]}>Mark all read</Text>
            </TouchableOpacity>
          )}
        </View>

        <FlatList
          data={filteredNotifications}
          keyExtractor={(item) => item.id}
          renderItem={renderNotification}
          ListHeaderComponent={renderHeader}
          ListEmptyComponent={renderEmpty}
          contentContainerStyle={[styles.listContent, isWeb && styles.listContentWeb]}
          showsVerticalScrollIndicator={false}
          ItemSeparatorComponent={() => <View style={{ height: Spacing.md }} />}
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
    paddingVertical: Spacing.lg,
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
  markAllRead: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
  },
  filtersContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    gap: Spacing.sm,
  },
  filterChip: {
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.sm,
    borderRadius: BorderRadius.full,
    borderWidth: 1,
  },
  filterChipText: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.medium,
  },
  listContent: {
    paddingHorizontal: Spacing.lg,
    paddingBottom: Spacing['3xl'],
  },
  listContentWeb: {
    width: '100%',
  },
  notificationCard: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    padding: Spacing.lg,
    paddingLeft: Spacing.lg + 6, // space for accent line
    borderRadius: BorderRadius['2xl'],
    gap: Spacing.md,
    position: 'relative',
    overflow: 'hidden',
    ...Shadow.soft,
  },
  notificationCardWeb: {
    width: '100%',
  },
  accentLine: {
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    width: 3,
  },
  iconContainer: {
    width: 44,
    height: 44,
    borderRadius: BorderRadius.lg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  contentContainer: {
    flex: 1,
    gap: 4,
  },
  notificationTitle: {
    fontSize: FontSize.sm,
  },
  notificationBody: {
    fontSize: FontSize.xs,
    lineHeight: FontSize.xs * 1.4,
  },
  timestampContainer: {
    alignItems: 'flex-end',
    gap: 4,
  },
  timestamp: {
    fontSize: FontSize.xs,
  },
  emptyContainer: {
    alignItems: 'center',
    paddingVertical: Spacing['5xl'],
    gap: Spacing.md,
  },
  emptyIconContainer: {
    width: 100,
    height: 100,
    borderRadius: BorderRadius.full,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.md,
  },
  emptyTitle: {
    fontSize: FontSize.lg,
    fontWeight: FontWeight.semibold,
  },
  emptySubtitle: {
    fontSize: FontSize.sm,
    textAlign: 'center',
    paddingHorizontal: Spacing['2xl'],
  },
});
