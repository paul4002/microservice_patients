package nur.edu.nurtricenter_patient.application.patients.addresses.addAddress;

import java.util.UUID;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nur.edu.nurtricenter_patient.core.results.ResultWithValue;

public record AddAddressCommand(
  UUID patientId,
  @Size(max = 255) String label,
  @NotBlank @Size(max = 255) String line1,
  @Size(max = 255) String line2,
  @NotBlank @Size(max = 255) String country,
  @Size(max = 255) String province,
  @NotBlank @Size(max = 255) String city,
  @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
  @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) implements Command<ResultWithValue<UUID>> {}
