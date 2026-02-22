package nur.edu.nurtricenter_patient.application.patients.mappers;

import java.util.ArrayList;
import java.util.List;

import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

public final class PatientMapper {
  private PatientMapper() {}

  public static PatientDto toDto(Patient patient) {
    if (patient == null) {
      return null;
    }
    List<AddressDto> addresses = new ArrayList<>();
    for (Address address : patient.getAddresses()) {
      addresses.add(new AddressDto(
        address.getId(),
        address.getLabel(),
        address.getLine1(),
        address.getLine2(),
        address.getCountry(),
        address.getProvince(),
        address.getCity(),
        address.getCoordinate().latitude(),
        address.getCoordinate().longitude(),
        address.isActive()
      ));
    }
    return new PatientDto(
      patient.getId(),
      patient.getName(),
      patient.getLastname(),
      patient.getBirthDate(),
      patient.getEmail().value(),
      patient.getCellphone().value(),
      patient.getDocument(),
      patient.getSubscriptionId(),
      addresses
    );
  }
}
