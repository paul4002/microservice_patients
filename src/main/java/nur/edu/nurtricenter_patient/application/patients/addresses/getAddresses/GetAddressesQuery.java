package nur.edu.nurtricenter_patient.application.patients.addresses.getAddresses;

import java.util.List;
import java.util.UUID;

import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.core.abstractions.IQuery;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

public record GetAddressesQuery(UUID patientId) implements IQuery<ResultWithValue<List<AddressDto>>> {}
