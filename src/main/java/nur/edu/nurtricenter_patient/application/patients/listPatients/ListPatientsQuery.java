package nur.edu.nurtricenter_patient.application.patients.listPatients;

import java.util.List;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

public record ListPatientsQuery() implements Command<ResultWithValue<List<PatientDto>>> {}
