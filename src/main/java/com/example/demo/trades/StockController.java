package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;


@RestController
public class StockController {
    
    private StocksRepository stocksRepository;

    @Autowired
    public StockController(StocksRepository stocksRepository) {
        this.stocksRepository = stocksRepository;
    }

    @GetMapping(path="/stocks")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Iterable<Stock> getStocks() {
        return null;
    }

    @GetMapping(path="/stocks/{symbol}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Stock getStockById(@PathVariable String symbol) {
        return null;
    }
}