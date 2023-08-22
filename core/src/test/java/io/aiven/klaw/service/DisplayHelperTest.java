package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import io.aiven.klaw.helpers.DisplayHelper;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DisplayHelperTest {
  @ParameterizedTest
  @MethodSource("displayableProvider")
  void testUpdateTeamNamesForDisplay(List<DisplayableTestMock> list, List<String> expected) {
    assertThat(list).hasSameSizeAs(expected);
    DisplayHelper.updateTeamNamesForDisplay(
        list, DisplayableTestMock::getTeamName, DisplayableTestMock::setTeamName);
    for (int i = 0; i < list.size(); i++) {
      assertThat(list.get(i).getTeamName()).isEqualTo(expected.get(i));
    }
  }

  static Stream<Arguments> displayableProvider() {
    return Stream.of(
        of(
            List.of(
                new DisplayableTestMock("test"),
                new DisplayableTestMock("test1"),
                new DisplayableTestMock("test2")),
            List.of("test", "test1", "test2")),
        of(
            List.of(
                new DisplayableTestMock("12345678945"), new DisplayableTestMock("hello world!")),
            List.of("12345678...", "hello wo...")),
        of(
            List.of(new DisplayableTestMock("12345678..."), new DisplayableTestMock("hello wo...")),
            List.of("12345678...", "hello wo...")));
  }

  static class DisplayableTestMock {
    private String teamName;

    public DisplayableTestMock(String teamName) {
      this.teamName = teamName;
    }

    public String getTeamName() {
      return teamName;
    }

    public void setTeamName(String teamName) {
      this.teamName = teamName;
    }

    @Override
    public String toString() {
      return "teamName='" + teamName + '\'';
    }
  }
}
