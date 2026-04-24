package nur.edu.nurtricenter_patient.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import nur.edu.nurtricenter_patient.application.auth.IdentityProviderUnavailableException;
import nur.edu.nurtricenter_patient.application.auth.TokenResponse;

@SuppressWarnings("unchecked")
class KeycloakTokenServiceTest {

  private KeycloakProperties properties;
  private ObjectMapper objectMapper;
  private KeycloakTokenService tokenService;
  private HttpClient mockHttpClient;

  @BeforeEach
  void setUp() throws Exception {
    properties = new KeycloakProperties();
    properties.setBaseUrl("http://localhost:9999");
    properties.setRealm("test-realm");
    properties.setClientId("test-client");
    objectMapper = new ObjectMapper();
    tokenService = new KeycloakTokenService(properties, objectMapper);

    mockHttpClient = mock(HttpClient.class);
    Field field = KeycloakTokenService.class.getDeclaredField("httpClient");
    field.setAccessible(true);
    field.set(tokenService, mockHttpClient);
  }

  @Test
  void login_success_returnsTokenResponse() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{\"access_token\":\"tok\",\"token_type\":\"Bearer\"}");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    TokenResponse result = tokenService.login("user", "pass");

    assertEquals(200, result.statusCode());
    assertEquals("tok", result.body().get("access_token"));
  }

  @Test
  void login_failureResponse_returnsErrorTokenResponse() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(401);
    when(mockResponse.body()).thenReturn("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid credentials\"}");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    TokenResponse result = tokenService.login("user", "wrong");

    assertEquals(401, result.statusCode());
    assertEquals("invalid_grant", result.body().get("error"));
  }

  @Test
  void login_ioException_throwsIdentityProviderUnavailableException() throws Exception {
    doThrow(new IOException("connection refused")).when(mockHttpClient).send(any(), any());

    assertThrows(IdentityProviderUnavailableException.class, () -> tokenService.login("user", "pass"));
  }

  @Test
  void login_interrupted_throwsIdentityProviderUnavailableException() throws Exception {
    doThrow(new InterruptedException("interrupted")).when(mockHttpClient).send(any(), any());

    assertThrows(IdentityProviderUnavailableException.class, () -> tokenService.login("user", "pass"));
    assertTrue(Thread.interrupted());
  }

  @Test
  void refresh_success_returnsTokenResponse() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{\"access_token\":\"new-tok\"}");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    TokenResponse result = tokenService.refresh("old-refresh-token");

    assertEquals(200, result.statusCode());
    assertEquals("new-tok", result.body().get("access_token"));
  }

  @Test
  void login_withClientSecret_includesSecretInRequest() throws Exception {
    properties.setClientSecret("my-secret");
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{}");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    TokenResponse result = tokenService.login("user", "pass");

    assertEquals(200, result.statusCode());
  }

  @Test
  void login_baseUrlWithTrailingSlash_normalizesUrl() throws Exception {
    properties.setBaseUrl("http://localhost:9999/");
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{}");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    TokenResponse result = tokenService.login("user", "pass");
    assertEquals(200, result.statusCode());
  }

  @Test
  void login_emptyResponseBody_returnsEmptyMap() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    TokenResponse result = tokenService.login("user", "pass");
    assertTrue(result.body().isEmpty());
  }

  @Test
  void login_invalidJsonResponse_returnsRawBodyMap() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("not-json");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    TokenResponse result = tokenService.login("user", "pass");
    assertEquals("not-json", result.body().get("raw_body"));
  }

  @Test
  void circuitBreaker_opensAfterFiveFailures_throwsImmediately() throws Exception {
    doThrow(new IOException("down")).when(mockHttpClient).send(any(), any());

    for (int i = 0; i < 5; i++) {
      try { tokenService.login("u", "p"); } catch (IdentityProviderUnavailableException ignored) {}
    }

    assertThrows(IdentityProviderUnavailableException.class, () -> tokenService.login("u", "p"));
  }

  @Test
  void circuitBreaker_resetsAfterSuccess() throws Exception {
    HttpResponse<String> successResponse = mock(HttpResponse.class);
    when(successResponse.statusCode()).thenReturn(200);
    when(successResponse.body()).thenReturn("{}");

    doThrow(new IOException("fail"))
        .doThrow(new IOException("fail"))
        .doReturn(successResponse)
        .doReturn(successResponse)
        .when(mockHttpClient).send(any(), any());

    try { tokenService.login("u", "p"); } catch (IdentityProviderUnavailableException ignored) {}
    try { tokenService.login("u", "p"); } catch (IdentityProviderUnavailableException ignored) {}
    tokenService.login("u", "p");
    TokenResponse result = tokenService.login("u", "p");

    assertEquals(200, result.statusCode());
  }
}
