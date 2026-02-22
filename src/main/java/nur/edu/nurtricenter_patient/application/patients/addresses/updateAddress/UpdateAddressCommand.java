package nur.edu.nurtricenter_patient.application.patients.addresses.updateAddress;

import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.Result;

public record UpdateAddressCommand(
  UUID patientId,
  UUID addressId,
  String label,
  String line1,
  String line2,
  String country,
  String province,
  String city,
  Double latitude,
  Double longitude
) implements Command<Result> {}
