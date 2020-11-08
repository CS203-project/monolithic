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

    private double generatePointDifference() {
        Random random = new Random();
        return random.nextDouble();
    }
}