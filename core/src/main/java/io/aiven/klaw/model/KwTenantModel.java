package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KwTenantModel implements Serializable {

  @NotNull
  @Size(min = 6, max = 25, message = "Tenant name must be at least 12 characters and no spaces")
  @Pattern(
      message = "Tenant name must be at least 12 characters and no spaces",
      regexp = "^[a-zA-Z0-9]{3,}$")
  private String tenantName;

  @NotNull
  @Size(min = 6, max = 25, message = "Tenant description is not enough.")
  @Pattern(message = "Invalid Tenant description", regexp = "^[a-zA-Z 0-9]{3,}$")
  private String tenantDesc;

  @NotNull
  @Size(min = 5, max = 50, message = "Name must be atleast 5 characters")
  private String contactPerson;

  @NotNull private String orgName;

  private String emailId;

  private Integer tenantId;

  private boolean isActiveTenant = true;

  private boolean authorizedToDelete;
}
