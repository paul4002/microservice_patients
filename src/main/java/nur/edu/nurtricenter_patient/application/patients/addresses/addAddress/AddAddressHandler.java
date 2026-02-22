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
import nur.edu.nurtricenter_patient.domain.patient.events.AddressAddedEvent;

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
      java.util.UUID addressId = patient.addAddressWithId(
        request.label(),
        request.line1(),
        request.line2(),
        request.country(),
        request.province(),
        request.city(),
        new Coordinate(request.latitude(), request.longitude())
      );
      patientRepository.update(patient);
      patient.addDomainEvent(new AddressAddedEvent(
        patient.getId(),
        addressId,
        request.label(),
        request.line1(),
        request.line2(),
        request.city(),
        request.province(),
        request.country(),
        request.latitude(),
        request.longitude()
      ));
      unitOfWork.commitAsync(patient);
      return ResultWithValue.success(addressId);
    } catch (DomainException e) {
      return ResultWithValue.fail(e.getError());
    }
  }
}
