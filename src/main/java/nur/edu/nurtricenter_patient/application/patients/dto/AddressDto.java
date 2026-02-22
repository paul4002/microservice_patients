package nur.edu.nurtricenter_patient.application.patients.dto;

import java.util.UUID;

public record AddressDto(
  UUID id,
  String label,
  String line1,
  String line2,
  String country,
  String province,
  String city,
  Double latitude,
  Double longitude,
  Boolean active
) {}
