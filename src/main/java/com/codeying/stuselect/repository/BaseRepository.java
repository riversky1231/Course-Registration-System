package com.codeying.stuselect.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public abstract class BaseRepository {

  protected final JdbcTemplate jdbcTemplate;

  protected BaseRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... args) {
    try {
      return jdbcTemplate.queryForObject(sql, mapper, args);
    } catch (EmptyResultDataAccessException ex) {
      return null;
    }
  }

  protected Timestamp toTimestamp(LocalDateTime value) {
    return value == null ? null : Timestamp.valueOf(value);
  }
}
