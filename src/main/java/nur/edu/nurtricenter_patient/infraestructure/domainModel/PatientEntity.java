package nur.edu.nurtricenter_patient.infraestructure.domainModel;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.converters.CellphoneConverter;
import nur.edu.nurtricenter_patient.infraestructure.domainModel.converters.EmailConverter;

@Entity
@Table(name = "patients")
public class PatientEntity {
  @Id
  private UUID id;
  private String name;
  private String lastname;
  private LocalDate birthDate;
  @Convert(converter = EmailConverter.class)
  private Email email;
  @Convert(converter = CellphoneConverter.class)
  private Cellphone cellphone;

  public UUID getId() {
    return id;
  }
  public void setId(UUID id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getLastname() {
    return lastname;
  }
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }
  public LocalDate getBirthDate() {
    return birthDate;
  }
  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }
  public Email getEmail() {
    return email;
  }
  public void setEmail(Email email) {
    this.email = email;
  }
  public Cellphone getCellphone() {
    return cellphone;
  }
  public void setCellphone(Cellphone cellphone) {
    this.cellphone = cellphone;
  }

  public static PatientEntity fromDomain(Patient patient) {
    PatientEntity patientEntity = new PatientEntity();
    patientEntity.id = patient.getId();
    patientEntity.name = patient.getName();
    patientEntity.lastname = patient.getLastname();
    patientEntity.birthDate = patient.getBirthDate();
    patientEntity.email = patient.getEmail();
    patientEntity.cellphone = patient.getCellphone();
    return patientEntity;
  }
}
