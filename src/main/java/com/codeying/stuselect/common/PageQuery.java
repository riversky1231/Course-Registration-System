package com.codeying.stuselect.common;

public record PageQuery(int page, int pageSize) {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int MAX_PAGE_SIZE = 100;

  public static PageQuery of(Integer page, Integer pageSize) {
    int normalizedPage = page == null || page < 1 ? DEFAULT_PAGE : page;
    int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    normalizedPageSize = Math.min(normalizedPageSize, MAX_PAGE_SIZE);
    return new PageQuery(normalizedPage, normalizedPageSize);
  }

  public int offset() {
    long offset = (long) (page - 1) * pageSize;
    return offset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
  }
}
