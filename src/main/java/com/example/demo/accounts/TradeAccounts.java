package com.example.demo.accounts;

import org.springframework.beans.factory.annotation.Autowired; 

public class TradeAccounts {

    @Autowired
    private AccountsController accController;

    public TradeAccounts() {}

    // Return account requested with ID
    public Account returnAccountWithID(int id) {
        return accController.getAccountById(id);
    }

    // Verify account belongs to customer
    public boolean verifyAccountOwnership(int customer_id, int account_id) {
        Account account = accController.getAccountById(account_id);
        if (account.getCustomer_id() != customer_id) return false;
        return true;
    }

}