package nur.edu.nurtricenter_patient.application.patients.getPatient;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.IPatientReadRepository;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;

@Component
public class GetPatientHandler implements Command.Handler<GetPatientQuery, ResultWithValue<PatientDto>> {
  private final IPatientReadRepository readRepository;

  public GetPatientHandler(IPatientReadRepository readRepository) {
    this.readRepository = readRepository;
  }

  @Override
  public ResultWithValue<PatientDto> handle(GetPatientQuery request) {
    return readRepository.findById(request.id())
        .map(ResultWithValue::success)
        .orElseGet(() -> ResultWithValue.fail(PatientErrors.PatientNotFound(request.id().toString())));
  }
}
