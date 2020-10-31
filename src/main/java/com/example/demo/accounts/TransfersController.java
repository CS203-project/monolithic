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

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;

import com.example.demo.security.AuthorizedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.example.demo.user.User;

import org.springframework.security.access.AccessDeniedException;

@RestController
public class TransfersController {

    @Autowired
    private TransfersRepository transfersRepository;

    @Autowired
    private AccountsRepository accRepository;

    public TransfersController() {}

    User currentUser;

    public void authenticateCurrentUser() {
        AuthorizedUser context = new AuthorizedUser();
        this.currentUser = context.getUser();
    }

    // Check if account with {id} belongs to currentUser
    public boolean verifyAccountOwnership(int id) {
        authenticateCurrentUser();

        int userID = currentUser.getId();

        Optional<Account> accountEntity = accRepository.findById(id);
        if (accountEntity.isPresent()) {
            int customerID = accountEntity.get().getCustomer_id();
            if (userID == customerID) {
                return true;
            }
        }

        return false;
    }

    @GetMapping(path="/accounts/{id}/transactions")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Iterable<Transfer> getTransfers(@PathVariable int id) {
        
        if (!verifyAccountOwnership(id)) {
            throw new AccessDeniedException("403 returned");
        }

        Iterable<Transfer> transfers = transfersRepository.findAll();
        Iterator<Transfer> iter = transfers.iterator();

        while (iter.hasNext()) {
            Transfer transfer = iter.next();
            if (transfer.getFrom() != id && transfer.getTo() != id) {
                iter.remove();
            }
        }

        return transfers;
    }

    @PostMapping(path="/accounts/{id}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Transfer createTransfer(@RequestBody Transfer transfer, @PathVariable int id) {

        if (!verifyAccountOwnership(transfer.getFrom())) {
            throw new AccessDeniedException("403 returned");
        }

        int sender_account_id = transfer.getFrom();
        int receiver_account_id = transfer.getTo();

        if (id != transfer.getFrom()) {
            throw new AccessDeniedException("403 returned");
        }

        Optional<Account> sender_account = accRepository.findById(sender_account_id);
        Optional<Account> receiver_account = accRepository.findById(receiver_account_id);
        if (!sender_account.isPresent()) throw new AccountNotFoundException(sender_account_id);
        if (!receiver_account.isPresent()) throw new AccountNotFoundException(receiver_account_id);

        double transfer_amount = transfer.getAmount();
        System.out.println("PRINTTT");
        System.out.println(transfer_amount);

        Account sender = sender_account.get();
        Account receiver = receiver_account.get();

        System.out.println("PRINTTT");
        System.out.println(sender);
        System.out.println(receiver);

        if (sender.getBalance() < transfer_amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        sender.updateBalance(-transfer_amount);
        receiver.updateBalance(transfer_amount);

        return(transfersRepository.save(transfer));
    }

}