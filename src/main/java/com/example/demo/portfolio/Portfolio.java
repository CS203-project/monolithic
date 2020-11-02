package com.example.demo.portfolio;

import java.util.*;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
public class Portfolio {

    @Id
    private int customer_id;
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    private List<Asset> assets;
    private double unrealized_gain_loss;
    private double total_gain_loss;

    public Portfolio() {
        this.customer_id = 0;
        this.assets = new ArrayList<>();
        this.unrealized_gain_loss = 0;
        this.total_gain_loss = 0;
    }

    public Portfolio(int customer_id) {
        this.customer_id = customer_id;
        this.assets = new ArrayList<>();
        this.unrealized_gain_loss = 0;
        this.total_gain_loss = 0;
    }

    public void addAsset(Asset asset) {
        assets.add(asset); // manually update db?
    }

    public double getUnrealizedGainLoss() {
        double gainLoss = 0;
        for (Asset a : assets)
            gainLoss += a.getGain_loss();
        return gainLoss;
    }

    // public double getRealizedGainLoss()
}