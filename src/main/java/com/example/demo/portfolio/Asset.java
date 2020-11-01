package com.example.demo.portfolio;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
public class Asset {
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;
    private String code;

    private int quantity;
    private double avg_price;
    private double current_price;

    private double value;
    private double gain_loss;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Portfolio portfolio;

    public Asset(String code, int quantity, double avg_price) {
        this.code = code;
        this.quantity = quantity;
        this.avg_price = avg_price;
        this.current_price = current_price;
        this.value = current_price * quantity;
    }
}