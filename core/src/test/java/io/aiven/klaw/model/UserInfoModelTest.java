package io.aiven.klaw.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.aiven.klaw.model.requests.UserInfoModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
public class UserInfoModelTest {

  private static Validator validator;

  @BeforeAll
  public static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void validateAccentsAllowed() {
    UserInfoModel model = getUserInfoModelForTeamContactValidation();
    model.setFullname("Aindriú");
    Set<ConstraintViolation<UserInfoModel>> violations = validator.validate(model);
    violations.forEach(
        vio -> {
          log.info(" {}", vio.getMessage());
        });
    assertThat(violations.isEmpty()).isTrue();
    model.setFullname("Françoise");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
  }

  @Test
  public void validateUmlautAllowed() {
    UserInfoModel model = getUserInfoModelForTeamContactValidation();
    model.setFullname("Matthias Schweighöfer");
    Set<ConstraintViolation<UserInfoModel>> violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
    model.setFullname("Schäfer");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();

    model.setFullname("Köhne jr");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();

    model.setFullname("Mr Jünemann");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue(); // 'ä', 'ö', 'Ä', 'ß'

    // explicitly just testing the special chars mentioned in raised Issue
    model.setFullname("ä ö Ä ß");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
  }

  @Test
  public void validateIrishSurnamesAllowed() {
    UserInfoModel model = getUserInfoModelForTeamContactValidation();
    model.setFullname("Aindriú O'Maolfábhail");
    Set<ConstraintViolation<UserInfoModel>> violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
    model.setFullname("Seán O'Neill");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
  }

  private UserInfoModel getUserInfoModelForTeamContactValidation() {
    UserInfoModel model = new UserInfoModel();
    model.setUsername("DORRIS");
    model.setMailid("bob@bob.com");
    model.setTeamId(1001);
    model.setRole("USER");
    model.setSwitchTeams(false);
    model.setUserPassword("");
    return model;
  }
}
