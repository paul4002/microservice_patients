package nur.edu.nurtricenter_patient.infraestructure.security;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class KeycloakJwtValidator {

  private final KeycloakProperties properties;
  private final KeycloakJwksService jwksService;

  public KeycloakJwtValidator(KeycloakProperties properties, KeycloakJwksService jwksService) {
    this.properties = properties;
    this.jwksService = jwksService;
  }

  public Map<String, Object> validateAndExtract(String token) {
    try {
      return decodeAndValidate(token, false);
    } catch (JwtValidationException e) {
      return decodeAndValidate(token, true);
    }
  }

  public List<String> extractRoles(Map<String, Object> claims) {
    Set<String> roles = new LinkedHashSet<>();

    Object realmAccessObj = claims.get("realm_access");
    if (realmAccessObj instanceof Map<?, ?> realmAccess) {
      Object realmRolesObj = realmAccess.get("roles");
      if (realmRolesObj instanceof Collection<?> realmRoles) {
        for (Object role : realmRoles) {
          if (role instanceof String roleName && !roleName.isBlank()) {
            roles.add(roleName);
          }
        }
      }
    }

    Object resourceAccessObj = claims.get("resource_access");
    if (resourceAccessObj instanceof Map<?, ?> resourceAccess) {
      Object clientObj = resourceAccess.get(properties.getClientId());
      if (clientObj instanceof Map<?, ?> clientAccess) {
        Object clientRolesObj = clientAccess.get("roles");
        if (clientRolesObj instanceof Collection<?> clientRoles) {
          for (Object role : clientRoles) {
            if (role instanceof String roleName && !roleName.isBlank()) {
              roles.add(roleName);
            }
          }
        }
      }
    }

    return new ArrayList<>(roles);
  }

  private Map<String, Object> decodeAndValidate(String token, boolean forceJwksRefresh) {
    SignedJWT jwt = parseToken(token);
    JWKSet jwkSet = jwksService.getJwks(forceJwksRefresh);
    verifySignature(jwt, jwkSet);

    JWTClaimsSet claimsSet;
    try {
      claimsSet = jwt.getJWTClaimsSet();
    } catch (ParseException e) {
      throw new JwtValidationException("Unable to parse claims", e);
    }

    validateIssuer(claimsSet);
    validateAudience(claimsSet);
    validateTimeClaims(claimsSet);

    return claimsSet.getClaims();
  }

  private SignedJWT parseToken(String token) {
    try {
      return SignedJWT.parse(token);
    } catch (ParseException e) {
      throw new JwtValidationException("Invalid JWT", e);
    }
  }

  private void verifySignature(SignedJWT jwt, JWKSet jwkSet) {
    List<JWK> keys = jwkSet.getKeys();
    if (keys == null || keys.isEmpty()) {
      throw new JwtValidationException("JWKS has no keys");
    }

    String kid = jwt.getHeader().getKeyID();
    for (JWK key : keys) {
      if (!(key instanceof RSAKey rsaKey)) {
        continue;
      }

      if (kid != null && !kid.isBlank() && !kid.equals(rsaKey.getKeyID())) {
        continue;
      }

      try {
        JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());
        if (jwt.verify(verifier)) {
          return;
        }
      } catch (JOSEException e) {
        throw new JwtValidationException("Unable to verify JWT signature", e);
      }
    }

    throw new JwtValidationException("JWT signature verification failed");
  }

  private void validateIssuer(JWTClaimsSet claimsSet) {
    String issuer = claimsSet.getIssuer();
    if (issuer == null || !issuer.equals(properties.getIssuer())) {
      throw new JwtValidationException("Invalid issuer");
    }
  }

  private void validateAudience(JWTClaimsSet claimsSet) {
    List<String> audience = claimsSet.getAudience();
    Object azpObj = claimsSet.getClaim("azp");
    String azp = azpObj instanceof String ? (String) azpObj : null;
    String expected = properties.getClientId();

    if (audience.contains(expected)) {
      return;
    }

    if (audience.contains("account") && expected.equals(azp)) {
      return;
    }

    throw new JwtValidationException("Invalid audience");
  }

  private void validateTimeClaims(JWTClaimsSet claimsSet) {
    Instant now = Instant.now();

    Date exp = claimsSet.getExpirationTime();
    if (exp == null || now.isAfter(exp.toInstant())) {
      throw new JwtValidationException("Token expired");
    }

    Date nbf = claimsSet.getNotBeforeTime();
    if (nbf != null && now.isBefore(nbf.toInstant())) {
      throw new JwtValidationException("Token not active yet");
    }
  }
}
