module.exports = {
  displayName: 'ui',
  preset: 'jest-expo',
  roots: ['<rootDir>/src'],
  testMatch: ['**/__tests__/ui/**/*.ui.test.tsx'],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx'],
  transformIgnorePatterns: [
    'node_modules/(?!((jest-)?react-native|@react-native(-community)?|expo(nent)?|@expo(nent)?/.*|@expo-google-fonts/.*|react-navigation|@react-navigation/.*|@unimodules/.*|unimodules|sentry-expo|native-base|react-native-svg|expo-modules-core))',
  ],
  moduleNameMapper: {
    '^@react-native-async-storage/async-storage$': '<rootDir>/src/services/__tests__/__mocks__/async-storage.ts',
    '^react-native-get-random-values$': '<rootDir>/src/services/__tests__/__mocks__/empty.ts',
    '^uuid$': '<rootDir>/src/services/__tests__/__mocks__/uuid.ts',
  },
};
