package com.example.demo.portfolio;

import java.util.List;

public class Portfolio {
    private int customerId;
    private List<Asset> assets;

    public Portfolio(int customerId, List<Asset> assets) {
        this.customerId = customerId;
        this.assets = assets;
    }

    public int getCustomerId() {
        return customerId;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void addAsset(Asset asset) {
        if (asset.getAssetId().getCustomerId() != customerId)
            throw new RuntimeException();
        assets.add(asset);
    }

    public double getUnrealizedGainLoss() {
        double gainLoss = 0;
        for (Asset a : assets)
            gainLoss += a.getGainLoss();
        return gainLoss;
    }

    // public double getRealizedGainLoss()
}