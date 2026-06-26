package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CourseTimeSlotTest {

  @Test
  void overlapsWhenSameDaySectionRangesIntersect() {
    assertTrue(CourseTimeSlot.overlaps("周一第1-2节", "周一第2-3节"));
    assertTrue(CourseTimeSlot.overlaps("星期一第十-十一节", "周一第11-12节"));
  }

  @Test
  void doesNotOverlapWhenDayOrSectionsDiffer() {
    assertFalse(CourseTimeSlot.overlaps("周一第1-2节", "周一第3-4节"));
    assertFalse(CourseTimeSlot.overlaps("周一第1-2节", "周二第1-2节"));
  }

  @Test
  void fallsBackToExactMatchWhenTimeSlotCannotBeParsed() {
    assertTrue(CourseTimeSlot.overlaps("校外实践", "校外实践"));
    assertFalse(CourseTimeSlot.overlaps("校外实践", "线上实践"));
  }
}
