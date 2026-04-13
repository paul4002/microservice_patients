package nur.edu.nurtricenter_patient.infraestructure.domainModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.SubscriptionStatus;
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
  @jakarta.persistence.Column(unique = true)
  private Email email;
  @Convert(converter = CellphoneConverter.class)
  @jakarta.persistence.Column(unique = true)
  private Cellphone cellphone;
  @jakarta.persistence.Column(unique = true)
  private String document;
  @jakarta.persistence.Column(name = "subscription_id")
  private UUID subscriptionId;
  @jakarta.persistence.Column(name = "subscription_status")
  @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
  private SubscriptionStatus subscriptionStatus;
  @jakarta.persistence.Column(name = "subscription_ends_on")
  private LocalDate subscriptionEndsOn;

  @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = 50)
  private List<AddressEntity> addresses = new ArrayList<>();

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
  public String getDocument() {
    return document;
  }
  public void setDocument(String document) {
    this.document = document;
  }
  public UUID getSubscriptionId() {
    return subscriptionId;
  }
  public void setSubscriptionId(UUID subscriptionId) {
    this.subscriptionId = subscriptionId;
  }
  public SubscriptionStatus getSubscriptionStatus() {
    return subscriptionStatus;
  }
  public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }
  public LocalDate getSubscriptionEndsOn() {
    return subscriptionEndsOn;
  }
  public void setSubscriptionEndsOn(LocalDate subscriptionEndsOn) {
    this.subscriptionEndsOn = subscriptionEndsOn;
  }
  public List<AddressEntity> getAddresses() {
    return addresses;
  }
  public void setAddresses(List<AddressEntity> addresses) {
    this.addresses = addresses;
  }

}
