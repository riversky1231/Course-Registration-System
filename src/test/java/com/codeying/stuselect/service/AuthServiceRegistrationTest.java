package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.dto.AuthRequests;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.model.Student;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuthServiceRegistrationTest {

  @Test
  void registerStudentSetsDefaultGradeAndEnrollmentYear() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    PasswordService passwordService = mock(PasswordService.class);
    when(studentMapper.selectOne(any())).thenReturn(null);
    when(passwordService.encode("abc123")).thenReturn("encoded");

    AuthService service =
        new AuthService(null, null, studentMapper, null, passwordService);

    service.register(new AuthRequests.RegisterRequest("stu_test", "abc123", "student"));

    ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
    verify(studentMapper).insert(captor.capture());
    Student inserted = captor.getValue();
    assertEquals(1, inserted.getGrade());
    assertEquals(Year.now().getValue(), inserted.getEnrollmentYear());
  }
}
