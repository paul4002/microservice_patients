package nur.edu.nurtricenter_patient.application.patients.addresses.getAddresses;

import java.util.List;
import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

public record GetAddressesQuery(UUID patientId) implements Command<ResultWithValue<List<AddressDto>>> {}
