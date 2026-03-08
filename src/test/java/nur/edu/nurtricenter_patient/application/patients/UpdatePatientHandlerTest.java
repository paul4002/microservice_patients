package nur.edu.nurtricenter_patient.application.patients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.patients.updatePatient.UpdatePatientCommand;
import nur.edu.nurtricenter_patient.application.patients.updatePatient.UpdatePatientHandler;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

class UpdatePatientHandlerTest {

  private IPatientRepository patientRepository;
  private IUnitOfWork unitOfWork;
  private UpdatePatientHandler handler;

  @BeforeEach
  void setUp() {
    patientRepository = mock(IPatientRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    handler = new UpdatePatientHandler(patientRepository, unitOfWork);
  }

  private Patient buildPatient(UUID id) {
    return new Patient(
        id, "Ana", "Perez", LocalDate.of(1990, 5, 20),
        new Email("ana@example.com"), new Cellphone("75123456"), "DOC-001",
        null, null, null
    );
  }

  @Test
  void shouldUpdatePatientSuccessfully() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(buildPatient(id));
    when(patientRepository.existsByEmailAndNotId(anyString(), eq(id))).thenReturn(false);
    when(patientRepository.existsByCellphoneAndNotId(anyString(), eq(id))).thenReturn(false);
    when(patientRepository.existsByDocumentAndNotId(anyString(), eq(id))).thenReturn(false);

    UpdatePatientCommand command = new UpdatePatientCommand(
        id, "Luis", "Gomez", LocalDate.of(1985, 3, 10),
        "luis@example.com", "76543210", "DOC-002", null
    );

    Result result = handler.handle(command);

    assertTrue(result.isSuccess());
    verify(patientRepository).update(any());
    verify(unitOfWork).commit(any());
  }

  @Test
  void shouldFailWhenPatientNotFound() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(null);

    UpdatePatientCommand command = new UpdatePatientCommand(
        id, "Luis", "Gomez", LocalDate.of(1985, 3, 10),
        "luis@example.com", "76543210", "DOC-002", null
    );

    Result result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.NotFound", result.getError().getCode());
    verify(patientRepository, never()).update(any());
  }

  @Test
  void shouldFailWhenEmailIsAlreadyUsedByAnotherPatient() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(buildPatient(id));
    when(patientRepository.existsByEmailAndNotId(anyString(), eq(id))).thenReturn(true);

    UpdatePatientCommand command = new UpdatePatientCommand(
        id, "Luis", "Gomez", LocalDate.of(1985, 3, 10),
        "other@example.com", "76543210", "DOC-002", null
    );

    Result result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.EmailAlreadyExists", result.getError().getCode());
    verify(patientRepository, never()).update(any());
  }

  @Test
  void shouldFailWhenCellphoneIsAlreadyUsedByAnotherPatient() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(buildPatient(id));
    when(patientRepository.existsByEmailAndNotId(anyString(), eq(id))).thenReturn(false);
    when(patientRepository.existsByCellphoneAndNotId(anyString(), eq(id))).thenReturn(true);

    UpdatePatientCommand command = new UpdatePatientCommand(
        id, "Luis", "Gomez", LocalDate.of(1985, 3, 10),
        "luis@example.com", "76543210", "DOC-002", null
    );

    Result result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.CellphoneAlreadyExists", result.getError().getCode());
  }

  @Test
  void shouldFailWhenDocumentIsAlreadyUsedByAnotherPatient() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(buildPatient(id));
    when(patientRepository.existsByEmailAndNotId(anyString(), eq(id))).thenReturn(false);
    when(patientRepository.existsByCellphoneAndNotId(anyString(), eq(id))).thenReturn(false);
    when(patientRepository.existsByDocumentAndNotId(anyString(), eq(id))).thenReturn(true);

    UpdatePatientCommand command = new UpdatePatientCommand(
        id, "Luis", "Gomez", LocalDate.of(1985, 3, 10),
        "luis@example.com", "76543210", "DOC-999", null
    );

    Result result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.DocumentAlreadyExists", result.getError().getCode());
  }

  @Test
  void shouldFailWhenNewDataIsInvalid() {
    UUID id = UUID.randomUUID();
    when(patientRepository.getById(id)).thenReturn(buildPatient(id));
    when(patientRepository.existsByEmailAndNotId(anyString(), eq(id))).thenReturn(false);
    when(patientRepository.existsByCellphoneAndNotId(anyString(), eq(id))).thenReturn(false);
    when(patientRepository.existsByDocumentAndNotId(anyString(), eq(id))).thenReturn(false);

    UpdatePatientCommand command = new UpdatePatientCommand(
        id, "", "Gomez", LocalDate.of(1985, 3, 10),
        "luis@example.com", "76543210", "DOC-002", null
    );

    Result result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.NameIsRequired", result.getError().getCode());
    verify(patientRepository, never()).update(any());
  }
}
