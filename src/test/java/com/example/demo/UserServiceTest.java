package com.example.demo;

import com.example.demo.sample.User;
import com.example.demo.sample.UserRepository;
import com.example.demo.sample.UserService;

import static org.mockito.Mockito.*;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  @Mock
  private UserRepository users;
  @InjectMocks
  private UserService userService;

  @Test
  public void addUser_NewUser_ReturnSavedUser()  {
    User user  = new User(900,"fullName","S9804803G","+6591234567","Address","manager_1","01_manager_01","ROLE_MANAGER",true);

    // mock Repository Operations
    // when(users.findAll())
    //   .thenReturn(new Iterable<User>()); // cannot instantiate abstract class
    when(users.save(any(User.class)))
      .thenReturn(user);
    
    User savedUser = userService.save(user);
    Assert.assertEquals(savedUser, user);

    // verify
    // verify(users).findAll();
    verify(users).save(user);
  }
}
