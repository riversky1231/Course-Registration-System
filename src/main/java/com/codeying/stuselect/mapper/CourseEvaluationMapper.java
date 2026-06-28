package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.CourseEvaluation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseEvaluationMapper extends BaseMapper<CourseEvaluation> {

  @Select(
      """
      <script>
      select e.id,
             e.course_id as courseId,
             c.name as courseName,
             e.student_id as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             e.teacher_id as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             e.rating,
             e.comment,
             e.anonymous,
             e.create_time as createTime
      from tb_course_evaluation e
      left join tb_course c on c.id = e.course_id
      left join tb_student st on st.id = e.student_id
      left join tb_teacher t on t.id = e.teacher_id
      where 1 = 1
      <if test="studentId != null and studentId != ''">
        and e.student_id = #{studentId}
      </if>
      <if test="teacherId != null and teacherId != ''">
        and e.teacher_id = #{teacherId}
      </if>
      <if test="courseId != null and courseId != ''">
        and e.course_id = #{courseId}
      </if>
      <if test="keyword != null and keyword != ''">
        and (
          ifnull(c.name, '') like concat('%', #{keyword}, '%')
          or ifnull(e.comment, '') like concat('%', #{keyword}, '%')
          or ifnull(t.tname, '') like concat('%', #{keyword}, '%')
        )
      </if>
      order by e.create_time desc, e.id desc
      </script>
      """)
  List<CourseEvaluation> selectJoinedList(
      @Param("keyword") String keyword,
      @Param("studentId") String studentId,
      @Param("teacherId") String teacherId,
      @Param("courseId") String courseId);

  @Select(
      """
      select e.id,
             e.course_id as courseId,
             c.name as courseName,
             e.student_id as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             e.teacher_id as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             e.rating,
             e.comment,
             e.anonymous,
             e.create_time as createTime
      from tb_course_evaluation e
      left join tb_course c on c.id = e.course_id
      left join tb_student st on st.id = e.student_id
      left join tb_teacher t on t.id = e.teacher_id
      where e.id = #{id}
      """)
  CourseEvaluation selectJoinedById(@Param("id") String id);

  @Select(
      """
      select id, course_id as courseId, student_id as studentId,
             teacher_id as teacherId, rating, comment, anonymous,
             create_time as createTime
      from tb_course_evaluation
      where course_id = #{courseId} and student_id = #{studentId}
      limit 1
      """)
  CourseEvaluation selectByCourseAndStudent(
      @Param("courseId") String courseId, @Param("studentId") String studentId);
}
