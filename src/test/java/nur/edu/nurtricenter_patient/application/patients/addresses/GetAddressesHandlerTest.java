package nur.edu.nurtricenter_patient.application.patients.addresses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.patients.IPatientReadRepository;
import nur.edu.nurtricenter_patient.application.patients.addresses.getAddresses.GetAddressesHandler;
import nur.edu.nurtricenter_patient.application.patients.addresses.getAddresses.GetAddressesQuery;
import nur.edu.nurtricenter_patient.application.patients.dto.AddressDto;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

class GetAddressesHandlerTest {

  private IPatientReadRepository readRepository;
  private GetAddressesHandler handler;

  @BeforeEach
  void setUp() {
    readRepository = mock(IPatientReadRepository.class);
    handler = new GetAddressesHandler(readRepository);
  }

  @Test
  void shouldReturnAddressesForPatient() {
    UUID patientId = UUID.randomUUID();
    AddressDto dto = new AddressDto(UUID.randomUUID(), "Casa", "Calle 1", null,
        "Bolivia", "La Paz", "La Paz", -16.5, -68.15, true);
    when(readRepository.findAddressesByPatientId(patientId)).thenReturn(List.of(dto));

    ResultWithValue<List<AddressDto>> result = handler.handle(new GetAddressesQuery(patientId));

    assertTrue(result.isSuccess());
    assertEquals(1, result.getValue().size());
  }

  @Test
  void shouldReturnEmptyListWhenNoAddresses() {
    UUID patientId = UUID.randomUUID();
    when(readRepository.findAddressesByPatientId(patientId)).thenReturn(List.of());

    ResultWithValue<List<AddressDto>> result = handler.handle(new GetAddressesQuery(patientId));

    assertTrue(result.isSuccess());
    assertTrue(result.getValue().isEmpty());
  }
}
