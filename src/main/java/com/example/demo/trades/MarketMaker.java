package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

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

    private StocksRepository stocksRepo;
    private TradeService tradeService;
    private Instant timestamp = Instant.now();
    private HashMap<String, List<Trade>> marketTrades; // empty if market is closed

    @Autowired
    public MarketMaker(StocksRepository stocksRepo, TradeService tradeService) {
        this.stocksRepo = stocksRepo;
        this.tradeService = tradeService;
        this.marketTrades = autoCreate();
    }

    // For testing: @Scheduled(cron="* * * * * *")
    @Scheduled(cron="* 0 9-17/1 * * MON-FRI")
    // At minute 0 past every hour from 9 through 17 on every day-of-week from Monday through Friday.
    public void updateEveryHour() {
        timestamp = Instant.now();
        this.marketTrades = autoCreate();

        if (!isMarketOpen()) {
            System.out.println("Market is closed.");
            expireTrades();
        }
    }

    // *** These trades are referred to as the market maker's trades - to create liquidity in the market.
    // *** The customers' trades can then be matched with these market maker's trades.
    public Trade matchTrade(Trade customerTrade) {
        String symbol = customerTrade.getSymbol();
        String action = customerTrade.getAction();
        int quantity = customerTrade.getQuantity();

        Trade tradeToMatch = locateOpenTrade(symbol, action, quantity);
        if (tradeToMatch == null) {
            System.out.println("No open trades suitable to match");
        }

        return null;
    }

    // Helper function
    public Trade locateOpenTrade(String symbol, String action, int quantity) {
        List<Trade> tradePairs = this.marketTrades.get(symbol);
        Trade openTrade;

        if (action.equals("buy")) {
            openTrade = tradePairs.get(0);
        } else {
            openTrade = tradePairs.get(1);
        }

        // check if open trade has sufficient volume
        if (openTrade.getQuantity() >= quantity) return openTrade;
        return null;
    }

    // *** When your API starts (or market starts), your API will auto-create multiple open buy and sell trades,
    // *** one pair (buy and sell) for each stock listed at the bid and ask price, respectively.
    // *** The volumes of these trades can be set to a fixed value, say 20000.
    public HashMap<String, List<Trade>> autoCreate() {

        // map of trade pairs - key: symbol, value: list of trades (2 trades)
        HashMap<String, List<Trade>> tradePairsBySymbol = new HashMap<>();
        if (!isMarketOpen()) return tradePairsBySymbol; // return empty map

        Random random = new Random();
        double pointDifference = random.nextDouble();

        List<Stock> stocks = stocksRepo.findAll();

        for (Stock stock : stocks) {
            String symbol = stock.getSymbol();

            Trade buy = new Trade("buy", symbol, 20000, "open");
            Trade sell = new Trade("sell", symbol, 20000, "open");

            double stock_last_price = stock.getLast_price();
            buy.setBid(stock_last_price - pointDifference);
            sell.setAsk(stock_last_price + pointDifference);

            List<Trade> tradePairs = new ArrayList<>();
            tradePairs.add(buy);
            tradePairs.add(sell);

            tradePairsBySymbol.put(symbol, tradePairs);
        }

        return tradePairsBySymbol;
    }

    // Market should open from 9am to 5pm on weekdays only.
    public boolean isMarketOpen() {
        int saturday = 6;
        int sunday = 7;

        int openingHour = 9;
        int closingHour = 17;

        int currentDay = timestamp.atZone(ZoneId.systemDefault()).getDayOfWeek().getValue();
        int currentHour = timestamp.atZone(ZoneId.systemDefault()).getHour();

        if (currentDay == saturday || currentDay == sunday) return false;
        if (currentHour < openingHour || currentHour > closingHour) return false;

        return true;
    }

    public HashMap<String, List<Trade>> getMarketTrades() {
        return this.marketTrades;
    }

    public void expireTrades() {
        List<Trade> trades = tradeService.listTrades();
        for (Trade trade : trades) {
            if ((!trade.getStatus().equals("partial-filled")) && (!trade.getStatus().equals("filled"))) {
                trade.setStatus("expired");
            }
        }
    }
}