package com.example.demo.trades;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="scheduling.enabled", matchIfMissing=true)
public class MarketMaker {

    /*
Market Maker function: your API should be generating various sell and buy orders
            for all traded stocks at appropriate bids and asks (based on actual last_price).
            The volumes for these orders should be specified or fixed by your API, i.e.,
            you can specify any volumes required for testing purposes, e.g., 20000.
            This is to create liquidity in the market, and facilitate fast order matching.
     
        *** Example:
        *** Your API can obtain initial static pricing from https://www.sgx.com/indices/products/sti
        *** When your API starts (or market starts), your API will auto-create multiple open buy and sell trades,
        *** one pair (buy and sell) for each stock listed at the bid and ask price, respectively.
        *** The volumes of these trades can be set to a fixed value, say 20000.

        *** E.g., for A17U, based on the actual last_price of $3.28, you can create a sell trade with ask = $3.29 & volume = 20000,
        *** and a buy trade with bid = $3.26 & volume = 20000.
        
        *** These trades are referred to as the market maker's trades - to create liquidity in the market.
        *** The customers' trades can then be matched with these market maker's trades.
*/

    private Instant timestamp = Instant.now();

    public MarketMaker() {}

    // For testing: @Scheduled(cron="* * * * * *")
    @Scheduled(cron="* 0 0/1 * * MON-FRI")
    // At minute 0 past every hour from 0 through 23 on every day-of-week from Monday through Friday.
    public void updateEveryHour() {
        timestamp = Instant.now();
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
}