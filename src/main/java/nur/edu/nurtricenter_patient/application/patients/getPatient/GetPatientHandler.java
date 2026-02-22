package nur.edu.nurtricenter_patient.application.patients.getPatient;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.application.patients.mappers.PatientMapper;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;

@Component
public class GetPatientHandler implements Command.Handler<GetPatientQuery, ResultWithValue<PatientDto>> {
  private final IPatientRepository patientRepository;

  public GetPatientHandler(IPatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  @Override
  public ResultWithValue<PatientDto> handle(GetPatientQuery request) {
    Patient patient = patientRepository.getById(request.id());
    if (patient == null) {
      return ResultWithValue.fail(PatientErrors.PatientNotFound(request.id().toString()));
    }
    return ResultWithValue.success(PatientMapper.toDto(patient));
  }
}
