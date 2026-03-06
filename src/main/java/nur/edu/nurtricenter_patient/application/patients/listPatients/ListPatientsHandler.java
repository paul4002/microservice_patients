package nur.edu.nurtricenter_patient.application.patients.listPatients;

import java.util.List;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.IPatientReadRepository;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

@Component
public class ListPatientsHandler implements Command.Handler<ListPatientsQuery, ResultWithValue<List<PatientDto>>> {
  private final IPatientReadRepository readRepository;

  public ListPatientsHandler(IPatientReadRepository readRepository) {
    this.readRepository = readRepository;
  }

  @Override
  public ResultWithValue<List<PatientDto>> handle(ListPatientsQuery request) {
    return ResultWithValue.success(readRepository.findAll());
  }
}
