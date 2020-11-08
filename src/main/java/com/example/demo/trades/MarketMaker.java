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
    private StockService stockService;
    private boolean marketOpen;

    @Autowired
    public MarketMaker(StockService stockService) {
        this.stockService = stockService;
        this.marketOpen = stockService.isOpen();
    }

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

    private double generatePointDifference() {
        Random random = new Random();
        return random.nextDouble();
    }
}