package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeServiceImpl implements TradeService {
    private TradeRepository tradeRepository;

    @Autowired
    public TradeServiceImpl(TradeRepository tradeRepository){
        this.tradeRepository = tradeRepository;
    }

    @Override
    public List<Trade> listTrades() {
        return tradeRepository.findAll();
    }

    @Override
    public Trade getTrade(int id) {
        return tradeRepository.findById(id).orElse(null);
    }

    @Override
    public Trade addTrade(Trade trade) {
        return tradeRepository.save(trade);
    }
}