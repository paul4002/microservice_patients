package nur.edu.nurtricenter_patient.webapi.controllers.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshRequest(@JsonProperty("refresh_token") String refreshToken) {
}
