package nur.edu.nurtricenter_patient.domain.patient;

import java.util.regex.Pattern;

import nur.edu.nurtricenter_patient.core.results.DomainException;

public record Cellphone(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8}$");

    public Cellphone {
        if (value == null || value.isBlank()) {
            throw new DomainException(PatientErrors.CellphoneIsRequired());
        }

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new DomainException(PatientErrors.CellphoneIsInvalid(value));
        }
    }
}
