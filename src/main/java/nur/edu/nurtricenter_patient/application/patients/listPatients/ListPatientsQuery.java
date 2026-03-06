package nur.edu.nurtricenter_patient.application.patients.listPatients;

import java.util.List;

import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.core.abstractions.IQuery;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

public record ListPatientsQuery() implements IQuery<ResultWithValue<List<PatientDto>>> {}
