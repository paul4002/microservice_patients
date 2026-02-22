package nur.edu.nurtricenter_patient.application.patients.addresses.addAddress;

import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

public record AddAddressCommand(
  UUID patientId,
  String label,
  String line1,
  String line2,
  String country,
  String province,
  String city,
  Double latitude,
  Double longitude
) implements Command<ResultWithValue<UUID>> {}
