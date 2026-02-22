package nur.edu.nurtricenter_patient.application.patients.createPatient;

import java.util.UUID;

import org.springframework.stereotype.Component;
import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientCreatedEvent;

@Component
public class CreatePatientHandler implements Command.Handler<CreatePatientCommand, ResultWithValue<UUID>> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;

  public CreatePatientHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
  }

  @Override
  @Transactional
  public ResultWithValue<UUID> handle(CreatePatientCommand request) {
    Patient patient;
    try {
      patient = new Patient(
        request.name(),
        request.lastname(),
        request.birthDate(),
        new Email(request.email()),
        new Cellphone(request.cellphone()),
        request.document(),
        request.subscriptionId()
      );
    } catch (DomainException e) {
      return ResultWithValue.fail(e.getError());
    }
    if (patientRepository.existsByEmail(patient.getEmail().value())) {
      return ResultWithValue.fail(PatientErrors.EmailAlreadyExists(patient.getEmail().value()));
    }
    if (patientRepository.existsByCellphone(patient.getCellphone().value())) {
      return ResultWithValue.fail(PatientErrors.CellphoneAlreadyExists(patient.getCellphone().value()));
    }
    if (patientRepository.existsByDocument(patient.getDocument())) {
      return ResultWithValue.fail(PatientErrors.DocumentAlreadyExists(patient.getDocument()));
    }
    this.patientRepository.add(patient);
    patient.addDomainEvent(new PatientCreatedEvent(
      patient.getId(),
      patient.getName() + " " + patient.getLastname(),
      patient.getDocument(),
      patient.getSubscriptionId()
    ));
    this.unitOfWork.commitAsync(patient);
    return ResultWithValue.success(patient.getId());
  }
}
