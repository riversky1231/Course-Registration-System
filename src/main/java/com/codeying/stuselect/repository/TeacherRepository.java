package com.codeying.stuselect.repository;

import com.codeying.stuselect.model.Teacher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TeacherRepository extends BaseRepository {

  private final RowMapper<Teacher> mapper =
      (rs, rowNum) -> {
        Teacher teacher = new Teacher();
        teacher.setId(rs.getString("id"));
        teacher.setUsername(rs.getString("username"));
        teacher.setPassword(rs.getString("password"));
        teacher.setNumb(rs.getString("numb"));
        teacher.setTname(rs.getString("tname"));
        Timestamp birthday = rs.getTimestamp("tbirthday");
        teacher.setTbirthday(birthday == null ? null : birthday.toLocalDateTime());
        teacher.setTposition(rs.getString("tposition"));
        teacher.setTtel(rs.getString("ttel"));
        teacher.setAge(rs.getObject("age", Integer.class));
        teacher.setGender(rs.getString("gender"));
        return teacher;
      };

  public TeacherRepository(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public List<Teacher> findAll(String keyword) {
    StringBuilder sql =
        new StringBuilder(
            "select id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender from tb_teacher where 1=1");
    List<Object> args = new ArrayList<>();
    if (StringUtils.hasText(keyword)) {
      String like = "%" + keyword.trim() + "%";
      sql.append(
          " and (username like ? or ifnull(numb, '') like ? or ifnull(tname, '') like ? or ifnull(ttel, '') like ? or ifnull(tposition, '') like ?)");
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
    }
    sql.append(" order by id desc");
    return jdbcTemplate.query(sql.toString(), mapper, args.toArray());
  }

  public Teacher findById(String id) {
    return queryOne(
        "select id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender from tb_teacher where id = ?",
        mapper,
        id);
  }

  public Teacher findByUsername(String username) {
    return queryOne(
        "select id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender from tb_teacher where username = ?",
        mapper,
        username);
  }

  public Teacher findByUsernameAndPassword(String username, String password) {
    return queryOne(
        "select id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender from tb_teacher where username = ? and password = ?",
        mapper,
        username,
        password);
  }

  public long countAll() {
    Long count = jdbcTemplate.queryForObject("select count(*) from tb_teacher", Long.class);
    return count == null ? 0L : count;
  }

  public int insert(Teacher teacher) {
    return jdbcTemplate.update(
        "insert into tb_teacher (id, username, password, numb, tname, tbirthday, tposition, ttel, age, gender) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        teacher.getId(),
        teacher.getUsername(),
        teacher.getPassword(),
        teacher.getNumb(),
        teacher.getTname(),
        toTimestamp(teacher.getTbirthday()),
        teacher.getTposition(),
        teacher.getTtel(),
        teacher.getAge(),
        teacher.getGender());
  }

  public int update(Teacher teacher) {
    return jdbcTemplate.update(
        "update tb_teacher set username = ?, password = ?, numb = ?, tname = ?, tbirthday = ?, tposition = ?, ttel = ?, age = ?, gender = ? where id = ?",
        teacher.getUsername(),
        teacher.getPassword(),
        teacher.getNumb(),
        teacher.getTname(),
        toTimestamp(teacher.getTbirthday()),
        teacher.getTposition(),
        teacher.getTtel(),
        teacher.getAge(),
        teacher.getGender(),
        teacher.getId());
  }

  public int deleteById(String id) {
    return jdbcTemplate.update("delete from tb_teacher where id = ?", id);
  }
}
