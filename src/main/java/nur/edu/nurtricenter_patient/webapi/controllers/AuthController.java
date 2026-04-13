package nur.edu.nurtricenter_patient.webapi.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nur.edu.nurtricenter_patient.application.auth.ITokenService;
import nur.edu.nurtricenter_patient.application.auth.IdentityProviderUnavailableException;
import nur.edu.nurtricenter_patient.application.auth.TokenResponse;
import nur.edu.nurtricenter_patient.webapi.controllers.auth.LoginRequest;
import nur.edu.nurtricenter_patient.webapi.controllers.auth.RefreshRequest;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final ITokenService tokenService;

  public AuthController(ITokenService tokenService) {
    this.tokenService = tokenService;
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
    try {
      return toResponse(tokenService.login(request.username(), request.password()));
    } catch (IdentityProviderUnavailableException e) {
      return identityUnavailable();
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshRequest request) {
    try {
      return toResponse(tokenService.refresh(request.refreshToken()));
    } catch (IdentityProviderUnavailableException e) {
      return identityUnavailable();
    }
  }

  private ResponseEntity<Map<String, Object>> toResponse(TokenResponse response) {
    return ResponseEntity.status(response.statusCode()).body(response.body());
  }

  private ResponseEntity<Map<String, Object>> identityUnavailable() {
    return ResponseEntity.status(503).body(Map.of(
      "error", "keycloak_unavailable",
      "error_description", "Unable to reach identity provider"
    ));
  }
}
