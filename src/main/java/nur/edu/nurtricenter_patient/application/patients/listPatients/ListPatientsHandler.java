package nur.edu.nurtricenter_patient.application.patients.listPatients;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.application.patients.mappers.PatientMapper;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;

@Component
public class ListPatientsHandler implements Command.Handler<ListPatientsQuery, ResultWithValue<List<PatientDto>>> {
  private final IPatientRepository patientRepository;

  public ListPatientsHandler(IPatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  @Override
  public ResultWithValue<List<PatientDto>> handle(ListPatientsQuery request) {
    List<PatientDto> result = new ArrayList<>();
    patientRepository.getAll().forEach(patient -> result.add(PatientMapper.toDto(patient)));
    return ResultWithValue.success(result);
  }
}
