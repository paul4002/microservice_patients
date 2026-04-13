package nur.edu.nurtricenter_patient.webapi.controllers.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(
    @JsonProperty("refresh_token")
    @NotBlank
    @Size(min = 1, max = 4096)
    String refreshToken
) {
}
