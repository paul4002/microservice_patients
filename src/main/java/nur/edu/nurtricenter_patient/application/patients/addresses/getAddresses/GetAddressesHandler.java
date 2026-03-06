package nur.edu.nurtricenter_patient.application.patients.addresses.getAddresses;

import java.util.List;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.IPatientReadRepository;
import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

@Component
public class GetAddressesHandler implements Command.Handler<GetAddressesQuery, ResultWithValue<List<AddressDto>>> {
  private final IPatientReadRepository readRepository;

  public GetAddressesHandler(IPatientReadRepository readRepository) {
    this.readRepository = readRepository;
  }

  @Override
  public ResultWithValue<List<AddressDto>> handle(GetAddressesQuery request) {
    return ResultWithValue.success(readRepository.findAddressesByPatientId(request.patientId()));
  }
}
