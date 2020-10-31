package com.example.demo.portfolio;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Asset {
    @EmbeddedId
    private AssetId assetId;

    private int quantity;
    private double avgPrice;
    private double currPrice;

    public Asset(AssetId assetId, int quantity, double avgPrice, double currPrice) {
        this.assetId = assetId;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.currPrice = currPrice;
    }

    public double getValue() {
        return currPrice * quantity;
    }

    public double getGainLoss() {
        return (currPrice - avgPrice) * quantity;
    }
}