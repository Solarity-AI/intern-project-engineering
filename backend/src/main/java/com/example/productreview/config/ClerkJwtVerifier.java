package com.example.productreview.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class ClerkJwtVerifier {

    private final ClerkAuthProperties authProperties;
    private volatile RSAPublicKey verificationKey;

    public ClerkJwtVerifier(ClerkAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @PostConstruct
    void validateConfiguration() {
        if (!authProperties.isEnabled()) {
            return;
        }

        if (authProperties.getVerificationKey() == null || authProperties.getVerificationKey().isBlank()) {
            throw new IllegalStateException(
                    "clerk.auth.verification-key must be configured when Clerk authentication is enabled");
        }

        verificationKey = parseVerificationKey(authProperties.getVerificationKey());
    }

    public VerifiedClerkToken verify(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);

            if (!JWSAlgorithm.RS256.equals(signedJwt.getHeader().getAlgorithm())) {
                throw new ClerkAuthenticationException("Unsupported authentication token algorithm");
            }

            boolean signatureValid = signedJwt.verify(new RSASSAVerifier(getVerificationKey()));
            if (!signatureValid) {
                throw new ClerkAuthenticationException("Invalid authentication token");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            validateClaims(claims);

            return new VerifiedClerkToken(claims.getSubject());
        } catch (ClerkAuthenticationException ex) {
            throw ex;
        } catch (JOSEException ex) {
            throw new ClerkAuthenticationException("Invalid authentication token", ex);
        } catch (Exception ex) {
            throw new ClerkAuthenticationException("Unable to verify authentication token", ex);
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        Instant now = Instant.now();

        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || !expirationTime.toInstant().isAfter(now)) {
            throw new ClerkAuthenticationException("Authentication token has expired");
        }

        Date notBeforeTime = claims.getNotBeforeTime();
        if (notBeforeTime != null && now.isBefore(notBeforeTime.toInstant())) {
            throw new ClerkAuthenticationException("Authentication token is not yet valid");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ClerkAuthenticationException("Authentication token subject is missing");
        }

        List<String> authorizedParties = authProperties.getAuthorizedParties();
        if (authorizedParties != null && !authorizedParties.isEmpty()) {
            Object authorizedParty = claims.getClaim("azp");
            if (!(authorizedParty instanceof String azp) || !authorizedParties.contains(azp)) {
                throw new ClerkAuthenticationException("Authentication token has an invalid authorized party");
            }
        }
    }

    private RSAPublicKey getVerificationKey() {
        RSAPublicKey key = verificationKey;
        if (key != null) {
            return key;
        }

        synchronized (this) {
            if (verificationKey == null) {
                verificationKey = parseVerificationKey(authProperties.getVerificationKey());
            }
            return verificationKey;
        }
    }

    private RSAPublicKey parseVerificationKey(String rawVerificationKey) {
        String normalizedKey = rawVerificationKey.trim().replace("\\n", "\n");

        try {
            if (normalizedKey.startsWith("{")) {
                return parseJwkKey(normalizedKey);
            }
            return parsePemKey(normalizedKey);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse Clerk verification key", ex);
        }
    }

    private RSAPublicKey parseJwkKey(String jsonKey) throws Exception {
        if (jsonKey.contains("\"keys\"")) {
            JWKSet jwkSet = JWKSet.parse(jsonKey);
            if (jwkSet.getKeys().isEmpty()) {
                throw new IllegalStateException("Clerk JWKS does not contain any keys");
            }
            return toRsaPublicKey(jwkSet.getKeys().get(0));
        }

        return toRsaPublicKey(JWK.parse(jsonKey));
    }

    private RSAPublicKey toRsaPublicKey(JWK jwk) throws JOSEException {
        if (!(jwk instanceof RSAKey rsaKey)) {
            throw new IllegalStateException("Clerk verification key must be an RSA public key");
        }
        return rsaKey.toRSAPublicKey();
    }

    private RSAPublicKey parsePemKey(String pemKey) throws Exception {
        String normalizedPem = pemKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decodedKey = Base64.getDecoder().decode(normalizedPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

        if (!(publicKey instanceof RSAPublicKey rsaPublicKey)) {
            throw new IllegalStateException("Clerk verification key must be an RSA public key");
        }

        return rsaPublicKey;
    }

    public record VerifiedClerkToken(String subject) {
    }
}
