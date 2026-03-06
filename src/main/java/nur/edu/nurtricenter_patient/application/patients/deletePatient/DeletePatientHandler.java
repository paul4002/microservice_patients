package nur.edu.nurtricenter_patient.application.patients.deletePatient;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.PatientErrors;
@Component
public class DeletePatientHandler implements Command.Handler<DeletePatientCommand, Result> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;

  public DeletePatientHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
  }

  @Override
  @Transactional
  public Result handle(DeletePatientCommand request) {
    Patient patient = patientRepository.getById(request.id());
    if (patient == null) {
      return Result.failure(PatientErrors.PatientNotFound(request.id().toString()));
    }
    patient.markAsDeleted();
    patientRepository.remove(request.id());
    unitOfWork.commit(patient);
    return Result.success();
  }
}
