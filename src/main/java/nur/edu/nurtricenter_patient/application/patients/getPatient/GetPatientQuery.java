package nur.edu.nurtricenter_patient.application.patients.getPatient;

import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

public record GetPatientQuery(UUID id) implements Command<ResultWithValue<PatientDto>> {}
