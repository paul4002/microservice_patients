package nur.edu.nurtricenter_patient.application.auth;

public class IdentityProviderUnavailableException extends RuntimeException {
  public IdentityProviderUnavailableException(String message) {
    super(message);
  }

  public IdentityProviderUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
