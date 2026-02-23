package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundEventRepository extends JpaRepository<InboundEventEntity, UUID> {
  Optional<InboundEventEntity> findByEventId(UUID eventId);
}
