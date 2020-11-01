package com.example.demo.trades;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.GetMapping; 
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
// import org.json.simple.JsonArray;
// import org.json.simple.JsonObject;

@RestController
public class StockController {
    
    private StocksRepository stocksRepository;

    private JSONArray getStocksFromAPI() {
        final String uri = "http://api.marketstack.com/v1/eod?access_key=fc9afa79e8a0b4c9be2d89574e7f8cae&symbols=MSFT,AAPL,AMZN,ADBE";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        JSONParser parser = new JSONParser(); 
        JSONObject json = new JSONObject();
        
        try {
            json = (JSONObject) parser.parse(result);
        } catch (ParseException pe) {
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        } catch (NullPointerException npe) {
            System.out.println("No stocks found");
        }

        JSONArray array = (JSONArray) json.get("data");

        return array;
    }

    private List<Stock> createStocks() {
        JSONArray array = getStocksFromAPI();
        Iterator<String> iterator = array.iterator();
        while (iterator.hasNext()) {

        }

        return null;
    }

    /*
        @Id
        private String symbol;
        
        private double last_price;
        private int bid_volume;
        private double bid;
        private int ask_volume;
        private double ask;

        public Stock() {}
    */
    
    @Autowired
    public StockController(StocksRepository stocksRepository) {
        this.stocksRepository = stocksRepository;
    }

    @GetMapping(path="/stocks")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Iterable<Stock> getStocks() {
        User currentUser;
        AuthorizedUser context = new AuthorizedUser();
        currentUser = context.getUser();

        getStocksFromAPI();
        return null;
    }

    @GetMapping(path="/stocks/{symbol}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Stock getStockById(@PathVariable String symbol) {
        return null;
    }
}