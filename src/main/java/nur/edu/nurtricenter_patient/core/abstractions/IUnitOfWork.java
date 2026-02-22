package nur.edu.nurtricenter_patient.core.abstractions;

import java.util.concurrent.CompletableFuture;

public interface IUnitOfWork {
    CompletableFuture<Void> commitAsync(AggregateRoot... aggregates);
}
