package nur.edu.nurtricenter_patient.domain.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.core.results.DomainException;

class EmailTest {

  @Test
  void shouldCreateValidEmail() {
    Email email = new Email("user@example.com");
    assertEquals("user@example.com", email.value());
  }

  @Test
  void shouldAcceptEmailWithPlusAndDots() {
    Email email = new Email("user.name+tag@sub.domain.com");
    assertEquals("user.name+tag@sub.domain.com", email.value());
  }

  @Test
  void shouldThrowWhenEmailIsNull() {
    DomainException ex = assertThrows(DomainException.class, () -> new Email(null));
    assertEquals("Patient.EmailIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenEmailIsBlank() {
    DomainException ex = assertThrows(DomainException.class, () -> new Email("   "));
    assertEquals("Patient.EmailIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenEmailHasNoAtSign() {
    DomainException ex = assertThrows(DomainException.class, () -> new Email("invalidemail.com"));
    assertEquals("Patient.EmailIsInvalid", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenEmailHasSpaces() {
    DomainException ex = assertThrows(DomainException.class, () -> new Email("user @example.com"));
    assertEquals("Patient.EmailIsInvalid", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenEmailHasNoDomain() {
    DomainException ex = assertThrows(DomainException.class, () -> new Email("user@"));
    assertEquals("Patient.EmailIsInvalid", ex.getError().getCode());
  }
}
