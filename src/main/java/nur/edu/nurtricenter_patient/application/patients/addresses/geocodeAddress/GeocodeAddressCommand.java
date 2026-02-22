package nur.edu.nurtricenter_patient.application.patients.addresses.geocodeAddress;

import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.Result;

public record GeocodeAddressCommand(
  UUID patientId,
  UUID addressId,
  Double latitude,
  Double longitude
) implements Command<Result> {}
