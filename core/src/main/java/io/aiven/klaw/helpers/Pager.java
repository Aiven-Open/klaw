package io.aiven.klaw.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Pager {
  public static final int DEFAULT_REC_PER_PAGE = 20;

  private static int deriveCurrentPage(String pageNo, String currentPage, int totalPages) {
    return switch (pageNo) {
      case ">" -> Integer.parseInt(currentPage) + 1;
      case ">>" -> totalPages;
      case "<" -> Integer.parseInt(currentPage) - 1;
      case "<<" -> 1;
      default -> currentPage.isBlank() ? 1 : Integer.parseInt(currentPage);
    };
  }

  private static void getAllPagesList(
      String pageNo, String currentPage, int totalPages, List<String> numList) {
    final int pageNoInt = Integer.parseInt(pageNo);
    if (currentPage != null && pageNoInt > 1 && totalPages > 1 && !currentPage.isEmpty()) {
      numList.add("<<");
      numList.add("<");
    }

    if (totalPages > pageNoInt) {
      numList.add(pageNo);
      numList.add(">");
      numList.add(">>");
    } else if (totalPages == pageNoInt) {
      numList.add(pageNo);
    }
  }

  public static <INPUT, OUTPUT> List<OUTPUT> getItemsList(
      String pageNo,
      String currentPage,
      List<INPUT> aclListMap,
      BiFunction<PageContext, INPUT, OUTPUT> consumer) {
    return getItemsList(pageNo, currentPage, DEFAULT_REC_PER_PAGE, aclListMap, consumer);
  }

  public static <INPUT, OUTPUT> List<OUTPUT> getItemsList(
      String pageNo,
      String currentPage,
      int recsPerPage,
      List<INPUT> aclListMap,
      BiFunction<PageContext, INPUT, OUTPUT> consumer) {
    List<OUTPUT> aclListMapUpdated = new ArrayList<>();

    int totalRecs = aclListMap.size();
    int totalPages =
        aclListMap.size() / recsPerPage + (aclListMap.size() % recsPerPage > 0 ? 1 : 0);

    final int requestPageNo = deriveCurrentPage(pageNo, currentPage, totalPages);
    final int startVar = Math.max(0, (requestPageNo - 1) * recsPerPage);
    final int lastVar = Math.min(requestPageNo * recsPerPage, totalRecs);

    List<String> numList = new ArrayList<>();
    getAllPagesList(pageNo, currentPage, totalPages, numList);

    PageContext pageContext = PageContext.of(totalPages, numList, pageNo);
    for (int i = startVar; i < lastVar; i++) {
      aclListMapUpdated.add(consumer.apply(pageContext, aclListMap.get(i)));
    }
    return aclListMapUpdated;
  }

  public static class PageContext {
    private final String totalPages;
    private final List<String> allPageNos;
    private final String pageNo;

    private PageContext(int totalPages, List<String> allPageNos, String pageNo) {
      this.totalPages = totalPages + "";
      this.allPageNos = allPageNos;
      this.pageNo = pageNo;
    }

    public static PageContext of(int totalPages, List<String> allPageNos, String pageNo) {
      return new PageContext(totalPages, allPageNos, pageNo);
    }

    public String getTotalPages() {
      return totalPages;
    }

    public List<String> getAllPageNos() {
      return allPageNos;
    }

    public String getPageNo() {
      return pageNo;
    }
  }
}
