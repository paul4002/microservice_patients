package nur.edu.nurtricenter_patient.application.patients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.application.patients.getPatient.GetPatientHandler;
import nur.edu.nurtricenter_patient.application.patients.getPatient.GetPatientQuery;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
class GetPatientHandlerTest {

  private IPatientReadRepository readRepository;
  private GetPatientHandler handler;

  @BeforeEach
  void setUp() {
    readRepository = mock(IPatientReadRepository.class);
    handler = new GetPatientHandler(readRepository);
  }

  @Test
  void shouldReturnPatientWhenFound() {
    UUID id = UUID.randomUUID();
    PatientDto dto = new PatientDto(id, "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", null, null, null, List.of());
    when(readRepository.findById(id)).thenReturn(Optional.of(dto));

    ResultWithValue<PatientDto> result = handler.handle(new GetPatientQuery(id));

    assertTrue(result.isSuccess());
    assertEquals(dto, result.getValue());
  }

  @Test
  void shouldReturnFailureWhenNotFound() {
    UUID id = UUID.randomUUID();
    when(readRepository.findById(id)).thenReturn(Optional.empty());

    ResultWithValue<PatientDto> result = handler.handle(new GetPatientQuery(id));

    assertFalse(result.isSuccess());
    assertEquals("Patient.NotFound", result.getError().getCode());
  }
}
