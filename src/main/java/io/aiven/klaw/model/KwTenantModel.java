package io.aiven.klaw.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KwTenantModel implements Serializable {

  @NotNull
  @Size(min = 12, max = 25, message = "Tenant name must be atleast 12 characters and no spaces")
  @Pattern(
      message = "Tenant name must be atleast 12 characters and no spaces",
      regexp = "^[a-zA-Z0-9]{3,}$")
  private String tenantName;

  @NotNull
  @Size(min = 6, max = 25, message = "Tenant description is not enough.")
  @Pattern(message = "Invalid Tenant description", regexp = "^[a-zA-Z 0-9]{3,}$")
  private String tenantDesc;

  private Integer tenantId;

  private String licenseExpiryDate;

  private String contactPerson;

  private boolean inTrialPhase;

  private boolean isActiveTenant;

  private String numberOfDays;

  private String numberOfHours;

  private String orgName;

  private boolean authorizedToDelete;

  private String emailId;
}
