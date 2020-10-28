package com.example.demo.accounts;

import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountsController {
    @Autowired
    private AccountsRepository accRepository;

    public AccountsController() {}
}