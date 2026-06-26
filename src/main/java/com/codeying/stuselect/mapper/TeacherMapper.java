package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.Teacher;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TeacherMapper extends BaseMapper<Teacher> {

  @Select("select count(*) from tb_course where tid = #{teacherId}")
  long countCourseReferences(@Param("teacherId") String teacherId);

  @Select("select count(*) from tb_sct where teaid = #{teacherId}")
  long countSelectionReferences(@Param("teacherId") String teacherId);
}
