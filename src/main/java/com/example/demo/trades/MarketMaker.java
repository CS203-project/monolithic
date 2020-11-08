package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import com.example.demo.accounts.Account;
import com.example.demo.portfolio.Portfolio;
import com.example.demo.portfolio.PortfolioRepository;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Random;
import java.util.Optional;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="scheduling.enabled", matchIfMissing=true)
public class MarketMaker {
    private StockService stockService;
    private TradeService tradeService;
    private PortfolioRepository pfRepository;
    private boolean marketOpen;
    private HashMap<String, List<Trade>> marketTrades; 

    @Autowired
    public MarketMaker(StockService stockService, TradeService tradeService, PortfolioRepository pfRepository) {
        this.stockService = stockService;
        this.tradeService = tradeService;
        this.pfRepository = pfRepository;
        this.marketOpen = stockService.isOpen();
        this.marketTrades = autoCreate();
    }

    // Checks and updates trades every hour
    @Scheduled(cron="* 0 9-17/1 * * MON-FRI")
    public void updateEveryHour() {
        if (marketOpen) {
            this.marketTrades = autoCreate();
            return;
        }

        expireTrades();
    }

    // Find Open Trades matching symbol and action
    public Trade locateOpenTrade(String symbol, String action) {
        List<Trade> tradePairs = this.marketTrades.get(symbol);
        Trade openTrade;

        if (action.equals("buy")) {
            // in the trade pair, the first one is a buy
            openTrade = tradePairs.get(0);
        } else {
            // in the trade pair, the second one is a sell
            openTrade = tradePairs.get(1);
        }

        return openTrade;
    }

    // Create Trades
    // From TestConstants.java:
    // *** When your API starts (or market starts), your API will auto-create multiple open buy and sell trades,
    // *** one pair (buy and sell) for each stock listed at the bid and ask price, respectively.
    // *** The volumes of these trades can be set to a fixed value, say 20000.
    public HashMap<String, List<Trade>> autoCreate() {
        // Map of trade pairs - key: symbol, value: list of trades (2 trades)
        HashMap<String, List<Trade>> tradePairsBySymbol = new HashMap<>();

        // Return empty map, when market is not open
        if (!marketOpen) return tradePairsBySymbol;

        // Get stocks
        Iterable<Stock> stocks = stockService.getStocks();

        for (Stock stock : stocks) {
            makeTrades(stock, tradePairsBySymbol);
        }

        return tradePairsBySymbol;
    }

    // Make Trades
    // Helper function
    private void makeTrades(Stock stock, HashMap<String, List<Trade>> tradePairsBySymbol) {
        String symbol = stock.getSymbol();

        Trade buy = new Trade("buy", symbol, 20000, "open");
        Trade sell = new Trade("sell", symbol, 20000, "open");

        double stock_last_price = stock.getLastPrice();

        // Random double generator for point difference between last_price and bid / ask
        double pointDifference = generatePointDifference();

        buy.setBid(stock_last_price - pointDifference);
        sell.setAsk(stock_last_price + pointDifference);

        List<Trade> tradePairs = new ArrayList<>();
        tradePairs.add(buy);
        tradePairs.add(sell);

        tradePairsBySymbol.put(symbol, tradePairs);
    }

    // Expire Trades
    public void expireTrades() {
        List<Trade> trades = tradeService.listTrades();
        for (Trade trade : trades) {
            int hourCreated = trade.getHour();
            // expire trades that are not already filled / partially filled, and those created while market was open
            if ((!trade.getStatus().equals("partial-filled")) && (!trade.getStatus().equals("filled")) && (hourCreated < 17 && hourCreated > 9) && !trade.createdOnWeekend()) {
                trade.setStatus("expired");
            }
        }
    }

    // Generate Difference
    private double generatePointDifference() {
        Random random = new Random();
        return random.nextDouble();
    }

    public void processMarketOrder(Trade trade, Stock stock, Account account) {
        if (!marketOpen) return;
        String action = trade.getAction();
        if (action.equals("buy")) {
            // market order - buy
            processMarketOrderBuy(trade, stock, account);
        } else if (action.equals("sell")) {
            // market order - sell
            // processMarketOrderSell(trade, stock, account);
        }
    }

    public void processLimitOrder(Trade trade, Stock stock, Account account) {
        if (!marketOpen) return;
        String action = trade.getAction();
        if (action.equals("buy")) {
            // limit order - buy
            // processLimitOrderBuy(trade, stock, account);
        } else if (action.equals("sell")) {
            // limit order - sell
            // processLimitOrderSell(trade, stock, account);
        }
    }

    // Process Market Order for a Buy Action
    public void processMarketOrderBuy(Trade trade, Stock stock, Account account) {
        Trade openTrade = locateOpenTrade(stock.getSymbol(), trade.getAction());

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

    // Process Market Order for a Sell Action
    private void processMarketOrderSell(Trade trade, Stock stock, Account account) {
        int customer_id = trade.getCustomer_id();
        Portfolio portfolio = getPortfolioForTrade(customer_id);

        if (!portfolio.containsAssetToSell(trade.getSymbol(), trade.getQuantity())) {
            System.out.println("No such asset in portfolio.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Trade openTrade = locateOpenTrade(stock.getSymbol(), trade.getAction());

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

    // Fill Trade for a Buy Action
    private void fillTrade(Trade trade, Trade openTrade, Stock stock, Account account) {
        fillTradeGeneric(trade, openTrade, stock);
        trade.setAvg_price(openTrade.getAsk());

        // Reflect changes in stock and account
        account.updateBalance(-(trade.getFilled_quantity() * trade.getAvg_price()));
        stock.setAskVolume(stock.getAskVolume() - trade.getFilled_quantity());
    }

    // Fill Trade for a Sell Action
    private void fillTradeSell(Trade trade, Trade openTrade, Stock stock, Account account) {
        fillTradeGeneric(trade, openTrade, stock);
        trade.setAvg_price(openTrade.getBid());

        // Reflect changes in stock and account
        account.updateBalance(trade.getFilled_quantity() * trade.getAvg_price());
        stock.setAskVolume(stock.getAskVolume() + trade.getFilled_quantity());
    }

    // Partial Fill Trade for a Buy Action
    private void partialFillTrade(Trade trade, Trade openTrade, Stock stock, Account account, int status) {
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
        
        // Reflect changes in stock and account
        account.updateBalance(-(trade.getFilled_quantity() * trade.getAvg_price()));
        stock.setAskVolume(stock.getAskVolume() - trade.getFilled_quantity());
    }

    // Partial Fill Trade for a Sell Action
    private void partialFillTradeSell(Trade trade, Trade openTrade, Stock stock, Account account) {
        partialFillTradeGeneric(trade, openTrade, stock);
        trade.setFilled_quantity(trade.getQuantity());
        trade.setAvg_price(openTrade.getBid());

        // Reflect changes in stock and account
        account.updateBalance(trade.getFilled_quantity() * trade.getAvg_price());
        stock.setAskVolume(stock.getAskVolume() + trade.getFilled_quantity());
    }

    // Helper function for fillTrade and fillTradeSell
    private void fillTradeGeneric(Trade trade, Trade openTrade, Stock stock) {
        trade.setStatus("filled");
        trade.setDate(Instant.now());
        trade.setFilled_quantity(trade.getQuantity());

        openTrade.setQuantity(openTrade.getQuantity() - trade.getFilled_quantity());
        stock.setLastPrice(trade.getAvg_price());
    }

    // Helper function for partial fill trade methods
    private void partialFillTradeGeneric(Trade trade, Trade openTrade, Stock stock) {
        trade.setStatus("partial-filled");
        trade.setDate(Instant.now());

        openTrade.setQuantity(openTrade.getQuantity() - trade.getFilled_quantity());
        stock.setLastPrice(trade.getAvg_price());
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
    // Get portfolio for trade
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
}