package nur.edu.nurtricenter_patient.application.patients.addresses;

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

import nur.edu.nurtricenter_patient.application.patients.addresses.updateAddress.UpdateAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.updateAddress.UpdateAddressHandler;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

class UpdateAddressHandlerTest {

  private IPatientRepository patientRepository;
  private IUnitOfWork unitOfWork;
  private UpdateAddressHandler handler;

  @BeforeEach
  void setUp() {
    patientRepository = mock(IPatientRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    handler = new UpdateAddressHandler(patientRepository, unitOfWork);
  }

  private Patient buildPatient(UUID id) {
    return new Patient(id, "Ana", "Perez", LocalDate.of(1990, 5, 20),
        new Email("ana@example.com"), new Cellphone("75123456"), "DOC-001", null, null, null);
  }

  @Test
  void shouldReturnFailureWhenPatientNotFound() {
    UUID patientId = UUID.randomUUID();
    when(patientRepository.getById(patientId)).thenReturn(null);

    Result result = handler.handle(new UpdateAddressCommand(patientId, UUID.randomUUID(),
        "Casa", "Calle 1", null, "Bolivia", "La Paz", "La Paz", -16.5, -68.15));

    assertFalse(result.isSuccess());
    verify(patientRepository, never()).update(any());
  }

  @Test
  void shouldReturnFailureWhenAddressNotFound() {
    UUID patientId = UUID.randomUUID();
    Patient patient = buildPatient(patientId);
    when(patientRepository.getById(patientId)).thenReturn(patient);

    Result result = handler.handle(new UpdateAddressCommand(patientId, UUID.randomUUID(),
        "Casa", "Calle 1", null, "Bolivia", "La Paz", "La Paz", -16.5, -68.15));

    assertFalse(result.isSuccess());
  }

  @Test
  void shouldUpdateAddressSuccessfully() {
    UUID patientId = UUID.randomUUID();
    Patient patient = buildPatient(patientId);
    Address address = new Address("Casa", "Calle 1", null, "Bolivia", "La Paz", "La Paz",
        new Coordinate(-16.5, -68.15));
    patient.addAddress(address.getLabel(), address.getLine1(), address.getLine2(),
        address.getCountry(), address.getProvince(), address.getCity(), address.getCoordinate());
    UUID addressId = patient.getAddresses().get(0).getId();
    when(patientRepository.getById(patientId)).thenReturn(patient);

    Result result = handler.handle(new UpdateAddressCommand(patientId, addressId,
        "Oficina", "Av. Principal 100", null, "Bolivia", "La Paz", "La Paz", -16.4, -68.1));

    assertTrue(result.isSuccess());
    verify(patientRepository).update(patient);
    verify(unitOfWork).commit(patient);
  }
}
