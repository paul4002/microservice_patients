package nur.edu.nurtricenter_patient.infraestructure.security;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.JWKSet;

@Service
public class KeycloakJwksService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakJwksService.class);

  private final KeycloakProperties properties;
  private final HttpClient httpClient;

  private volatile JWKSet cachedJwks;
  private volatile Instant cachedAt;

  public KeycloakJwksService(KeycloakProperties properties) {
    this.properties = properties;
    this.httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(2))
      .build();
  }

  public JWKSet getJwks(boolean forceRefresh) {
    if (!forceRefresh && isCacheValid()) {
      return cachedJwks;
    }

    synchronized (this) {
      if (!forceRefresh && isCacheValid()) {
        return cachedJwks;
      }

      JWKSet jwkSet = fetchJwks();
      cachedJwks = jwkSet;
      cachedAt = Instant.now();
      return jwkSet;
    }
  }

  private boolean isCacheValid() {
    if (cachedJwks == null || cachedAt == null) {
      return false;
    }

    long ttl = Math.max(1L, properties.getJwksTtlSeconds());
    return Instant.now().isBefore(cachedAt.plusSeconds(ttl));
  }

  private JWKSet fetchJwks() {
    String base = properties.getBaseUrl();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }

    String jwksUrl = base + "/realms/" + properties.getRealm() + "/protocol/openid-connect/certs";

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(jwksUrl))
      .timeout(Duration.ofSeconds(5))
      .GET()
      .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      LOGGER.warn("No se pudo obtener JWKS de Keycloak", e);
      throw new JwtValidationException("Unable to fetch JWKS", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.warn("Consulta JWKS interrumpida", e);
      throw new JwtValidationException("JWKS request interrupted", e);
    }

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new JwtValidationException("Invalid JWKS response status: " + response.statusCode());
    }

    try {
      return JWKSet.parse(response.body());
    } catch (Exception e) {
      throw new JwtValidationException("Unable to parse JWKS", e);
    }
  }
}
