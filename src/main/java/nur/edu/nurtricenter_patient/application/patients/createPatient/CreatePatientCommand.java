package nur.edu.nurtricenter_patient.application.patients.createPatient;

import java.time.LocalDate;
import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CreatePatientCommand(String name, String lastname, @JsonFormat(pattern = "dd-MM-yyyy") LocalDate birthDate, String email, String cellphone) implements Command<ResultWithValue<UUID>> {

}
