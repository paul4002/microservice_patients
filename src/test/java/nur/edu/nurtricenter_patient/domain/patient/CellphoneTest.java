package nur.edu.nurtricenter_patient.domain.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.core.results.DomainException;

class CellphoneTest {

  @Test
  void shouldCreateValidCellphone() {
    Cellphone phone = new Cellphone("75123456");
    assertEquals("75123456", phone.value());
  }

  @Test
  void shouldThrowWhenCellphoneIsNull() {
    DomainException ex = assertThrows(DomainException.class, () -> new Cellphone(null));
    assertEquals("Patient.CellphoneIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCellphoneIsBlank() {
    DomainException ex = assertThrows(DomainException.class, () -> new Cellphone("   "));
    assertEquals("Patient.CellphoneIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCellphoneHasLessThan8Digits() {
    DomainException ex = assertThrows(DomainException.class, () -> new Cellphone("1234567"));
    assertEquals("Patient.CellphoneIsInvalid", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCellphoneHasMoreThan8Digits() {
    DomainException ex = assertThrows(DomainException.class, () -> new Cellphone("123456789"));
    assertEquals("Patient.CellphoneIsInvalid", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCellphoneHasLetters() {
    DomainException ex = assertThrows(DomainException.class, () -> new Cellphone("7512345a"));
    assertEquals("Patient.CellphoneIsInvalid", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCellphoneHasDashes() {
    DomainException ex = assertThrows(DomainException.class, () -> new Cellphone("75-12345"));
    assertEquals("Patient.CellphoneIsInvalid", ex.getError().getCode());
  }
}
