package com.codeying.stuselect.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private static final AtomicInteger SEQ = new AtomicInteger(10000000);

  private IdGenerator() {}

  public static String newId() {
    int next = SEQ.updateAndGet(value -> value >= (1 << 26) ? 10000000 : value + 1);
    return LocalDateTime.now().format(FORMATTER) + next;
  }
}
