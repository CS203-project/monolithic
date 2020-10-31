package com.example.demo.portfolio;

import javax.persistence.Embeddable;

@Embeddable
public class AssetId {
    private int customerId;
    private String stockCode;

    public AssetId(int customerId, String stockCode) {
        this.customerId = customerId;
        this.stockCode = stockCode;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getStockCode() {
        return stockCode;
    }
}