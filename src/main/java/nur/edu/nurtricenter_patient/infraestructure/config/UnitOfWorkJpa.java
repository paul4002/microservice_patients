package nur.edu.nurtricenter_patient.infraestructure.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.AggregateRoot;
import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.infraestructure.outbox.OutboxEventEntity;
import nur.edu.nurtricenter_patient.infraestructure.outbox.OutboxEventMapper;
import nur.edu.nurtricenter_patient.infraestructure.outbox.OutboxEventRepository;

@Component
public class UnitOfWorkJpa implements IUnitOfWork {
  private final EntityManager em;
  private final OutboxEventRepository outboxRepository;
  private final OutboxEventMapper mapper;
  

  public UnitOfWorkJpa(EntityManager em, OutboxEventRepository outboxRepository, ObjectMapper objectMapper) {
    this.em = em;
    this.outboxRepository = outboxRepository;
    this.mapper = new OutboxEventMapper(objectMapper);
  }

  @Override
  @Transactional
  public CompletableFuture<Void> commitAsync(AggregateRoot... aggregates) {
    List<DomainEvent> events = new ArrayList<>();
    if (aggregates != null) {
      for (AggregateRoot aggregate : aggregates) {
        if (aggregate != null) {
          events.addAll(aggregate.getDomainEvents());
        }
      }
    }
    if (!events.isEmpty()) {
      List<OutboxEventEntity> outbox = new ArrayList<>();
      for (DomainEvent event : events) {
        outbox.add(mapper.toEntity(event));
      }
      outboxRepository.saveAll(outbox);
    }
    this.em.flush();
    if (aggregates != null) {
      for (AggregateRoot aggregate : aggregates) {
        if (aggregate != null) {
          aggregate.clearDomainEvents();
        }
      }
    }
    return CompletableFuture.completedFuture(null);
  }
}
