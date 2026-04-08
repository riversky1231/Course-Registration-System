package com.codeying.stuselect.common;

public class UserSession {

  private String id;
  private String username;
  private Role role;
  private String displayName;

  public UserSession() {}

  public UserSession(String id, String username, Role role, String displayName) {
    this.id = id;
    this.username = username;
    this.role = role;
    this.displayName = displayName;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
