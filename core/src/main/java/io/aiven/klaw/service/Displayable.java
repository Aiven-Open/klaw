package io.aiven.klaw.service;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Displayable {
  String ELLIPSIS = "...";
  int NAME_MAX_LENGTH = 11;

  default <T> void updateTeamNamesForDisplay(
      List<T> topicListUpdated, Function<T, String> strProvider, BiConsumer<T, String> setter) {
    topicListUpdated.stream()
        .filter(
            t -> {
              String s = strProvider.apply(t);
              return s.length() > NAME_MAX_LENGTH - ELLIPSIS.length() + 1
                  && (s.length() != NAME_MAX_LENGTH || !s.endsWith(ELLIPSIS));
            })
        .forEach(
            t ->
                setter.accept(t,
                    strProvider
                            .apply(t)
                            .substring(0, NAME_MAX_LENGTH - ELLIPSIS.length())
                        + ELLIPSIS));
  }
}
