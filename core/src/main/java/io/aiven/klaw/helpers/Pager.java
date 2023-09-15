package io.aiven.klaw.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import org.apache.commons.lang3.math.NumberUtils;

public class Pager {
  public static final int DEFAULT_REC_PER_PAGE = 20;

  private static int deriveCurrentPage(String pageNo, String currentPage, int totalPages) {
    return switch (pageNo) {
      case ">" -> Integer.parseInt(currentPage) + 1;
      case ">>" -> totalPages;
      case "<" -> Integer.parseInt(currentPage) - 1;
      case "<<" -> 1;
      default -> !NumberUtils.isCreatable(pageNo) ? 1 : Integer.parseInt(pageNo);
    };
  }

  private static void getAllPagesList(
      int pageNo, String currentPage, int totalPages, List<String> numList) {

    if (currentPage != null && pageNo > 1 && totalPages > 1 && !currentPage.isEmpty()) {
      numList.add("<<");
      numList.add("<");
    }

    if (totalPages > pageNo) {
      numList.add(Integer.toString(pageNo));
      numList.add(">");
      numList.add(">>");
    } else if (totalPages == pageNo) {
      numList.add(Integer.toString(pageNo));
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
    getAllPagesList(requestPageNo, currentPage, totalPages, numList);

    PageContext pageContext =
        PageContext.of(totalPages, numList, Integer.toString(requestPageNo), totalRecs);
    for (int i = startVar; i < lastVar; i++) {
      aclListMapUpdated.add(consumer.apply(pageContext, aclListMap.get(i)));
    }
    return aclListMapUpdated;
  }

  public static class PageContext {
    private final String totalPages;
    private final List<String> allPageNos;
    private final String pageNo;
    private final int totalRecs;

    private PageContext(int totalPages, List<String> allPageNos, String pageNo, int totalRecs) {
      this.totalPages = totalPages + "";
      this.allPageNos = allPageNos;
      this.pageNo = pageNo;
      this.totalRecs = totalRecs;
    }

    public static PageContext of(
        int totalPages, List<String> allPageNos, String pageNo, int totalRecs) {
      return new PageContext(totalPages, allPageNos, pageNo, totalRecs);
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

    public int getTotalRecs() {
      return totalRecs;
    }
  }
}
