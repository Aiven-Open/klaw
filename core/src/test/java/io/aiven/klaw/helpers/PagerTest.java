package io.aiven.klaw.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class PagerTest {

  @ParameterizedTest
  @MethodSource("pagerInfoProvider")
  void testGetItemsList(PageInfoTestSpec pageInfoTestSpec) {
    int currentPage =
        pageInfoTestSpec.currentPage.isEmpty() ? 1 : Integer.parseInt(pageInfoTestSpec.pageNo);
    int fromIndex = (currentPage - 1) * pageInfoTestSpec.itemsPerPage;
    List expected =
        pageInfoTestSpec.list.subList(
            fromIndex,
            Math.min(pageInfoTestSpec.list.size(), fromIndex + pageInfoTestSpec.itemsPerPage + 1));
    assertThat(expected)
        .containsSequence(
            Pager.getItemsList(
                pageInfoTestSpec.pageNo,
                pageInfoTestSpec.currentPage,
                pageInfoTestSpec.itemsPerPage,
                pageInfoTestSpec.list,
                pageInfoTestSpec.biFunction));
  }

  static Stream<PageInfoTestSpec> pagerInfoProvider() {
    return Stream.of(
        PageInfoTestSpec.of(
            "1",
            "2",
            5,
            List.of("1", "2", "3", "4", "5", "6", "7"),
            (BiFunction<Pager.PageContext, String, String>) (pageContext, s) -> s),
        PageInfoTestSpec.of(
            "1",
            "1",
            5,
            List.of("1", "2", "3", "4", "5", "6", "7"),
            (BiFunction<Pager.PageContext, String, String>) (pageContext, s) -> s),
        PageInfoTestSpec.of(
            "1",
            "",
            5,
            List.of("1", "2", "3", "4", "5", "6", "7"),
            (BiFunction<Pager.PageContext, String, String>) (pageContext, s) -> s),
        PageInfoTestSpec.of(
            "1",
            "2",
            5,
            List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"),
            (BiFunction<Pager.PageContext, String, String>) (pageContext, s) -> s));
  }

  static class PageInfoTestSpec {
    private final String pageNo;
    private final String currentPage;
    private final int itemsPerPage;
    private final List list;
    private final BiFunction biFunction;

    private PageInfoTestSpec(
        String pageNo, String currentPage, int itemsPerPage, List list, BiFunction biFunction) {
      this.pageNo = pageNo;
      this.currentPage = currentPage;
      this.itemsPerPage = itemsPerPage;
      this.list = list;
      this.biFunction = biFunction;
    }

    private static PageInfoTestSpec of(
        String pageNo, String currentPage, int itemsPerPage, List list, BiFunction biFunction) {
      return new PageInfoTestSpec(pageNo, currentPage, itemsPerPage, list, biFunction);
    }
  }
}
