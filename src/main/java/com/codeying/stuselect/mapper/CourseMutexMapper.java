package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.CourseMutex;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseMutexMapper extends BaseMapper<CourseMutex> {

  @Select(
      "SELECT m.id, m.course_id_a, m.course_id_b, m.reason, "
          + "c1.name AS course_name_a, c2.name AS course_name_b "
          + "FROM tb_course_mutex m "
          + "LEFT JOIN tb_course c1 ON m.course_id_a = c1.id "
          + "LEFT JOIN tb_course c2 ON m.course_id_b = c2.id "
          + "WHERE m.course_id_a = #{courseId} OR m.course_id_b = #{courseId}")
  List<CourseMutex> selectWithNamesByCourseId(String courseId);
}
