// React Native CategoryFilter Component
// Horizontal scrollable category chips

import React from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Platform,
  useWindowDimensions,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { categories } from '../constants/data';
import { Spacing, FontSize, BorderRadius, Shadow, Gradients, Glow, Breakpoints } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

interface CategoryFilterProps {
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
}

export const CategoryFilter: React.FC<CategoryFilterProps> = ({
  selectedCategory,
  onCategoryChange,
}) => {
  const { colors } = useTheme();
  const { width } = useWindowDimensions();
  const isWeb = Platform.OS === 'web';
  // On desktop-width web viewports, wrap chips instead of horizontal scroll
  const useWrapLayout = isWeb && width >= Breakpoints.desktop;

  const chips = categories.map((category) => {
    const isSelected = selectedCategory === category;

    if (isSelected) {
      return (
        <TouchableOpacity
          key={category}
          onPress={() => onCategoryChange(category)}
          activeOpacity={0.8}
        >
          <LinearGradient
            colors={Gradients.brand}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
            style={[styles.chip, Glow.primarySoft]}
          >
            <Text style={[styles.chipText, { color: colors.primaryForeground }]}>
              {category}
            </Text>
          </LinearGradient>
        </TouchableOpacity>
      );
    }

    return (
      <TouchableOpacity
        key={category}
        onPress={() => onCategoryChange(category)}
        activeOpacity={0.7}
        style={[styles.chip, { backgroundColor: colors.secondary, borderWidth: 1, borderColor: colors.border }]}
      >
        <Text style={[styles.chipText, { color: colors.secondaryForeground }]}>
          {category}
        </Text>
      </TouchableOpacity>
    );
  });

  if (useWrapLayout) {
    return (
      <View style={styles.wrapContent}>
        {chips}
      </View>
    );
  }

  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.scrollContent}
    >
      {chips}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  scrollContent: {
    paddingHorizontal: Spacing.lg,
    gap: Spacing.sm,
    flexDirection: 'row',
  },
  wrapContent: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: Spacing.lg,
    gap: Spacing.sm,
    paddingVertical: Spacing.sm,
  },
  chip: {
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.full,
  },
  chipText: {
    fontSize: FontSize.sm,
    fontWeight: '600',
    letterSpacing: 0.3,
  },
});
