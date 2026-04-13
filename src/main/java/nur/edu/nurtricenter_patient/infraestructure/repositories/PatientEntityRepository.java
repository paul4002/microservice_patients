package nur.edu.nurtricenter_patient.infraestructure.repositories;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.PatientEntity;

public interface PatientEntityRepository extends CrudRepository<PatientEntity, UUID> {

  @EntityGraph(attributePaths = "addresses")
  @Override
  Optional<PatientEntity> findById(UUID id);

  @EntityGraph(attributePaths = "addresses")
  @Override
  Iterable<PatientEntity> findAll();

  @EntityGraph(attributePaths = "addresses")
  List<PatientEntity> findBySubscriptionId(UUID subscriptionId);

  boolean existsByEmail(Email email);
  boolean existsByCellphone(Cellphone cellphone);
  boolean existsByDocument(String document);
  boolean existsByEmailAndIdNot(Email email, UUID id);
  boolean existsByCellphoneAndIdNot(Cellphone cellphone, UUID id);
  boolean existsByDocumentAndIdNot(String document, UUID id);
}
