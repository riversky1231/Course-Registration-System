package com.codeying.stuselect.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeying.stuselect.config.CacheInitializer;
import com.codeying.stuselect.model.Student;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = "spring.data.redis.repositories.enabled=false")
class StudentMapperVisibilityTest {

  @Autowired
  private StudentMapper studentMapper;

  @MockBean
  private CacheInitializer cacheInitializer;

  @Test
  void selectVisibleToTeacherReturnsOnlyStudentsWithSelectionsForTeacher() {
    List<Student> students = studentMapper.selectVisibleToTeacher("T2003", null);

    assertEquals(List.of("S3002"), students.stream().map(Student::getId).toList());
    assertEquals(1L, studentMapper.countVisibleToTeacher("T2003"));
  }

  @Test
  void selectVisibleToTeacherAppliesKeywordInsideTeacherScope() {
    List<Student> matched = studentMapper.selectVisibleToTeacher("T2003", "stu_lin");
    List<Student> unrelated = studentMapper.selectVisibleToTeacher("T2003", "stu_chen");

    assertEquals(List.of("S3002"), matched.stream().map(Student::getId).toList());
    assertTrue(unrelated.isEmpty());
  }
}
