package nur.edu.nurtricenter_patient.infraestructure.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.PatientEntityMapper;

@Repository
public class PatientEntityJpaRepository implements IPatientRepository {

  private final PatientEntityRepository jpaRepository;

  public PatientEntityJpaRepository(PatientEntityRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public UUID add(Patient patient) {
    jpaRepository.save(PatientEntityMapper.toEntity(patient));
    return patient.getId();
  }

  @Override
  public Patient getById(UUID id) {
    return jpaRepository.findById(id)
        .map(PatientEntityMapper::toDomain)
        .orElse(null);
  }

  @Override
  public List<Patient> getAll() {
    List<Patient> result = new ArrayList<>();
    jpaRepository.findAll().forEach(entity -> result.add(PatientEntityMapper.toDomain(entity)));
    return result;
  }

  @Override
  public List<Patient> getBySubscriptionId(UUID subscriptionId) {
    List<Patient> result = new ArrayList<>();
    jpaRepository.findBySubscriptionId(subscriptionId)
        .forEach(entity -> result.add(PatientEntityMapper.toDomain(entity)));
    return result;
  }

  @Override
  public void update(Patient patient) {
    jpaRepository.save(PatientEntityMapper.toEntity(patient));
  }

  @Override
  public void remove(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(new Email(email));
  }

  @Override
  public boolean existsByCellphone(String cellphone) {
    return jpaRepository.existsByCellphone(new Cellphone(cellphone));
  }

  @Override
  public boolean existsByDocument(String document) {
    return jpaRepository.existsByDocument(document);
  }

  @Override
  public boolean existsByEmailAndNotId(String email, UUID id) {
    return jpaRepository.existsByEmailAndIdNot(new Email(email), id);
  }

  @Override
  public boolean existsByCellphoneAndNotId(String cellphone, UUID id) {
    return jpaRepository.existsByCellphoneAndIdNot(new Cellphone(cellphone), id);
  }

  @Override
  public boolean existsByDocumentAndNotId(String document, UUID id) {
    return jpaRepository.existsByDocumentAndIdNot(document, id);
  }
}
