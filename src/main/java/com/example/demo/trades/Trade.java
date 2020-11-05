package com.example.demo.trades;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
public class Trade {

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    private String action;
    private String symbol;
    private int quantity;
    private double bid;
    private double ask;
    private double avg_price;
    private int filled_quantity;

    // submitted time in Unix timestamp, expired after 5pm of the day
    private long date;

    private int account_id;
    private int customer_id;

    private String status;

    public Trade() {}

    public Trade(String action, String symbol, int quantity, String status) {
        this.action = action;
        this.symbol = symbol;
        this.quantity = quantity;
        this.status = status;
    }

    public void setDate(Instant date) {
        this.date = date.getEpochSecond();
    }

    public Instant getDate() {
        return Instant.ofEpochSecond(this.date);
    }

    public int getHour() {
        return ZonedDateTime.ofInstant(getDate(), ZoneId.of("Asia/Singapore")).getHour();
    }

    public boolean createdOnWeekend() {
        int dayOfWeek = ZonedDateTime.ofInstant(getDate(), ZoneId.of("Asia/Singapore")).getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) return true;
        return false;
    }
}