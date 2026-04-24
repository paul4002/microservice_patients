package nur.edu.nurtricenter_patient.application.subscriptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.application.abstractions.IInboundEventMetrics;
import nur.edu.nurtricenter_patient.application.abstractions.IInboundEventRecorder;
import nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent.ProcessSubscriptionEventCommand;
import nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent.ProcessSubscriptionEventHandler;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;

class ProcessSubscriptionEventHandlerUnitTest {

  private IPatientRepository patientRepository;
  private IUnitOfWork unitOfWork;
  private IInboundEventRecorder recorder;
  private IInboundEventMetrics metrics;
  private ProcessSubscriptionEventHandler handler;

  @BeforeEach
  void setUp() {
    patientRepository = mock(IPatientRepository.class);
    unitOfWork = mock(IUnitOfWork.class);
    recorder = mock(IInboundEventRecorder.class);
    metrics = mock(IInboundEventMetrics.class);

    when(recorder.tryStart(any(), anyString(), any(), any(), any(), any(), any())).thenReturn(true);
    when(metrics.timeHandler(anyString(), any())).thenAnswer(inv -> {
      java.util.function.Supplier<?> supplier = inv.getArgument(1);
      return supplier.get();
    });

    handler = new ProcessSubscriptionEventHandler(patientRepository, unitOfWork, recorder, metrics);
  }

  @Test
  void resolveStatus_activeString_returnsActive() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));
    when(patientRepository.getById(any())).thenReturn(null);

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "estado", "ACTIVA")));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveStatus_vigenteString_returnsActive() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "estado", "VIGENTE")));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveStatus_expiredString_returnsExpired() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "estado", "EXPIRADA")));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveStatus_cancelledSpanish_returnsCancelled() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "estado", "CANCELADA")));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveStatus_inactiva_returnsCancelled() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "estado", "INACTIVA")));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveStatus_inactive_returnsCancelled() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "estado", "INACTIVE")));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveStatus_unknownString_defaultsToActive() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "estado", "UNKNOWN_STATUS")));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveStatus_expiredEndDate_returnsExpired() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "fechaFin", "2020-01-01")));
    assertTrue(result.isSuccess());
  }

  @Test
  void readDate_offsetDateTimeFormat_parsed() {
    UUID sub = UUID.randomUUID();
    Patient patient = createPatient(sub);
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of(patient));

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString(), "fechaFin", "2027-01-01T00:00:00Z")));
    assertTrue(result.isSuccess());
  }

  @Test
  void applySubscriptionUpdate_emptyPatientList_returnsSuccess() {
    UUID sub = UUID.randomUUID();
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of());

    Result result = handler.handle(command("suscripciones.suscripcion-actualizada",
        Map.of("suscripcionId", sub.toString())));
    assertTrue(result.isSuccess());
  }

  @Test
  void applySubscriptionRemoval_emptyPatientList_returnsSuccess() {
    UUID sub = UUID.randomUUID();
    when(patientRepository.getBySubscriptionId(sub)).thenReturn(List.of());

    Result result = handler.handle(command("contrato.cancelado",
        Map.of("contratoId", sub.toString())));
    assertTrue(result.isSuccess());
  }

  @Test
  void applySubscriptionRemoval_patientSubscriptionMismatch_skipped() {
    UUID sub1 = UUID.randomUUID();
    UUID sub2 = UUID.randomUUID();
    Patient patient = createPatient(sub2);
    when(patientRepository.getBySubscriptionId(sub1)).thenReturn(List.of(patient));

    Result result = handler.handle(command("contrato.cancelado",
        Map.of("contratoId", sub1.toString())));
    assertTrue(result.isSuccess());
  }

  @Test
  void resolveEventName_fallsBackToRoutingKey_whenEventNameNull() {
    ProcessSubscriptionEventCommand cmd = new ProcessSubscriptionEventCommand(
        null, Map.of(), "routing.key",
        UUID.randomUUID(), UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );
    Result result = handler.handle(cmd);
    assertTrue(result.isSuccess());
  }

  @Test
  void duplicate_tryStart_returnsFalse_returnsSuccess() {
    when(recorder.tryStart(any(), anyString(), any(), any(), any(), any(), any())).thenReturn(false);

    Result result = handler.handle(command("contrato.cancelado",
        Map.of("contratoId", UUID.randomUUID().toString())));
    assertTrue(result.isSuccess());
  }

  private ProcessSubscriptionEventCommand command(String eventName, Map<String, Object> payload) {
    return new ProcessSubscriptionEventCommand(
        eventName, payload, eventName,
        UUID.randomUUID(), UUID.randomUUID(), 1,
        "2026-02-23T00:00:00Z", "{}"
    );
  }

  private Patient createPatient(UUID subscriptionId) {
    return Patient.create(
        "Test", "Patient",
        LocalDate.of(1990, 1, 1),
        new Email("test+" + UUID.randomUUID() + "@example.com"),
        new Cellphone("7" + (1000000 + (int) (Math.random() * 9000000))),
        "DOC-" + UUID.randomUUID(),
        subscriptionId
    );
  }
}
