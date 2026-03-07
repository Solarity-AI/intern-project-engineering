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
type VerificationStrategy =
  | 'email_code'
  | 'phone_code'
  | 'email_code_mfa'
  | 'phone_code_mfa'
  | 'totp'
  | 'backup_code';

type VerificationConfig = {
  strategy: VerificationStrategy;
  title: string;
  placeholder: string;
  infoMessage: string;
  submitLabel: string;
  resendLabel?: string;
  keyboardType?: 'default' | 'number-pad';
  isSecondFactor: boolean;
};

type SupportedFactor = {
  strategy?: string | null;
};

export const SignInScreen: React.FC<Props> = ({ navigation }) => {
  const { colors } = useTheme();
  const { signIn, errors, fetchStatus } = useSignIn();

  const [emailAddress, setEmailAddress] = useState('');
  const [password, setPassword] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [infoMessage, setInfoMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [verificationConfig, setVerificationConfig] = useState<VerificationConfig | null>(null);

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

  const getCurrentStatus = (attemptStatus?: string | null) => signIn.status ?? attemptStatus ?? null;

  const resetVerificationUi = () => {
    setVerificationCode('');
    setVerificationConfig(null);
    setInfoMessage(null);
  };

  const finalizeSignInSession = async () => {
    const finalizeResult = await signIn.finalize();
    if (finalizeResult.error) {
      setErrorMessage(getAuthErrorMessage(finalizeResult.error, 'Unable to finalize sign in session.'));
      return false;
    }

    resetVerificationUi();
    return true;
  };

  const supportsStrategy = (factors: SupportedFactor[] | null | undefined, strategy: string) =>
    Boolean(factors?.some(factor => factor?.strategy === strategy));

  const beginAdditionalVerification = async (attemptStatus?: string | null) => {
    const status = getCurrentStatus(attemptStatus);
    const firstFactors = (signIn.supportedFirstFactors ?? []) as SupportedFactor[];
    const secondFactors = (signIn.supportedSecondFactors ?? []) as SupportedFactor[];

    setErrorMessage(null);

    if (status === 'needs_first_factor') {
      if (supportsStrategy(firstFactors, 'email_code')) {
        const sendCodeResult = await signIn.emailCode.sendCode();
        if (sendCodeResult.error) {
          setErrorMessage(getAuthErrorMessage(sendCodeResult.error, 'Unable to send sign-in verification code.'));
          return;
        }

        setVerificationConfig({
          strategy: 'email_code',
          title: 'Verify your email',
          placeholder: 'Enter code from your email',
          infoMessage: 'We sent a sign-in code to your email address.',
          submitLabel: 'Verify Email',
          resendLabel: 'Resend Code',
          keyboardType: 'number-pad',
          isSecondFactor: false,
        });
        setVerificationCode('');
        setInfoMessage('We sent a sign-in code to your email address.');
        return;
      }

      if (supportsStrategy(firstFactors, 'phone_code')) {
        const sendCodeResult = await signIn.phoneCode.sendCode();
        if (sendCodeResult.error) {
          setErrorMessage(getAuthErrorMessage(sendCodeResult.error, 'Unable to send sign-in verification code.'));
          return;
        }

        setVerificationConfig({
          strategy: 'phone_code',
          title: 'Verify your phone',
          placeholder: 'Enter code from your phone',
          infoMessage: 'We sent a sign-in code to your phone.',
          submitLabel: 'Verify Phone',
          resendLabel: 'Resend Code',
          keyboardType: 'number-pad',
          isSecondFactor: false,
        });
        setVerificationCode('');
        setInfoMessage('We sent a sign-in code to your phone.');
        return;
      }
    }

    if (status === 'needs_second_factor') {
      if (supportsStrategy(secondFactors, 'email_code')) {
        const sendCodeResult = await signIn.mfa.sendEmailCode();
        if (sendCodeResult.error) {
          setErrorMessage(getAuthErrorMessage(sendCodeResult.error, 'Unable to send the second-factor email code.'));
          return;
        }

        setVerificationConfig({
          strategy: 'email_code_mfa',
          title: 'Second-factor email code',
          placeholder: 'Enter code from your email',
          infoMessage: 'We sent a second-factor code to your email address.',
          submitLabel: 'Complete Sign In',
          resendLabel: 'Resend Code',
          keyboardType: 'number-pad',
          isSecondFactor: true,
        });
        setVerificationCode('');
        setInfoMessage('We sent a second-factor code to your email address.');
        return;
      }

      if (supportsStrategy(secondFactors, 'phone_code')) {
        const sendCodeResult = await signIn.mfa.sendPhoneCode();
        if (sendCodeResult.error) {
          setErrorMessage(getAuthErrorMessage(sendCodeResult.error, 'Unable to send the second-factor phone code.'));
          return;
        }

        setVerificationConfig({
          strategy: 'phone_code_mfa',
          title: 'Second-factor phone code',
          placeholder: 'Enter code from your phone',
          infoMessage: 'We sent a second-factor code to your phone.',
          submitLabel: 'Complete Sign In',
          resendLabel: 'Resend Code',
          keyboardType: 'number-pad',
          isSecondFactor: true,
        });
        setVerificationCode('');
        setInfoMessage('We sent a second-factor code to your phone.');
        return;
      }

      if (supportsStrategy(secondFactors, 'totp')) {
        setVerificationConfig({
          strategy: 'totp',
          title: 'Authenticator code',
          placeholder: 'Enter code from your authenticator app',
          infoMessage: 'Enter the verification code from your authenticator app.',
          submitLabel: 'Complete Sign In',
          keyboardType: 'number-pad',
          isSecondFactor: true,
        });
        setVerificationCode('');
        setInfoMessage('Enter the verification code from your authenticator app.');
        return;
      }

      if (supportsStrategy(secondFactors, 'backup_code')) {
        setVerificationConfig({
          strategy: 'backup_code',
          title: 'Backup code',
          placeholder: 'Enter one of your backup codes',
          infoMessage: 'Use one of your backup codes to complete sign in.',
          submitLabel: 'Complete Sign In',
          keyboardType: 'default',
          isSecondFactor: true,
        });
        setVerificationCode('');
        setInfoMessage('Use one of your backup codes to complete sign in.');
        return;
      }
    }

    if (status === 'needs_new_password') {
      setErrorMessage('This account requires a password reset before you can sign in.');
      return;
    }

    setErrorMessage(
      status
        ? `Sign in requires an additional verification step (${status}) that this screen does not yet support.`
        : 'Unable to determine the next verification step for sign in.'
    );
  };

  const handleVerificationSubmit = async () => {
    if (!verificationConfig) {
      return;
    }

    if (!verificationCode.trim()) {
      setErrorMessage('Verification code is required.');
      return;
    }

    setErrorMessage(null);

    try {
      const code = verificationCode.trim();
      let verificationResult: { error: unknown | null };

      switch (verificationConfig.strategy) {
        case 'email_code':
          verificationResult = await signIn.emailCode.verifyCode({ code });
          break;
        case 'phone_code':
          verificationResult = await signIn.phoneCode.verifyCode({ code });
          break;
        case 'email_code_mfa':
          verificationResult = await signIn.mfa.verifyEmailCode({ code });
          break;
        case 'phone_code_mfa':
          verificationResult = await signIn.mfa.verifyPhoneCode({ code });
          break;
        case 'totp':
          verificationResult = await signIn.mfa.verifyTOTP({ code });
          break;
        case 'backup_code':
          verificationResult = await signIn.mfa.verifyBackupCode({ code });
          break;
      }

      if (verificationResult.error) {
        setErrorMessage(getAuthErrorMessage(verificationResult.error, 'Unable to verify the provided code.'));
        return;
      }

      const currentStatus = getCurrentStatus();
      if (currentStatus === 'complete') {
        await finalizeSignInSession();
        return;
      }

      await beginAdditionalVerification(currentStatus);
    } catch (error) {
      setErrorMessage(getAuthErrorMessage(error, 'Unable to verify the provided code.'));
    }
  };

  const resendVerificationCode = async () => {
    if (!verificationConfig) {
      return;
    }

    setErrorMessage(null);

    try {
      let resendResult: { error: unknown | null } | null = null;

      switch (verificationConfig.strategy) {
        case 'email_code':
          resendResult = await signIn.emailCode.sendCode();
          break;
        case 'phone_code':
          resendResult = await signIn.phoneCode.sendCode();
          break;
        case 'email_code_mfa':
          resendResult = await signIn.mfa.sendEmailCode();
          break;
        case 'phone_code_mfa':
          resendResult = await signIn.mfa.sendPhoneCode();
          break;
        default:
          resendResult = null;
      }

      if (resendResult?.error) {
        setErrorMessage(getAuthErrorMessage(resendResult.error, 'Unable to resend the verification code.'));
        return;
      }

      setInfoMessage(verificationConfig.infoMessage);
    } catch (error) {
      setErrorMessage(getAuthErrorMessage(error, 'Unable to resend the verification code.'));
    }
  };

  const resetSignInAttempt = async () => {
    setErrorMessage(null);
    setInfoMessage(null);
    resetVerificationUi();
    setVerificationCode('');

    try {
      await signIn.reset();
    } catch (error) {
      console.warn('Unable to reset the sign-in attempt cleanly.', error);
    }
  };

  const handleSignIn = async () => {
    if (formValidationError) {
      setErrorMessage(formValidationError);
      return;
    }

    setErrorMessage(null);
    setInfoMessage(null);

    try {
      const signInAttempt = await signIn.password({
        emailAddress: trimmedEmail,
        password,
      });

      if (signInAttempt.error) {
        setErrorMessage(getAuthErrorMessage(signInAttempt.error, 'Unable to sign in. Please check your credentials.'));
        return;
      }

      if (getCurrentStatus((signInAttempt as { status?: string | null }).status) === 'complete') {
        await finalizeSignInSession();
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
        null
      );

      if (!(fieldError ?? globalError)) {
        await beginAdditionalVerification((signInAttempt as { status?: string | null }).status);
      }
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
            {!verificationConfig ? (
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
                  placeholder="Enter your password"
                  placeholderTextColor={colors.mutedForeground}
                  secureTextEntry
                  textContentType="password"
                  autoComplete="password"
                  editable={!isSubmitting}
                />

                <Button
                  onPress={handleSignIn}
                  loading={isSubmitting}
                  disabled={isSubmitting}
                  fullWidth
                  variant="premium"
                >
                  Sign In
                </Button>
              </>
            ) : (
              <>
                <Text style={[styles.label, { color: colors.foreground }]}>{verificationConfig.title}</Text>
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
                  placeholder={verificationConfig.placeholder}
                  placeholderTextColor={colors.mutedForeground}
                  autoCapitalize="none"
                  autoCorrect={false}
                  keyboardType={verificationConfig.keyboardType ?? 'number-pad'}
                  editable={!isSubmitting}
                />

                <Button
                  onPress={handleVerificationSubmit}
                  loading={isSubmitting}
                  disabled={isSubmitting}
                  fullWidth
                  variant="premium"
                >
                  {verificationConfig.submitLabel}
                </Button>

                {verificationConfig.resendLabel ? (
                  <Button
                    onPress={resendVerificationCode}
                    loading={isSubmitting}
                    disabled={isSubmitting}
                    fullWidth
                    variant="outline"
                  >
                    {verificationConfig.resendLabel}
                  </Button>
                ) : null}

                <Button
                  onPress={resetSignInAttempt}
                  loading={isSubmitting}
                  disabled={isSubmitting}
                  fullWidth
                  variant="ghost"
                >
                  Start Over
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
  infoText: {
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
