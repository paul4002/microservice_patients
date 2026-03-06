package nur.edu.nurtricenter_patient.core.abstractions;

public interface IUnitOfWork {
    void commit(AggregateRoot... aggregates);
}
