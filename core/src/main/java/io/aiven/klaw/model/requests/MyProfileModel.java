package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class MyProfileModel implements Serializable {

  @NotNull(message = "Fullname cannot be null")
  @Size(min = 5, max = 50, message = "Name must be above 4 characters")
  @Pattern(
      message = "Invalid Full name",
      regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ' ]*$") // Pattern a-zA-z accents and umlaut and/or spaces.
  private String fullname;

  @Email(message = "Email should be valid")
  private String mailid;
}
