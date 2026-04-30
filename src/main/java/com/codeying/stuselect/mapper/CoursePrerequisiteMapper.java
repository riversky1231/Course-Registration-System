package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.CoursePrerequisite;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CoursePrerequisiteMapper extends BaseMapper<CoursePrerequisite> {

  @Select(
      "SELECT p.id, p.course_id, p.prerequisite_course_id, p.min_score, "
          + "c1.name AS course_name, c2.name AS prerequisite_course_name "
          + "FROM tb_course_prerequisite p "
          + "LEFT JOIN tb_course c1 ON p.course_id = c1.id "
          + "LEFT JOIN tb_course c2 ON p.prerequisite_course_id = c2.id "
          + "WHERE p.course_id = #{courseId}")
  List<CoursePrerequisite> selectWithNamesByCourseId(String courseId);
}
