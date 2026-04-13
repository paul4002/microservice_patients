package nur.edu.nurtricenter_patient.infraestructure.security;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import nur.edu.nurtricenter_patient.application.auth.IdentityProviderUnavailableException;
import nur.edu.nurtricenter_patient.application.auth.ITokenService;
import nur.edu.nurtricenter_patient.application.auth.TokenResponse;

@Service
public class KeycloakTokenService implements ITokenService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakTokenService.class);

  private static final int CB_FAILURE_THRESHOLD = 5;
  private static final Duration CB_COOLDOWN = Duration.ofSeconds(30);

  private final KeycloakProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  private final AtomicReference<Instant> openUntil = new AtomicReference<>(Instant.EPOCH);

  public KeycloakTokenService(KeycloakProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(2))
      .build();
  }

  @Override
  public TokenResponse login(String username, String password) {
    Map<String, String> payload = new LinkedHashMap<>();
    payload.put("grant_type", "password");
    payload.put("client_id", properties.getClientId());
    payload.put("username", username);
    payload.put("password", password);
    putClientSecret(payload);

    return executeTokenRequest(payload, "login", username);
  }

  @Override
  public TokenResponse refresh(String refreshToken) {
    Map<String, String> payload = new LinkedHashMap<>();
    payload.put("grant_type", "refresh_token");
    payload.put("client_id", properties.getClientId());
    payload.put("refresh_token", refreshToken);
    putClientSecret(payload);

    return executeTokenRequest(payload, "refresh", null);
  }

  private TokenResponse executeTokenRequest(
    Map<String, String> payload,
    String operation,
    String username
  ) {
    checkCircuitBreaker();

    String tokenUrl = tokenEndpoint();

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(tokenUrl))
      .timeout(Duration.ofSeconds(5))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(toFormData(payload)))
      .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      recordFailure();
      LOGGER.error("{} en Keycloak no disponible", operation, e);
      throw new IdentityProviderUnavailableException("Unable to reach identity provider", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      recordFailure();
      LOGGER.error("{} en Keycloak interrumpido", operation, e);
      throw new IdentityProviderUnavailableException("Identity provider request interrupted", e);
    }

    recordSuccess();
    Map<String, Object> body = parseBody(response.body());
    logResult(operation, username, response.statusCode(), body);

    return new TokenResponse(response.statusCode(), body);
  }

  private void checkCircuitBreaker() {
    Instant until = openUntil.get();
    if (until.isAfter(Instant.now())) {
      throw new IdentityProviderUnavailableException("Identity provider circuit breaker is open");
    }
  }

  private void recordFailure() {
    int failures = consecutiveFailures.incrementAndGet();
    if (failures >= CB_FAILURE_THRESHOLD) {
      openUntil.set(Instant.now().plus(CB_COOLDOWN));
      LOGGER.warn("Keycloak circuit breaker opened for {} after {} consecutive failures",
          CB_COOLDOWN, failures);
    }
  }

  private void recordSuccess() {
    consecutiveFailures.set(0);
    openUntil.set(Instant.EPOCH);
  }

  private void putClientSecret(Map<String, String> payload) {
    if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
      payload.put("client_secret", properties.getClientSecret());
    }
  }

  private String tokenEndpoint() {
    String base = properties.getBaseUrl();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";
  }

  private String toFormData(Map<String, String> payload) {
    StringJoiner joiner = new StringJoiner("&");
    payload.forEach((key, value) -> joiner.add(
      URLEncoder.encode(key, StandardCharsets.UTF_8)
      + "="
      + URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
    ));
    return joiner.toString();
  }

  private Map<String, Object> parseBody(String body) {
    if (body == null || body.isBlank()) {
      return Map.of();
    }

    try {
      return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() { });
    } catch (Exception e) {
      return Map.of("raw_body", body);
    }
  }

  private void logResult(String operation, String username, int statusCode, Map<String, Object> body) {
    boolean ok = statusCode >= HttpStatus.OK.value() && statusCode < HttpStatus.MULTIPLE_CHOICES.value();

    if (ok) {
      LOGGER.info("{} en Keycloak exitoso: realm={}, client_id={}, username={}",
        operation,
        properties.getRealm(),
        properties.getClientId(),
        username
      );
      return;
    }

    LOGGER.warn("{} en Keycloak fallido: realm={}, client_id={}, username={}, status={}, error={}, error_description={}",
      operation,
      properties.getRealm(),
      properties.getClientId(),
      username,
      statusCode,
      body.get("error"),
      body.get("error_description")
    );
  }
}
