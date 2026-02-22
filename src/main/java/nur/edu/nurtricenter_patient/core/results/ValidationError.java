package nur.edu.nurtricenter_patient.core.results;

import java.util.List;

public final class ValidationError extends Error {

    private final Error[] errors;

    public ValidationError(Error... errors) {
        super(
            "Validation.General",
            "One or more validation errors occurred",
            ErrorType.VALIDATION
        );
        this.errors = errors != null ? errors.clone() : new Error[0];
    }

    public Error[] getErrors() {
        return errors.clone(); // Devuelve copia para mantener inmutabilidad
    }

    // Factory method similar a FromResults
    public static ValidationError fromResults(List<Result> results) {
        Error[] failedErrors = results.stream()
                .filter(Result::isFailure)
                .map(Result::getError)
                .toArray(Error[]::new);

        return new ValidationError(failedErrors);
    }
}
