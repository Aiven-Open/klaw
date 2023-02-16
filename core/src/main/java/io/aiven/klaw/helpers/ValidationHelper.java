package io.aiven.klaw.helpers;

import io.aiven.klaw.error.KlawRestException;
import org.apache.commons.lang3.Validate;

public class ValidationHelper {

  public static void validateNotEmptyOrBlank(String value, String errMsg) throws KlawRestException {
    try {
      Validate.notEmpty(value);
      Validate.notBlank(value);
    } catch (Exception ex) {
      throw new KlawRestException(errMsg);
    }
  }
}
