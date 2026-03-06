import React from 'react';

/**
 * Signals that the Clerk auth token provider has been installed in the API
 * layer and is ready to vend session tokens.
 *
 * AuthBootstrap sets this to true synchronously (as a state update queued in
 * its useEffect) immediately after calling setAuthTokenProvider().  Because
 * React fires useEffect hooks bottom-up (child before parent), WishlistContext
 * and NotificationContext would otherwise start fetching before the token
 * provider is available.  By reading this context and waiting for it to be
 * true, those contexts avoid sending unauthenticated requests on sign-in.
 */
export const AuthTokenReadyContext = React.createContext<boolean>(false);
