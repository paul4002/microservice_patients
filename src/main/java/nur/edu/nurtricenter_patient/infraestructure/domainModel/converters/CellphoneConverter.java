package nur.edu.nurtricenter_patient.infraestructure.domainModel.converters;

import jakarta.persistence.AttributeConverter;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;

public class CellphoneConverter implements AttributeConverter<Cellphone, String> {

  @Override
  public String convertToDatabaseColumn(Cellphone attribute) {
    return attribute != null ? attribute.value() : null;
  }

  @Override
  public Cellphone convertToEntityAttribute(String dbData) {
    return dbData != null ? new Cellphone(dbData) : null;
  }
}
