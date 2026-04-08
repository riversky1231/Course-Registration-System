package com.codeying.stuselect.repository;

import com.codeying.stuselect.model.SelectionRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SelectionRepository extends BaseRepository {

  private final RowMapper<SelectionRecord> mapper =
      (rs, rowNum) -> {
        SelectionRecord record = new SelectionRecord();
        record.setId(rs.getString("id"));
        record.setCourseId(rs.getString("courseid"));
        record.setCourseName(rs.getString("course_name"));
        record.setStudentId(rs.getString("studentId"));
        record.setStudentName(rs.getString("student_name"));
        record.setTeacherId(rs.getString("teaid"));
        record.setTeacherName(rs.getString("teacher_name"));
        record.setScore(rs.getObject("score", Double.class));
        Timestamp createTime = rs.getTimestamp("createtime");
        record.setCreateTime(createTime == null ? null : createTime.toLocalDateTime());
        return record;
      };

  public SelectionRepository(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public List<SelectionRecord> findAll(String keyword, String studentId, String teacherId) {
    StringBuilder sql =
        new StringBuilder(
            """
            select s.id, s.courseid, s.studentId, s.teaid, s.score, s.createtime,
                   c.name as course_name,
                   coalesce(nullif(st.sname, ''), st.username) as student_name,
                   coalesce(nullif(t.tname, ''), t.username) as teacher_name
            from tb_sct s
            left join tb_course c on c.id = s.courseid
            left join tb_student st on st.id = s.studentId
            left join tb_teacher t on t.id = s.teaid
            where 1=1
            """);
    List<Object> args = new ArrayList<>();
    if (StringUtils.hasText(studentId)) {
      sql.append(" and s.studentId = ?");
      args.add(studentId);
    }
    if (StringUtils.hasText(teacherId)) {
      sql.append(" and s.teaid = ?");
      args.add(teacherId);
    }
    if (StringUtils.hasText(keyword)) {
      String like = "%" + keyword.trim() + "%";
      sql.append(
          " and (ifnull(c.name, '') like ? or ifnull(st.sname, '') like ? or ifnull(t.tname, '') like ?)");
      args.add(like);
      args.add(like);
      args.add(like);
    }
    sql.append(" order by s.createtime desc, s.id desc");
    return jdbcTemplate.query(sql.toString(), mapper, args.toArray());
  }

  public SelectionRecord findById(String id) {
    return queryOne(
        """
        select s.id, s.courseid, s.studentId, s.teaid, s.score, s.createtime,
               c.name as course_name,
               coalesce(nullif(st.sname, ''), st.username) as student_name,
               coalesce(nullif(t.tname, ''), t.username) as teacher_name
        from tb_sct s
        left join tb_course c on c.id = s.courseid
        left join tb_student st on st.id = s.studentId
        left join tb_teacher t on t.id = s.teaid
        where s.id = ?
        """,
        mapper,
        id);
  }

  public SelectionRecord findByCourseAndStudent(String courseId, String studentId) {
    return queryOne(
        """
        select s.id, s.courseid, s.studentId, s.teaid, s.score, s.createtime,
               c.name as course_name,
               coalesce(nullif(st.sname, ''), st.username) as student_name,
               coalesce(nullif(t.tname, ''), t.username) as teacher_name
        from tb_sct s
        left join tb_course c on c.id = s.courseid
        left join tb_student st on st.id = s.studentId
        left join tb_teacher t on t.id = s.teaid
        where s.courseid = ? and s.studentId = ?
        """,
        mapper,
        courseId,
        studentId);
  }

  public long countAll(String studentId, String teacherId) {
    StringBuilder sql = new StringBuilder("select count(*) from tb_sct where 1=1");
    List<Object> args = new ArrayList<>();
    if (StringUtils.hasText(studentId)) {
      sql.append(" and studentId = ?");
      args.add(studentId);
    }
    if (StringUtils.hasText(teacherId)) {
      sql.append(" and teaid = ?");
      args.add(teacherId);
    }
    Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, args.toArray());
    return count == null ? 0L : count;
  }

  public int insert(
      String id,
      String courseId,
      String studentId,
      String teacherId,
      Double score,
      LocalDateTime createTime) {
    return jdbcTemplate.update(
        "insert into tb_sct (id, courseid, studentId, teaid, score, createtime) values (?, ?, ?, ?, ?, ?)",
        id,
        courseId,
        studentId,
        teacherId,
        score,
        toTimestamp(createTime));
  }

  public int updateScore(String id, Double score) {
    return jdbcTemplate.update("update tb_sct set score = ? where id = ?", score, id);
  }

  public int update(String id, String courseId, String studentId, String teacherId, Double score) {
    return jdbcTemplate.update(
        "update tb_sct set courseid = ?, studentId = ?, teaid = ?, score = ? where id = ?",
        courseId,
        studentId,
        teacherId,
        score,
        id);
  }

  public int deleteById(String id) {
    return jdbcTemplate.update("delete from tb_sct where id = ?", id);
  }
}
