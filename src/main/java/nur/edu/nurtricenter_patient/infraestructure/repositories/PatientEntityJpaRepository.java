package nur.edu.nurtricenter_patient.infraestructure.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.PatientEntity;

@Repository
public class PatientEntityJpaRepository implements IPatientRepository {

  @Autowired
  private PatientEntityRepository patientEntityRepository;

  @Override
  public UUID add(Patient patient) {
    PatientEntity entity = PatientEntity.fromDomain(patient);
    this.patientEntityRepository.save(entity);
    return patient.getId();
  }

  @Override
  public Patient getById(UUID id) {
    return this.patientEntityRepository.findById(id)
      .map(PatientEntity::toDomain)
      .orElse(null);
  }

  @Override
  public List<Patient> getAll() {
    List<Patient> result = new ArrayList<>();
    this.patientEntityRepository.findAll().forEach(entity -> result.add(PatientEntity.toDomain(entity)));
    return result;
  }

  @Override
  public List<Patient> getBySubscriptionId(UUID subscriptionId) {
    List<Patient> result = new ArrayList<>();
    this.patientEntityRepository.findBySubscriptionId(subscriptionId)
      .forEach(entity -> result.add(PatientEntity.toDomain(entity)));
    return result;
  }

  @Override
  public void update(Patient patient) {
    PatientEntity entity = PatientEntity.fromDomain(patient);
    this.patientEntityRepository.save(entity);
  }

  @Override
  public void remove(UUID id) {
    this.patientEntityRepository.deleteById(id);
  }

  @Override
  public boolean existsByEmail(String email) {
    return this.patientEntityRepository.existsByEmail(new nur.edu.nurtricenter_patient.domain.patient.Email(email));
  }

  @Override
  public boolean existsByCellphone(String cellphone) {
    return this.patientEntityRepository.existsByCellphone(new nur.edu.nurtricenter_patient.domain.patient.Cellphone(cellphone));
  }

  @Override
  public boolean existsByDocument(String document) {
    return this.patientEntityRepository.existsByDocument(document);
  }

  @Override
  public boolean existsByEmailAndNotId(String email, UUID id) {
    return this.patientEntityRepository.existsByEmailAndIdNot(new nur.edu.nurtricenter_patient.domain.patient.Email(email), id);
  }

  @Override
  public boolean existsByCellphoneAndNotId(String cellphone, UUID id) {
    return this.patientEntityRepository.existsByCellphoneAndIdNot(new nur.edu.nurtricenter_patient.domain.patient.Cellphone(cellphone), id);
  }

  @Override
  public boolean existsByDocumentAndNotId(String document, UUID id) {
    return this.patientEntityRepository.existsByDocumentAndIdNot(document, id);
  }
}
