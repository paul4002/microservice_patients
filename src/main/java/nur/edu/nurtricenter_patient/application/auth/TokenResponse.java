package nur.edu.nurtricenter_patient.application.auth;

import java.util.Map;

public record TokenResponse(int statusCode, Map<String, Object> body) {}
