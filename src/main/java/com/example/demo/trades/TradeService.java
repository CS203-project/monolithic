package com.example.demo.trades;

import java.util.List;

public interface TradeService {
    List<Trade> listTrades();
    Trade getTrade(int id);
    Trade addTrade(Trade trade);
    Trade updateTrade(int id, Trade trade);
}