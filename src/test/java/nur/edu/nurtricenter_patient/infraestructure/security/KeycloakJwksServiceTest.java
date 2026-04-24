package nur.edu.nurtricenter_patient.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class KeycloakJwksServiceTest {

  private KeycloakProperties properties;
  private KeycloakJwksService jwksService;
  private HttpClient mockHttpClient;

  private static final String SAMPLE_JWKS = """
      {"keys":[{"kty":"RSA","n":"smt6OHUxywGrBKbscj3aq1hVtQ","e":"AQAB","kid":"test-kid","alg":"RS256","use":"sig"}]}
      """;

  @BeforeEach
  void setUp() throws Exception {
    properties = new KeycloakProperties();
    properties.setBaseUrl("http://localhost:9999");
    properties.setRealm("test-realm");
    properties.setJwksTtlSeconds(60);

    jwksService = new KeycloakJwksService(properties);

    mockHttpClient = mock(HttpClient.class);
    Field field = KeycloakJwksService.class.getDeclaredField("httpClient");
    field.setAccessible(true);
    field.set(jwksService, mockHttpClient);
  }

  @Test
  void getJwks_cacheMiss_fetchesFromKeycloak() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(SAMPLE_JWKS);
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    JWKSet result = jwksService.getJwks(false);

    assertNotNull(result);
    verify(mockHttpClient).send(any(), any());
  }

  @Test
  void getJwks_cacheHit_returnsCachedWithoutSecondFetch() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(SAMPLE_JWKS);
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    JWKSet first = jwksService.getJwks(false);
    JWKSet second = jwksService.getJwks(false);

    assertSame(first, second);
    verify(mockHttpClient, times(1)).send(any(), any());
  }

  @Test
  void getJwks_forceRefresh_alwaysFetches() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(SAMPLE_JWKS);
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    jwksService.getJwks(false);
    jwksService.getJwks(true);

    verify(mockHttpClient, times(2)).send(any(), any());
  }

  @Test
  void getJwks_ioException_throwsJwtValidationException() throws Exception {
    doThrow(new IOException("connection refused")).when(mockHttpClient).send(any(), any());

    assertThrows(JwtValidationException.class, () -> jwksService.getJwks(false));
  }

  @Test
  void getJwks_interruptedException_throwsJwtValidationException_setsInterruptFlag() throws Exception {
    doThrow(new InterruptedException("interrupted")).when(mockHttpClient).send(any(), any());

    assertThrows(JwtValidationException.class, () -> jwksService.getJwks(false));
    assertTrue(Thread.interrupted());
  }

  @Test
  void getJwks_nonSuccessStatus_throwsJwtValidationException() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(503);
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    assertThrows(JwtValidationException.class, () -> jwksService.getJwks(false));
  }

  @Test
  void getJwks_invalidJsonResponse_throwsJwtValidationException() throws Exception {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("not-valid-jwks-json");
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    assertThrows(JwtValidationException.class, () -> jwksService.getJwks(false));
  }

  @Test
  void getJwks_baseUrlWithTrailingSlash_normalizes() throws Exception {
    properties.setBaseUrl("http://localhost:9999/");
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(SAMPLE_JWKS);
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    assertDoesNotThrow(() -> jwksService.getJwks(true));
  }

  @Test
  void getJwks_zeroTtl_usesMinimumOneSec() throws Exception {
    properties.setJwksTtlSeconds(0);
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(SAMPLE_JWKS);
    doReturn(mockResponse).when(mockHttpClient).send(any(), any());

    JWKSet result = jwksService.getJwks(false);
    assertNotNull(result);
  }
}
