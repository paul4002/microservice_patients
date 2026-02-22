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
    super(UUID.randomUUID());
    if (label.isBlank()) {
      throw new DomainException(AddressErrors.LabelIsRequired());
    } else if (line1.isBlank()) {
      throw new DomainException(AddressErrors.Line1IsRequired());
    } else if (country.isBlank()) {
      throw new DomainException(AddressErrors.CountryIsRequired());
    } else if (province.isBlank()) {
      throw new DomainException(AddressErrors.ProvinceIsRequired());
    } else if (city.isBlank()) {
      throw new DomainException(AddressErrors.CityIsRequired());
    } else if (coordinate == null) {
      throw new DomainException(AddressErrors.CoordinateIsRequired());
    }
    this.label = label;
    this.line1 = line1;
    this.line2 = line2;
    this.country = country;
    this.province = province;
    this.city = city;
    this.coordinate = coordinate;
    this.state = true;
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
}
