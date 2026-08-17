package io.aiven.klaw.validation;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX;
import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX_VALIDATION_STR;
import static io.aiven.klaw.helpers.KwConstants.PASSWORD_VALIDATION_AD_STR;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class PasswordUpdateValidatorImpl
    implements ConstraintValidator<PasswordUpdateValidator, String> {

  // Precompiled once: Pattern.compile() was previously called on every validation,
  // which is expensive on the hot path (every user password update).
  private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Override
  public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
    if (StringUtils.isEmpty(password)) {
      //      This is an update so we dont always have a password to change
      return true;
    }
    if (StringUtils.isEmpty(authenticationType) || authenticationType.equalsIgnoreCase("db")) {
      Matcher matcher = PASSWORD_PATTERN.matcher(password);
      if (!matcher.find()) {
        constraintValidatorContext
            .buildConstraintViolationWithTemplate(PASSWORD_REGEX_VALIDATION_STR)
            .addConstraintViolation()
            .disableDefaultConstraintViolation();
        return false;
      }
    } else {
      if (StringUtils.isNotEmpty(password)) {
        constraintValidatorContext
            .buildConstraintViolationWithTemplate(PASSWORD_VALIDATION_AD_STR)
            .addConstraintViolation()
            .disableDefaultConstraintViolation();
        return false;
      }
    }
    return true;
  }
}
