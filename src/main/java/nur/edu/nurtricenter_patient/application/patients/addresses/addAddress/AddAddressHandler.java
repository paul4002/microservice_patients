package nur.edu.nurtricenter_patient.application.patients.addresses.addAddress;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;
@Component
public class AddAddressHandler implements Command.Handler<AddAddressCommand, ResultWithValue<java.util.UUID>> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;

  public AddAddressHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
  }

  @Override
  @Transactional
  public ResultWithValue<java.util.UUID> handle(AddAddressCommand request) {
    Patient patient = patientRepository.getById(request.patientId());
    if (patient == null) {
      return ResultWithValue.fail(PatientErrors.PatientNotFound(request.patientId().toString()));
    }
    try {
      java.util.UUID addressId = patient.addAddress(
          request.label(),
          request.line1(),
          request.line2(),
          request.country(),
          request.province(),
          request.city(),
          new Coordinate(request.latitude(), request.longitude())
      );
      patientRepository.update(patient);
      unitOfWork.commit(patient);
      return ResultWithValue.success(addressId);
    } catch (DomainException e) {
      return ResultWithValue.fail(e.getError());
    }
  }
}
