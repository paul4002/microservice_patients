package nur.edu.nurtricenter_patient.webapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import nur.edu.nurtricenter_patient.application.auth.ITokenService;
import nur.edu.nurtricenter_patient.application.auth.IdentityProviderUnavailableException;
import nur.edu.nurtricenter_patient.application.auth.TokenResponse;
import nur.edu.nurtricenter_patient.infraestructure.security.KeycloakJwtValidator;

@SpringBootTest
class PatientControllerMvcTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private ObjectMapper mapper;

  @MockitoBean
  private KeycloakJwtValidator jwtValidator;

  @MockitoBean
  private ITokenService tokenService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac)
        .apply(springSecurity())
        .build();
    Map<String, Object> claims = Map.of("sub", "test-user", "preferred_username", "admin");
    when(jwtValidator.validateAndExtract(anyString())).thenReturn(claims);
    when(jwtValidator.extractRoles(any())).thenReturn(List.of("admin", "nutritionist"));
  }

  @Test
  void listPatients_authenticated_returns200() throws Exception {
    mockMvc.perform(get("/api/patients")
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isOk());
  }

  @Test
  void listPatients_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/patients"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createPatient_validBody_returns201() throws Exception {
    Map<String, Object> body = Map.of(
        "name", "Ana",
        "lastname", "Perez",
        "birthDate", "20-05-1990",
        "email", "ana." + UUID.randomUUID() + "@example.com",
        "cellphone", "7" + (1000000 + (int) (Math.random() * 9000000)),
        "document", "DOC-" + UUID.randomUUID()
    );
    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andExpect(status().isCreated());
  }

  @Test
  void createPatient_duplicateEmail_returns409() throws Exception {
    String email = "dup." + UUID.randomUUID() + "@example.com";
    String c1 = "7" + (1000000 + (int) (Math.random() * 9000000));
    String c2 = "7" + (1000000 + (int) (Math.random() * 9000000));
    Map<String, Object> b1 = Map.of("name", "A", "lastname", "P",
        "birthDate", "01-01-1990", "email", email, "cellphone", c1, "document", "D1-" + UUID.randomUUID());
    Map<String, Object> b2 = Map.of("name", "A", "lastname", "P",
        "birthDate", "01-01-1990", "email", email, "cellphone", c2, "document", "D2-" + UUID.randomUUID());

    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(b1)))
        .andExpect(status().isCreated());
    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(b2)))
        .andExpect(status().isConflict());
  }

  @Test
  void getPatient_notFound_returns404() throws Exception {
    mockMvc.perform(get("/api/patients/" + UUID.randomUUID())
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getPatient_found_returns200() throws Exception {
    String email = "get." + UUID.randomUUID() + "@example.com";
    String c = "7" + (1000000 + (int) (Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "20-05-1990", "email", email, "cellphone", c, "document", "D-" + UUID.randomUUID());

    String resp = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();

    String id = mapper.readTree(resp).path("value").asText();
    mockMvc.perform(get("/api/patients/" + id)
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isOk());
  }

  @Test
  void updatePatient_existing_returns200() throws Exception {
    String email = "upd." + UUID.randomUUID() + "@example.com";
    String c = "7" + (1000000 + (int) (Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "20-05-1990", "email", email, "cellphone", c, "document", "D-" + UUID.randomUUID());

    String resp = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();

    String id = mapper.readTree(resp).path("value").asText();
    String e2 = "upd2." + UUID.randomUUID() + "@example.com";
    String c2 = "7" + (1000000 + (int) (Math.random() * 9000000));
    Map<String, Object> upd = Map.of("name", "Ana2", "lastname", "P2",
        "birthDate", "20-05-1990", "email", e2, "cellphone", c2, "document", "D2-" + UUID.randomUUID());

    mockMvc.perform(put("/api/patients/" + id)
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(upd)))
        .andExpect(status().isOk());
  }

  @Test
  void deletePatient_existing_returns204() throws Exception {
    String email = "del." + UUID.randomUUID() + "@example.com";
    String c = "7" + (1000000 + (int) (Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Del", "lastname", "P",
        "birthDate", "20-05-1990", "email", email, "cellphone", c, "document", "D-" + UUID.randomUUID());

    String resp = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();

    String id = mapper.readTree(resp).path("value").asText();
    mockMvc.perform(delete("/api/patients/" + id)
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isNoContent());
  }

  @Test
  void getAddresses_returns200() throws Exception {
    mockMvc.perform(get("/api/patients/" + UUID.randomUUID() + "/addresses")
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isOk());
  }

  @Test
  void addAddress_patientNotFound_returns404() throws Exception {
    Map<String, Object> addr = Map.of("label", "Casa", "line1", "Calle 1",
        "country", "Bolivia", "city", "La Paz", "latitude", -16.5, "longitude", -68.15);
    mockMvc.perform(post("/api/patients/" + UUID.randomUUID() + "/addresses")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(addr)))
        .andExpect(status().isNotFound());
  }

  @Test
  void createPatient_malformedJson_returns400() throws Exception {
    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{invalid-json}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createPatient_missingRequiredField_returns400() throws Exception {
    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("name", "Ana"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_delegatesToTokenService_returns200() throws Exception {
    when(tokenService.login("user", "pass"))
        .thenReturn(new TokenResponse(200, Map.of("access_token", "tok")));
    mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("username", "user", "password", "pass"))))
        .andExpect(status().isOk());
  }

  @Test
  void login_keycloakUnavailable_returns503() throws Exception {
    when(tokenService.login(anyString(), anyString()))
        .thenThrow(new IdentityProviderUnavailableException("down"));
    mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("username", "u", "password", "p"))))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void refresh_delegatesToTokenService_returns200() throws Exception {
    when(tokenService.refresh("ref-tok"))
        .thenReturn(new TokenResponse(200, Map.of("access_token", "new")));
    mockMvc.perform(post("/api/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("refresh_token", "ref-tok"))))
        .andExpect(status().isOk());
  }

  @Test
  void refresh_keycloakUnavailable_returns503() throws Exception {
    when(tokenService.refresh(anyString()))
        .thenThrow(new IdentityProviderUnavailableException("down"));
    mockMvc.perform(post("/api/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("refresh_token", "tok"))))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void updateAddress_existing_returns200() throws Exception {
    String email = "ua." + UUID.randomUUID() + "@example.com";
    String c = "7" + (1000000 + (int) (Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "20-05-1990", "email", email, "cellphone", c, "document", "D-" + UUID.randomUUID());

    String resp = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();
    String patientId = mapper.readTree(resp).path("value").asText();

    Map<String, Object> addr = Map.of("label", "Casa", "line1", "Calle 1",
        "country", "Bolivia", "province", "La Paz", "city", "La Paz", "latitude", -16.5, "longitude", -68.15);
    String addrResp = mockMvc.perform(post("/api/patients/" + patientId + "/addresses")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(addr)))
        .andReturn().getResponse().getContentAsString();
    String addressId = mapper.readTree(addrResp).path("value").asText();

    Map<String, Object> upd = Map.of("label", "Oficina", "line1", "Av. Principal",
        "country", "Bolivia", "province", "Cochabamba", "city", "Cochabamba", "latitude", -17.4, "longitude", -66.15);
    mockMvc.perform(put("/api/patients/" + patientId + "/addresses/" + addressId)
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(upd)))
        .andExpect(status().isOk());
  }

  @Test
  void deactivateAddress_existing_returns204() throws Exception {
    String email = "da." + UUID.randomUUID() + "@example.com";
    String c = "7" + (1000000 + (int) (Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "20-05-1990", "email", email, "cellphone", c, "document", "D-" + UUID.randomUUID());

    String resp = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();
    String patientId = mapper.readTree(resp).path("value").asText();

    Map<String, Object> addr = Map.of("label", "Casa", "line1", "Calle 1",
        "country", "Bolivia", "province", "La Paz", "city", "La Paz", "latitude", -16.5, "longitude", -68.15);
    String addrResp = mockMvc.perform(post("/api/patients/" + patientId + "/addresses")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(addr)))
        .andReturn().getResponse().getContentAsString();
    String addressId = mapper.readTree(addrResp).path("value").asText();

    mockMvc.perform(delete("/api/patients/" + patientId + "/addresses/" + addressId)
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isNoContent());
  }

  @Test
  void listPatients_wrongRole_returns403() throws Exception {
    when(jwtValidator.extractRoles(any())).thenReturn(List.of("patient"));
    mockMvc.perform(get("/api/patients")
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isForbidden());
  }

  @Test
  void deletePatient_wrongRole_returns403() throws Exception {
    when(jwtValidator.extractRoles(any())).thenReturn(List.of("nutritionist"));
    mockMvc.perform(delete("/api/patients/" + UUID.randomUUID())
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isForbidden());
  }
}
