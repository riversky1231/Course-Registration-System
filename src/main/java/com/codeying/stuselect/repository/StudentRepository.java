package com.codeying.stuselect.repository;

import com.codeying.stuselect.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StudentRepository extends BaseRepository {

  private final RowMapper<Student> mapper =
      (rs, rowNum) -> {
        Student student = new Student();
        student.setId(rs.getString("id"));
        student.setUsername(rs.getString("username"));
        student.setPassword(rs.getString("password"));
        student.setNumb(rs.getString("numb"));
        student.setSname(rs.getString("sname"));
        student.setSdept(rs.getString("sdept"));
        Timestamp birthday = rs.getTimestamp("sbirthday");
        student.setSbirthday(birthday == null ? null : birthday.toLocalDateTime());
        student.setTele(rs.getString("tele"));
        student.setSsex(rs.getString("ssex"));
        student.setAge(rs.getObject("age", Integer.class));
        student.setSmajor(rs.getString("smajor"));
        student.setSclass(rs.getString("sclass"));
        return student;
      };

  public StudentRepository(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public List<Student> findAll(String keyword) {
    StringBuilder sql =
        new StringBuilder(
            "select id, username, password, numb, sname, sdept, sbirthday, tele, ssex, age, smajor, sclass from tb_student where 1=1");
    List<Object> args = new ArrayList<>();
    if (StringUtils.hasText(keyword)) {
      String like = "%" + keyword.trim() + "%";
      sql.append(
          " and (username like ? or ifnull(numb, '') like ? or ifnull(sname, '') like ? or ifnull(sdept, '') like ? or ifnull(smajor, '') like ? or ifnull(sclass, '') like ?)");
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
      args.add(like);
    }
    sql.append(" order by id desc");
    return jdbcTemplate.query(sql.toString(), mapper, args.toArray());
  }

  public Student findById(String id) {
    return queryOne(
        "select id, username, password, numb, sname, sdept, sbirthday, tele, ssex, age, smajor, sclass from tb_student where id = ?",
        mapper,
        id);
  }

  public Student findByUsername(String username) {
    return queryOne(
        "select id, username, password, numb, sname, sdept, sbirthday, tele, ssex, age, smajor, sclass from tb_student where username = ?",
        mapper,
        username);
  }

  public Student findByUsernameAndPassword(String username, String password) {
    return queryOne(
        "select id, username, password, numb, sname, sdept, sbirthday, tele, ssex, age, smajor, sclass from tb_student where username = ? and password = ?",
        mapper,
        username,
        password);
  }

  public long countAll() {
    Long count = jdbcTemplate.queryForObject("select count(*) from tb_student", Long.class);
    return count == null ? 0L : count;
  }

  public int insert(Student student) {
    return jdbcTemplate.update(
        "insert into tb_student (id, username, password, numb, sname, sdept, sbirthday, tele, ssex, age, smajor, sclass) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        student.getId(),
        student.getUsername(),
        student.getPassword(),
        student.getNumb(),
        student.getSname(),
        student.getSdept(),
        toTimestamp(student.getSbirthday()),
        student.getTele(),
        student.getSsex(),
        student.getAge(),
        student.getSmajor(),
        student.getSclass());
  }

  public int update(Student student) {
    return jdbcTemplate.update(
        "update tb_student set username = ?, password = ?, numb = ?, sname = ?, sdept = ?, sbirthday = ?, tele = ?, ssex = ?, age = ?, smajor = ?, sclass = ? where id = ?",
        student.getUsername(),
        student.getPassword(),
        student.getNumb(),
        student.getSname(),
        student.getSdept(),
        toTimestamp(student.getSbirthday()),
        student.getTele(),
        student.getSsex(),
        student.getAge(),
        student.getSmajor(),
        student.getSclass(),
        student.getId());
  }

  public int deleteById(String id) {
    return jdbcTemplate.update("delete from tb_student where id = ?", id);
  }
}
