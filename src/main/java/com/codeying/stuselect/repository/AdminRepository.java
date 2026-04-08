package com.codeying.stuselect.repository;

import com.codeying.stuselect.model.Admin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AdminRepository extends BaseRepository {

  private final RowMapper<Admin> mapper =
      (rs, rowNum) -> {
        Admin admin = new Admin();
        admin.setId(rs.getString("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setName(rs.getString("name"));
        admin.setTele(rs.getString("tele"));
        return admin;
      };

  public AdminRepository(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  public List<Admin> findAll(String keyword) {
    StringBuilder sql =
        new StringBuilder("select id, username, password, name, tele from tb_admin where 1=1");
    List<Object> args = new ArrayList<>();
    if (StringUtils.hasText(keyword)) {
      String like = "%" + keyword.trim() + "%";
      sql.append(" and (username like ? or ifnull(name, '') like ? or ifnull(tele, '') like ?)");
      args.add(like);
      args.add(like);
      args.add(like);
    }
    sql.append(" order by username asc");
    return jdbcTemplate.query(sql.toString(), mapper, args.toArray());
  }

  public Admin findById(String id) {
    return queryOne("select id, username, password, name, tele from tb_admin where id = ?", mapper, id);
  }

  public Admin findByUsername(String username) {
    return queryOne(
        "select id, username, password, name, tele from tb_admin where username = ?",
        mapper,
        username);
  }

  public Admin findByUsernameAndPassword(String username, String password) {
    return queryOne(
        "select id, username, password, name, tele from tb_admin where username = ? and password = ?",
        mapper,
        username,
        password);
  }

  public long countAll() {
    Long count = jdbcTemplate.queryForObject("select count(*) from tb_admin", Long.class);
    return count == null ? 0L : count;
  }

  public int insert(Admin admin) {
    return jdbcTemplate.update(
        "insert into tb_admin (id, username, password, name, tele) values (?, ?, ?, ?, ?)",
        admin.getId(),
        admin.getUsername(),
        admin.getPassword(),
        admin.getName(),
        admin.getTele());
  }

  public int update(Admin admin) {
    return jdbcTemplate.update(
        "update tb_admin set username = ?, password = ?, name = ?, tele = ? where id = ?",
        admin.getUsername(),
        admin.getPassword(),
        admin.getName(),
        admin.getTele(),
        admin.getId());
  }

  public int deleteById(String id) {
    return jdbcTemplate.update("delete from tb_admin where id = ?", id);
  }
}
