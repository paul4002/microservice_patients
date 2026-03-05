package nur.edu.nurtricenter_patient.infraestructure.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

  private String baseUrl = "http://keycloak:8080";
  private String realm = "classroom";
  private String clientId = "api-gateway";
  private String clientSecret = "";
  private String issuer = "http://keycloak:8080/realms/classroom";
  private long jwksTtlSeconds = 600;
  private List<String> blockedUsers = new ArrayList<>();

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public long getJwksTtlSeconds() {
    return jwksTtlSeconds;
  }

  public void setJwksTtlSeconds(long jwksTtlSeconds) {
    this.jwksTtlSeconds = jwksTtlSeconds;
  }

  public List<String> getBlockedUsers() {
    return blockedUsers;
  }

  public void setBlockedUsers(List<String> blockedUsers) {
    this.blockedUsers = blockedUsers;
  }
}
