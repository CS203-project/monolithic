package com.example.demo.accounts;

import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import com.example.demo.security.AuthorizedUser;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.example.demo.user.User;

@RestController
public class AccountsController {
    @Autowired
    private AccountsRepository accRepository;

    User user = new User();
    public AccountsController() {
        // this.user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping(path="/accounts")
    public @ResponseBody String addAccount (@RequestBody Account account) {
        return "";
    }
}