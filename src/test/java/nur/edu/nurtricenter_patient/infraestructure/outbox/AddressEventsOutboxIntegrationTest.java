package nur.edu.nurtricenter_patient.infraestructure.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nur.edu.nurtricenter_patient.application.patients.addresses.addAddress.AddAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.addAddress.AddAddressHandler;
import nur.edu.nurtricenter_patient.application.patients.addresses.geocodeAddress.GeocodeAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.geocodeAddress.GeocodeAddressHandler;
import nur.edu.nurtricenter_patient.application.patients.addresses.updateAddress.UpdateAddressCommand;
import nur.edu.nurtricenter_patient.application.patients.addresses.updateAddress.UpdateAddressHandler;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.infraestructure.repositories.PatientEntityRepository;

@SpringBootTest
class AddressEventsOutboxIntegrationTest {

  @Autowired
  private AddAddressHandler addAddressHandler;

  @Autowired
  private UpdateAddressHandler updateAddressHandler;

  @Autowired
  private GeocodeAddressHandler geocodeAddressHandler;

  @Autowired
  private IPatientRepository patientRepository;

  @Autowired
  private PatientEntityRepository patientEntityRepository;

  @Autowired
  private OutboxEventRepository outboxEventRepository;

  @BeforeEach
  void clean() {
    outboxEventRepository.deleteAll();
    patientEntityRepository.deleteAll();
  }

  @Test
  void addAddress_shouldWriteDireccionCreadaToOutbox() {
    Patient patient = createPatient();
    patientRepository.add(patient);

    var result = addAddressHandler.handle(new AddAddressCommand(
      patient.getId(),
      "Casa",
      "Calle 1",
      "Depto 2",
      "Bolivia",
      "Santa Cruz",
      "Santa Cruz",
      -17.7833,
      -63.1821
    ));

    assertTrue(result.isSuccess());
    assertEquals(1L, countOutboxByEventName("paciente.direccion-creada"));
  }

  @Test
  void updateAndGeocodeAddress_shouldWriteExpectedEventsToOutbox() {
    Patient patient = createPatient();
    patientRepository.add(patient);

    var addResult = addAddressHandler.handle(new AddAddressCommand(
      patient.getId(),
      "Casa",
      "Calle 1",
      "Depto 2",
      "Bolivia",
      "Santa Cruz",
      "Santa Cruz",
      -17.7833,
      -63.1821
    ));
    assertTrue(addResult.isSuccess());
    UUID addressId = addResult.getValue();

    var updateResult = updateAddressHandler.handle(new UpdateAddressCommand(
      patient.getId(),
      addressId,
      "Trabajo",
      "Avenida 2",
      "Piso 3",
      "Bolivia",
      "Santa Cruz",
      "Santa Cruz",
      -17.79,
      -63.18
    ));
    assertTrue(updateResult.isSuccess());

    var geocodeResult = geocodeAddressHandler.handle(new GeocodeAddressCommand(
      patient.getId(),
      addressId,
      -17.8,
      -63.17
    ));
    assertTrue(geocodeResult.isSuccess());

    assertEquals(1L, countOutboxByEventName("paciente.direccion-creada"));
    assertEquals(1L, countOutboxByEventName("paciente.direccion-actualizada"));
    assertEquals(1L, countOutboxByEventName("paciente.direccion-geocodificada"));
  }

  private long countOutboxByEventName(String eventName) {
    long count = 0;
    for (OutboxEventEntity row : outboxEventRepository.findAll()) {
      if (eventName.equals(row.getEventName())) {
        count++;
      }
    }
    return count;
  }

  private Patient createPatient() {
    return new Patient(
      UUID.randomUUID(),
      "Ana",
      "Perez",
      LocalDate.of(1995, 1, 10),
      new Email("ana.perez+" + UUID.randomUUID() + "@example.com"),
      new Cellphone(randomCellphone()),
      "DOC-" + UUID.randomUUID(),
      null
    );
  }

  private String randomCellphone() {
    int value = 10000000 + (int) (Math.random() * 89999999);
    return Integer.toString(value);
  }
}
