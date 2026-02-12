import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Gradients, FontSize, FontWeight, Spacing } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';

interface GradientDividerProps {
  label?: string;
}

export const GradientDivider: React.FC<GradientDividerProps> = ({ label }) => {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      <LinearGradient
        colors={Gradients.dividerGlow as [string, string, ...string[]]}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 0 }}
        style={styles.line}
      />
      {label && (
        <Text style={[styles.label, { color: colors.mutedForeground }]}>{label}</Text>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { alignItems: 'center', paddingVertical: Spacing.lg },
  line: { width: '100%', height: 1 },
  label: { fontSize: FontSize.xs, fontWeight: FontWeight.medium, letterSpacing: 1, textTransform: 'uppercase', marginTop: Spacing.sm },
});
