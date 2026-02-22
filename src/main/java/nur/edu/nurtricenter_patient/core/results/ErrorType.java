package nur.edu.nurtricenter_patient.core.results;

public enum ErrorType {
    FAILURE(0),
    VALIDATION(1),
    PROBLEM(2),
    NOT_FOUND(3),
    CONFLICT(4);

    private final int code;

    ErrorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
