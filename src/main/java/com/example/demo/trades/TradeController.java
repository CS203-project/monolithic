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
    private MarketMaker marketMaker;

    @Autowired
    public TradeController(MarketMaker marketMaker, TradeService tradeService, AccountsRepository accRepository, StocksRepository stocksRepository, AssetRepository assetRepository) {
        this.tradeService = tradeService;
        this.accRepository = accRepository;
        this.stocksRepository = stocksRepository;
        this.assetRepository = assetRepository;
        this.marketMaker = marketMaker;
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

        String action = trade.getAction();

        // If neither buy or sell, invalid trade
        if (!action.equals("buy") && !action.equals("sell")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String stockSymbol = trade.getSymbol();
        Stock stock = getStockForTrade(stockSymbol);
        Account account = getAccountForTrade(trade.getAccount_id());

        /*
        *** Example: if you place a trade to buy a stock worth $5000 and it's still open,
        *** your available_balance should be balance - $5000. 
        */

        // Check type of order
        if (trade.getBid() == 0.0 || trade.getAsk() == 0.0) {
            // market order
            if (action.equals("buy")) {
                processMarketOrderBuy(trade, stock, account);
            } else if (action.equals("sell")) {
                processMarketOrderSell(trade, stock, account);
            }
        } else {
            // limit order
            if (action.equals("buy")) {
                processLimitOrderBuy(trade, stock, account);
            } else if (action.equals("sell")) {
                processLimitOrderSell(trade, stock, account);
            }
        }

        // proceed to trade matching?
        reflectInPortfolio(trade, stockSymbol);
        return trade; // temp
    }

    private void processLimitOrderBuy(Trade trade, Stock stock, Account account) {

        Trade openTrade = marketMaker.locateOpenTrade(stock.getSymbol(), trade.getAction(), trade.getQuantity());

        if (openTrade == null) {
            System.out.println("No open trades found.");
            return;
        }

        double price = trade.getBid();
        double ask = openTrade.getAsk();

        if (price >= ask) {
            boolean sufficient_quantity = checkQuantity(openTrade, trade);
            if (sufficient_quantity) {
                fillTrade(trade, openTrade, stock, account);
            } else {
                partialFillTrade(trade, openTrade, stock, account, 2);
            }
        }
    }

    private void processLimitOrderSell(Trade trade, Stock stock, Account account) {
        
    }

    private void processMarketOrderSell(Trade trade, Stock stock, Account account) {
        
    }

    private void processMarketOrderBuy(Trade trade, Stock stock, Account account) {
        
        Trade openTrade = marketMaker.locateOpenTrade(stock.getSymbol(), trade.getAction(), trade.getQuantity());

        if (openTrade == null) {
            System.out.println("No open trades found.");
            return;
        }

        boolean sufficient_funds = checkFunds(openTrade, trade, account);
        boolean sufficient_quantity = checkQuantity(openTrade, trade);

        if (sufficient_funds && sufficient_quantity) {
            fillTrade(trade, openTrade, stock, account);
            System.out.println("Trade filled");
            return;
        }

        if (!sufficient_funds && sufficient_quantity) {
            partialFillTrade(trade, openTrade, stock, account, 1);
            System.out.println("Trade partially filled");
            return;
        }

        if (sufficient_funds && !sufficient_quantity) {
            partialFillTrade(trade, openTrade, stock, account, 2);
            System.out.println("Trade partially filled");
            return;
        }

        // else 
        System.out.println("Trade unable to be filled / matched.");
    }

    private void partialFillTrade(Trade trade, Trade openTrade, Stock stock, Account account, int status) {
        trade.setStatus("partial-filled");
        trade.setDate(Instant.now());
        
        
        int insufficient_funds = 1;
        if (status == insufficient_funds) {
            // insufficient funds
            int qtyToFill = (int)(account.getBalance() / openTrade.getAsk());
            trade.setFilled_quantity(qtyToFill);
        } else {
            // quantity not matched
            trade.setFilled_quantity(openTrade.getQuantity());
            trade.setQuantity(trade.getQuantity() - trade.getFilled_quantity());
        }

        trade.setAvg_price(openTrade.getAsk());
    
        // Deduct from account balance
        account.updateBalance(-(trade.getFilled_quantity() * trade.getAvg_price()));

        // Reflect in openTrade / stock
        openTrade.setQuantity(openTrade.getQuantity() - trade.getFilled_quantity());
        stock.setLast_price(trade.getAvg_price());
        stock.setAsk_volume(stock.getAsk_volume() - trade.getFilled_quantity());
    }

    private void fillTrade(Trade trade, Trade openTrade, Stock stock, Account account) {
        // Fill trade
        trade.setStatus("filled");
        trade.setDate(Instant.now());
        trade.setFilled_quantity(trade.getQuantity());
        trade.setAvg_price(openTrade.getAsk());

        // Deduct from account balance
        account.updateBalance(-(trade.getFilled_quantity() * trade.getAvg_price()));
        
        // Reflect in openTrade / stock
        openTrade.setQuantity(openTrade.getQuantity() - trade.getFilled_quantity());
        stock.setLast_price(trade.getAvg_price());
        stock.setAsk_volume(stock.getAsk_volume() - trade.getFilled_quantity());
    }

    // Helper function
    private boolean checkFunds(Trade openTrade, Trade customerTrade, Account account) {

        double available_balance = account.getAvailable_balance();
        double openTradePrice = openTrade.getAsk();
        int tradeQuantity = customerTrade.getQuantity();

        if (available_balance < (openTradePrice * tradeQuantity)) return false;
        return true;
    }

    // Helper function
    private boolean checkQuantity(Trade openTrade, Trade customerTrade) {

        int openQuantity = openTrade.getQuantity();
        int tradeQuantity = customerTrade.getQuantity();

        if (openQuantity < tradeQuantity) return false;
        return true;
    }

    private void reflectInPortfolio(Trade trade, String stockSymbol) {
        Asset asset = new Asset(stockSymbol, trade.getFilled_quantity(), trade.getAvg_price());
        addAsset(asset);
    }

    // Helper function
    private Asset addAsset(Asset asset) {
        return assetRepository.save(asset);
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

}