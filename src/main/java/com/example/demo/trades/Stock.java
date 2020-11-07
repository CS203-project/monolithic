package com.example.demo.trades;

import java.util.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.*;

import org.json.JSONObject;
import org.json.JSONArray;

@Entity
@Getter
@Setter
@ToString
public class Stock {
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;
    private String symbol;
    private double last_price;
    private int bid_volume;
    private double bid;
    private int ask_volume;
    private double ask;

    public Stock() {}
    public Stock(String symbol) {
        this.symbol = "A17U";
        this.last_price = 3.28;
        this.bid_volume = 20000;
        this.bid = 3.26;
        this.ask_volume = 20000;
        this.ask = 3.29;
    }
    public Stock(JSONObject json) {
        this.symbol = json.getString("symbol");
        this.last_price = json.optDouble("close", 0);
        this.bid_volume = json.getInt("volume");
        this.bid = json.optDouble("low", 0);
        this.ask_volume = json.getInt("volume");
        this.ask = json.optDouble("high", 0);
    }

}