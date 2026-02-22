package nur.edu.nurtricenter_patient.webapi.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import an.awesome.pipelinr.Pipeline;
import nur.edu.nurtricenter_patient.application.patients.createPatient.CreatePatientCommand;
import nur.edu.nurtricenter_patient.core.results.ErrorType;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

  private final Pipeline pipeline;

  public PatientController(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  @PostMapping
  public ResponseEntity<ResultWithValue<UUID>> createPatient(@RequestBody CreatePatientCommand command) {
    ResultWithValue<UUID> result = command.execute(pipeline);
    if (result.isSuccess()) {
      return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    HttpStatus status = mapErrorStatus(result.getError().getType());
    return ResponseEntity.status(status).body(result);
  }

  private HttpStatus mapErrorStatus(ErrorType type) {
    return switch (type) {
      case VALIDATION -> HttpStatus.BAD_REQUEST;
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case CONFLICT -> HttpStatus.CONFLICT;
      case PROBLEM -> HttpStatus.UNPROCESSABLE_ENTITY;
      case FAILURE -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
