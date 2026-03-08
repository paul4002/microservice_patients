package nur.edu.nurtricenter_patient.application.patients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.patients.createPatient.CreatePatientCommand;
import nur.edu.nurtricenter_patient.application.patients.createPatient.CreatePatientHandler;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;

class CreatePatientHandlerTest {

  private IPatientRepository patientRepository;
  private IUnitOfWork unitOfWork;
  private CreatePatientHandler handler;

  @BeforeEach
  void setUp() {
    patientRepository = mock(IPatientRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    handler = new CreatePatientHandler(patientRepository, unitOfWork);
  }

  @Test
  void shouldCreatePatientSuccessfully() {
    when(patientRepository.existsByEmail(anyString())).thenReturn(false);
    when(patientRepository.existsByCellphone(anyString())).thenReturn(false);
    when(patientRepository.existsByDocument(anyString())).thenReturn(false);

    CreatePatientCommand command = new CreatePatientCommand(
        "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", null
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertTrue(result.isSuccess());
    assertNotNull(result.getValue());
    verify(patientRepository).add(any());
    verify(unitOfWork).commit(any());
  }

  @Test
  void shouldFailWhenEmailAlreadyExists() {
    when(patientRepository.existsByEmail(anyString())).thenReturn(true);

    CreatePatientCommand command = new CreatePatientCommand(
        "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", null
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.EmailAlreadyExists", result.getError().getCode());
    verify(patientRepository, never()).add(any());
  }

  @Test
  void shouldFailWhenCellphoneAlreadyExists() {
    when(patientRepository.existsByEmail(anyString())).thenReturn(false);
    when(patientRepository.existsByCellphone(anyString())).thenReturn(true);

    CreatePatientCommand command = new CreatePatientCommand(
        "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", null
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.CellphoneAlreadyExists", result.getError().getCode());
    verify(patientRepository, never()).add(any());
  }

  @Test
  void shouldFailWhenDocumentAlreadyExists() {
    when(patientRepository.existsByEmail(anyString())).thenReturn(false);
    when(patientRepository.existsByCellphone(anyString())).thenReturn(false);
    when(patientRepository.existsByDocument(anyString())).thenReturn(true);

    CreatePatientCommand command = new CreatePatientCommand(
        "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", null
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.DocumentAlreadyExists", result.getError().getCode());
    verify(patientRepository, never()).add(any());
  }

  @Test
  void shouldFailWhenNameIsBlank() {
    CreatePatientCommand command = new CreatePatientCommand(
        "  ", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", null
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.NameIsRequired", result.getError().getCode());
    verify(patientRepository, never()).add(any());
  }

  @Test
  void shouldFailWhenEmailIsInvalid() {
    CreatePatientCommand command = new CreatePatientCommand(
        "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "not-an-email", "75123456", "DOC-001", null
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.EmailIsInvalid", result.getError().getCode());
  }

  @Test
  void shouldFailWhenCellphoneIsInvalid() {
    CreatePatientCommand command = new CreatePatientCommand(
        "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "123", "DOC-001", null
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertFalse(result.isSuccess());
    assertEquals("Patient.CellphoneIsInvalid", result.getError().getCode());
  }

  @Test
  void shouldCreatePatientWithSubscription() {
    when(patientRepository.existsByEmail(anyString())).thenReturn(false);
    when(patientRepository.existsByCellphone(anyString())).thenReturn(false);
    when(patientRepository.existsByDocument(anyString())).thenReturn(false);

    UUID subId = UUID.randomUUID();
    CreatePatientCommand command = new CreatePatientCommand(
        "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", subId
    );

    ResultWithValue<UUID> result = handler.handle(command);

    assertTrue(result.isSuccess());
    assertNotNull(result.getValue());
  }
}
