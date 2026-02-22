package nur.edu.nurtricenter_patient.application.patients.deletePatient;

import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.Result;

public record DeletePatientCommand(UUID id) implements Command<Result> {}
