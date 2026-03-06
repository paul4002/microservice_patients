package nur.edu.nurtricenter_patient.infraestructure.domainModel;

import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

/**
 * Mapper entre el agregado de dominio Patient y su entidad JPA PatientEntity.
 * Centraliza la lógica de conversión fuera de las clases de entidad.
 */
public final class PatientEntityMapper {

  private PatientEntityMapper() {}

  public static PatientEntity toEntity(Patient patient) {
    PatientEntity entity = new PatientEntity();
    entity.setId(patient.getId());
    entity.setName(patient.getName());
    entity.setLastname(patient.getLastname());
    entity.setBirthDate(patient.getBirthDate());
    entity.setEmail(patient.getEmail());
    entity.setCellphone(patient.getCellphone());
    entity.setDocument(patient.getDocument());
    entity.setSubscriptionId(patient.getSubscriptionId());
    entity.setSubscriptionStatus(patient.getSubscriptionStatus());
    entity.setSubscriptionEndsOn(patient.getSubscriptionEndsOn());
    for (Address address : patient.getAddresses()) {
      entity.getAddresses().add(toAddressEntity(address, entity));
    }
    return entity;
  }

  public static Patient toDomain(PatientEntity entity) {
    if (entity == null) return null;
    Patient patient = new Patient(
        entity.getId(),
        entity.getName(),
        entity.getLastname(),
        entity.getBirthDate(),
        entity.getEmail(),
        entity.getCellphone(),
        entity.getDocument(),
        entity.getSubscriptionId(),
        entity.getSubscriptionStatus(),
        entity.getSubscriptionEndsOn()
    );
    if (entity.getAddresses() != null) {
      for (AddressEntity addressEntity : entity.getAddresses()) {
        patient.restoreAddress(toAddressDomain(addressEntity));
      }
    }
    return patient;
  }

  public static AddressEntity toAddressEntity(Address address, PatientEntity patient) {
    AddressEntity entity = new AddressEntity();
    entity.setId(address.getId());
    entity.setPatient(patient);
    entity.setLabel(address.getLabel());
    entity.setLine1(address.getLine1());
    entity.setLine2(address.getLine2());
    entity.setCountry(address.getCountry());
    entity.setProvince(address.getProvince());
    entity.setCity(address.getCity());
    entity.setLatitude(address.getCoordinate().latitude());
    entity.setLongitude(address.getCoordinate().longitude());
    entity.setState(address.isActive());
    return entity;
  }

  public static Address toAddressDomain(AddressEntity entity) {
    if (entity == null) return null;
    Coordinate coordinate = new Coordinate(entity.getLatitude(), entity.getLongitude());
    return new Address(
        entity.getId(), entity.getLabel(), entity.getLine1(), entity.getLine2(),
        entity.getCountry(), entity.getProvince(), entity.getCity(),
        coordinate, entity.getState()
    );
  }
}
