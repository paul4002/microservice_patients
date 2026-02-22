package nur.edu.nurtricenter_patient.domain.patient;

import java.util.UUID;
import java.util.List;

public interface IPatientRepository {
  UUID add(Patient patient);
  Patient getById(UUID id);
  List<Patient> getAll();
  List<Patient> getBySubscriptionId(UUID subscriptionId);
  void update(Patient patient);
  void remove(UUID id);
  boolean existsByEmail(String email);
  boolean existsByCellphone(String cellphone);
  boolean existsByDocument(String document);
  boolean existsByEmailAndNotId(String email, UUID id);
  boolean existsByCellphoneAndNotId(String cellphone, UUID id);
  boolean existsByDocumentAndNotId(String document, UUID id);
}
