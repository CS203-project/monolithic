package com.example.demo.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.example.demo.user.User;

public class AuthorizedUser {
  private User user;
  public AuthorizedUser() {
    this.user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
  public User getUser() { return this.user; }
  public boolean isManager() {
    boolean isManager = false;
    for (GrantedAuthority authority : this.user.getAuthorities()) {
      if (authority.getAuthority().equals("ROLE_MANAGER")) {
        isManager = true;
        break;
      }
    }
    return isManager;
  }
}