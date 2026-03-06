package nur.edu.nurtricenter_patient.application.patients.addresses.updateAddress;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;
@Component
public class UpdateAddressHandler implements Command.Handler<UpdateAddressCommand, Result> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;

  public UpdateAddressHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
  }

  @Override
  @Transactional
  public Result handle(UpdateAddressCommand request) {
    Patient patient = patientRepository.getById(request.patientId());
    if (patient == null) {
      return Result.failure(PatientErrors.PatientNotFound(request.patientId().toString()));
    }
    try {
      patient.updateAddress(
        request.addressId(),
        request.label(),
        request.line1(),
        request.line2(),
        request.country(),
        request.province(),
        request.city(),
        new Coordinate(request.latitude(), request.longitude())
      );
    } catch (DomainException e) {
      return Result.failure(e.getError());
    }
    patientRepository.update(patient);
    unitOfWork.commit(patient);
    return Result.success();
  }
}
