package nur.edu.nurtricenter_patient.infraestructure.repositories;

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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void update(Patient patient) {
    // TODO Auto-generated method stub
    
  }
}
