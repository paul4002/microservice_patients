package nur.edu.nurtricenter_patient.application.patients.addresses;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import nur.edu.nurtricenter_patient.application.patients.addresses.addAddress.AddAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.addAddress.AddAddressHandler;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

class AddAddressHandlerTest {

  private IPatientRepository patientRepository;
  private IUnitOfWork unitOfWork;
  private AddAddressHandler handler;

  @BeforeEach
  void setUp() {
    patientRepository = mock(IPatientRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    handler = new AddAddressHandler(patientRepository, unitOfWork);
  }

  private Patient buildPatient(UUID id) {
    return new Patient(id, "Ana", "Perez", LocalDate.of(1990, 5, 20),
        new Email("ana@example.com"), new Cellphone("75123456"), "DOC-001", null, null, null);
  }

  @Test
  void shouldAddAddressSuccessfully() {
    UUID patientId = UUID.randomUUID();
    when(patientRepository.getById(patientId)).thenReturn(buildPatient(patientId));

    AddAddressCommand cmd = new AddAddressCommand(patientId, "Casa", "Calle 1", null,
        "Bolivia", "La Paz", "La Paz", -16.5, -68.15);

    ResultWithValue<UUID> result = handler.handle(cmd);

    assertTrue(result.isSuccess());
    assertNotNull(result.getValue());
    verify(patientRepository).update(any());
    verify(unitOfWork).commit(any());
  }

  @Test
  void shouldReturnFailureWhenPatientNotFound() {
    UUID patientId = UUID.randomUUID();
    when(patientRepository.getById(patientId)).thenReturn(null);

    AddAddressCommand cmd = new AddAddressCommand(patientId, "Casa", "Calle 1", null,
        "Bolivia", "La Paz", "La Paz", -16.5, -68.15);

    ResultWithValue<UUID> result = handler.handle(cmd);

    assertFalse(result.isSuccess());
    verify(patientRepository, never()).update(any());
  }
}
