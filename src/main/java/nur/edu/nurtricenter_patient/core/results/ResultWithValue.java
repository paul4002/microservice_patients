package nur.edu.nurtricenter_patient.core.results;

public class ResultWithValue<T> extends Result {

    private final T value;

    public ResultWithValue(T value, boolean isSuccess, Error error) {
        super(isSuccess, error);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public static <T> ResultWithValue<T> of(T value) {
        if (value != null) {
            return new ResultWithValue<>(value, true, Error.NONE);
        } else {
            return new ResultWithValue<>(null, false, Error.NULL_VALUE);
        }
    }

    public static <T> ResultWithValue<T> success(T value) {
        return new ResultWithValue<>(value, true, Error.NONE);
    }

    public static <T> ResultWithValue<T> validationFailure(Error error) {
        return new ResultWithValue<>(null, false, error);
    }

    public static <T> ResultWithValue<T> fail(Error error) {
        return new ResultWithValue<>(null, false, error);
    }
}

