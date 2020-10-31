// package com.example.demo.accounts;

// import org.springframework.beans.factory.annotation.Autowired; 

// import java.nio.file.AccessDeniedException;

// public class TradeAccounts {

//     @Autowired
//     private AccountsController accController;

//     public TradeAccounts() {}

//     // Return account requested with ID
//     public Account returnAccountWithID(int id) {
//         try {
//             return accController.getAccountById(id);
//         } catch (AccessDeniedException e) {
//             return null;
//         }
        
//     }

//     // Verify account belongs to customer
//     public boolean verifyAccountOwnership(int customer_id, int account_id) {
//         try {
//             Account account = accController.getAccountById(account_id);
//             if (account.getCustomer_id() != customer_id) return false;
//             return true;
//         } catch (AccessDeniedException e) {
//             return false;
//         }
//     }

// }