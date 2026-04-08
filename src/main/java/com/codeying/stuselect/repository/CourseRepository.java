package com.codeying.stuselect.repository;

import com.codeying.stuselect.model.Course;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CourseRepository extends BaseRepository {

  private final RowMapper<Course> mapper =
      (rs, rowNum) -> {
        Course course = new Course();
        course.setId(rs.getString("id"));
        course.setName(rs.getString("name"));
        course.setScore(rs.getObject("score", Double.class));
        course.setNumb(rs.getString("numb"));
        course.setTid(rs.getString("tid"));
        course.setTeacherName(rs.getString("teacher_name"));
        course.setJianjie(rs.getString("jianjie"));
        return course;
      };

  public CourseRepository(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public List<Course> findAll(String keyword, String teacherId) {
    StringBuilder sql =
        new StringBuilder(
            """
            select c.id, c.name, c.score, c.numb, c.tid, c.jianjie,
                   coalesce(nullif(t.tname, ''), t.username) as teacher_name
            from tb_course c
            left join tb_teacher t on t.id = c.tid
            where 1=1
            """);
    List<Object> args = new ArrayList<>();
    if (StringUtils.hasText(teacherId)) {
      sql.append(" and c.tid = ?");
      args.add(teacherId);
    }
    if (StringUtils.hasText(keyword)) {
      String like = "%" + keyword.trim() + "%";
      sql.append(
          " and (c.name like ? or ifnull(c.numb, '') like ? or ifnull(c.jianjie, '') like ? or ifnull(t.tname, '') like ?)");
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
    }
    sql.append(" order by c.id desc");
    return jdbcTemplate.query(sql.toString(), mapper, args.toArray());
  }

  public Course findById(String id) {
    return queryOne(
        """
        select c.id, c.name, c.score, c.numb, c.tid, c.jianjie,
               coalesce(nullif(t.tname, ''), t.username) as teacher_name
        from tb_course c
        left join tb_teacher t on t.id = c.tid
        where c.id = ?
        """,
        mapper,
        id);
  }

  public Course findByNumb(String numb) {
    return queryOne(
        """
        select c.id, c.name, c.score, c.numb, c.tid, c.jianjie,
               coalesce(nullif(t.tname, ''), t.username) as teacher_name
        from tb_course c
        left join tb_teacher t on t.id = c.tid
        where c.numb = ?
        """,
        mapper,
        numb);
  }

  public long countAll(String teacherId) {
    Long count =
        jdbcTemplate.queryForObject(
            "select count(*) from tb_course where (? is null or tid = ?)",
            Long.class,
            teacherId,
            teacherId);
    return count == null ? 0L : count;
  }

  public int insert(Course course) {
    return jdbcTemplate.update(
        "insert into tb_course (id, name, score, numb, tid, jianjie) values (?, ?, ?, ?, ?, ?)",
        course.getId(),
        course.getName(),
        course.getScore(),
        course.getNumb(),
        course.getTid(),
        course.getJianjie());
  }

  public int update(Course course) {
    return jdbcTemplate.update(
        "update tb_course set name = ?, score = ?, numb = ?, tid = ?, jianjie = ? where id = ?",
        course.getName(),
        course.getScore(),
        course.getNumb(),
        course.getTid(),
        course.getJianjie(),
        course.getId());
  }

  public int deleteById(String id) {
    return jdbcTemplate.update("delete from tb_course where id = ?", id);
  }
}
