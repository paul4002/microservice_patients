package nur.edu.nurtricenter_patient.application.patients.addresses.deactivateAddress;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;

@Component
public class DeactivateAddressHandler implements Command.Handler<DeactivateAddressCommand, Result> {
  private final IPatientRepository patientRepository;

  public DeactivateAddressHandler(IPatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  @Override
  @Transactional
  public Result handle(DeactivateAddressCommand request) {
    Patient patient = patientRepository.getById(request.patientId());
    if (patient == null) {
      return Result.failure(PatientErrors.PatientNotFound(request.patientId().toString()));
    }
    try {
      patient.deactivateAddress(request.addressId());
    } catch (DomainException e) {
      return Result.failure(e.getError());
    }
    patientRepository.update(patient);
    return Result.success();
  }
}
