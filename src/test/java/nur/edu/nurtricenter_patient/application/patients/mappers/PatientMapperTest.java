package nur.edu.nurtricenter_patient.application.patients.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.patients.dto.PatientDto;
import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

class PatientMapperTest {

  @Test
  void shouldReturnNullForNullPatient() {
    assertNull(PatientMapper.toDto(null));
  }

  @Test
  void shouldMapPatientWithoutAddresses() {
    Patient patient = new Patient(UUID.randomUUID(), "Ana", "Perez", LocalDate.of(1990, 5, 20),
        new Email("ana@example.com"), new Cellphone("75123456"), "DOC-001", null, null, null);

    PatientDto dto = PatientMapper.toDto(patient);

    assertNotNull(dto);
    assertEquals("Ana", dto.name());
    assertEquals("ana@example.com", dto.email());
    assertTrue(dto.addresses().isEmpty());
  }

  @Test
  void shouldMapPatientWithAddresses() {
    Patient patient = new Patient(UUID.randomUUID(), "Ana", "Perez", LocalDate.of(1990, 5, 20),
        new Email("ana@example.com"), new Cellphone("75123456"), "DOC-001", null, null, null);
    patient.addAddress("Casa", "Calle 1", null, "Bolivia", "La Paz", "La Paz",
        new Coordinate(-16.5, -68.15));

    PatientDto dto = PatientMapper.toDto(patient);

    assertNotNull(dto);
    assertEquals(1, dto.addresses().size());
    assertEquals("Casa", dto.addresses().get(0).label());
  }
}
