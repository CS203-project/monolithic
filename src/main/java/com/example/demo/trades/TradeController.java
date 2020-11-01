package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.security.AuthorizedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.example.demo.user.User;

import java.time.Instant;
import com.example.demo.accounts.AccountsRepository;
import com.example.demo.accounts.AccountNotFoundException;
import com.example.demo.accounts.Account;

import java.util.Optional;
import java.util.List;

@RestController
public class TradeController {
    
    private TradeService tradeService;
    private AccountsRepository accRepository;

    // either Stocks repository or a list of stocks available to trade

    @Autowired
    public TradeController(TradeService tradeService, AccountsRepository accRepository) {
        this.tradeService = tradeService;
        this.accRepository = accRepository;
    }

    private Account getAccountForTrade(int account_id) {
        Optional<Account> accountEntity = accRepository.findById(account_id);
        Account account;
        if (!accountEntity.isPresent()) {
            throw new AccountNotFoundException(account_id);
        } else {
            account = accountEntity.get();
        }

        return account;
    }

    @PostMapping("/trades")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Trade createTrade(@RequestBody Trade trade) {
        User currentUser;
        AuthorizedUser context = new AuthorizedUser();
        currentUser = context.getUser();

        if (!trade.getAction().equals("buy") && !trade.getAction().equals("sell")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Market order but insufficient volume
        // 1) GET STOCK BY SYMBOL
        if ((trade.getBid() == 0.0 || trade.getAsk() == 0.0) && false) {
            // () && stock.getAsk_volume() < trade.getQuantity()
            trade.setStatus("partial-filled");
            trade.setDate(Instant.now());
            // 2) trade.setAvg_price(stock.getPrice)
        }

        // Market orders - filled immediately - extract to method fillMarketOrder
        // 1) GET STOCK BY SYMBOL
        if (trade.getBid() == 0.0 || trade.getAsk() == 0.0) {
            trade.setStatus("filled");
            trade.setDate(Instant.now());
            trade.setFilled_quantity(trade.getQuantity());
            // 2) trade.setAvg_price(stock.getPrice)
            
            int account_id = trade.getAccount_id();
            int customer_id = trade.getCustomer_id();

            Account account = getAccountForTrade(account_id);
            if (account.getCustomer_id() != customer_id) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            account.updateBalance(-(trade.getAvg_price() * trade.getFilled_quantity()));

            // REFLECT TO PORTFOLIO
        }


        
        return null;
    }

    @GetMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade getTradeByID(@PathVariable int id) {
        return null;
    }

    @PutMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade cancelTrade(@PathVariable int id, @RequestBody Trade trade) {
        return null;
    }

}