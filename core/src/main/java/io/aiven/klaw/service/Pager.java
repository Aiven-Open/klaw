package io.aiven.klaw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Pager {
  public static final int DEFAULT_REC_PER_PAGE = 20;

  static int deriveCurrentPage(String pageNo, String currentPage, int totalPages) {
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
    if (currentPage != null
        && !currentPage.equals("")
        && !currentPage.equals(pageNo)
        && Integer.parseInt(pageNo) > 1
        && totalPages > 1) {
      numList.add("<<");
      numList.add("<");
    } else if (currentPage != null
        && currentPage.equals(pageNo)
        && Integer.parseInt(pageNo) > 1
        && totalPages > 1) {
      numList.add("<<");
      numList.add("<");
    }

    if (totalPages > Integer.parseInt(pageNo)) {
      numList.add(pageNo);
      numList.add(">");
      numList.add(">>");
    } else if (totalPages == Integer.parseInt(pageNo)) {
      numList.add(pageNo);
    }
  }

  public static <T> List<T> getItemsList(
      String pageNo,
      String currentPage,
      List<T> aclListMap,
      BiFunction<PageContext, T, T> consumer) {
    return getItemsList(pageNo, currentPage, DEFAULT_REC_PER_PAGE, aclListMap, consumer);
  }

  public static <T> List<T> getItemsList(
      String pageNo,
      String currentPage,
      int recsPerPage,
      List<T> aclListMap,
      BiFunction<PageContext, T, T> consumer) {
    List<T> aclListMapUpdated = new ArrayList<>();

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
