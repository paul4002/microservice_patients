package nur.edu.nurtricenter_patient.webapi.controllers.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Size(min = 1, max = 255) String username,
    @NotBlank @Size(min = 1, max = 255) String password
) {
}
