package nur.edu.nurtricenter_patient.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

class SecurityFilterUnitTest {

  private KeycloakJwtValidator jwtValidator;
  private KeycloakProperties properties;
  private ObjectMapper objectMapper;
  private KeycloakJwtAuthenticationFilter jwtFilter;
  private DenyUsersFilter denyFilter;

  @BeforeEach
  void setUp() {
    jwtValidator = mock(KeycloakJwtValidator.class);
    properties = new KeycloakProperties();
    objectMapper = new ObjectMapper();
    jwtFilter = new KeycloakJwtAuthenticationFilter(jwtValidator, objectMapper);
    denyFilter = new DenyUsersFilter(properties, objectMapper);
    SecurityContextHolder.clearContext();
  }

  // --- KeycloakJwtAuthenticationFilter ---

  @Test
  void jwtFilter_shouldNotFilter_nonApiPath() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/health");
    assertTrue(jwtFilter.shouldNotFilter(req));
  }

  @Test
  void jwtFilter_shouldNotFilter_loginPath() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/api/login");
    assertTrue(jwtFilter.shouldNotFilter(req));
  }

  @Test
  void jwtFilter_shouldNotFilter_refreshPath() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/api/refresh");
    assertTrue(jwtFilter.shouldNotFilter(req));
  }

  @Test
  void jwtFilter_shouldNotFilter_actuatorPath() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/actuator/health");
    assertTrue(jwtFilter.shouldNotFilter(req));
  }

  @Test
  void jwtFilter_shouldFilter_apiPath() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/api/patients");
    assertFalse(jwtFilter.shouldNotFilter(req));
  }

  @Test
  void jwtFilter_noAuthHeader_returns401() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    when(req.getHeader("Authorization")).thenReturn(null);
    when(res.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
      @Override public boolean isReady() { return true; }
      @Override public void setWriteListener(jakarta.servlet.WriteListener l) {}
      @Override public void write(int b) { out.write(b); }
    });

    jwtFilter.doFilterInternal(req, res, chain);

    verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(chain, never()).doFilter(any(), any());
  }

  @Test
  void jwtFilter_blankAuthHeader_returns401() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    when(req.getHeader("Authorization")).thenReturn("   ");
    when(res.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
      @Override public boolean isReady() { return true; }
      @Override public void setWriteListener(jakarta.servlet.WriteListener l) {}
      @Override public void write(int b) { out.write(b); }
    });

    jwtFilter.doFilterInternal(req, res, chain);

    verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void jwtFilter_invalidPrefix_returns401() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    when(req.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
    when(res.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
      @Override public boolean isReady() { return true; }
      @Override public void setWriteListener(jakarta.servlet.WriteListener l) {}
      @Override public void write(int b) { out.write(b); }
    });

    jwtFilter.doFilterInternal(req, res, chain);

    verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void jwtFilter_invalidToken_returns401() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    when(req.getHeader("Authorization")).thenReturn("Bearer bad-token");
    when(jwtValidator.validateAndExtract("bad-token"))
        .thenThrow(new JwtValidationException("invalid"));
    when(res.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
      @Override public boolean isReady() { return true; }
      @Override public void setWriteListener(jakarta.servlet.WriteListener l) {}
      @Override public void write(int b) { out.write(b); }
    });

    jwtFilter.doFilterInternal(req, res, chain);

    verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void jwtFilter_validToken_setsAuthAndContinues() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    Map<String, Object> claims = Map.of("sub", "user-id", "preferred_username", "test-user");
    when(req.getHeader("Authorization")).thenReturn("Bearer valid");
    when(jwtValidator.validateAndExtract("valid")).thenReturn(claims);
    when(jwtValidator.extractRoles(claims)).thenReturn(List.of("admin"));

    jwtFilter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void jwtFilter_dpopPrefix_accepted() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    Map<String, Object> claims = Map.of("sub", "user-id");
    when(req.getHeader("Authorization")).thenReturn("DPoP valid-dpop");
    when(jwtValidator.validateAndExtract("valid-dpop")).thenReturn(claims);
    when(jwtValidator.extractRoles(any())).thenReturn(List.of());

    jwtFilter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void jwtFilter_noSubClaim_usesAnonymousPrincipal() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    Map<String, Object> claims = Map.of();
    when(req.getHeader("Authorization")).thenReturn("Bearer token");
    when(jwtValidator.validateAndExtract("token")).thenReturn(claims);
    when(jwtValidator.extractRoles(any())).thenReturn(List.of());

    jwtFilter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
    assertEquals("anonymous", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  // --- DenyUsersFilter ---

  @Test
  void denyFilter_shouldNotFilter_nonApiPath() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/health");
    assertTrue(denyFilter.shouldNotFilter(req));
  }

  @Test
  void denyFilter_shouldFilter_apiPath() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/api/patients");
    assertFalse(denyFilter.shouldNotFilter(req));
  }

  @Test
  void denyFilter_noBlockedUsers_passes() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    properties.setBlockedUsers(List.of());

    denyFilter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void jwtValidationException_constructors() {
    JwtValidationException e1 = new JwtValidationException("msg");
    assertEquals("msg", e1.getMessage());

    JwtValidationException e2 = new JwtValidationException("msg2", new RuntimeException("cause"));
    assertEquals("msg2", e2.getMessage());
    assertNotNull(e2.getCause());
  }
}
