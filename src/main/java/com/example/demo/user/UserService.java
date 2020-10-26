package com.example.demo.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService {
  private UserRepository UR;
  
  @Autowired
  public UserService(UserRepository UR) {
    this.UR = UR;
  }

  public User save(User user) { return this.UR.save(user); }
  public Iterable<User> findAll() { return this.UR.findAll(); }
  public User findById(int id) {
    Optional<User> search = this.UR.findById(id);
    if (!search.isPresent()) return null;
    return search.get();
  }
  public User findByUsername(String username) {
    Optional<User> search = this.UR.findByUsername(username);
    if (!search.isPresent()) return null;
    return search.get();
  }
}