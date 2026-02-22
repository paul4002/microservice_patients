package nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.AggregateRoot;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.Error;
import nur.edu.nurtricenter_patient.core.results.ErrorType;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.SubscriptionStatus;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionRemovedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionUpdatedEvent;

@Component
public class ProcessSubscriptionEventHandler implements Command.Handler<ProcessSubscriptionEventCommand, Result> {
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;
  private final Clock clock;

  public ProcessSubscriptionEventHandler(IPatientRepository patientRepository, IUnitOfWork unitOfWork) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
    this.clock = Clock.systemUTC();
  }

  @Override
  @Transactional
  public Result handle(ProcessSubscriptionEventCommand request) {
    String eventName = resolveEventName(request);
    if (eventName == null || eventName.isBlank()) {
      return Result.failure(new Error("SubscriptionEvent.EventNameMissing", "Event name is required", ErrorType.VALIDATION));
    }

    return switch (eventName) {
      case "suscripciones.suscripcion-actualizada", "contrato.creado" -> applySubscriptionUpdate(eventName, request.payload());
      case "suscripciones.suscripcion-eliminada", "contrato.cancelado", "contrato.cancelar" -> applySubscriptionRemoval(eventName, request.payload());
      default -> Result.success();
    };
  }

  private Result applySubscriptionUpdate(String eventName, Map<String, Object> payload) {
    UUID subscriptionId = readUuid(payload, "suscripcionId", "subscriptionId", "contratoId", "id");
    if (subscriptionId == null) {
      return Result.failure(new Error("SubscriptionEvent.SubscriptionIdMissing", "Missing subscription id in {event}", ErrorType.VALIDATION, eventName));
    }

    List<Patient> patients = patientRepository.getBySubscriptionId(subscriptionId);
    if (patients.isEmpty()) {
      return Result.success();
    }

    LocalDate endsOn = readDate(payload, "fechaFin", "endDate", "expiresOn", "vencimiento");
    SubscriptionStatus status = resolveStatus(payload, endsOn);

    List<AggregateRoot> changed = new ArrayList<>();
    for (Patient patient : patients) {
      boolean didChange = patient.syncSubscription(subscriptionId, status, endsOn);
      if (!didChange) {
        continue;
      }
      patientRepository.update(patient);
      patient.addDomainEvent(new PatientSubscriptionUpdatedEvent(
        patient.getId(),
        subscriptionId,
        status,
        endsOn,
        eventName
      ));

      changed.add(patient);
    }

    if (!changed.isEmpty()) {
      unitOfWork.commitAsync(changed.toArray(new AggregateRoot[0]));
    }
    return Result.success();
  }

  private Result applySubscriptionRemoval(String eventName, Map<String, Object> payload) {
    UUID subscriptionId = readUuid(payload, "suscripcionId", "subscriptionId", "contratoId", "id");
    if (subscriptionId == null) {
      return Result.failure(new Error("SubscriptionEvent.SubscriptionIdMissing", "Missing subscription id in {event}", ErrorType.VALIDATION, eventName));
    }

    List<Patient> patients = patientRepository.getBySubscriptionId(subscriptionId);
    if (patients.isEmpty()) {
      return Result.success();
    }

    String reason = readString(payload, "motivoCancelacion", "reason", "motivo");
    List<AggregateRoot> changed = new ArrayList<>();
    for (Patient patient : patients) {
      UUID previous = patient.getSubscriptionId();
      if (!Objects.equals(previous, subscriptionId)) {
        continue;
      }
      boolean didChange = patient.removeSubscription(SubscriptionStatus.CANCELLED, null);
      if (!didChange) {
        continue;
      }
      patientRepository.update(patient);
      patient.addDomainEvent(new PatientSubscriptionRemovedEvent(
        patient.getId(),
        previous,
        reason != null ? reason : "subscription-removed",
        eventName
      ));
      changed.add(patient);
    }

    if (!changed.isEmpty()) {
      unitOfWork.commitAsync(changed.toArray(new AggregateRoot[0]));
    }
    return Result.success();
  }

  private SubscriptionStatus resolveStatus(Map<String, Object> payload, LocalDate endsOn) {
    String raw = readString(payload, "estado", "status", "subscriptionStatus");
    LocalDate today = LocalDate.now(clock);
    if (endsOn != null && endsOn.isBefore(today)) {
      return SubscriptionStatus.EXPIRED;
    }
    if (raw == null || raw.isBlank()) {
      return SubscriptionStatus.ACTIVE;
    }

    String normalized = raw.trim().toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "ACTIVE", "ACTIVA", "VIGENTE" -> SubscriptionStatus.ACTIVE;
      case "EXPIRED", "EXPIRADA", "VENCIDA" -> SubscriptionStatus.EXPIRED;
      case "CANCELLED", "CANCELADA", "INACTIVA", "INACTIVE" -> SubscriptionStatus.CANCELLED;
      default -> SubscriptionStatus.ACTIVE;
    };
  }

  private UUID readUuid(Map<String, Object> payload, String... keys) {
    for (String key : keys) {
      String value = readString(payload, key);
      if (value == null || value.isBlank()) {
        continue;
      }
      try {
        return UUID.fromString(value);
      } catch (IllegalArgumentException ex) {
        return null;
      }
    }
    return null;
  }

  private LocalDate readDate(Map<String, Object> payload, String... keys) {
    for (String key : keys) {
      String value = readString(payload, key);
      if (value == null || value.isBlank()) {
        continue;
      }
      try {
        return LocalDate.parse(value);
      } catch (DateTimeParseException ex) {
        try {
          return OffsetDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
          return null;
        }
      }
    }
    return null;
  }

  private String readString(Map<String, Object> payload, String... keys) {
    if (payload == null) {
      return null;
    }
    for (String key : keys) {
      Object value = payload.get(key);
      if (value == null) {
        continue;
      }
      if (value instanceof String s) {
        return s;
      }
      if (value instanceof Number n) {
        return String.valueOf(n);
      }
    }
    return null;
  }

  private String resolveEventName(ProcessSubscriptionEventCommand request) {
    if (request.eventName() != null && !request.eventName().isBlank()) {
      return request.eventName();
    }
    return request.routingKey();
  }
}
