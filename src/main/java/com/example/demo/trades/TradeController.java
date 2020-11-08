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
import com.example.demo.config.*;
import com.example.demo.portfolio.AssetRepository;
import com.example.demo.portfolio.Portfolio;
import com.example.demo.portfolio.PortfolioRepository;
import com.example.demo.portfolio.Asset;

import com.example.demo.config.*;

import java.util.Optional;
import java.util.List;
import java.util.Iterator;

@RestController
public class TradeController {
    private TradeService tradeService;
    private AccountsRepository accRepository;
    private StocksRepository stocksRepository;
    private AssetRepository assetRepository;
    private PortfolioRepository pfRepository;
    private MarketMaker marketMaker;

    @Autowired
    public TradeController(MarketMaker marketMaker, TradeService tradeService, AccountsRepository accRepository, StocksRepository stocksRepository, AssetRepository assetRepository) {
        this.tradeService = tradeService;
        this.accRepository = accRepository;
        this.stocksRepository = stocksRepository;
        this.assetRepository = assetRepository;
        this.marketMaker = marketMaker;
    }

    @PostMapping("/trades")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Trade createTrade(@RequestBody Trade trade) throws UnauthorizedException, NotFoundException {
        // Authentication
        User currentUser;
        AuthorizedUser context = new AuthorizedUser();
        currentUser = context.getUser();

        // Prevents user from creating trade for another customer
        verifyTradeOwnership(trade, currentUser.getId());

        // If neither buy or sell, invalid trade
        String action = trade.getAction();
        if (!action.equals("buy") && !action.equals("sell")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // String stockSymbol = trade.getSymbol();
        // Stock stock = getStockForTrade(stockSymbol);
        // Account account = getAccountForTrade(trade.getAccount_id());

        return trade;
    }

    @GetMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade getTradeByID(@PathVariable int id) throws UnauthorizedException {
        // Authentication
        User currentUser;
        AuthorizedUser context = new AuthorizedUser();
        currentUser = context.getUser();

        Trade trade = tradeService.getTrade(id);
        verifyTradeOwnership(trade, currentUser.getId());

        return trade;
    }

    @PutMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade cancelTrade(@PathVariable int id) throws UnauthorizedException {
        // Authentication
        User currentUser;
        AuthorizedUser context = new AuthorizedUser();
        currentUser = context.getUser();
        Trade trade = tradeService.getTrade(id);

        verifyTradeOwnership(trade, currentUser.getId());

        trade.setStatus("cancelled");
        
        // Reflect status in database
        tradeService.addTrade(trade);
        return trade;
    }

    // Helper function
    // Checks if the trade belongs to the currentUser
    private void verifyTradeOwnership(Trade trade, int customer_id) {
        if(trade.getCustomer_id() != customer_id) {
            System.out.println("Trade of this ID is not accessible to this user");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}