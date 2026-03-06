process.env.EXPO_PUBLIC_API_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080';
globalThis.IS_REACT_ACT_ENVIRONMENT = true;

jest.mock('@clerk/expo', () => {
  const resolvedResult = Promise.resolve({ error: null });

  return {
    ClerkProvider: ({ children }) => children,
    useAuth: () => ({
      isLoaded: true,
      isSignedIn: true,
      getToken: async () => 'test-session-token',
      sessionId: 'sess_test',
      userId: 'user_test',
    }),
    useClerk: () => ({
      signOut: jest.fn().mockResolvedValue(undefined),
    }),
    useSignIn: () => ({
      signIn: {
        status: null,
        supportedFirstFactors: [],
        supportedSecondFactors: [],
        password: jest.fn().mockResolvedValue({ error: null, status: 'complete' }),
        finalize: jest.fn().mockResolvedValue({ error: null }),
        reset: jest.fn().mockResolvedValue({ error: null }),
        emailCode: {
          sendCode: jest.fn().mockResolvedValue({ error: null }),
          verifyCode: jest.fn().mockResolvedValue({ error: null }),
        },
        phoneCode: {
          sendCode: jest.fn().mockResolvedValue({ error: null }),
          verifyCode: jest.fn().mockResolvedValue({ error: null }),
        },
        mfa: {
          sendEmailCode: jest.fn().mockResolvedValue({ error: null }),
          verifyEmailCode: jest.fn().mockResolvedValue({ error: null }),
          sendPhoneCode: jest.fn().mockResolvedValue({ error: null }),
          verifyPhoneCode: jest.fn().mockResolvedValue({ error: null }),
          verifyTOTP: jest.fn().mockResolvedValue({ error: null }),
          verifyBackupCode: jest.fn().mockResolvedValue({ error: null }),
        },
      },
      errors: { fields: {}, global: [] },
      fetchStatus: 'idle',
    }),
    useSignUp: () => ({
      signUp: {
        status: null,
        password: jest.fn().mockResolvedValue({ error: null, status: 'missing_requirements' }),
        finalize: jest.fn().mockResolvedValue({ error: null }),
        verifications: {
          sendEmailCode: jest.fn().mockResolvedValue({ error: null }),
          verifyEmailCode: jest.fn().mockResolvedValue({ error: null }),
        },
      },
      errors: { fields: {}, global: [] },
      fetchStatus: 'idle',
    }),
  };
});
