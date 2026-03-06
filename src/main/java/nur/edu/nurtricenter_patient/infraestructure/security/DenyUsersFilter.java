package nur.edu.nurtricenter_patient.infraestructure.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DenyUsersFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DenyUsersFilter.class);

  private final KeycloakProperties properties;
  private final ObjectMapper objectMapper;

  public DenyUsersFilter(KeycloakProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = normalizePath(request.getRequestURI());
    return !path.startsWith("/api/")
      || path.startsWith("/api/login")
      || path.startsWith("/api/refresh")
      || path.startsWith("/actuator/");
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    List<String> blocked = properties.getBlockedUsers();
    if (blocked == null || blocked.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      filterChain.doFilter(request, response);
      return;
    }

    Map<String, Object> claims = extractClaims(auth.getDetails());
    String sub = claimAsString(claims, "sub");
    String username = claimAsString(claims, "preferred_username");

    if ((sub != null && blocked.contains(sub)) || (username != null && blocked.contains(username))) {
      LOGGER.warn("Usuario de Keycloak bloqueado: sub={}, preferred_username={}", sub, username);
      forbidden(response);
      return;
    }

    filterChain.doFilter(request, response);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractClaims(Object details) {
    if (details instanceof Map<?, ?> map) {
      return (Map<String, Object>) map;
    }

    return Map.of();
  }

  private String claimAsString(Map<String, Object> claims, String name) {
    Object value = claims.get(name);
    return value instanceof String ? (String) value : null;
  }

  private void forbidden(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), Map.of("message", "Forbidden"));
  }

  private String normalizePath(String rawPath) {
    if (rawPath == null || rawPath.isBlank()) {
      return "/";
    }

    String normalized = rawPath.replaceAll("/+", "/");
    if (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }

    return normalized;
  }
}
