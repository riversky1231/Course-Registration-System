package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.Student;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StudentMapper extends BaseMapper<Student> {

  @Select("select * from tb_student where id = #{id} for update")
  Student selectByIdForUpdate(@Param("id") String id);

  @Select(
      """
      <script>
      select distinct st.*
      from tb_student st
      inner join tb_sct s on s.studentId = st.id
      where s.teaid = #{teacherId}
      <if test="keyword != null and keyword != ''">
        and (
          ifnull(st.username, '') like concat('%', #{keyword}, '%')
          or ifnull(st.numb, '') like concat('%', #{keyword}, '%')
          or ifnull(st.sname, '') like concat('%', #{keyword}, '%')
          or ifnull(st.sdept, '') like concat('%', #{keyword}, '%')
          or ifnull(st.smajor, '') like concat('%', #{keyword}, '%')
          or ifnull(st.sclass, '') like concat('%', #{keyword}, '%')
        )
      </if>
      order by st.id desc
      </script>
      """)
  List<Student> selectVisibleToTeacher(
      @Param("teacherId") String teacherId,
      @Param("keyword") String keyword);

  @Select(
      """
      select count(distinct st.id)
      from tb_student st
      inner join tb_sct s on s.studentId = st.id
      where s.teaid = #{teacherId}
      """)
  long countVisibleToTeacher(@Param("teacherId") String teacherId);

  @Select("select count(*) from tb_sct where studentId = #{studentId}")
  long countSelectionReferences(@Param("studentId") String studentId);
}
