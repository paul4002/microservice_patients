package nur.edu.nurtricenter_patient.application.patients.updatePatient;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.Result;

public record UpdatePatientCommand(
  UUID id,
  String name,
  String lastname,
  @JsonFormat(pattern = "dd-MM-yyyy") LocalDate birthDate,
  String email,
  String cellphone,
  String document,
  UUID subscriptionId
) implements Command<Result> {}
