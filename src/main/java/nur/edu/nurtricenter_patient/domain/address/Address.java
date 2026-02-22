package nur.edu.nurtricenter_patient.domain.address;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.Entity;
import nur.edu.nurtricenter_patient.core.results.DomainException;

public class Address extends Entity {
  private String label;
  private String line1;
  private String line2;
  private String country;
  private String province;
  private String city;
  private Coordinate coordinate;
  private Boolean state;

  public Address(String label, String line1, String line2, String country, String province, String city, Coordinate coordinate) {
    this(UUID.randomUUID(), label, line1, line2, country, province, city, coordinate, true);
  }

  public Address(UUID id, String label, String line1, String line2, String country, String province, String city, Coordinate coordinate, Boolean state) {
    super(id);
    validate(label, line1, country, province, city, coordinate);
    this.label = label;
    this.line1 = line1;
    this.line2 = line2;
    this.country = country;
    this.province = province;
    this.city = city;
    this.coordinate = coordinate;
    this.state = state != null ? state : true;
  }

  public String getLabel() {
    return this.label;
  }

  public String getLine1() {
    return this.line1;
  }

  public String getLine2() {
    return this.line2;
  }

  public String getCountry() {
    return this.country;
  }

  public String getProvince() {
    return this.province;
  }

  public String getCity() {
    return this.city;
  }

  public void setCoordinate(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public Coordinate getCoordinate() {
    return this.coordinate;
  }

  public Boolean isActive() {
    return this.state;
  }

  public void update(String label, String line1, String line2, String country, String province, String city, Coordinate coordinate) {
    validate(label, line1, country, province, city, coordinate);
    this.label = label;
    this.line1 = line1;
    this.line2 = line2;
    this.country = country;
    this.province = province;
    this.city = city;
    this.coordinate = coordinate;
  }

  public void deactivate() {
    this.state = false;
  }

  public void updateGeo(Coordinate coordinate) {
    if (coordinate == null) {
      throw new DomainException(AddressErrors.CoordinateIsRequired());
    }
    this.coordinate = coordinate;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static void validate(String label, String line1, String country, String province, String city, Coordinate coordinate) {
    if (isBlank(label)) {
      throw new DomainException(AddressErrors.LabelIsRequired());
    } else if (isBlank(line1)) {
      throw new DomainException(AddressErrors.Line1IsRequired());
    } else if (isBlank(country)) {
      throw new DomainException(AddressErrors.CountryIsRequired());
    } else if (isBlank(province)) {
      throw new DomainException(AddressErrors.ProvinceIsRequired());
    } else if (isBlank(city)) {
      throw new DomainException(AddressErrors.CityIsRequired());
    } else if (coordinate == null) {
      throw new DomainException(AddressErrors.CoordinateIsRequired());
    }
  }
}
