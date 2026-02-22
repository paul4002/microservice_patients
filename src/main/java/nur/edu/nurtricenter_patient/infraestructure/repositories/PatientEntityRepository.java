package nur.edu.nurtricenter_patient.infraestructure.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import nur.edu.nurtricenter_patient.infraestructure.domainModel.PatientEntity;

public interface PatientEntityRepository extends CrudRepository<PatientEntity, UUID> {

}
