package com.codeying.stuselect;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.codeying.stuselect.mapper")
public class StudentCourseSelectionApplication {

  public static void main(String[] args) {
    SpringApplication.run(StudentCourseSelectionApplication.class, args);
  }
}
