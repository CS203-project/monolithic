package com.example.demo.portfolio;

import java.util.List;

public class Portfolio {
    private int customerId;
    private List<Asset> assets;

    public Portfolio(int customerId, List<Asset> assets) {
        this.customerId = customerId;
        this.assets = assets;
    }

    public double getUnrealizedGainLoss() {
        double gainLoss = 0;
        for (Asset a : assets)
            gainLoss += a.getGainLoss();
        return gainLoss;
    }

    // public double getRealizedGainLoss()
}