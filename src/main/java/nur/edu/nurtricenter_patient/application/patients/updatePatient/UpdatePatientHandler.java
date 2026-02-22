package nur.edu.nurtricenter_patient.application.patients.updatePatient;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientUpdatedEvent;

@Component
public class UpdatePatientHandler implements Command.Handler<UpdatePatientCommand, Result> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;

  public UpdatePatientHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
  }

  @Override
  @Transactional
  public Result handle(UpdatePatientCommand request) {
    Patient patient = patientRepository.getById(request.id());
    if (patient == null) {
      return Result.failure(PatientErrors.PatientNotFound(request.id().toString()));
    }
    if (patientRepository.existsByEmailAndNotId(request.email(), request.id())) {
      return Result.failure(PatientErrors.EmailAlreadyExists(request.email()));
    }
    if (patientRepository.existsByCellphoneAndNotId(request.cellphone(), request.id())) {
      return Result.failure(PatientErrors.CellphoneAlreadyExists(request.cellphone()));
    }
    if (patientRepository.existsByDocumentAndNotId(request.document(), request.id())) {
      return Result.failure(PatientErrors.DocumentAlreadyExists(request.document()));
    }
    try {
      patient.update(
        request.name(),
        request.lastname(),
        request.birthDate(),
        new Email(request.email()),
        new Cellphone(request.cellphone()),
        request.document(),
        request.subscriptionId()
      );
    } catch (DomainException e) {
      return Result.failure(e.getError());
    }
    patientRepository.update(patient);
    patient.addDomainEvent(new PatientUpdatedEvent(
      patient.getId(),
      patient.getName() + " " + patient.getLastname(),
      patient.getDocument(),
      patient.getSubscriptionId()
    ));
    unitOfWork.commitAsync(patient);
    return Result.success();
  }
}
