package nur.edu.nurtricenter_patient.application.patients.addresses.deactivateAddress;

import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.Result;

public record DeactivateAddressCommand(UUID patientId, UUID addressId) implements Command<Result> {}
