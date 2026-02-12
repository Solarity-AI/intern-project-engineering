// React Native SortFilter Component
// Horizontal scrollable sort chips

import React from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Spacing, FontSize, BorderRadius, FontWeight, Glow, Shadow } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

export type SortOption = {
  value: string;
  label: string;
  icon: keyof typeof Ionicons.glyphMap;
};

export const SORT_OPTIONS: SortOption[] = [
  { value: 'name,asc', label: 'Aa Name (A-Z)', icon: 'text' },
  { value: 'name,desc', label: 'Aa Name (Z-A)', icon: 'text' },
  { value: 'averageRating,desc', label: '★ Top Rated', icon: 'star' },
  { value: 'price,asc', label: 'Low → High', icon: 'arrow-up' },
  { value: 'price,desc', label: 'High → Low', icon: 'arrow-down' },
  { value: 'reviewCount,desc', label: 'Most Reviews', icon: 'chatbubbles' },
];

interface SortFilterProps {
  selectedSort: string;
  onSortChange: (sort: string) => void;
}

export const SortFilter: React.FC<SortFilterProps> = ({
  selectedSort,
  onSortChange,
}) => {
  const { colors } = useTheme();

  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.scrollContent}
    >
      {SORT_OPTIONS.map((option) => {
        const isSelected = selectedSort === option.value;

        return (
          <TouchableOpacity
            key={option.value}
            onPress={() => onSortChange(option.value)}
            activeOpacity={0.7}
            style={[
              styles.chip,
              {
                backgroundColor: isSelected ? colors.primary : colors.secondary,
                borderColor: isSelected ? colors.primary : colors.border,
                ...(isSelected ? Glow.primarySoft : {}),
              },
            ]}
          >
            <Text
              style={[
                styles.chipText,
                { color: isSelected ? colors.primaryForeground : colors.secondaryForeground },
              ]}
            >
              {option.label}
            </Text>
          </TouchableOpacity>
        );
      })}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  scrollContent: {
    paddingHorizontal: Spacing.lg,
    gap: Spacing.sm,
    flexDirection: 'row',
    paddingVertical: Spacing.sm,
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.full,
    borderWidth: 1,
  },
  chipText: {
    fontSize: FontSize.sm,
    fontWeight: FontWeight.semibold,
    letterSpacing: 0.3,
  },
});