import React, { useMemo, useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSignUp } from '@clerk/expo';
import { NativeStackScreenProps } from '@react-navigation/native-stack';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { Button } from '../components/Button';
import { useTheme } from '../context/ThemeContext';
import { RootStackParamList } from '../types';
import { getAuthErrorMessage } from './authErrorMessage';

type Props = NativeStackScreenProps<RootStackParamList, 'SignUp'>;

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LENGTH = 8;

export const SignUpScreen: React.FC<Props> = ({ navigation }) => {
  const { colors } = useTheme();
  const { signUp, errors, fetchStatus } = useSignUp();

  const [emailAddress, setEmailAddress] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [isPendingVerification, setIsPendingVerification] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);

  const trimmedEmail = emailAddress.trim().toLowerCase();
  const isSubmitting = fetchStatus === 'fetching';

  const formValidationError = useMemo(() => {
    if (!trimmedEmail) {
      return 'Email is required.';
    }
    if (!EMAIL_REGEX.test(trimmedEmail)) {
      return 'Please enter a valid email address.';
    }
    if (password.length < MIN_PASSWORD_LENGTH) {
      return `Password must be at least ${MIN_PASSWORD_LENGTH} characters.`;
    }
    if (password !== confirmPassword) {
      return 'Passwords do not match.';
    }
    return null;
  }, [confirmPassword, password, trimmedEmail]);

  const finalizeSignUpSession = async () => {
    const finalizeResult = await signUp.finalize();
    if (finalizeResult.error) {
      setErrorMessage(getAuthErrorMessage(finalizeResult.error, 'Unable to initialize session after sign up.'));
      return false;
    }

    return true;
  };

  const handleSignUp = async () => {
    if (formValidationError) {
      setErrorMessage(formValidationError);
      return;
    }

    setErrorMessage(null);
    setInfoMessage(null);

    try {
      const signUpAttempt = await signUp.password({
        emailAddress: trimmedEmail,
        password,
      });

      if (signUpAttempt.error) {
        setErrorMessage(getAuthErrorMessage(signUpAttempt.error, 'Unable to create your account right now.'));
        return;
      }

      if (signUp.status === 'complete') {
        await finalizeSignUpSession();
        return;
      }

      const sendCodeResult = await signUp.verifications.sendEmailCode();
      if (sendCodeResult.error) {
        setErrorMessage(getAuthErrorMessage(sendCodeResult.error, 'Unable to send verification code.'));
        return;
      }

      setIsPendingVerification(true);
      setInfoMessage('We sent a verification code to your email.');
    } catch (error) {
      setErrorMessage(getAuthErrorMessage(error, 'Unable to create your account right now.'));
    }
  };

  const handleVerifyCode = async () => {
    if (!verificationCode.trim()) {
      setErrorMessage('Verification code is required.');
      return;
    }

    setErrorMessage(null);

    try {
      const verificationAttempt = await signUp.verifications.verifyEmailCode({
        code: verificationCode.trim(),
      });

      if (verificationAttempt.error) {
        setErrorMessage(getAuthErrorMessage(verificationAttempt.error, 'Unable to verify the code.'));
        return;
      }

      if (signUp.status === 'complete') {
        await finalizeSignUpSession();
        return;
      }

      const fieldError =
        errors.fields.code?.longMessage ??
        errors.fields.code?.message;
      const globalError =
        errors.global?.[0]?.longMessage ??
        errors.global?.[0]?.message;

      setErrorMessage(fieldError ?? globalError ?? 'Verification is not complete yet. Please check your code and try again.');
    } catch (error) {
      setErrorMessage(getAuthErrorMessage(error, 'Unable to verify the code.'));
    }
  };

  const resendCode = async () => {
    if (!isPendingVerification) {
      return;
    }

    setErrorMessage(null);

    try {
      const sendCodeResult = await signUp.verifications.sendEmailCode();
      if (sendCodeResult.error) {
        setErrorMessage(getAuthErrorMessage(sendCodeResult.error, 'Unable to resend verification code.'));
        return;
      }

      setInfoMessage('A new verification code was sent.');
    } catch (error) {
      setErrorMessage(getAuthErrorMessage(error, 'Unable to resend verification code.'));
    }
  };

  return (
    <ScreenWrapper edges={['top', 'left', 'right', 'bottom']}>
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <ScrollView
          contentContainerStyle={styles.contentContainer}
          keyboardShouldPersistTaps="handled"
        >
          <View style={styles.headerContainer}>
            <Text style={[styles.title, { color: colors.foreground }]}>Create account</Text>
            <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
              Sign up with email and password to start your session.
            </Text>
          </View>

          <View style={styles.formContainer}>
            {!isPendingVerification ? (
              <>
                <Text style={[styles.label, { color: colors.foreground }]}>Email</Text>
                <TextInput
                  style={[
                    styles.input,
                    {
                      borderColor: colors.border,
                      backgroundColor: colors.card,
                      color: colors.foreground,
                    },
                  ]}
                  value={emailAddress}
                  onChangeText={setEmailAddress}
                  placeholder="you@example.com"
                  placeholderTextColor={colors.mutedForeground}
                  autoCapitalize="none"
                  autoCorrect={false}
                  keyboardType="email-address"
                  textContentType="emailAddress"
                  autoComplete="email"
                  editable={!isSubmitting}
                />

                <Text style={[styles.label, { color: colors.foreground }]}>Password</Text>
                <TextInput
                  style={[
                    styles.input,
                    {
                      borderColor: colors.border,
                      backgroundColor: colors.card,
                      color: colors.foreground,
                    },
                  ]}
                  value={password}
                  onChangeText={setPassword}
                  placeholder={`At least ${MIN_PASSWORD_LENGTH} characters`}
                  placeholderTextColor={colors.mutedForeground}
                  secureTextEntry
                  textContentType="newPassword"
                  autoComplete="new-password"
                  editable={!isSubmitting}
                />

                <Text style={[styles.label, { color: colors.foreground }]}>Confirm password</Text>
                <TextInput
                  style={[
                    styles.input,
                    {
                      borderColor: colors.border,
                      backgroundColor: colors.card,
                      color: colors.foreground,
                    },
                  ]}
                  value={confirmPassword}
                  onChangeText={setConfirmPassword}
                  placeholder="Re-enter your password"
                  placeholderTextColor={colors.mutedForeground}
                  secureTextEntry
                  textContentType="password"
                  autoComplete="password"
                  editable={!isSubmitting}
                />

                <Button
                  onPress={handleSignUp}
                  loading={isSubmitting}
                  disabled={isSubmitting}
                  fullWidth
                  variant="premium"
                >
                  Create Account
                </Button>
              </>
            ) : (
              <>
                <Text style={[styles.label, { color: colors.foreground }]}>Verification code</Text>
                <TextInput
                  style={[
                    styles.input,
                    {
                      borderColor: colors.border,
                      backgroundColor: colors.card,
                      color: colors.foreground,
                    },
                  ]}
                  value={verificationCode}
                  onChangeText={setVerificationCode}
                  placeholder="Enter code from your email"
                  placeholderTextColor={colors.mutedForeground}
                  keyboardType="number-pad"
                  autoCapitalize="none"
                  autoCorrect={false}
                  editable={!isSubmitting}
                />

                <Button
                  onPress={handleVerifyCode}
                  loading={isSubmitting}
                  disabled={isSubmitting}
                  fullWidth
                  variant="premium"
                >
                  Verify Email
                </Button>

                <Button
                  onPress={resendCode}
                  loading={isSubmitting}
                  disabled={isSubmitting}
                  fullWidth
                  variant="outline"
                >
                  Resend Code
                </Button>
              </>
            )}

            {infoMessage ? (
              <Text style={[styles.infoText, { color: colors.primary }]}>{infoMessage}</Text>
            ) : null}

            {errorMessage ? (
              <Text style={[styles.errorText, { color: colors.destructive }]}>{errorMessage}</Text>
            ) : null}

            <TouchableOpacity
              onPress={() => navigation.navigate('SignIn')}
              disabled={isSubmitting}
              style={styles.switchLink}
            >
              <Text style={[styles.switchText, { color: colors.mutedForeground }]}>
                Already have an account?{' '}
                <Text style={[styles.switchTextStrong, { color: colors.primary }]}>Sign in</Text>
              </Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </ScreenWrapper>
  );
};

const styles = StyleSheet.create({
  flex: {
    flex: 1,
  },
  contentContainer: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
    paddingVertical: 24,
    gap: 24,
  },
  headerContainer: {
    gap: 6,
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
  },
  subtitle: {
    fontSize: 15,
    lineHeight: 22,
  },
  formContainer: {
    gap: 12,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
  },
  input: {
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
  },
  infoText: {
    fontSize: 13,
    lineHeight: 18,
  },
  errorText: {
    fontSize: 13,
    lineHeight: 18,
  },
  switchLink: {
    marginTop: 4,
    alignItems: 'center',
    paddingVertical: 6,
  },
  switchText: {
    fontSize: 14,
  },
  switchTextStrong: {
    fontWeight: '700',
  },
});
