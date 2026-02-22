package nur.edu.nurtricenter_patient.domain.address;

import nur.edu.nurtricenter_patient.core.results.Error;
import nur.edu.nurtricenter_patient.core.results.ErrorType;

public class AddressErrors {
  public static Error LabelIsRequired() {
    return new Error("LabelIsRequired", "The address label is required", ErrorType.VALIDATION);
  }

  public static Error Line1IsRequired() {
    return new Error("Line1IsRequired", "The address line1 is required", ErrorType.VALIDATION);
  }

  public static Error CountryIsRequired() {
    return new Error("CountryIsRequired", "The address country is required", ErrorType.VALIDATION);
  }

  public static Error ProvinceIsRequired() {
    return new Error("ProvinceIsRequired", "The address province is required", ErrorType.VALIDATION);
  }

  public static Error CityIsRequired() {
    return new Error("CityIsRequired", "The address city is required", ErrorType.VALIDATION);
  }

  public static Error CoordinateIsRequired() {
    return new Error("CoordinateIsRequired", "The address coordinate is required", ErrorType.VALIDATION);
  }
}
