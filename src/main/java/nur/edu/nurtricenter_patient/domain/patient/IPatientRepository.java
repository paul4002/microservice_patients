package nur.edu.nurtricenter_patient.domain.patient;

import java.util.UUID;

public interface IPatientRepository {
  UUID add(Patient patient);
  Patient getById(UUID id);
  void update(Patient patient);
}
