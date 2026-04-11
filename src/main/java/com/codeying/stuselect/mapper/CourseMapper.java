package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.Course;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseMapper extends BaseMapper<Course> {

  @Select(
      """
      <script>
      select c.id, c.name, c.score, c.numb, c.tid, c.jianjie, c.dept,
             c.max_students as maxStudents, c.time_slot as timeSlot,
             coalesce(nullif(t.tname, ''), t.username) as teacherName
      from tb_course c
      left join tb_teacher t on t.id = c.tid
      where 1 = 1
      <if test="teacherId != null and teacherId != ''">
        and c.tid = #{teacherId}
      </if>
      <if test="keyword != null and keyword != ''">
        and (
          c.name like concat('%', #{keyword}, '%')
          or ifnull(c.numb, '') like concat('%', #{keyword}, '%')
          or ifnull(c.jianjie, '') like concat('%', #{keyword}, '%')
          or ifnull(t.tname, '') like concat('%', #{keyword}, '%')
        )
      </if>
      order by c.id desc
      </script>
      """)
  List<Course> selectListWithTeacher(@Param("keyword") String keyword, @Param("teacherId") String teacherId);

  @Select(
      """
      select c.id, c.name, c.score, c.numb, c.tid, c.jianjie, c.dept,
             c.max_students as maxStudents, c.time_slot as timeSlot,
             coalesce(nullif(t.tname, ''), t.username) as teacherName
      from tb_course c
      left join tb_teacher t on t.id = c.tid
      where c.id = #{id}
      """)
  Course selectByIdWithTeacher(@Param("id") String id);

  @Select(
      """
      select c.id, c.name, c.score, c.numb, c.tid, c.jianjie, c.dept,
             c.max_students as maxStudents, c.time_slot as timeSlot,
             coalesce(nullif(t.tname, ''), t.username) as teacherName
      from tb_course c
      left join tb_teacher t on t.id = c.tid
      where c.id = #{id}
      for update
      """)
  Course selectByIdForUpdate(@Param("id") String id);

  @Select(
      """
      select c.id, c.name, c.score, c.numb, c.tid, c.jianjie, c.dept,
             c.max_students as maxStudents, c.time_slot as timeSlot,
             coalesce(nullif(t.tname, ''), t.username) as teacherName
      from tb_course c
      left join tb_teacher t on t.id = c.tid
      where c.numb = #{numb}
      """)
  Course selectByNumbWithTeacher(@Param("numb") String numb);
}
