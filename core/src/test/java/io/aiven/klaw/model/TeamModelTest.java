package io.aiven.klaw.model;

import static org.assertj.core.api.Assertions.assertThat;

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
public class TeamModelTest {

  private static Validator validator;

  @BeforeAll
  public static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void validateInternationalSymbolsSupported() {
    TeamModel model = getTeamModelForPhoneValidation();
    model.setTeamphone("00353");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
    model.setTeamphone("+353");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();

    model.setTeamphone("+49123456789");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();

    model.setTeamphone("0049123456789");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
  }

  @Test
  public void validateInternationalPlusNotSupportedIfNotLeading() {
    TeamModel model = getTeamModelForPhoneValidation();

    model.setTeamphone("353+");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.size()).isEqualTo(1);
  }

  @Test
  public void validateInternationalPlusOnlyOneSupported() {
    TeamModel model = getTeamModelForPhoneValidation();
    model.setTeamphone("+353");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
    model.setTeamphone("++353");
    violations = validator.validate(model);
    assertThat(violations.size()).isEqualTo(1);
  }

  @Test
  public void validateHyphenNotSupported() {
    TeamModel model = getTeamModelForPhoneValidation();
    model.setTeamphone("+353-123-4567");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.size()).isEqualTo(1);
  }

  @Test
  public void validateParanthesisNotSupported() {
    TeamModel model = getTeamModelForPhoneValidation();
    model.setTeamphone("(353)1234567");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.size()).isEqualTo(1);
  }

  @Test
  public void validateSpaceNotSupported() {
    TeamModel model = getTeamModelForPhoneValidation();
    model.setTeamphone("353 1234567");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.size()).isEqualTo(1);
  }

  @Test
  public void validateAccentsAllowed() {
    TeamModel model = getTeamModelForTeamContactValidation();
    model.setContactperson("Aindriú");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
    model.setContactperson("Françoise");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
  }

  @Test
  public void validateUmlautAllowed() {
    TeamModel model = getTeamModelForTeamContactValidation();
    model.setContactperson("Matthias Schweighöfer");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
    model.setContactperson("Schäfer");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();

    model.setContactperson("Köhne jr");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();

    model.setContactperson("Mr Jünemann");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue(); // 'ä', 'ö', 'Ä', 'ß'

    // explicitly just testing the special chars mentioned in raised Issue
    model.setContactperson("äöÄß");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
  }

  @Test
  public void validateIrishSurnamesAllowed() {
    TeamModel model = getTeamModelForTeamContactValidation();
    model.setContactperson("Aindriú O'Maolfábhail");
    Set<ConstraintViolation<TeamModel>> violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
    model.setContactperson("Seán O'Neill");
    violations = validator.validate(model);
    assertThat(violations.isEmpty()).isTrue();
  }

  // validations for non nulls shouldn't impact our test.
  private TeamModel getTeamModelForPhoneValidation() {
    TeamModel model = new TeamModel();
    model.setContactperson("valid");
    model.setTeamname("team");
    return model;
  }

  private TeamModel getTeamModelForTeamContactValidation() {
    TeamModel model = new TeamModel();
    model.setTeamphone("123456789");
    model.setTeamname("team");
    return model;
  }
}
