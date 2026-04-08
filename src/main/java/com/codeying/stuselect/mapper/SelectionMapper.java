package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.SelectionRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SelectionMapper extends BaseMapper<SelectionRecord> {

  @Select(
      """
      <script>
      select s.id,
             s.courseid as courseId,
             c.name as courseName,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.score,
             s.createtime as createTime
      from tb_sct s
      left join tb_course c on c.id = s.courseid
      left join tb_student st on st.id = s.studentId
      left join tb_teacher t on t.id = s.teaid
      where 1 = 1
      <if test="studentId != null and studentId != ''">
        and s.studentId = #{studentId}
      </if>
      <if test="teacherId != null and teacherId != ''">
        and s.teaid = #{teacherId}
      </if>
      <if test="keyword != null and keyword != ''">
        and (
          ifnull(c.name, '') like concat('%', #{keyword}, '%')
          or ifnull(st.sname, '') like concat('%', #{keyword}, '%')
          or ifnull(t.tname, '') like concat('%', #{keyword}, '%')
        )
      </if>
      order by s.createtime desc, s.id desc
      </script>
      """)
  List<SelectionRecord> selectJoinedList(
      @Param("keyword") String keyword,
      @Param("studentId") String studentId,
      @Param("teacherId") String teacherId);

  @Select(
      """
      select s.id,
             s.courseid as courseId,
             c.name as courseName,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.score,
             s.createtime as createTime
      from tb_sct s
      left join tb_course c on c.id = s.courseid
      left join tb_student st on st.id = s.studentId
      left join tb_teacher t on t.id = s.teaid
      where s.id = #{id}
      """)
  SelectionRecord selectJoinedById(@Param("id") String id);

  @Select(
      """
      select s.id,
             s.courseid as courseId,
             c.name as courseName,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.score,
             s.createtime as createTime
      from tb_sct s
      left join tb_course c on c.id = s.courseid
      left join tb_student st on st.id = s.studentId
      left join tb_teacher t on t.id = s.teaid
      where s.courseid = #{courseId} and s.studentId = #{studentId}
      """)
  SelectionRecord selectByCourseAndStudent(
      @Param("courseId") String courseId, @Param("studentId") String studentId);
}
