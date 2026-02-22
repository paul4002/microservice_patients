package nur.edu.nurtricenter_patient.domain.address;

import java.util.Objects;

public record Coordinate(Double latitude, Double longitude) {
  public Coordinate {
    Objects.requireNonNull(latitude, "Latitude cannot be null");
    Objects.requireNonNull(longitude, "Longitude cannot be null");
  }
}
