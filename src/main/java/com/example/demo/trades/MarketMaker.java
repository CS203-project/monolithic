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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Random;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="scheduling.enabled", matchIfMissing=true)
public class MarketMaker {
    private StockService stockService;
    private TradeService tradeService;
    private boolean marketOpen;
    private HashMap<String, List<Trade>> marketTrades; 

    @Autowired
    public MarketMaker(StockService stockService, TradeService tradeService) {
        this.stockService = stockService;
        this.tradeService = tradeService;
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

    public void processMarketOrderBuy(Trade trade, Stock stock, Account account) {
        Trade openTrade = locateOpenTrade(stock.getSymbol(), trade.getAction());

        boolean sufficient_funds = checkFunds(openTrade, trade, account);
        boolean sufficient_quantity = checkQuantity(openTrade, trade);

        // Market order (buy) conditions for fill / partial fill
        if (sufficient_funds && sufficient_quantity) {
            // fillTrade(trade, openTrade, stock, account);
            // System.out.println("Trade filled");
            // return;
        }

        if (!sufficient_funds && sufficient_quantity) {
            // partialFillTrade(trade, openTrade, stock, account, 1);
            // System.out.println("Trade partially filled");
            // return;
        }

        if (sufficient_funds && !sufficient_quantity) {
            // partialFillTrade(trade, openTrade, stock, account, 2);
            // System.out.println("Trade partially filled");
            // return;
        }

        // else 
        System.out.println("Trade unable to be filled / matched.");
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
}