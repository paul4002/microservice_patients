package nur.edu.nurtricenter_patient.domain.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.core.results.DomainException;

class AddressTest {

  private static final Coordinate VALID_COORDINATE = new Coordinate(-17.7833, -63.1821);

  @Test
  void shouldCreateValidAddress() {
    Address address = new Address("Casa", "Calle 1", "Depto 2", "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE);
    assertNotNull(address.getId());
    assertEquals("Casa", address.getLabel());
    assertEquals("Calle 1", address.getLine1());
    assertEquals("Depto 2", address.getLine2());
    assertEquals("Bolivia", address.getCountry());
    assertEquals("Santa Cruz", address.getProvince());
    assertEquals("Santa Cruz", address.getCity());
    assertTrue(address.isActive());
  }

  @Test
  void shouldThrowWhenLabelIsNull() {
    DomainException ex = assertThrows(DomainException.class,
        () -> new Address(null, "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE));
    assertEquals("LabelIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenLabelIsBlank() {
    DomainException ex = assertThrows(DomainException.class,
        () -> new Address("  ", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE));
    assertEquals("LabelIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenLine1IsNull() {
    DomainException ex = assertThrows(DomainException.class,
        () -> new Address("Casa", null, null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE));
    assertEquals("Line1IsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCountryIsBlank() {
    DomainException ex = assertThrows(DomainException.class,
        () -> new Address("Casa", "Calle 1", null, "", "Santa Cruz", "Santa Cruz", VALID_COORDINATE));
    assertEquals("CountryIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenProvinceIsNull() {
    DomainException ex = assertThrows(DomainException.class,
        () -> new Address("Casa", "Calle 1", null, "Bolivia", null, "Santa Cruz", VALID_COORDINATE));
    assertEquals("ProvinceIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCityIsBlank() {
    DomainException ex = assertThrows(DomainException.class,
        () -> new Address("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "  ", VALID_COORDINATE));
    assertEquals("CityIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldThrowWhenCoordinateIsNull() {
    DomainException ex = assertThrows(DomainException.class,
        () -> new Address("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", null));
    assertEquals("CoordinateIsRequired", ex.getError().getCode());
  }

  @Test
  void shouldDeactivateAddress() {
    Address address = new Address("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE);
    assertTrue(address.isActive());
    address.deactivate();
    assertFalse(address.isActive());
  }

  @Test
  void shouldUpdateAddress() {
    Address address = new Address("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE);
    Coordinate newCoord = new Coordinate(-17.79, -63.18);
    address.update("Trabajo", "Avenida 2", "Piso 3", "Bolivia", "Cochabamba", "Cochabamba", newCoord);
    assertEquals("Trabajo", address.getLabel());
    assertEquals("Avenida 2", address.getLine1());
    assertEquals("Piso 3", address.getLine2());
    assertEquals("Cochabamba", address.getCity());
    assertEquals(newCoord, address.getCoordinate());
  }

  @Test
  void shouldUpdateGeocoordinate() {
    Address address = new Address("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE);
    Coordinate newCoord = new Coordinate(-17.80, -63.17);
    address.updateGeo(newCoord);
    assertEquals(newCoord, address.getCoordinate());
  }

  @Test
  void shouldThrowWhenUpdateGeoWithNullCoordinate() {
    Address address = new Address("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORDINATE);
    DomainException ex = assertThrows(DomainException.class, () -> address.updateGeo(null));
    assertEquals("CoordinateIsRequired", ex.getError().getCode());
  }

  @Test
  void coordinateShouldThrowWhenLatitudeIsNull() {
    assertThrows(NullPointerException.class, () -> new Coordinate(null, -63.18));
  }

  @Test
  void coordinateShouldThrowWhenLongitudeIsNull() {
    assertThrows(NullPointerException.class, () -> new Coordinate(-17.78, null));
  }
}
