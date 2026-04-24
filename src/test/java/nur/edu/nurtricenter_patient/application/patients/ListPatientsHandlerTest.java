package nur.edu.nurtricenter_patient.application.patients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.application.patients.listPatients.ListPatientsHandler;
import nur.edu.nurtricenter_patient.application.patients.listPatients.ListPatientsQuery;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
class ListPatientsHandlerTest {

  private IPatientReadRepository readRepository;
  private ListPatientsHandler handler;

  @BeforeEach
  void setUp() {
    readRepository = mock(IPatientReadRepository.class);
    handler = new ListPatientsHandler(readRepository);
  }

  @Test
  void shouldReturnAllPatients() {
    PatientDto dto = new PatientDto(UUID.randomUUID(), "Ana", "Perez", LocalDate.of(1990, 5, 20),
        "ana@example.com", "75123456", "DOC-001", null, null, null, List.of());
    when(readRepository.findAll()).thenReturn(List.of(dto));

    ResultWithValue<List<PatientDto>> result = handler.handle(new ListPatientsQuery());

    assertTrue(result.isSuccess());
    assertEquals(1, result.getValue().size());
  }

  @Test
  void shouldReturnEmptyList() {
    when(readRepository.findAll()).thenReturn(List.of());

    ResultWithValue<List<PatientDto>> result = handler.handle(new ListPatientsQuery());

    assertTrue(result.isSuccess());
    assertTrue(result.getValue().isEmpty());
  }
}
