package com.example.demo.trades;

import java.util.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
public class Stock {

    @Id
    private String symbol;
    
    private double last_price;
    private int bid_volume;
    private double bid;
    private int ask_volume;
    private double ask;

    public Stock() {}

}