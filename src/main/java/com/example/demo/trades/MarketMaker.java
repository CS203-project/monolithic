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

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="scheduling.enabled", matchIfMissing=true)
public class MarketMaker {

    private StocksRepository stocksRepo;
    private Instant timestamp = Instant.now();
    private HashMap<String, List<Trade>> marketTrades; // empty if market is closed

    @Autowired
    public MarketMaker(StocksRepository stocksRepo) {
        this.stocksRepo = stocksRepo;
        this.marketTrades = autoCreate();
    }

    // For testing: @Scheduled(cron="* * * * * *")
    @Scheduled(cron="* 0 0/1 * * MON-FRI")
    // At minute 0 past every hour from 0 through 23 on every day-of-week from Monday through Friday.
    public void updateEveryHour() {
        timestamp = Instant.now();

        if (!isMarketOpen()) {
            System.out.println("Market is closed.");
            expireTrades();
        }
    }

    // *** These trades are referred to as the market maker's trades - to create liquidity in the market.
    // *** The customers' trades can then be matched with these market maker's trades.
    public Trade matchTrade(Trade customerTrade) {
        String symbol = customerTrade.getSymbol();
        List<Trade> tradePairs = this.marketTrades.get(symbol);
        return null;
    }

    // *** When your API starts (or market starts), your API will auto-create multiple open buy and sell trades,
    // *** one pair (buy and sell) for each stock listed at the bid and ask price, respectively.
    // *** The volumes of these trades can be set to a fixed value, say 20000.
    public HashMap<String, List<Trade>> autoCreate() {
        // map of trade pairs - key: symbol, value: list of trades (2 trades)
        HashMap<String, List<Trade>> tradePairsBySymbol = new HashMap<>();
        if (!isMarketOpen()) return tradePairsBySymbol; // return empty map

        List<Stock> stocks = stocksRepo.findAll();

        for (Stock stock : stocks) {
            String symbol = stock.getSymbol();

            Trade buy = new Trade("buy", symbol, 20000, "open");
            Trade sell = new Trade("sell", symbol, 20000, "open");

            double stock_bid_price = stock.getBid();
            double stock_ask_price = stock.getAsk();
            buy.setBid(stock_ask_price);
            sell.setAsk(stock_bid_price);

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
        Iterator it = marketTrades.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            for (Trade trade : marketTrades.get(pair.getKey())) {
                trade.setStatus("expired");
            }

            it.remove();
        }
    }
}