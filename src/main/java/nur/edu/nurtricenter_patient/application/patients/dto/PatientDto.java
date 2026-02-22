package nur.edu.nurtricenter_patient.application.patients.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PatientDto(
  UUID id,
  String name,
  String lastname,
  LocalDate birthDate,
  String email,
  String cellphone,
  String document,
  UUID subscriptionId,
  List<AddressDto> addresses
) {}
