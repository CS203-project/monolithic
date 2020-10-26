package com.example.demo.sample;

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
  public Iterable<User> findAll() { return UR.findAll(); }

}