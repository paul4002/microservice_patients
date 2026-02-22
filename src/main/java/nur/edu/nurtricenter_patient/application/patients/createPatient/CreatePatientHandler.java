package nur.edu.nurtricenter_patient.application.patients.createPatient;

import java.util.UUID;

import org.springframework.stereotype.Component;
import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

@Component
public class CreatePatientHandler implements Command.Handler<CreatePatientCommand, ResultWithValue<UUID>> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;

  public CreatePatientHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
  }

  @Override
  public ResultWithValue<UUID> handle(CreatePatientCommand request) {
    Patient patient;
    try {
      patient = new Patient(request.name(), request.lastname(), request.birthDate(), new Email(request.email()), new Cellphone(request.cellphone()));
    } catch (DomainException e) {
      return ResultWithValue.failure(e.getError());
    }
    this.patientRepository.add(patient);
    this.unitOfWork.commitAsync();
    return ResultWithValue.success(patient.getId());
  }
}
