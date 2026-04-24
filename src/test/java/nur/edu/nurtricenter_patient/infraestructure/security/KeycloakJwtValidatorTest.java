package nur.edu.nurtricenter_patient.infraestructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeycloakJwtValidatorTest {

  private KeycloakProperties properties;

  @BeforeEach
  void setUp() {
    properties = new KeycloakProperties();
    properties.setClientId("api-gateway");
  }

  @Test
  void extractRoles_realmAndResourceRoles() {
    KeycloakProperties props = new KeycloakProperties();
    props.setClientId("my-client");
    KeycloakJwksService jwksService = org.mockito.Mockito.mock(KeycloakJwksService.class);
    KeycloakJwtValidator validator = new KeycloakJwtValidator(props, jwksService);

    Map<String, Object> claims = new HashMap<>();
    claims.put("realm_access", Map.of("roles", List.of("admin", "user")));
    Map<String, Object> clientRoles = Map.of("roles", List.of("manager"));
    claims.put("resource_access", Map.of("my-client", clientRoles));

    List<String> roles = validator.extractRoles(claims);

    assertTrue(roles.contains("admin"));
    assertTrue(roles.contains("user"));
    assertTrue(roles.contains("manager"));
    assertEquals(3, roles.size());
  }

  @Test
  void extractRoles_emptyClaimsReturnsEmpty() {
    KeycloakJwksService jwksService = org.mockito.Mockito.mock(KeycloakJwksService.class);
    KeycloakJwtValidator validator = new KeycloakJwtValidator(properties, jwksService);

    List<String> roles = validator.extractRoles(Map.of());
    assertTrue(roles.isEmpty());
  }

  @Test
  void extractRoles_blankRoleSkipped() {
    KeycloakJwksService jwksService = org.mockito.Mockito.mock(KeycloakJwksService.class);
    KeycloakJwtValidator validator = new KeycloakJwtValidator(properties, jwksService);

    List<String> roleList = new ArrayList<>();
    roleList.add("admin");
    roleList.add("  ");
    roleList.add(null);
    Map<String, Object> claims = Map.of("realm_access", Map.of("roles", roleList));

    List<String> roles = validator.extractRoles(claims);
    assertEquals(1, roles.size());
    assertEquals("admin", roles.get(0));
  }

  @Test
  void extractRoles_deduplicates() {
    KeycloakJwksService jwksService = org.mockito.Mockito.mock(KeycloakJwksService.class);
    KeycloakJwtValidator validator = new KeycloakJwtValidator(properties, jwksService);

    Map<String, Object> claims = new HashMap<>();
    claims.put("realm_access", Map.of("roles", List.of("admin")));
    Map<String, Object> clientRoles = Map.of("roles", List.of("admin"));
    claims.put("resource_access", Map.of("api-gateway", clientRoles));

    List<String> roles = validator.extractRoles(claims);
    assertEquals(1, roles.size());
  }

  @Test
  void keycloakProperties_defaults() {
    KeycloakProperties p = new KeycloakProperties();
    assertEquals("http://keycloak:8080", p.getBaseUrl());
    assertEquals("classroom", p.getRealm());
    assertEquals("api-gateway", p.getClientId());
    assertEquals(600L, p.getJwksTtlSeconds());
    assertTrue(p.getBlockedUsers().isEmpty());
  }

  @Test
  void keycloakProperties_setters() {
    KeycloakProperties p = new KeycloakProperties();
    p.setBaseUrl("http://my-kc:8080");
    p.setRealm("my-realm");
    p.setClientId("my-client");
    p.setClientSecret("secret");
    p.setIssuer("http://my-kc:8080/realms/my-realm");
    p.setJwksTtlSeconds(300L);
    p.setBlockedUsers(List.of("user1"));
    assertEquals("http://my-kc:8080", p.getBaseUrl());
    assertEquals("my-realm", p.getRealm());
    assertEquals("my-client", p.getClientId());
    assertEquals("secret", p.getClientSecret());
    assertEquals("http://my-kc:8080/realms/my-realm", p.getIssuer());
    assertEquals(300L, p.getJwksTtlSeconds());
    assertEquals(1, p.getBlockedUsers().size());
  }
}
