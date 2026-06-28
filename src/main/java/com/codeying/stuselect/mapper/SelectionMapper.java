package com.codeying.stuselect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codeying.stuselect.model.SelectionRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SelectionMapper extends BaseMapper<SelectionRecord> {

  @Select(
      """
      <script>
      select s.id,
             s.courseid as courseId,
             c.name as courseName,
             c.score as courseCredit,
             c.dept as courseDept,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.graded as graded,
             s.score,
             s.createtime as createTime,
             c.max_students as maxStudents,
             c.time_slot as timeSlot
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
             c.score as courseCredit,
             c.dept as courseDept,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.graded as graded,
             s.score,
             s.createtime as createTime,
             c.max_students as maxStudents,
             c.time_slot as timeSlot
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
             c.score as courseCredit,
             c.dept as courseDept,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.graded as graded,
             s.score,
             s.createtime as createTime,
             c.max_students as maxStudents,
             c.time_slot as timeSlot
      from tb_sct s
      left join tb_course c on c.id = s.courseid
      left join tb_student st on st.id = s.studentId
      left join tb_teacher t on t.id = s.teaid
      where s.courseid = #{courseId} and s.studentId = #{studentId}
      """)
  SelectionRecord selectByCourseAndStudent(
      @Param("courseId") String courseId, @Param("studentId") String studentId);

  @Select(
      """
      <script>
      select count(*)
      from tb_sct
      where courseid = #{courseId}
      <if test="excludeId != null and excludeId != ''">
        and id != #{excludeId}
      </if>
      FOR UPDATE
      </script>
      """)
  long countByCourse(@Param("courseId") String courseId, @Param("excludeId") String excludeId);

  /**
   * 统计课程已选人数（不加行锁）。
   *
   * <p>仅用于课程列表、可选判断、删除前的引用检查等只读场景；容量并发控制
   * 仍由 {@link #countByCourse} 的 {@code FOR UPDATE} 版本在事务内负责。
   */
  @Select("select count(*) from tb_sct where courseid = #{courseId}")
  long countSelectionsByCourse(@Param("courseId") String courseId);

  @Select(
      """
      <script>
      select s.id,
             s.courseid as courseId,
             c.name as courseName,
             c.score as courseCredit,
             c.dept as courseDept,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.graded as graded,
             s.score,
             s.createtime as createTime,
             c.max_students as maxStudents,
             c.time_slot as timeSlot
      from tb_sct s
      left join tb_course c on c.id = s.courseid
      left join tb_student st on st.id = s.studentId
      left join tb_teacher t on t.id = s.teaid
      where s.courseid = #{courseId}
        and s.studentId = #{studentId}
        and s.id != #{excludeId}
      limit 1
      </script>
      """)
  SelectionRecord selectByCourseAndStudentExcludingId(
      @Param("courseId") String courseId,
      @Param("studentId") String studentId,
      @Param("excludeId") String excludeId);

  @Select(
      """
      select s.id,
             s.courseid as courseId,
             c.name as courseName,
             c.score as courseCredit,
             c.dept as courseDept,
             s.studentId as studentId,
             coalesce(nullif(st.sname, ''), st.username) as studentName,
             st.numb as studentNumb,
             st.sdept as studentDept,
             st.smajor as studentMajor,
             st.sclass as studentClass,
             s.teaid as teacherId,
             coalesce(nullif(t.tname, ''), t.username) as teacherName,
             s.graded as graded,
             s.score,
             s.createtime as createTime,
             c.max_students as maxStudents,
             c.time_slot as timeSlot
      from tb_sct s
      left join tb_course c on c.id = s.courseid
      left join tb_student st on st.id = s.studentId
      left join tb_teacher t on t.id = s.teaid
      where s.courseid = #{courseId}
      order by s.createtime desc, s.id desc
      """)
  List<SelectionRecord> selectByCourseId(@Param("courseId") String courseId);

  @Update(
      """
      update tb_sct
      set teaid = #{teacherId}
      where courseid = #{courseId}
      """)
  int updateTeacherByCourseId(
      @Param("courseId") String courseId, @Param("teacherId") String teacherId);
}
