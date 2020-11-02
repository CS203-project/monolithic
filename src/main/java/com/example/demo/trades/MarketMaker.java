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

    private Instant timestamp = Instant.now();

    public MarketMaker() {}

    // @Scheduled(cron="* 0 0/1 * * MON-FRI")
    @Scheduled(cron="* * * * * *")
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