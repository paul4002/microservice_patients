package nur.edu.nurtricenter_patient.application.patients;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;

/**
 * Repositorio de solo lectura (query side de CQRS).
 * Devuelve DTOs directamente sin reconstituir agregados de dominio.
 */
public interface IPatientReadRepository {
  Optional<PatientDto> findById(UUID id);
  List<PatientDto> findAll();
  List<AddressDto> findAddressesByPatientId(UUID patientId);
}
