package nur.edu.nurtricenter_patient.infraestructure.security;

public class KeycloakUnavailableException extends RuntimeException {

  public KeycloakUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
