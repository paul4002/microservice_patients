package nur.edu.nurtricenter_patient.infraestructure.security;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(KeycloakProperties.class)
public class SecurityConfig {

  private final ObjectMapper objectMapper;

  public SecurityConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    KeycloakJwtAuthenticationFilter keycloakJwtAuthenticationFilter,
    DenyUsersFilter denyUsersFilter
  ) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/login", "/api/refresh", "/actuator/health", "/actuator/info").permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(exception -> exception
        .authenticationEntryPoint((request, response, authException) ->
          writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
        .accessDeniedHandler((request, response, accessDeniedException) ->
          writeJson(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
      )
      .addFilterBefore(keycloakJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterAfter(denyUsersFilter, KeycloakJwtAuthenticationFilter.class);

    return http.build();
  }

  private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), Map.of("message", message));
  }
}
