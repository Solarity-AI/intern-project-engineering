type ClerkApiErrorItem = {
  longMessage?: string;
  message?: string;
};

type ClerkApiErrorShape = {
  errors?: ClerkApiErrorItem[];
  message?: string;
};

export const getAuthErrorMessage = (
  error: unknown,
  fallbackMessage = 'Authentication failed. Please try again.'
): string => {
  if (typeof error === 'string' && error.trim().length > 0) {
    return error;
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  if (error && typeof error === 'object') {
    const maybeClerkError = error as ClerkApiErrorShape;
    const firstApiError = maybeClerkError.errors?.[0];
    if (firstApiError?.longMessage) {
      return firstApiError.longMessage;
    }
    if (firstApiError?.message) {
      return firstApiError.message;
    }
    if (maybeClerkError.message && maybeClerkError.message.trim().length > 0) {
      return maybeClerkError.message;
    }
  }

  return fallbackMessage;
};
