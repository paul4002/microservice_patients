package nur.edu.nurtricenter_patient.domain.patient;

import nur.edu.nurtricenter_patient.core.results.Error;
import nur.edu.nurtricenter_patient.core.results.ErrorType;

public final class PatientErrors {
  private PatientErrors() {}

  public static Error NameIsRequired() {
    return new Error("Patient.NameIsRequired", "The patient name is required", ErrorType.VALIDATION);
  }

  public static Error LastnameIsRequired() {
    return new Error("Patient.LastnameIsRequired", "The patient lastname is required", ErrorType.VALIDATION);
  }

  public static Error BirthDateIsRequired() {
    return new Error("Patient.BirthDateIsRequired", "The patient birth date is required", ErrorType.VALIDATION);
  }

  public static Error BirthDateInFuture() {
    return new Error("Patient.BirthDateInFuture", "The patient birth date cannot be in the future", ErrorType.VALIDATION);
  }

  public static Error EmailIsInvalid(String value) {
    return new Error("Patient.EmailIsInvalid", "Invalid email format: {value}", ErrorType.VALIDATION, value);
  }

  public static Error EmailIsRequired() {
    return new Error("Patient.EmailIsRequired", "The patient email is required", ErrorType.VALIDATION);
  }

  public static Error CellphoneIsInvalid(String value) {
    return new Error("Patient.CellphoneIsInvalid", "Invalid cellphone format: {value}. It must have exactly 8 digits.", ErrorType.VALIDATION, value);
  }

  public static Error CellphoneIsRequired() {
    return new Error("Patient.CellphoneIsRequired", "The patient cellphone is required", ErrorType.VALIDATION);
  }

  public static Error DocumentIsRequired() {
    return new Error("Patient.DocumentIsRequired", "The patient document is required", ErrorType.VALIDATION);
  }

  public static Error SubscriptionIdIsRequired() {
    return new Error("Patient.SubscriptionIdIsRequired", "The patient subscription id is required", ErrorType.VALIDATION);
  }

  public static Error EmailAlreadyExists(String value) {
    return Error.conflict("Patient.EmailAlreadyExists", "Email already exists: {value}", value);
  }

  public static Error CellphoneAlreadyExists(String value) {
    return Error.conflict("Patient.CellphoneAlreadyExists", "Cellphone already exists: {value}", value);
  }

  public static Error DocumentAlreadyExists(String value) {
    return Error.conflict("Patient.DocumentAlreadyExists", "Document already exists: {value}", value);
  }

  public static Error PatientNotFound(String id) {
    return new Error("Patient.NotFound", "Patient not found: {id}", ErrorType.NOT_FOUND, id);
  }

  public static Error AddressNotFound(String id) {
    return new Error("Patient.AddressNotFound", "Address not found: {id}", ErrorType.NOT_FOUND, id);
  }
}
