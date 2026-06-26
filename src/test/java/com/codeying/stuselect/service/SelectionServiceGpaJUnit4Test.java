package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class SelectionServiceGpaJUnit4Test {

  private double invokeToGpa(final Double score) throws Exception {
    SelectionService service =
        new SelectionService(null, null, null, null, null, null);
    Method method = SelectionService.class.getDeclaredMethod("toGpa", Double.class);
    method.setAccessible(true);
    return ((Double) method.invoke(service, score)).doubleValue();
  }

  @Test
  public void testToGpaBoundaryAndNormalScores() throws Exception {
    assertEquals(0.0, invokeToGpa(null), 0.001);
    assertEquals(0.0, invokeToGpa(59.0), 0.001);
    assertEquals(1.0, invokeToGpa(60.0), 0.001);
    assertEquals(5.0, invokeToGpa(100.0), 0.001);
  }
}
