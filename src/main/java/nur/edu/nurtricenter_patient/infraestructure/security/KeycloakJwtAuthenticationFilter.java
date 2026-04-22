package nur.edu.nurtricenter_patient.infraestructure.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KeycloakJwtAuthenticationFilter extends OncePerRequestFilter {

  private final KeycloakJwtValidator jwtValidator;
  private final ObjectMapper objectMapper;

  public KeycloakJwtAuthenticationFilter(KeycloakJwtValidator jwtValidator, ObjectMapper objectMapper) {
    this.jwtValidator = jwtValidator;
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
    String auth = request.getHeader("Authorization");
    if (auth == null || auth.isBlank()) {
      unauthorized(response);
      return;
    }

    String token;
    if (auth.startsWith("Bearer ")) {
      token = auth.substring(7).trim();
    } else if (auth.startsWith("DPoP ")) {
      token = auth.substring(5).trim();
    } else {
      unauthorized(response);
      return;
    }

    if (token.isBlank()) {
      unauthorized(response);
      return;
    }

    Map<String, Object> claims;
    try {
      claims = jwtValidator.validateAndExtract(token);
    } catch (JwtValidationException e) {
      unauthorized(response);
      return;
    }

    List<SimpleGrantedAuthority> authorities = jwtValidator.extractRoles(claims)
      .stream()
      .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toLowerCase(java.util.Locale.ROOT)))
      .distinct()
      .toList();

    String principal = claimAsString(claims, "sub");
    if (principal == null || principal.isBlank()) {
      principal = claimAsString(claims, "preferred_username");
    }
    if (principal == null || principal.isBlank()) {
      principal = "anonymous";
    }

    UsernamePasswordAuthenticationToken authentication =
      new UsernamePasswordAuthenticationToken(principal, null, authorities);
    authentication.setDetails(claims);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }

  private String claimAsString(Map<String, Object> claims, String name) {
    Object value = claims.get(name);
    return value instanceof String ? (String) value : null;
  }

  private void unauthorized(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), Map.of("message", "Unauthorized"));
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
