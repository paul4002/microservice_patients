package nur.edu.nurtricenter_patient.domain.patient;

import java.util.regex.Pattern;

import nur.edu.nurtricenter_patient.core.results.DomainException;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public Email {
        if (value == null || value.isBlank()) {
            throw new DomainException(PatientErrors.EmailIsRequired());
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new DomainException(PatientErrors.EmailIsInvalid(value));
        }
    }
}
