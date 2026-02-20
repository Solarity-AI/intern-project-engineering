import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { FontSize, FontWeight, Spacing } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

interface SectionHeaderProps {
  title: string;
  accentColor?: string;
}

export const SectionHeader: React.FC<SectionHeaderProps> = ({ title, accentColor }) => {
  const { colors } = useTheme();
  const barColor = accentColor || colors.primary;
  return (
    <View style={styles.container}>
      <View style={[styles.accentBar, { backgroundColor: barColor }]} />
      <Text style={[styles.title, { color: colors.foreground }]} numberOfLines={1}>{title}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md },
  accentBar: { width: 3, height: 20, borderRadius: 2 },
  title: { fontSize: FontSize.xl, fontWeight: FontWeight.bold },
});
