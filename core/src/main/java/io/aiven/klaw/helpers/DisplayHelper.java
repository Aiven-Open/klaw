package io.aiven.klaw.helpers;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DisplayHelper {
  private static final String ELLIPSIS = "...";
  private static final int NAME_MAX_LENGTH = 11;

  public static <T> void updateTeamNamesForDisplay(
      List<T> list, Function<T, String> strProvider, BiConsumer<T, String> setter) {
    list.stream()
        .filter(
            t -> {
              String s = strProvider.apply(t);
              return s.length() > NAME_MAX_LENGTH - ELLIPSIS.length() + 1
                  && (s.length() != NAME_MAX_LENGTH || !s.endsWith(ELLIPSIS));
            })
        .forEach(
            t ->
                setter.accept(
                    t,
                    strProvider.apply(t).substring(0, NAME_MAX_LENGTH - ELLIPSIS.length())
                        + ELLIPSIS));
  }
}
