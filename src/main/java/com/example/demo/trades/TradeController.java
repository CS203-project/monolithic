package com.example.demo.trades;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.security.AuthorizedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import com.example.demo.user.User;

@RestController
public class TradeController {
    
    private TradeService tradeService;

    @Autowired
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/trades")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Trade createTrade(@RequestBody Trade trade) {
        return null;
    }

    @GetMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade getTradeByID(@PathVariable int id) {
        return null;
    }

    @PutMapping("/trades/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Trade cancelTrade(@PathVariable int id, @RequestBody Trade trade) {
        return null;
    }

}