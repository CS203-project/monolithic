package com.example.demo.trades;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.Instant;

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
    private Instant date;

    private int account_id;
    private int customer_id;

    private String status;

    public Trade() {}
}