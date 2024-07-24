package io.aiven.klaw.helpers;

import java.util.Objects;

public class CompareUtils {
  public static Boolean isFalse(String s) {
    return Boolean.FALSE.toString().equals(s);
  }

  public static Boolean isTrue(String s) {
    return Boolean.TRUE.toString().equals(s);
  }

  public static Boolean isEqual(Integer i, String s) {
    return Objects.equals(i.toString(), s);
  }
}
