package com.example.demo.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.*;
import java.lang.IllegalArgumentException;

import com.example.demo.user.UserRepository;
import com.example.demo.config.ForbiddenException;
import com.example.demo.config.BadRequestException;
import com.example.demo.security.AuthorizedUser;

@RestController
public class UserController {
  private UserService US;

  @Autowired
  public UserController(UserService US) {
    this.US = US;
  }

  // Test Route
  @RequestMapping(value = "/all", method = RequestMethod.GET)
  public @ResponseBody Iterable<User> getAllUsers() {
    return US.findAll();
  }

  /*
  * POST /customers
  * Authentication    MANAGER ONLY
  * @RequestBody      User Object to be Created
  * @ResponseBody     User Object Created
  * @ResponseStatus   201 CREATED
  * DETAILS           Create User if not exists, throw 400 BAD REQUEST if exists
  */
  @RequestMapping(value = "/customers", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public @ResponseBody User addNewUser (@Valid @RequestBody User user) throws BadRequestException {
    System.out.println("POST /customers | " + user);
    User search = this.US.findByUsername(user.getUsername());
    if (search != null) throw new BadRequestException("User already exists");

    String passwordHash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
    user.setPassword(passwordHash);
    return this.US.save(user);
  }

  /*
  * GET /customers/{id}
  * Authentication    MANAGER view anyone, USER can view themselves
  * @PathVariable     ID of user searched
  * @ResponseBody     User Object Found
  * @ResponseStatus   200 OK
  * DETAILS           Return user if exists, unauthorized (403 FORBIDDEN), invalid ID (400 BAD REQUEST)
  */
  @RequestMapping(value = "/customers/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public @ResponseBody User getUser (@PathVariable int id) throws ForbiddenException, BadRequestException {
    System.out.println("GET /customers | " + id);
    AuthorizedUser context = new AuthorizedUser();

    if (!context.isManager() && (id != context.getUser().getId().intValue())) throw new ForbiddenException("Unauthorized to view another user");
  
    User search = this.US.findById(id);
    if (search == null) throw new BadRequestException("Searched User does not exists");
    return search;
  }

  /*
  * PUT /customers
  * Authentication    MANAGER edit anyone, USER & ANALYST can edit themselves (only address, password, phone)
  * @RequestBody      User Details to be edited
  * @ResponseBody     User Object Edited
  * @ResponseStatus   200 OK
  * DETAILS           Return user if edited, unauthorized (403 FORBIDDEN), invalid edited user (400 BAD REQUEST)
  */
  @RequestMapping(value = "/customers", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  public @ResponseBody User editUser (@Valid @RequestBody User user) throws BadRequestException, ForbiddenException {
    System.out.println("PUT /customers | " + user);
    AuthorizedUser context = new AuthorizedUser();

    if (user.getId() == null && user.getUsername() == null) throw new BadRequestException("Edited User's ID not provided");
    User search = (user.getId() != null) ? this.US.findById(user.getId()) : this.US.findByUsername(user.getUsername());
    if (search == null) throw new BadRequestException("Edited User does not exists");
    
    if (context.isManager()) return this.US.save(user);
    if (search.getId() != context.getUser().getId()) throw new ForbiddenException("Unauthorized to edit another user");
    String passwordHash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
    search.setAddress(user.getAddress());
    search.setPassword(passwordHash);
    search.setPhone(user.getPhone());
    return this.US.save(search);
  }
}