package io.aiven.klaw.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CompareUtilsTest {
  @Test
  public void isFalse() {
    assertThat(CompareUtils.isFalse("false")).isTrue();
    assertThat(CompareUtils.isFalse("")).isFalse();
    assertThat(CompareUtils.isFalse(null)).isFalse();
    assertThat(CompareUtils.isFalse("true")).isFalse();
  }

  @Test
  public void isTrue() {
    assertThat(CompareUtils.isTrue("true")).isTrue();
    assertThat(CompareUtils.isTrue("")).isFalse();
    assertThat(CompareUtils.isTrue(null)).isFalse();
    assertThat(CompareUtils.isTrue("false")).isFalse();
  }

  @Test
  public void isEqualInteger() {
    assertThat(CompareUtils.isEqual(1, "1")).isTrue();
    assertThat(CompareUtils.isEqual(1, "2")).isFalse();
    assertThat(CompareUtils.isEqual(1, "")).isFalse();
    assertThat(CompareUtils.isEqual(1, null)).isFalse();
  }
}
