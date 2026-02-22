package nur.edu.nurtricenter_patient.infraestructure.config;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;

@Component
public class UnitOfWorkJpa implements IUnitOfWork {
  private final EntityManager em;
  

  public UnitOfWorkJpa(EntityManager em) {
    this.em = em;
  }

  @Override
  @Async
  @Transactional
  public CompletableFuture<Void> commitAsync() {
    this.em.flush();
    return CompletableFuture.completedFuture(null);
  }
}
