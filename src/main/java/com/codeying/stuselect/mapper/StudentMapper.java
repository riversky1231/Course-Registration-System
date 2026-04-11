package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StudentMapper extends BaseMapper<Student> {

  @Select("select * from tb_student where id = #{id} for update")
  Student selectByIdForUpdate(@Param("id") String id);
}
