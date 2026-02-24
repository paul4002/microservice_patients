package nur.edu.nurtricenter_patient.webapi.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import an.awesome.pipelinr.Pipeline;
import nur.edu.nurtricenter_patient.application.patients.addresses.addAddress.AddAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.deactivateAddress.DeactivateAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.geocodeAddress.GeocodeAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.getAddresses.GetAddressesQuery;
import nur.edu.nurtricenter_patient.application.patients.addresses.updateAddress.UpdateAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.createPatient.CreatePatientCommand;
import nur.edu.nurtricenter_patient.application.patients.deletePatient.DeletePatientCommand;
import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.application.patients.getPatient.GetPatientQuery;
import nur.edu.nurtricenter_patient.application.patients.listPatients.ListPatientsQuery;
import nur.edu.nurtricenter_patient.application.patients.updatePatient.UpdatePatientCommand;
import nur.edu.nurtricenter_patient.core.results.ErrorType;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

@RestController
@RequestMapping({"/api/patient", "/api/patients"})
public class PatientController {

  private final Pipeline pipeline;

  public PatientController(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  @PostMapping
  public ResponseEntity<ResultWithValue<UUID>> createPatient(@RequestBody CreatePatientCommand command) {
    ResultWithValue<UUID> result = command.execute(pipeline);
    return toResponse(result, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResultWithValue<PatientDto>> getPatient(@PathVariable UUID id) {
    ResultWithValue<PatientDto> result = new GetPatientQuery(id).execute(pipeline);
    return toResponse(result, HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<ResultWithValue<List<PatientDto>>> listPatients() {
    ResultWithValue<List<PatientDto>> result = new ListPatientsQuery().execute(pipeline);
    return toResponse(result, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Result> updatePatient(@PathVariable UUID id, @RequestBody UpdatePatientCommand command) {
    Result result = new UpdatePatientCommand(
      id,
      command.name(),
      command.lastname(),
      command.birthDate(),
      command.email(),
      command.cellphone(),
      command.document(),
      command.subscriptionId()
    ).execute(pipeline);
    return toResponse(result, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Result> deletePatient(@PathVariable UUID id) {
    Result result = new DeletePatientCommand(id).execute(pipeline);
    return toResponse(result, HttpStatus.NO_CONTENT);
  }

  @PostMapping({"/{id}/address", "/{id}/addresses"})
  public ResponseEntity<ResultWithValue<UUID>> addAddress(@PathVariable UUID id, @RequestBody AddAddressCommand command) {
    ResultWithValue<UUID> result = new AddAddressCommand(
      id,
      command.label(),
      command.line1(),
      command.line2(),
      command.country(),
      command.province(),
      command.city(),
      command.latitude(),
      command.longitude()
    ).execute(pipeline);
    return toResponse(result, HttpStatus.CREATED);
  }

  @PutMapping({"/{id}/address/{addressId}", "/{id}/addresses/{addressId}"})
  public ResponseEntity<Result> updateAddress(@PathVariable UUID id, @PathVariable UUID addressId, @RequestBody UpdateAddressCommand command) {
    Result result = new UpdateAddressCommand(
      id,
      addressId,
      command.label(),
      command.line1(),
      command.line2(),
      command.country(),
      command.province(),
      command.city(),
      command.latitude(),
      command.longitude()
    ).execute(pipeline);
    return toResponse(result, HttpStatus.OK);
  }

  @PutMapping({"/{id}/address/{addressId}/geo", "/{id}/addresses/{addressId}/geo"})
  public ResponseEntity<Result> geocodeAddress(@PathVariable UUID id, @PathVariable UUID addressId, @RequestBody GeocodeAddressCommand command) {
    Result result = new GeocodeAddressCommand(
      id,
      addressId,
      command.latitude(),
      command.longitude()
    ).execute(pipeline);
    return toResponse(result, HttpStatus.OK);
  }

  @DeleteMapping({"/{id}/address/{addressId}", "/{id}/addresses/{addressId}"})
  public ResponseEntity<Result> deactivateAddress(@PathVariable UUID id, @PathVariable UUID addressId) {
    Result result = new DeactivateAddressCommand(id, addressId).execute(pipeline);
    return toResponse(result, HttpStatus.NO_CONTENT);
  }

  @GetMapping({"/{id}/address", "/{id}/addresses"})
  public ResponseEntity<ResultWithValue<List<AddressDto>>> getAddresses(@PathVariable UUID id) {
    ResultWithValue<List<AddressDto>> result = new GetAddressesQuery(id).execute(pipeline);
    return toResponse(result, HttpStatus.OK);
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

  private <T> ResponseEntity<ResultWithValue<T>> toResponse(ResultWithValue<T> result, HttpStatus successStatus) {
    if (result.isSuccess()) {
      return ResponseEntity.status(successStatus).body(result);
    }
    HttpStatus status = mapErrorStatus(result.getError().getType());
    return ResponseEntity.status(status).body(result);
  }

  private ResponseEntity<Result> toResponse(Result result, HttpStatus successStatus) {
    if (result.isSuccess()) {
      return ResponseEntity.status(successStatus).body(result);
    }
    HttpStatus status = mapErrorStatus(result.getError().getType());
    return ResponseEntity.status(status).body(result);
  }
}
