package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {
    
    private StocksRepository stocksRepository;

    @Autowired
    public StockController(StocksRepository stocksRepository) {
        this.stocksRepository = stocksRepository;
    }
}