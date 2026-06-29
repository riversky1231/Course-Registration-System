package com.codeying.stuselect.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

  @Test
  void oversizedPageDoesNotOverflowIntoNegativeOffset() {
    PageResult<String> result = PageResult.of(List.of("a", "b"), PageQuery.of(Integer.MAX_VALUE, 100));

    assertTrue(result.items().isEmpty());
  }
}
