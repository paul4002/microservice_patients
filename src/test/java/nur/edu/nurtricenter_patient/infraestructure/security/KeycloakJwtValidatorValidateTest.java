package nur.edu.nurtricenter_patient.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

class KeycloakJwtValidatorValidateTest {

  private static final String ISSUER = "http://keycloak:8080/realms/classroom";
  private static final String CLIENT_ID = "api-gateway";
  private static final String KEY_ID = "test-key";

  private static RSAKey rsaKey;
  private static JWKSet jwkSet;
  private static KeycloakProperties properties;

  @BeforeAll
  static void generateKeys() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair keyPair = kpg.generateKeyPair();

    rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
        .privateKey((RSAPrivateKey) keyPair.getPrivate())
        .keyID(KEY_ID)
        .build();
    jwkSet = new JWKSet(rsaKey);

    properties = new KeycloakProperties();
    properties.setIssuer(ISSUER);
    properties.setClientId(CLIENT_ID);
  }

  private String buildToken(JWTClaimsSet claims) throws Exception {
    SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY_ID).build(),
        claims
    );
    jwt.sign(new RSASSASigner(rsaKey));
    return jwt.serialize();
  }

  private KeycloakJwtValidator validatorWith(JWKSet set) {
    KeycloakJwksService jwksService = mock(KeycloakJwksService.class);
    when(jwksService.getJwks(false)).thenReturn(set);
    when(jwksService.getJwks(true)).thenReturn(set);
    return new KeycloakJwtValidator(properties, jwksService);
  }

  @Test
  void validateAndExtract_validToken_returnsClaims() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("test-user")
        .issuer(ISSUER)
        .audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .claim("preferred_username", "admin")
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    Map<String, Object> result = validator.validateAndExtract(buildToken(claims));

    assertEquals("test-user", result.get("sub"));
    assertEquals("admin", result.get("preferred_username"));
  }

  @Test
  void validateAndExtract_invalidToken_throwsJwtValidationException() {
    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract("not.a.jwt"));
  }

  @Test
  void validateAndExtract_emptyJwks_throwsJwtValidationException() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .build();

    KeycloakJwksService jwksService = mock(KeycloakJwksService.class);
    when(jwksService.getJwks(false)).thenReturn(new JWKSet());
    when(jwksService.getJwks(true)).thenReturn(new JWKSet());
    KeycloakJwtValidator validator = new KeycloakJwtValidator(properties, jwksService);

    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(buildToken(claims)));
  }

  @Test
  void validateAndExtract_wrongIssuer_throwsJwtValidationException() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u")
        .issuer("http://other-issuer/realms/other")
        .audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(buildToken(claims)));
  }

  @Test
  void validateAndExtract_wrongAudience_throwsJwtValidationException() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience("other-client")
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(buildToken(claims)));
  }

  @Test
  void validateAndExtract_expiredToken_throwsJwtValidationException() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() - 60_000))
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(buildToken(claims)));
  }

  @Test
  void validateAndExtract_validAudienceViaAzp_succeeds() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience("account")
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .claim("azp", CLIENT_ID)
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    Map<String, Object> result = validator.validateAndExtract(buildToken(claims));
    assertNotNull(result);
    assertEquals("u", result.get("sub"));
  }

  @Test
  void validateAndExtract_tokenWithNotBefore_inFuture_throws() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 120_000))
        .notBeforeTime(new Date(System.currentTimeMillis() + 60_000))
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(buildToken(claims)));
  }

  @Test
  void validateAndExtract_tokenWithNoKid_stillValidates() throws Exception {
    RSAKey keyNoKid = new RSAKey.Builder((RSAPublicKey) rsaKey.toRSAPublicKey())
        .privateKey(rsaKey.toRSAPrivateKey())
        .build();
    JWKSet noKidSet = new JWKSet(keyNoKid);

    SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
        new JWTClaimsSet.Builder()
            .subject("u").issuer(ISSUER).audience(CLIENT_ID)
            .expirationTime(new Date(System.currentTimeMillis() + 60_000))
            .build()
    );
    jwt.sign(new RSASSASigner(keyNoKid));

    KeycloakJwksService jwksService = mock(KeycloakJwksService.class);
    when(jwksService.getJwks(false)).thenReturn(noKidSet);
    when(jwksService.getJwks(true)).thenReturn(noKidSet);
    KeycloakJwtValidator validator = new KeycloakJwtValidator(properties, jwksService);

    Map<String, Object> result = validator.validateAndExtract(jwt.serialize());
    assertEquals("u", result.get("sub"));
  }

  @Test
  void validateAndExtract_firstJwksFailsThenSucceeds_retriesWithForceRefresh() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .build();
    String token = buildToken(claims);

    KeycloakJwksService jwksService = mock(KeycloakJwksService.class);
    when(jwksService.getJwks(false)).thenReturn(new JWKSet());
    when(jwksService.getJwks(true)).thenReturn(jwkSet);

    KeycloakJwtValidator validator = new KeycloakJwtValidator(properties, jwksService);
    Map<String, Object> result = validator.validateAndExtract(token);
    assertEquals("u", result.get("sub"));
  }

  @Test
  void validateAndExtract_missingIssuerInToken_throws() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(buildToken(claims)));
  }

  @Test
  void validateAndExtract_signedWithWrongKey_throws() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair wrongKeyPair = kpg.generateKeyPair();
    RSAKey wrongKey = new RSAKey.Builder((RSAPublicKey) wrongKeyPair.getPublic())
        .privateKey((RSAPrivateKey) wrongKeyPair.getPrivate())
        .keyID(KEY_ID)
        .build();

    SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY_ID).build(),
        new JWTClaimsSet.Builder()
            .subject("u").issuer(ISSUER).audience(CLIENT_ID)
            .expirationTime(new Date(System.currentTimeMillis() + 60_000))
            .build()
    );
    jwt.sign(new RSASSASigner(wrongKey));

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(jwt.serialize()));
  }

  @Test
  void validateAndExtract_tokenWithMismatchedKid_throws() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .build();
    SignedJWT jwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("different-kid").build(),
        claims
    );
    jwt.sign(new RSASSASigner(rsaKey));

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(jwt.serialize()));
  }

  @Test
  void validateAndExtract_missingExpiry_throws() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience(CLIENT_ID)
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    assertThrows(JwtValidationException.class, () -> validator.validateAndExtract(buildToken(claims)));
  }

  @Test
  void validateAndExtract_extractRolesFromValidToken() throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("u").issuer(ISSUER).audience(CLIENT_ID)
        .expirationTime(new Date(System.currentTimeMillis() + 60_000))
        .claim("realm_access", Map.of("roles", List.of("admin", "nutritionist")))
        .build();

    KeycloakJwtValidator validator = validatorWith(jwkSet);
    Map<String, Object> result = validator.validateAndExtract(buildToken(claims));
    List<String> roles = validator.extractRoles(result);
    assertEquals(2, roles.size());
  }
}
