package nur.edu.nurtricenter_patient.application.patients.createPatient;

import java.time.LocalDate;
import java.util.UUID;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CreatePatientCommand(
  @NotBlank @Size(max = 255) String name,
  @NotBlank @Size(max = 255) String lastname,
  @NotNull @Past @JsonFormat(pattern = "dd-MM-yyyy") LocalDate birthDate,
  @Size(max = 255) String email,
  @Size(max = 255) String cellphone,
  @NotBlank @Size(max = 255) String document,
  UUID subscriptionId
) implements Command<ResultWithValue<UUID>> {

}
