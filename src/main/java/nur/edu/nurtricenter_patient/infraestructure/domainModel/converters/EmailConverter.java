package nur.edu.nurtricenter_patient.infraestructure.domainModel.converters;

import jakarta.persistence.AttributeConverter;
import nur.edu.nurtricenter_patient.domain.patient.Email;

public class EmailConverter implements AttributeConverter<Email, String> {
  @Override
  public String convertToDatabaseColumn(Email attribute) {
    return attribute != null ? attribute.value() : null;
  }

  @Override
  public Email convertToEntityAttribute(String dbData) {
    return dbData != null ? new Email(dbData) : null;
  }
}
