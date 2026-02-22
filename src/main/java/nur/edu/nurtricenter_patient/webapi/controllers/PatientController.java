package nur.edu.nurtricenter_patient.webapi.controllers;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import an.awesome.pipelinr.Pipeline;
import nur.edu.nurtricenter_patient.application.patients.createPatient.CreatePatientCommand;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

  private final Pipeline pipeline;

  public PatientController(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  @PostMapping
  public ResultWithValue<UUID> createPatient(@RequestBody CreatePatientCommand command) {
    return command.execute(pipeline);
  }
}
