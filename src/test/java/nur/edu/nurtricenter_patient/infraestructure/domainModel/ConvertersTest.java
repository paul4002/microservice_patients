package nur.edu.nurtricenter_patient.infraestructure.domainModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.converters.CellphoneConverter;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.converters.EmailConverter;

class ConvertersTest {

  private final CellphoneConverter cellphoneConverter = new CellphoneConverter();
  private final EmailConverter emailConverter = new EmailConverter();

  @Test
  void cellphone_toDatabase() {
    assertEquals("75123456", cellphoneConverter.convertToDatabaseColumn(new Cellphone("75123456")));
  }

  @Test
  void cellphone_toDatabase_null() {
    assertNull(cellphoneConverter.convertToDatabaseColumn(null));
  }

  @Test
  void cellphone_fromDatabase() {
    assertEquals("75123456", cellphoneConverter.convertToEntityAttribute("75123456").value());
  }

  @Test
  void cellphone_fromDatabase_null() {
    assertNull(cellphoneConverter.convertToEntityAttribute(null));
  }

  @Test
  void email_toDatabase() {
    assertEquals("ana@example.com", emailConverter.convertToDatabaseColumn(new Email("ana@example.com")));
  }

  @Test
  void email_toDatabase_null() {
    assertNull(emailConverter.convertToDatabaseColumn(null));
  }

  @Test
  void email_fromDatabase() {
    assertEquals("ana@example.com", emailConverter.convertToEntityAttribute("ana@example.com").value());
  }

  @Test
  void email_fromDatabase_null() {
    assertNull(emailConverter.convertToEntityAttribute(null));
  }
}
