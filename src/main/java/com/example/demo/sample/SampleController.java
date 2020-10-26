package com.example.demo.sample;

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
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;   

import java.util.*;
import java.lang.IllegalArgumentException;

import com.example.demo.sample.UserRepository;

@RestController
public class SampleController {
  private UserService US;

  @Autowired
  public SampleController(UserService US) {
    this.US = US;
  }

  @RequestMapping(value = "/add", method = RequestMethod.POST)
  public @ResponseBody String addNewUser (@Valid @RequestBody User user) {
    System.out.println(user);
    US.save(user);
    return "Saved";
  }

  @RequestMapping(value = "/all", method = RequestMethod.GET)
  public @ResponseBody Iterable<User> getAllUsers() {
    return US.findAll();
  }

}