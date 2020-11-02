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
import com.example.demo.portfolio.AssetRepository;
import com.example.demo.portfolio.Asset;

import java.util.Optional;
import java.util.List;

@RestController
public class TradeController {

    private TradeService tradeService;
    private AccountsRepository accRepository;
    private StocksRepository stocksRepository;
    private AssetRepository assetRepository;

    @Autowired
    public TradeController(TradeService tradeService, AccountsRepository accRepository, StocksRepository stocksRepository, AssetRepository assetRepository) {
        this.tradeService = tradeService;
        this.accRepository = accRepository;
        this.stocksRepository = stocksRepository;
        this.assetRepository = assetRepository;
    }

    @PostMapping("/trades")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Trade createTrade(@RequestBody Trade trade) {

        // Authentication
        User currentUser;
        AuthorizedUser context = new AuthorizedUser();
        currentUser = context.getUser();

        // Unauthorized
        // if (currentUser.getId() != trade.getCustomer_id()) {
        //     throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        // }

        // If neither buy or sell, invalid trade
        if (!trade.getAction().equals("buy") && !trade.getAction().equals("sell")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String stockSymbol = trade.getSymbol();
        Stock stock = getStockForTrade(stockSymbol);
        Account account = getAccountForTrade(trade.getAccount_id());

        // Check type of order
        if (trade.getBid() == 0.0 || trade.getAsk() == 0.0) {
            processMarketOrder(trade, stock, account);
        } else {
            processLimitOrder(trade, stock, account);
        }

        // proceed to trade matching?
        return trade; // temp
    }

    private void processMarketOrder(Trade trade, Stock stock, Account account) {

        String action = trade.getAction();

        if (action.equals("buy")) {

            double available_balance = account.getAvailable_balance();
            double stockPrice = stock.getAsk();
            int tradeQuantity = trade.getQuantity();
            if (!verifyPurchaseAbility(available_balance, stockPrice, tradeQuantity)) {
                System.out.println("Insufficient funds for purchase!");
                return;
            }

            // trade matching
            
        } else if (action.equals("sell")) {

        }

        return;
    }

    private void processLimitOrder(Trade trade, Stock stock, Account account) {
        // remember to process buy/sell as well
    }

    private void reflectInPortfolio(Trade trade) {

    }

    // Helper function
    private boolean verifyPurchaseAbility(double balance, double price, int quantity) {
        if (balance < (price * quantity)) return false;
        return true;
    }

    // Helper function
    private Stock getStockForTrade(String stockSymbol) {
        Optional<Stock> stockEntity = stocksRepository.findBySymbol(stockSymbol);
        Stock stock;
        if (!stockEntity.isPresent()) {
            throw new StockNotFoundException(stockSymbol);
        } else {
            stock = stockEntity.get();
        }

        return stock;
    }

    // Helper function
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