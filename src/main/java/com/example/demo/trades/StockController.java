package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.client.RestTemplate;

import com.example.demo.security.AuthorizedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.example.demo.user.User;
import com.example.demo.config.NotFoundException;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import java.util.*;

@RestController
public class StockController {
    
    private StocksRepository stocksRepository;

    private List<Stock> getStocksFromAPI() {
        final String uri = "http://api.marketstack.com/v1/eod?access_key=fc9afa79e8a0b4c9be2d89574e7f8cae&symbols=MSFT,AAPL,AMZN,ADBE";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        JSONObject object = new JSONObject(new JSONTokener(result));

        JSONArray arr = object.getJSONArray("data");
        List<Stock> stocks = this.parseStocks(arr);
        return stocks;
    }

    private List<Stock> parseStocks(JSONArray arr) {
        List<Stock> stocks = new ArrayList<Stock>();
        // JSONObject obj = arr.getJSONObject(0);
        // System.out.println(obj);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i); 
            stocks.add(new Stock(obj));
        }
        return stocks;
    }

    // private void changesDB() {
    //     Iterable<Stock> stocks = this.stocksRepository.findAll();
    //     for (Stock stock : stocks) {
    //         stock.setBid();
    //         stock.setAsk();
    //         this.stocksRepository.save(stock);
    //     }
    // }

    @Autowired
    public StockController(StocksRepository stocksRepository) {
        this.stocksRepository = stocksRepository;
    }

    @PostMapping(path="/stocks")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Iterable<Stock> createStocks() {
        List<Stock> stocks = getStocksFromAPI();
        for (Stock stock : stocks) {
            stocksRepository.save(stock);
        }
        return stocks;
    }

    @GetMapping(path="/stocks")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Iterable<Stock> getStocks() {
        // User currentUser;
        // AuthorizedUser context = new AuthorizedUser();
        return stocksRepository.findAll();
    }

    @GetMapping(path="/stocks/{symbol}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Stock getStockById(@PathVariable String symbol) throws NotFoundException {
        Optional<Stock> stock = stocksRepository.findBySymbol(symbol);
        if (!stock.isPresent()) throw new NotFoundException("Stock not found");
        return stock.get();
    }
}