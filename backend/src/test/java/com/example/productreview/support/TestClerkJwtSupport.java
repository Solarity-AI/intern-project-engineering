package com.example.productreview.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

public final class TestClerkJwtSupport {

    private static final RSAKey TEST_RSA_KEY = generateTestKey();

    private TestClerkJwtSupport() {
    }

    public static String publicKeyPem() {
        try {
            byte[] encoded = TEST_RSA_KEY.toRSAPublicKey().getEncoded();
            String base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(encoded);
            return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
        } catch (JOSEException ex) {
            throw new IllegalStateException("Unable to render test public key", ex);
        }
    }

    public static String validToken(String subject) {
        Instant now = Instant.now();
        return token(subject, now.plus(1, ChronoUnit.HOURS), now.minus(1, ChronoUnit.MINUTES));
    }

    public static String validToken(String subject, RSAKey signingKey) {
        Instant now = Instant.now();
        return token(subject, now.plus(1, ChronoUnit.HOURS), now.minus(1, ChronoUnit.MINUTES), "http://localhost:19006", signingKey);
    }

    public static String expiredToken(String subject) {
        Instant now = Instant.now();
        return token(subject, now.minus(5, ChronoUnit.MINUTES), now.minus(10, ChronoUnit.MINUTES));
    }

    public static String futureToken(String subject) {
        Instant now = Instant.now();
        return token(subject, now.plus(1, ChronoUnit.HOURS), now.plus(5, ChronoUnit.MINUTES));
    }

    public static String tokenWithAuthorizedParty(String subject, String authorizedParty) {
        Instant now = Instant.now();
        return token(subject, now.plus(1, ChronoUnit.HOURS), now.minus(1, ChronoUnit.MINUTES), authorizedParty);
    }

    public static String publicJwkSet(RSAKey... keys) {
        return new JWKSet(Arrays.stream(keys)
                .map(RSAKey::toPublicJWK)
                .collect(Collectors.toList()))
                .toString();
    }

    public static RSAKey generateRsaKey(String keyId) {
        try {
            return new RSAKeyGenerator(2048)
                    .keyID(keyId)
                    .generate();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Unable to generate RSA key pair for tests", ex);
        }
    }

    private static String token(String subject, Instant expiration, Instant notBefore) {
        return token(subject, expiration, notBefore, "http://localhost:19006");
    }

    private static String token(String subject, Instant expiration, Instant notBefore, String authorizedParty) {
        return token(subject, expiration, notBefore, authorizedParty, TEST_RSA_KEY);
    }

    private static String token(String subject, Instant expiration, Instant notBefore, String authorizedParty, RSAKey signingKey) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(expiration))
                    .notBeforeTime(Date.from(notBefore))
                    .claim("azp", authorizedParty)
                    .build();

            SignedJWT signedJwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .type(JOSEObjectType.JWT)
                            .keyID(signingKey.getKeyID())
                            .build(),
                    claims);

            JWSSigner signer = new RSASSASigner(signingKey.toPrivateKey());
            signedJwt.sign(signer);
            return signedJwt.serialize();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Unable to generate test Clerk token", ex);
        }
    }

    private static RSAKey generateTestKey() {
        return generateRsaKey("test-clerk-key");
    }
}
