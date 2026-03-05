package nur.edu.nurtricenter_patient.webapi.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nur.edu.nurtricenter_patient.infraestructure.security.KeycloakTokenService;
import nur.edu.nurtricenter_patient.infraestructure.security.KeycloakUnavailableException;
import nur.edu.nurtricenter_patient.webapi.controllers.auth.LoginRequest;
import nur.edu.nurtricenter_patient.webapi.controllers.auth.RefreshRequest;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final KeycloakTokenService tokenService;

  public AuthController(KeycloakTokenService tokenService) {
    this.tokenService = tokenService;
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
    if (request == null || isBlank(request.username()) || isBlank(request.password())) {
      return ResponseEntity.badRequest().body(Map.of("message", "Validation failed"));
    }

    try {
      return tokenService.login(request.username(), request.password());
    } catch (KeycloakUnavailableException e) {
      return ResponseEntity.status(503).body(Map.of(
        "error", "keycloak_unavailable",
        "error_description", "Unable to reach identity provider"
      ));
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, Object>> refresh(@RequestBody RefreshRequest request) {
    if (request == null || isBlank(request.refreshToken())) {
      return ResponseEntity.badRequest().body(Map.of("message", "Validation failed"));
    }

    try {
      return tokenService.refresh(request.refreshToken());
    } catch (KeycloakUnavailableException e) {
      return ResponseEntity.status(503).body(Map.of(
        "error", "keycloak_unavailable",
        "error_description", "Unable to reach identity provider"
      ));
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
