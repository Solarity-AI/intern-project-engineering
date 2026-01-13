// React Native SearchBar Component
// Cross-platform search input with history

import React, { useState, useRef } from 'react';
import {
  View,
  TextInput,
  StyleSheet,
  TouchableOpacity,
  Text,
  FlatList,
  Keyboard,
  Platform,
  Modal, // ✨ Using Modal for overlay behavior without actual Modal component issues
  Dimensions,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Spacing, FontSize, BorderRadius, Shadow } from '../constants/theme';
import { useTheme } from '../context/ThemeContext';
import { useSearch } from '../context/SearchContext';

interface SearchBarProps {
  value: string;
  onChangeText: (text: string) => void;
  onSearchSubmit: (term: string) => void;
  placeholder?: string;
}

export const SearchBar: React.FC<SearchBarProps> = ({
  value,
  onChangeText,
  onSearchSubmit,
  placeholder = 'Search products...',
}) => {
  const { colors } = useTheme();
  const { searchHistory, removeSearchTerm, clearHistory } = useSearch();
  const [isFocused, setIsFocused] = useState(false);
  const inputRef = useRef<TextInput>(null);
  const [layout, setLayout] = useState({ x: 0, y: 0, width: 0, height: 0 });

  const handleFocus = () => setIsFocused(true);
  
  const handleHistoryPress = (term: string) => {
    onChangeText(term);
    onSearchSubmit(term);
    setIsFocused(false);
    Keyboard.dismiss();
  };
  
  const handleRemoveItem = (term: string) => {
    removeSearchTerm(term);
    // Keep focus
    inputRef.current?.focus();
  };

  const handleClearHistory = () => {
    clearHistory();
    inputRef.current?.focus();
  };

  const closeHistory = () => {
    setIsFocused(false);
    Keyboard.dismiss();
  };

  return (
    <View 
      style={[styles.wrapper, { zIndex: isFocused ? 9999 : 1 }]}
      onLayout={(event) => setLayout(event.nativeEvent.layout)}
    >
      <View style={[styles.container, { backgroundColor: colors.secondary }]}>
        <Ionicons name="search" size={18} color={colors.mutedForeground} />
        <TextInput
          ref={inputRef}
          style={[styles.input, { color: colors.foreground }]}
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder}
          placeholderTextColor={colors.mutedForeground}
          autoCapitalize="none"
          autoCorrect={false}
          onFocus={handleFocus}
          onSubmitEditing={() => {
            onSearchSubmit(value);
            setIsFocused(false);
          }}
          returnKeyType="search"
        />
        {value.length > 0 && (
          <Ionicons
            name="close-circle"
            size={18}
            color={colors.mutedForeground}
            onPress={() => {
              onChangeText('');
              inputRef.current?.focus();
            }}
          />
        )}
      </View>

      {/* ✨ Overlay to handle outside clicks */}
      {isFocused && searchHistory.length > 0 && (
        <>
          <TouchableOpacity 
            style={styles.overlay} 
            activeOpacity={1} 
            onPress={closeHistory}
          />
          <View style={[styles.historyContainer, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <FlatList
              data={searchHistory}
              keyExtractor={(item) => item}
              renderItem={({ item }) => (
                <TouchableOpacity 
                  style={styles.historyItem} 
                  onPress={() => handleHistoryPress(item)}
                >
                  <Ionicons name="time-outline" size={16} color={colors.mutedForeground} />
                  <Text style={[styles.historyText, { color: colors.foreground }]}>{item}</Text>
                  <TouchableOpacity 
                    onPress={(e) => {
                      e.stopPropagation();
                      handleRemoveItem(item);
                    }} 
                    style={{ padding: 8 }}
                    hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                  >
                    <Ionicons
                      name="close"
                      size={18}
                      color={colors.mutedForeground}
                    />
                  </TouchableOpacity>
                </TouchableOpacity>
              )}
              keyboardShouldPersistTaps="always"
              ListFooterComponent={
                <TouchableOpacity 
                  style={[styles.clearHistoryButton, { borderTopColor: colors.border }]} 
                  onPress={handleClearHistory}
                >
                  <Text style={[styles.clearHistoryText, { color: colors.destructive || '#ef4444' }]}>
                    Clear Search History
                  </Text>
                </TouchableOpacity>
              }
            />
          </View>
        </>
      )}
    </View>
  );
};

const { height: screenHeight, width: screenWidth } = Dimensions.get('window');

const styles = StyleSheet.create({
  wrapper: {
    position: 'relative',
    // zIndex is set dynamically
  },
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    borderRadius: BorderRadius.lg,
    gap: Spacing.sm,
    marginHorizontal: Spacing.lg,
  },
  input: {
    flex: 1,
    fontSize: FontSize.base,
    paddingVertical: Spacing.xs,
  },
  // ✨ Full screen overlay
  overlay: {
    position: 'absolute',
    top: 50, // Offset for search bar height
    left: -1000, // Cover everything
    right: -1000,
    bottom: -10000, // Cover everything down
    backgroundColor: 'transparent', // Invisible
    zIndex: 9998, // Just below history container
  },
  historyContainer: {
    position: 'absolute',
    top: '100%',
    left: Spacing.lg,
    right: Spacing.lg,
    marginTop: Spacing.xs,
    borderRadius: BorderRadius.lg,
    borderWidth: 1,
    maxHeight: 300,
    ...Shadow.soft,
    zIndex: 9999,
    elevation: Platform.OS === 'android' ? 50 : 10,
  },
  historyItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: Spacing.md,
    gap: Spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(0,0,0,0.05)',
  },
  historyText: {
    flex: 1,
    fontSize: FontSize.base,
  },
  clearHistoryButton: {
    padding: Spacing.md,
    alignItems: 'center',
    borderTopWidth: 1,
  },
  clearHistoryText: {
    fontSize: FontSize.sm,
    fontWeight: '600',
  },
});
