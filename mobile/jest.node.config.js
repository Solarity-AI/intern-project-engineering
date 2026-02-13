/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  displayName: 'unit',
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/src'],
  testMatch: ['**/__tests__/**/*.test.ts'],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx'],
  transform: {
    '^.+\\.tsx?$': ['ts-jest', { tsconfig: 'tsconfig.json' }],
  },
  moduleNameMapper: {
    '^@react-native-async-storage/async-storage$': '<rootDir>/src/services/__tests__/__mocks__/async-storage.ts',
    '^react-native-get-random-values$': '<rootDir>/src/services/__tests__/__mocks__/empty.ts',
    '^uuid$': '<rootDir>/src/services/__tests__/__mocks__/uuid.ts',
  },
};
