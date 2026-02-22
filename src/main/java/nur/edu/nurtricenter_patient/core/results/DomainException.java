package nur.edu.nurtricenter_patient.core.results;

public class DomainException extends RuntimeException {

    private final Error error;

    public DomainException(Error error) {
        super(error != null ? error.toString() : null); // opcionalmente usar el mensaje del error
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
