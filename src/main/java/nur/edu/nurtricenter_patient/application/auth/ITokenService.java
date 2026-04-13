package nur.edu.nurtricenter_patient.application.auth;

public interface ITokenService {
  TokenResponse login(String username, String password);

  TokenResponse refresh(String refreshToken);
}
