package nur.edu.nurtricenter_patient.domain.patient;

import java.util.Objects;
import java.util.regex.Pattern;

public record Cellphone(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8}$");

    public Cellphone {
        Objects.requireNonNull(value, "Cellphone cannot be null");

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Cellphone format: " + value +
                                               ". It must have exactly 8 digits.");
        }
    }
}
