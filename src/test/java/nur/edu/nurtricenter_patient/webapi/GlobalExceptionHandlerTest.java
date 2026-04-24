package nur.edu.nurtricenter_patient.webapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import nur.edu.nurtricenter_patient.webapi.controllers.GlobalExceptionHandler;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleConstraintViolation_returns400() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("field1");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("must not be null");

    ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
    ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Validation.Failed", response.getBody().get("code"));
    @SuppressWarnings("unchecked")
    List<Map<String, String>> errors = (List<Map<String, String>>) response.getBody().get("errors");
    assertEquals(1, errors.size());
    assertEquals("field1", errors.get(0).get("field"));
  }

  @Test
  void handleGeneric_returns500() {
    ResponseEntity<Map<String, Object>> response = handler.handleGeneric(new RuntimeException("boom"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Server.Error", response.getBody().get("code"));
  }

  @Test
  void handleAccessDenied_returns403() {
    ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(new AccessDeniedException("denied"));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("Access.Denied", response.getBody().get("code"));
  }

  @Test
  void handleAuth_returns401() {
    ResponseEntity<Map<String, Object>> response = handler.handleAuth(new BadCredentialsException("bad creds"));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Auth.Unauthorized", response.getBody().get("code"));
  }
}
