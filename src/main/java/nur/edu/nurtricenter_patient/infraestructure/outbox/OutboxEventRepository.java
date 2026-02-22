package nur.edu.nurtricenter_patient.infraestructure.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface OutboxEventRepository extends CrudRepository<OutboxEventEntity, UUID> {
  List<OutboxEventEntity> findByProcessedOnIsNullAndNextAttemptAtLessThanEqualOrderByOccurredOnAsc(LocalDateTime now, Pageable pageable);
}
