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
import com.example.demo.trades.StocksRepository;
import com.example.demo.trades.Stock;
import com.example.demo.trades.StockNotFoundException;

import java.util.Optional;
import java.util.List;

@RestController
public class TradeController {
    
    private TradeService tradeService;
    private AccountsRepository accRepository;
    private StocksRepository stocksRepository;

    // either Stocks repository or a list of stocks available to trade

    @Autowired
    public TradeController(TradeService tradeService, AccountsRepository accRepository) {
        this.tradeService = tradeService;
        this.accRepository = accRepository;
    }

    private Account getAccountForTrade(int account_id) {
        Account accountEntity = accRepository.findById(account_id).orElseThrow();
        // Account account;
        // if (!accountEntity.isPresent()) {
        //     throw new AccountNotFoundException(account_id);
        // } else {
        //     account = accountEntity.get();
        // }

        // Account account = accountEntity.get();

        return accountEntity;
    }

    private Stock getStockForTrade(String stockSymbol) {
        Stock stockEntity = stocksRepository.findBySymbol(stockSymbol).orElseThrow();
        // Stock stock;
        // if (!stockEntity.isPresent()) {
        //     throw new StockNotFoundException(stockSymbol);
        // } else {
        //     stock = stockEntity.get();
        // }

        // Stock stock = stockEntity.get();

        return stockEntity;
    }

    private boolean processTransaction(Trade trade) {
        int account_id = trade.getAccount_id();
        int customer_id = trade.getCustomer_id();

        Account account = getAccountForTrade(account_id);
        if (account.getCustomer_id() != customer_id) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (account.getBalance() < trade.getAvg_price() * trade.getFilled_quantity()) {
            return false;
        }

        account.updateBalance(-(trade.getAvg_price() * trade.getFilled_quantity()));
        return true;
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

        String stockSymbol = trade.getSymbol();
        Stock stock = getStockForTrade(stockSymbol);
        
        if ((trade.getBid() == 0.0 || trade.getAsk() == 0.0) && stock.getAsk_volume() < trade.getQuantity()) {
            // Market order but insufficient volume

            trade.setStatus("partial-filled");
            trade.setDate(Instant.now());
            trade.setFilled_quantity(stock.getAsk_volume());
            trade.setAvg_price(stock.getAsk());

            if(!processTransaction(trade)) {
                // insufficient balance, partially fill
                Account acc = getAccountForTrade(trade.getAccount_id());
                int canFill = (int)(acc.getBalance() / stock.getAsk());

                if (canFill > 0) {
                    trade.setFilled_quantity(canFill);
                }
            }

            // REFLECT TO PORTFOLIO

        } else if (trade.getBid() == 0.0 || trade.getAsk() == 0.0) {
            // Market orders - filled immediately - extract to method fillMarketOrder

            trade.setStatus("filled");
            trade.setDate(Instant.now());
            trade.setFilled_quantity(trade.getQuantity());
            trade.setAvg_price(stock.getAsk());

            if(!processTransaction(trade)) {
                // insufficient balance, partially fill
                Account acc = getAccountForTrade(trade.getAccount_id());
                int canFill = (int)(acc.getBalance() / stock.getAsk());

                if (canFill > 0) {
                    trade.setFilled_quantity(canFill);
                }
            }

            // REFLECT TO PORTFOLIO
        }

        // PROCESS LIMIT ORDERS
        
        return null;
    }

    @GetMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade getTradeByID(@PathVariable int id) {
        return tradeService.getTrade(id);
    }

    @PutMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade cancelTrade(@PathVariable int id) {
        Trade trade = tradeService.getTrade(id);
        trade.setStatus("cancelled");
        return trade;
    }

}