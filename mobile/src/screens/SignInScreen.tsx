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
import { useSignIn } from '@clerk/expo';
import { NativeStackScreenProps } from '@react-navigation/native-stack';

import { ScreenWrapper } from '../components/ScreenWrapper';
import { Button } from '../components/Button';
import { useTheme } from '../context/ThemeContext';
import { RootStackParamList } from '../types';
import { getAuthErrorMessage } from './authErrorMessage';

type Props = NativeStackScreenProps<RootStackParamList, 'SignIn'>;

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const SignInScreen: React.FC<Props> = ({ navigation }) => {
  const { colors } = useTheme();
  const { signIn, errors, fetchStatus } = useSignIn();

  const [emailAddress, setEmailAddress] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const trimmedEmail = emailAddress.trim().toLowerCase();
  const isSubmitting = fetchStatus === 'fetching';

  const formValidationError = useMemo(() => {
    if (!trimmedEmail) {
      return 'Email is required.';
    }
    if (!EMAIL_REGEX.test(trimmedEmail)) {
      return 'Please enter a valid email address.';
    }
    if (!password.trim()) {
      return 'Password is required.';
    }
    return null;
  }, [password, trimmedEmail]);

  const handleSignIn = async () => {
    if (formValidationError) {
      setErrorMessage(formValidationError);
      return;
    }

    setErrorMessage(null);

    try {
      const signInAttempt = await signIn.password({
        emailAddress: trimmedEmail,
        password,
      });

      if (signInAttempt.error) {
        setErrorMessage(getAuthErrorMessage(signInAttempt.error, 'Unable to sign in. Please check your credentials.'));
        return;
      }

      if (signIn.status === 'complete') {
        const finalizeResult = await signIn.finalize();
        if (finalizeResult.error) {
          setErrorMessage(getAuthErrorMessage(finalizeResult.error, 'Unable to finalize sign in session.'));
        }
        return;
      }

      const fieldError =
        errors.fields.identifier?.longMessage ??
        errors.fields.identifier?.message ??
        errors.fields.password?.longMessage ??
        errors.fields.password?.message;
      const globalError =
        errors.global?.[0]?.longMessage ??
        errors.global?.[0]?.message;

      setErrorMessage(
        fieldError ??
        globalError ??
        'Additional verification is required to complete sign in.'
      );
    } catch (error) {
      setErrorMessage(getAuthErrorMessage(error, 'Unable to sign in. Please check your credentials.'));
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
            <Text style={[styles.title, { color: colors.foreground }]}>Welcome back</Text>
            <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
              Sign in to continue using Product Review.
            </Text>
          </View>

          <View style={styles.formContainer}>
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
              placeholder="Enter your password"
              placeholderTextColor={colors.mutedForeground}
              secureTextEntry
              textContentType="password"
              autoComplete="password"
              editable={!isSubmitting}
            />

            {errorMessage ? (
              <Text style={[styles.errorText, { color: colors.destructive }]}>{errorMessage}</Text>
            ) : null}

            <Button
              onPress={handleSignIn}
              loading={isSubmitting}
              disabled={isSubmitting}
              fullWidth
              variant="premium"
            >
              Sign In
            </Button>

            <TouchableOpacity
              onPress={() => navigation.navigate('SignUp')}
              disabled={isSubmitting}
              style={styles.switchLink}
            >
              <Text style={[styles.switchText, { color: colors.mutedForeground }]}>
                Need an account?{' '}
                <Text style={[styles.switchTextStrong, { color: colors.primary }]}>Create one</Text>
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
