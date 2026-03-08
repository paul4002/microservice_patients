package nur.edu.nurtricenter_patient.application.patients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.patients.deletePatient.DeletePatientCommand;
import nur.edu.nurtricenter_patient.application.patients.deletePatient.DeletePatientHandler;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

class DeletePatientHandlerTest {

  private IPatientRepository patientRepository;
  private IUnitOfWork unitOfWork;
  private DeletePatientHandler handler;

  @BeforeEach
  void setUp() {
    patientRepository = mock(IPatientRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    handler = new DeletePatientHandler(patientRepository, unitOfWork);
  }

  private Patient buildPatient(UUID id) {
    return new Patient(
        id, "Ana", "Perez", LocalDate.of(1990, 5, 20),
        new Email("ana@example.com"), new Cellphone("75123456"), "DOC-001",
        null, null, null
    );
  }

  @Test
  void shouldDeletePatientSuccessfully() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(buildPatient(id));

    Result result = handler.handle(new DeletePatientCommand(id));

    assertTrue(result.isSuccess());
    verify(patientRepository).remove(id);
    verify(unitOfWork).commit(any());
  }

  @Test
  void shouldEmitPatientDeletedEventOnDelete() {
    UUID id = UUID.randomUUID();
    Patient patient = buildPatient(id);
    when(patientRepository.getById(id)).thenReturn(patient);

    handler.handle(new DeletePatientCommand(id));

    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof
        nur.edu.nurtricenter_patient.domain.patient.events.PatientDeletedEvent);
  }

  @Test
  void shouldFailWhenPatientNotFound() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(null);

    Result result = handler.handle(new DeletePatientCommand(id));

    assertFalse(result.isSuccess());
    assertEquals("Patient.NotFound", result.getError().getCode());
    verify(patientRepository, never()).remove(any());
    verify(unitOfWork, never()).commit(any());
  }
}
