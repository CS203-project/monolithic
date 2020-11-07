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
import com.example.demo.portfolio.Portfolio;
import com.example.demo.portfolio.PortfolioRepository;
import com.example.demo.portfolio.Asset;

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

    // Checks if the trade belongs to the currentUser
    private void verifyTradeOwnership(Trade trade, int customer_id) {
        if(trade.getCustomer_id() != customer_id) {
            System.out.println("Trade of this ID is not accessible to this user");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade getTradeByID(@PathVariable int id) {
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
    public @ResponseBody Trade cancelTrade(@PathVariable int id) {
        // Authentication
        User currentUser;
        AuthorizedUser context = new AuthorizedUser();
        currentUser = context.getUser();
        Trade trade = tradeService.getTrade(id);

        verifyTradeOwnership(trade, currentUser.getId());

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

        verifyTradeOwnership(trade, currentUser.getId());

        String action = trade.getAction();

        // If neither buy or sell, invalid trade
        if (!action.equals("buy") && !action.equals("sell")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String stockSymbol = trade.getSymbol();
        Stock stock = getStockForTrade(stockSymbol);
        Account account = getAccountForTrade(trade.getAccount_id());

        // Check type of order
        if (trade.getBid() == 0.0 || trade.getAsk() == 0.0) {
            // market order
            if (action.equals("buy")) {
                // market order - buy
                processMarketOrderBuy(trade, stock, account);
            } else if (action.equals("sell")) {
                // market order - sell
                processMarketOrderSell(trade, stock, account);
            }
        } else {
            // limit order
            if (action.equals("buy")) {
                // limit order - buy
                processLimitOrderBuy(trade, stock, account);
            } else if (action.equals("sell")) {
                // limit order - sell
                processLimitOrderSell(trade, stock, account);
            }
        }

        reflectInPortfolio(trade, stockSymbol);
        return trade;
    }

    private void processLimitOrderBuy(Trade trade, Stock stock, Account account) {

        Trade openTrade = marketMaker.locateOpenTrade(stock.getSymbol(), trade.getAction(), trade.getQuantity());

        if (openTrade == null) {
            System.out.println("No open trades found.");
            return;
        }

        double price = trade.getBid();
        double ask = openTrade.getAsk();

        // Limit order (buy) conditions for fill / partial-fill
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
        int customer_id = trade.getCustomer_id();
        Portfolio portfolio = getPortfolioForTrade(customer_id);
        
        // Check if asset in portfolio, will throw exception if not found
        checkPortfolioContainsAsset(portfolio, trade);

        Trade openTrade = marketMaker.locateOpenTrade(stock.getSymbol(), trade.getAction(), trade.getQuantity());

        if (openTrade == null) {
            System.out.println("No open trades found.");
            return;
        }

        double price = trade.getAsk();
        double bid = openTrade.getBid();

        // Limit order (sell) conditions for fill / partial-fill
        if (price <= bid) {
            boolean willing_quantity = checkQuantity(openTrade, trade);
            if (willing_quantity) {
                fillTradeSell(trade, openTrade, stock, account);
            } else {
                partialFillTradeSell(trade, openTrade, stock, account);
            }
        }
    }

    private void processMarketOrderSell(Trade trade, Stock stock, Account account) {
        int customer_id = trade.getCustomer_id();
        Portfolio portfolio = getPortfolioForTrade(customer_id);
        
        // Check if asset in portfolio, will throw exception if not found
        checkPortfolioContainsAsset(portfolio, trade);

        Trade openTrade = marketMaker.locateOpenTrade(stock.getSymbol(), trade.getAction(), trade.getQuantity());

        if (openTrade == null) {
            System.out.println("No open trades found.");
            return;
        }

        // Market order (sell) conditions for fill / partial fill
        boolean willing_quantity = checkQuantity(openTrade, trade);

        if (willing_quantity) {
            fillTradeSell(trade, openTrade, stock, account);
            System.out.println("Trade filled");
            return;
        } else {
            partialFillTradeSell(trade, openTrade, stock, account);
            System.out.println("Trade partially-filled");
            return;
        }

    }

    private void processMarketOrderBuy(Trade trade, Stock stock, Account account) {
        
        Trade openTrade = marketMaker.locateOpenTrade(stock.getSymbol(), trade.getAction(), trade.getQuantity());

        if (openTrade == null) {
            System.out.println("No open trades found.");
            return;
        }

        boolean sufficient_funds = checkFunds(openTrade, trade, account);
        boolean sufficient_quantity = checkQuantity(openTrade, trade);

        // Market order (buy) conditions for fill / partial fill
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

    private void partialFillTradeSell(Trade trade, Trade openTrade, Stock stock, Account account) {
        trade.setStatus("partial-filled");
        trade.setDate(Instant.now());
        trade.setFilled_quantity(trade.getQuantity());
        trade.setAvg_price(openTrade.getBid());

        // Add to account balance
        account.updateBalance(trade.getFilled_quantity() * trade.getAvg_price());

        // Reflect in openTrade / stock
        openTrade.setQuantity(openTrade.getQuantity() - trade.getFilled_quantity());
        stock.setLast_price(trade.getAvg_price());
        stock.setAsk_volume(stock.getAsk_volume() + trade.getFilled_quantity());
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

    private void fillTradeSell(Trade trade, Trade openTrade, Stock stock, Account account) {
        // Fill trade
        trade.setStatus("filled");
        trade.setDate(Instant.now());
        trade.setFilled_quantity(trade.getQuantity());
        trade.setAvg_price(openTrade.getBid());

        // Add to account balance
        account.updateBalance(trade.getFilled_quantity() * trade.getAvg_price());

        // Reflect in openTrade / stock
        openTrade.setQuantity(openTrade.getQuantity() - trade.getFilled_quantity());
        stock.setLast_price(trade.getAvg_price());
        stock.setAsk_volume(stock.getAsk_volume() + trade.getFilled_quantity());
    }

    private void checkPortfolioContainsAsset(Portfolio portfolio, Trade trade) {
        Iterable<Asset> allAssets = assetRepository.findAll();
        Iterator<Asset> iter = allAssets.iterator();

        while (iter.hasNext()) {
            Asset asset = (Asset)iter.next();
            if (!asset.getPortfolio().equals(portfolio)) {
                iter.remove();
            }
        }

        String symbol = trade.getSymbol();
        int quantity = trade.getQuantity();

        for (Asset asset : allAssets) {
            if(asset.getCode().equals(symbol) && (asset.getQuantity() == quantity)) {
                return;
            }
        }

        System.out.println("No such asset in portfolio.");
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private void reflectInPortfolio(Trade trade, String stockSymbol) {
        Asset asset = new Asset(stockSymbol, trade.getFilled_quantity(), trade.getAvg_price());
        addAsset(asset);
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

        String action = customerTrade.getAction();

        if (action.equals("buy")) {
            if (openQuantity < tradeQuantity) {
                return false;
            } else {
                return true;
            }
        }
        
        if (action.equals("sell")) {
            if (openQuantity > tradeQuantity) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    // Helper function
    private Asset addAsset(Asset asset) {
        return assetRepository.save(asset);
    }

    // Helper function
    private Portfolio getPortfolioForTrade(int id) {
        Optional<Portfolio> pfEntity = pfRepository.findById(id);
        Portfolio portfolio;
        if (!pfEntity.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            portfolio = pfEntity.get();
        }

        return portfolio;
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