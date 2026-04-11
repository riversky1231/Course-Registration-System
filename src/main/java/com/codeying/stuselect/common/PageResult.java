package com.codeying.stuselect.common;

import java.util.Collections;
import java.util.List;

public record PageResult<T>(List<T> items, long total, int page, int pageSize, long totalPages) {

  public static <T> PageResult<T> of(List<T> source, PageQuery query) {
    List<T> safeSource = source == null ? List.of() : source;
    int fromIndex = Math.min(query.offset(), safeSource.size());
    int toIndex = Math.min(fromIndex + query.pageSize(), safeSource.size());
    long total = safeSource.size();
    long totalPages = total == 0 ? 0 : (total + query.pageSize() - 1) / query.pageSize();
    List<T> pageItems =
        fromIndex >= toIndex ? Collections.emptyList() : safeSource.subList(fromIndex, toIndex);
    return new PageResult<>(pageItems, total, query.page(), query.pageSize(), totalPages);
  }
}
