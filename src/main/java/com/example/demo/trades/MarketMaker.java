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
    private HashMap<String, List<Trade>> marketTrades; 

    @Autowired
    public MarketMaker(StocksRepository stocksRepo, TradeService tradeService) {
        this.stocksRepo = stocksRepo;
        this.tradeService = tradeService;
        this.marketTrades = autoCreate();
    }

    @Scheduled(cron="* 0 9-17/1 * * MON-FRI")
    // Cron: At minute 0 past every hour from 9 through 17 on every day-of-week from Monday through Friday.
    // Every hour while market is open, a new pair of buy and sell trades will be created for each symbol
    // If the market is closed, trades will be expired
    public void updateEveryHour() {

        if (isMarketOpen()) {
            timestamp = Instant.now();
            this.marketTrades = autoCreate();

        } else {
            System.out.println("Market is closed.");
            expireTrades();
        }
    }

    // Helper function
    // Find open trades that were created by MarketMaker for a specific symbol
    public Trade locateOpenTrade(String symbol, String action, int quantity) {
        List<Trade> tradePairs = this.marketTrades.get(symbol);
        Trade openTrade;

        if (action.equals("buy")) {
            // in the trade pair, the first one is a buy
            openTrade = tradePairs.get(0);
        } else {
            // in the trade pair, the second one is a sell
            openTrade = tradePairs.get(1);
        }

        // check if open trade has sufficient volume
        if (openTrade.getQuantity() >= quantity) return openTrade;
        return null;
    }

    // From TestConstants.java:
    // *** When your API starts (or market starts), your API will auto-create multiple open buy and sell trades,
    // *** one pair (buy and sell) for each stock listed at the bid and ask price, respectively.
    // *** The volumes of these trades can be set to a fixed value, say 20000.
    public HashMap<String, List<Trade>> autoCreate() {

        // Map of trade pairs - key: symbol, value: list of trades (2 trades)
        HashMap<String, List<Trade>> tradePairsBySymbol = new HashMap<>();

        // Return empty map, when market is not open
        if (!isMarketOpen()) return tradePairsBySymbol;

        // Random double generator for point difference between last_price and bid / ask
        Random random = new Random();
        double pointDifference = random.nextDouble();

        List<Stock> stocks = stocksRepo.findAll();

        // Create new trades for each stock symbol
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
            int hourCreated = trade.getHour();
            // expire trades that are not already filled / partially filled, and those created while market was open
            if ((!trade.getStatus().equals("partial-filled")) && (!trade.getStatus().equals("filled")) && (hourCreated < 17 && hourCreated > 9) && !trade.createdOnWeekend()) {
                trade.setStatus("expired");
            }
        }
    }
}