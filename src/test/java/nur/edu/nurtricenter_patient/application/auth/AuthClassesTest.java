package nur.edu.nurtricenter_patient.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class AuthClassesTest {

  @Test
  void tokenResponse_fields() {
    Map<String, Object> body = Map.of("access_token", "tok");
    TokenResponse tr = new TokenResponse(200, body);
    assertEquals(200, tr.statusCode());
    assertEquals(body, tr.body());
  }

  @Test
  void identityProviderUnavailableException_message() {
    IdentityProviderUnavailableException e = new IdentityProviderUnavailableException("down");
    assertEquals("down", e.getMessage());
  }

  @Test
  void identityProviderUnavailableException_withCause() {
    RuntimeException cause = new RuntimeException("io");
    IdentityProviderUnavailableException e = new IdentityProviderUnavailableException("down", cause);
    assertEquals("down", e.getMessage());
    assertNotNull(e.getCause());
  }
}
