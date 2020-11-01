package com.example.demo.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.example.demo.config.ForbiddenException;
import com.example.demo.config.BadRequestException;
import com.example.demo.config.NotFoundException;

@Service
public class UserService {
  private UserRepository UR;
  
  @Autowired
  public UserService(UserRepository UR) {
    this.UR = UR;
  }
  
  public Iterable<User> findAll() { return this.UR.findAll(); }
  public User save(User user) { return this.UR.save(user); }
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

  public User getUser(int id, User user, boolean isManager) throws ForbiddenException, NotFoundException {
    if ((user.getId() != id) && !isManager) throw new ForbiddenException("Unauthorized to view another user");
    Optional<User> search = this.UR.findById(id);
    if (!search.isPresent()) throw new NotFoundException("Searched User does not exists");
    return search.get();
  }

  public User createUser(User user) throws BadRequestException {
    Optional<User> search = this.UR.findByUsername(user.getUsername());
    if (search.isPresent()) throw new BadRequestException("User already exists");
    String passwordHash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
    user.setPassword(passwordHash);
    return this.UR.save(user);
  }
  
  public User editUser(User edits, int id, User user, boolean isManager) throws NotFoundException, ForbiddenException {
    Optional<User> search = this.UR.findById(id);
    if (!search.isPresent()) throw new NotFoundException("Invalid Id: " + id);
    if (!isManager && (id != user.getId())) throw new ForbiddenException("User cannot edit another user");
    User searchUser = search.get();

    String passwordHash = (edits.getPassword() != null) ? BCrypt.hashpw(edits.getPassword(), BCrypt.gensalt(12)) : null;
    if (passwordHash != null) searchUser.setPassword(passwordHash);
    if (edits.getAddress() != null) searchUser.setAddress(edits.getAddress());
    if (edits.getPhone() != null) searchUser.setPhone(edits.getPhone());
    if (!isManager) return this.UR.save(searchUser);

    if (edits.getActive() != null) searchUser.setActive(edits.getActive());
    if (edits.getFullName() != null) searchUser.setFullName(edits.getFullName());
    if (edits.getNric() != null) searchUser.setNric(edits.getNric());
    if (edits.getUsername() != null) searchUser.setUsername(edits.getUsername());
    if (edits.getStrAuthorities() != null) searchUser.setAuthorities(edits.getStrAuthorities());
    return this.UR.save(searchUser);
  }
}