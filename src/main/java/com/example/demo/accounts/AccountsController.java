package com.example.demo.accounts;

import java.util.Iterator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PathVariable;
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

    public AccountsController() {}

    User currentUser;
    
    public void authenticateCurrentUser() {
        AuthorizedUser context = new AuthorizedUser();
        this.currentUser = context.getUser();
    }

    @PostMapping(path="/accounts")
    public @ResponseBody String addAccount (@RequestBody Account account) {
        authenticateCurrentUser();

        accRepository.save(account);

        return "Account saved!\n" + account.toString();
    }

    @GetMapping(path="/accounts")
    public @ResponseBody Iterable<Account> getAccounts() {
        authenticateCurrentUser();

        int userID = currentUser.getId();
        
        Iterable<Account> accounts = accRepository.findAll();
        Iterator<Account> iter = accounts.iterator();

        while(iter.hasNext()) {
            Account acc = iter.next();
            if(acc.getCustomer_id() != userID) {
                iter.remove();
            }
        }
        return accounts;
    }

    @GetMapping(path="/accounts/{id}")
    public @ResponseBody Account getAccountById(@PathVariable int id) {
        authenticateCurrentUser();

        int userID = currentUser.getId();

        Optional<Account> accountEntity = accRepository.findById(id);
        Account account;
        if (!accountEntity.isPresent()) {
            throw new AccountNotFoundException(id);
        } else {
            account = accountEntity.get();
            if (account.getCustomer_id() != userID) {
                System.out.println("No account of this ID associated with this user.");
                throw new AccountNotFoundException(id);
            }
        }

        return account;
    }
}