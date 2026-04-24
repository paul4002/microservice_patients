package nur.edu.nurtricenter_patient.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.core.results.Error;
import nur.edu.nurtricenter_patient.core.results.ErrorType;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;
import nur.edu.nurtricenter_patient.core.results.ValidationError;

class CoreTest {

  @Test
  void result_success_isSuccess() {
    Result r = Result.success();
    assertTrue(r.isSuccess());
    assertFalse(r.isFailure());
  }

  @Test
  void result_failure_isFailure() {
    Error e = new Error("Code", "msg", ErrorType.VALIDATION);
    Result r = Result.failure(e);
    assertFalse(r.isSuccess());
    assertTrue(r.isFailure());
    assertEquals("Code", r.getError().getCode());
  }

  @Test
  void result_constructor_invalidArgs_throws() {
    assertThrows(IllegalArgumentException.class, () ->
        new Result(true, new Error("X", "y", ErrorType.FAILURE)));
    assertThrows(IllegalArgumentException.class, () ->
        new Result(false, Error.NONE));
  }

  @Test
  void resultWithValue_success_hasValue() {
    ResultWithValue<String> r = ResultWithValue.success("hello");
    assertTrue(r.isSuccess());
    assertEquals("hello", r.getValue());
  }

  @Test
  void resultWithValue_fail_noValue() {
    Error e = new Error("Err", "msg", ErrorType.NOT_FOUND);
    ResultWithValue<String> r = ResultWithValue.fail(e);
    assertFalse(r.isSuccess());
    assertNull(r.getValue());
  }

  @Test
  void resultWithValue_of_nonNull() {
    ResultWithValue<String> r = ResultWithValue.of("data");
    assertTrue(r.isSuccess());
    assertEquals("data", r.getValue());
  }

  @Test
  void resultWithValue_of_null() {
    ResultWithValue<String> r = ResultWithValue.of(null);
    assertFalse(r.isSuccess());
    assertEquals("General.Null", r.getError().getCode());
  }

  @Test
  void resultWithValue_validationFailure() {
    Error e = new Error("Val", "v", ErrorType.VALIDATION);
    ResultWithValue<String> r = ResultWithValue.validationFailure(e);
    assertFalse(r.isSuccess());
  }

  @Test
  void error_factories() {
    assertEquals(ErrorType.FAILURE, Error.failure("A", "a").getType());
    assertEquals(ErrorType.NOT_FOUND, Error.notFound("B", "b").getType());
    assertEquals(ErrorType.PROBLEM, Error.problem("C", "c").getType());
    assertEquals(ErrorType.CONFLICT, Error.conflict("D", "d").getType());
  }

  @Test
  void error_messagePlaceholder() {
    Error e = new Error("Code", "Hello {name}, you are {age}", ErrorType.VALIDATION, "Ana", "30");
    assertEquals("Hello Ana, you are 30", e.getDescription());
    assertEquals("Hello {name}, you are {age}", e.getStructuredMessage());
  }

  @Test
  void error_noArgs_returnsStructuredMessage() {
    Error e = new Error("Code", "No placeholders", ErrorType.VALIDATION);
    assertEquals("No placeholders", e.getDescription());
  }

  @Test
  void error_nullArgs_handled() {
    Error e = new Error("Code", "Value: {v}", ErrorType.VALIDATION, (Object) null);
    assertEquals("Value: ", e.getDescription());
  }

  @Test
  void error_none_constants() {
    assertNotNull(Error.NONE);
    assertNotNull(Error.NULL_VALUE);
  }

  @Test
  void errorType_allValues() {
    assertEquals(5, ErrorType.values().length);
  }

  @Test
  void domainException_getError() {
    Error e = new Error("Code", "msg", ErrorType.FAILURE);
    DomainException ex = new DomainException(e);
    assertEquals(e, ex.getError());
    assertNotNull(ex.getMessage());
  }

  @Test
  void validationError_constructor() {
    Error e1 = new Error("E1", "msg1", ErrorType.VALIDATION);
    Error e2 = new Error("E2", "msg2", ErrorType.VALIDATION);
    ValidationError ve = new ValidationError(e1, e2);
    assertEquals("Validation.General", ve.getCode());
    assertEquals(2, ve.getErrors().length);
  }

  @Test
  void validationError_fromResults() {
    Result ok = Result.success();
    Error e = new Error("Err", "msg", ErrorType.VALIDATION);
    Result fail = Result.failure(e);
    ValidationError ve = ValidationError.fromResults(List.of(ok, fail));
    assertEquals(1, ve.getErrors().length);
    assertEquals("Err", ve.getErrors()[0].getCode());
  }

  @Test
  void validationError_nullErrors() {
    ValidationError ve = new ValidationError((Error[]) null);
    assertArrayEquals(new Error[0], ve.getErrors());
  }
}
