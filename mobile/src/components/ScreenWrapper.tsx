// Global SafeArea + StatusBar wrapper for consistent screen layout
// ✨ Fixed: Proper edge-to-edge support for Android
import React from 'react';
import {
  View,
  StatusBar,
  StyleSheet,
  Platform,
  ViewStyle,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useTheme } from '../context/ThemeContext';

interface ScreenWrapperProps {
  children: React.ReactNode;
  backgroundColor?: string;
  statusBarStyle?: 'light-content' | 'dark-content';
  edges?: ('top' | 'bottom' | 'left' | 'right')[];
  style?: ViewStyle;
  // Option to disable bottom inset for screens with their own bottom handling
  disableBottomInset?: boolean;
  /**
   * When set, wraps children in a centered View with this maxWidth.
   * Use this for screens that need consistent width-capping instead of
   * managing it individually in each screen component.
   */
  contentMaxWidth?: number;
}

export const ScreenWrapper: React.FC<ScreenWrapperProps> = ({
  children,
  backgroundColor,
  statusBarStyle,
  edges = ['top', 'left', 'right'],
  style,
  disableBottomInset = false,
  contentMaxWidth,
}) => {
  const insets = useSafeAreaInsets();
  const { colors, colorScheme } = useTheme();
  const bgColor = backgroundColor ?? colors.background;
  
  // Auto-determine status bar style based on theme if not provided
  const barStyle = statusBarStyle ?? (colorScheme === 'dark' ? 'light-content' : 'dark-content');

  // ✨ FIX: For Android edge-to-edge, we need to handle bottom inset properly
  // When edgeToEdgeEnabled is true, the navigation bar overlays content
  // So we need to add bottom padding to prevent content from being hidden
  const isAndroidEdgeToEdge = Platform.OS === 'android';
  
  // Calculate padding based on edges
  const paddingStyle: ViewStyle = {
    paddingTop: edges.includes('top') ? insets.top : 0,
    paddingLeft: edges.includes('left') ? insets.left : 0,
    paddingRight: edges.includes('right') ? insets.right : 0,
    // ✨ FIX: Always apply bottom inset on Android for edge-to-edge support
    // unless explicitly disabled or not in edges array
    paddingBottom: disableBottomInset 
      ? 0 
      : edges.includes('bottom') 
        ? insets.bottom 
        : isAndroidEdgeToEdge 
          ? insets.bottom 
          : 0,
  };

  const content = contentMaxWidth ? (
    <View style={[styles.contentContainer, { maxWidth: contentMaxWidth }]}>
      {children}
    </View>
  ) : children;

  return (
    <View style={[styles.container, { backgroundColor: bgColor }, paddingStyle, style]}>
      <StatusBar
        barStyle={barStyle}
        backgroundColor="transparent"
        translucent={Platform.OS === 'android'}
      />
      {content}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    flex: 1,
    width: '100%',
    alignSelf: 'center',
  },
});