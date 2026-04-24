package nur.edu.nurtricenter_patient.webapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import nur.edu.nurtricenter_patient.application.auth.ITokenService;
import nur.edu.nurtricenter_patient.application.auth.IdentityProviderUnavailableException;
import nur.edu.nurtricenter_patient.application.auth.TokenResponse;
import nur.edu.nurtricenter_patient.infraestructure.security.KeycloakJwtValidator;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private KeycloakJwtValidator jwtValidator;

  @MockBean
  private ITokenService tokenService;

  @BeforeEach
  void setUp() {
    Map<String, Object> claims = Map.of("sub", "test-user", "preferred_username", "admin");
    when(jwtValidator.validateAndExtract(anyString())).thenReturn(claims);
    when(jwtValidator.extractRoles(any())).thenReturn(List.of("admin", "nutritionist"));
  }

  // --- Patient CRUD ---

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
        "birthDate", "1990-05-20",
        "email", "ana." + UUID.randomUUID() + "@example.com",
        "cellphone", "7" + (1000000 + (int)(Math.random() * 9000000)),
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
    String cellphone1 = "7" + (1000000 + (int)(Math.random() * 9000000));
    String cellphone2 = "7" + (1000000 + (int)(Math.random() * 9000000));
    Map<String, Object> body1 = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "1990-05-20", "email", email, "cellphone", cellphone1, "document", "D1-" + UUID.randomUUID());
    Map<String, Object> body2 = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "1990-05-20", "email", email, "cellphone", cellphone2, "document", "D2-" + UUID.randomUUID());

    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body1)))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body2)))
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
    String cellphone = "7" + (1000000 + (int)(Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "1990-05-20", "email", email, "cellphone", cellphone, "document", "D-" + UUID.randomUUID());

    String response = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();

    String id = mapper.readTree(response).path("value").asText();
    mockMvc.perform(get("/api/patients/" + id)
            .header("Authorization", "Bearer valid"))
        .andExpect(status().isOk());
  }

  @Test
  void updatePatient_existing_returns200() throws Exception {
    String email = "upd." + UUID.randomUUID() + "@example.com";
    String cellphone = "7" + (1000000 + (int)(Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Ana", "lastname", "P",
        "birthDate", "1990-05-20", "email", email, "cellphone", cellphone, "document", "D-" + UUID.randomUUID());

    String response = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();

    String id = mapper.readTree(response).path("value").asText();
    String newEmail = "upd2." + UUID.randomUUID() + "@example.com";
    String newCellphone = "7" + (1000000 + (int)(Math.random() * 9000000));
    Map<String, Object> updateBody = Map.of("name", "Ana2", "lastname", "P2",
        "birthDate", "1990-05-20", "email", newEmail, "cellphone", newCellphone, "document", "D2-" + UUID.randomUUID());

    mockMvc.perform(put("/api/patients/" + id)
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(updateBody)))
        .andExpect(status().isOk());
  }

  @Test
  void deletePatient_existing_returns204() throws Exception {
    String email = "del." + UUID.randomUUID() + "@example.com";
    String cellphone = "7" + (1000000 + (int)(Math.random() * 9000000));
    Map<String, Object> body = Map.of("name", "Del", "lastname", "P",
        "birthDate", "1990-05-20", "email", email, "cellphone", cellphone, "document", "D-" + UUID.randomUUID());

    String response = mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andReturn().getResponse().getContentAsString();

    String id = mapper.readTree(response).path("value").asText();
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
  void addAddress_notFoundPatient_returns404() throws Exception {
    Map<String, Object> addressBody = Map.of(
        "label", "Casa", "line1", "Calle 1", "country", "Bolivia",
        "city", "La Paz", "latitude", -16.5, "longitude", -68.15);

    mockMvc.perform(post("/api/patients/" + UUID.randomUUID() + "/addresses")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(addressBody)))
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
    Map<String, Object> body = Map.of("name", "Ana");
    mockMvc.perform(post("/api/patients")
            .header("Authorization", "Bearer valid")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest());
  }

  // --- Auth endpoints ---

  @Test
  void login_delegatesToTokenService() throws Exception {
    when(tokenService.login("user", "pass"))
        .thenReturn(new TokenResponse(200, Map.of("access_token", "tok")));

    Map<String, String> body = Map.of("username", "user", "password", "pass");
    mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andExpect(status().isOk());
  }

  @Test
  void login_keycloakUnavailable_returns503() throws Exception {
    when(tokenService.login(anyString(), anyString()))
        .thenThrow(new IdentityProviderUnavailableException("down"));

    Map<String, String> body = Map.of("username", "user", "password", "pass");
    mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void refresh_delegatesToTokenService() throws Exception {
    when(tokenService.refresh("refresh-tok"))
        .thenReturn(new TokenResponse(200, Map.of("access_token", "new-tok")));

    Map<String, String> body = Map.of("refreshToken", "refresh-tok");
    mockMvc.perform(post("/api/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andExpect(status().isOk());
  }

  @Test
  void refresh_keycloakUnavailable_returns503() throws Exception {
    when(tokenService.refresh(anyString()))
        .thenThrow(new IdentityProviderUnavailableException("down"));

    Map<String, String> body = Map.of("refreshToken", "tok");
    mockMvc.perform(post("/api/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(body)))
        .andExpect(status().isServiceUnavailable());
  }
}
