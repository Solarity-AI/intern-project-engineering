package com.example.productreview.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.example.productreview.support.TestClerkJwtSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClerkJwtVerifierTest {

    private ClerkAuthProperties authProperties;
    private ClerkJwtVerifier verifier;

    @BeforeEach
    void setUp() {
        authProperties = new ClerkAuthProperties();
        authProperties.setEnabled(true);
        authProperties.setVerificationKey(TestClerkJwtSupport.publicKeyPem());
        verifier = new ClerkJwtVerifier(authProperties);
        verifier.validateConfiguration();
    }

    @Test
    void verify_WithValidToken_ShouldReturnSubject() {
        ClerkJwtVerifier.VerifiedClerkToken verifiedToken = verifier.verify(
                TestClerkJwtSupport.validToken("clerk-user-123"));

        assertEquals("clerk-user-123", verifiedToken.subject());
    }

    @Test
    void verify_WithExpiredToken_ShouldThrowAuthenticationException() {
        ClerkAuthenticationException exception = assertThrows(ClerkAuthenticationException.class,
                () -> verifier.verify(TestClerkJwtSupport.expiredToken("expired-user")));

        assertEquals("Authentication token has expired", exception.getMessage());
    }

    @Test
    void verify_WithFutureToken_ShouldThrowAuthenticationException() {
        ClerkAuthenticationException exception = assertThrows(ClerkAuthenticationException.class,
                () -> verifier.verify(TestClerkJwtSupport.futureToken("future-user")));

        assertEquals("Authentication token is not yet valid", exception.getMessage());
    }

    @Test
    void verify_WithInvalidSignature_ShouldThrowAuthenticationException() {
        String invalidToken = TestClerkJwtSupport.validToken("user") + "tampered";

        ClerkAuthenticationException exception = assertThrows(ClerkAuthenticationException.class,
                () -> verifier.verify(invalidToken));

        assertTrue(exception.getMessage().contains("authentication token"));
    }

    @Test
    void verify_WithUnexpectedAuthorizedParty_ShouldThrowAuthenticationException() {
        authProperties.setAuthorizedParties(List.of("https://allowed.example"));
        verifier = new ClerkJwtVerifier(authProperties);
        verifier.validateConfiguration();

        ClerkAuthenticationException exception = assertThrows(ClerkAuthenticationException.class,
                () -> verifier.verify(TestClerkJwtSupport.tokenWithAuthorizedParty(
                        "user-with-bad-azp", "https://unexpected.example")));

        assertEquals("Authentication token has an invalid authorized party", exception.getMessage());
    }

    @Test
    void verify_WithMatchingKidInMultiKeyJwkSet_ShouldReturnSubject() {
        RSAKey activeKey = TestClerkJwtSupport.generateRsaKey("active-key");
        RSAKey staleKey = TestClerkJwtSupport.generateRsaKey("stale-key");
        authProperties.setVerificationKey(TestClerkJwtSupport.publicJwkSet(staleKey, activeKey));
        verifier = new ClerkJwtVerifier(authProperties);
        verifier.validateConfiguration();

        ClerkJwtVerifier.VerifiedClerkToken verifiedToken = verifier.verify(
                TestClerkJwtSupport.validToken("rotated-user", activeKey));

        assertEquals("rotated-user", verifiedToken.subject());
    }

    @Test
    void verify_WithUnknownKidInMultiKeyJwkSet_ShouldThrowAuthenticationException() {
        RSAKey activeKey = TestClerkJwtSupport.generateRsaKey("active-key");
        RSAKey staleKey = TestClerkJwtSupport.generateRsaKey("stale-key");
        RSAKey unknownKey = TestClerkJwtSupport.generateRsaKey("unknown-key");
        authProperties.setVerificationKey(TestClerkJwtSupport.publicJwkSet(staleKey, activeKey));
        verifier = new ClerkJwtVerifier(authProperties);
        verifier.validateConfiguration();

        ClerkAuthenticationException exception = assertThrows(ClerkAuthenticationException.class,
                () -> verifier.verify(TestClerkJwtSupport.validToken("rotated-user", unknownKey)));

        assertEquals("Authentication token references an unknown verification key", exception.getMessage());
    }
}
