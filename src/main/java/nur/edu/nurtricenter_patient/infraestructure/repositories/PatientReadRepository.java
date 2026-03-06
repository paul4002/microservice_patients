package nur.edu.nurtricenter_patient.infraestructure.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import nur.edu.nurtricenter_patient.application.patients.IPatientReadRepository;
import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.AddressEntity;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.PatientEntity;

/**
 * Implementación del repositorio de lectura (query side de CQRS).
 * Mapea directamente desde JPA a DTOs, sin pasar por el aggregate de dominio.
 */
@Repository
public class PatientReadRepository implements IPatientReadRepository {

  private final PatientEntityRepository jpaRepository;

  public PatientReadRepository(PatientEntityRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<PatientDto> findById(UUID id) {
    return jpaRepository.findById(id).map(this::toDto);
  }

  @Override
  public List<PatientDto> findAll() {
    List<PatientDto> result = new ArrayList<>();
    jpaRepository.findAll().forEach(entity -> result.add(toDto(entity)));
    return result;
  }

  @Override
  public List<AddressDto> findAddressesByPatientId(UUID patientId) {
    return jpaRepository.findById(patientId)
        .map(entity -> entity.getAddresses().stream().map(this::toAddressDto).toList())
        .orElse(List.of());
  }

  private PatientDto toDto(PatientEntity entity) {
    List<AddressDto> addresses = new ArrayList<>();
    if (entity.getAddresses() != null) {
      entity.getAddresses().forEach(a -> addresses.add(toAddressDto(a)));
    }
    return new PatientDto(
        entity.getId(),
        entity.getName(),
        entity.getLastname(),
        entity.getBirthDate(),
        entity.getEmail() != null ? entity.getEmail().value() : null,
        entity.getCellphone() != null ? entity.getCellphone().value() : null,
        entity.getDocument(),
        entity.getSubscriptionId(),
        entity.getSubscriptionStatus(),
        entity.getSubscriptionEndsOn(),
        addresses
    );
  }

  private AddressDto toAddressDto(AddressEntity entity) {
    return new AddressDto(
        entity.getId(),
        entity.getLabel(),
        entity.getLine1(),
        entity.getLine2(),
        entity.getCountry(),
        entity.getProvince(),
        entity.getCity(),
        entity.getLatitude(),
        entity.getLongitude(),
        entity.getState()
    );
  }
}
