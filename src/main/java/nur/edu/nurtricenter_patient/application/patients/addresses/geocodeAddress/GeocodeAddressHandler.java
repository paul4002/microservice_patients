package nur.edu.nurtricenter_patient.application.patients.addresses.geocodeAddress;

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
import nur.edu.nurtricenter_patient.domain.patient.events.AddressGeocodedEvent;

@Component
public class GeocodeAddressHandler implements Command.Handler<GeocodeAddressCommand, Result> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;

  public GeocodeAddressHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
  }

  @Override
  @Transactional
  public Result handle(GeocodeAddressCommand request) {
    Patient patient = patientRepository.getById(request.patientId());
    if (patient == null) {
      return Result.failure(PatientErrors.PatientNotFound(request.patientId().toString()));
    }
    try {
      patient.updateAddressGeo(request.addressId(), new Coordinate(request.latitude(), request.longitude()));
    } catch (DomainException e) {
      return Result.failure(e.getError());
    }
    patientRepository.update(patient);
    patient.addDomainEvent(new AddressGeocodedEvent(patient.getId(), request.addressId(), request.latitude(), request.longitude()));
    unitOfWork.commitAsync(patient);
    return Result.success();
  }
}
