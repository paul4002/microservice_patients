package nur.edu.nurtricenter_patient.application.patients.addresses.getAddresses;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;

@Component
public class GetAddressesHandler implements Command.Handler<GetAddressesQuery, ResultWithValue<List<AddressDto>>> {
  private final IPatientRepository patientRepository;

  public GetAddressesHandler(IPatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  @Override
  public ResultWithValue<List<AddressDto>> handle(GetAddressesQuery request) {
    Patient patient = patientRepository.getById(request.patientId());
    if (patient == null) {
      return ResultWithValue.fail(PatientErrors.PatientNotFound(request.patientId().toString()));
    }
    List<AddressDto> result = new ArrayList<>();
    patient.getAddresses().forEach(address -> result.add(new AddressDto(
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
    )));
    return ResultWithValue.success(result);
  }
}
