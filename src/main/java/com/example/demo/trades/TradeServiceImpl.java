package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
public class TradeServiceImpl implements TradeService {
    private TradeRepository tradeRepository;

    @Autowired
    public TradeServiceImpl(TradeRepository tradeRepository){
        this.tradeRepository = tradeRepository;
    }

    @Override
    public Trade getTrade(int id) {
        return null;
    }
}