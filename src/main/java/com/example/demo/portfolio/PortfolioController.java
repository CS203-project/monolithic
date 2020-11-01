package com.example.demo.portfolio;

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

import java.util.Optional;

@RestController
public class PortfolioController {
    private PortfolioRepository pfRepository;

    @Autowired
    public PortfolioController(PortfolioRepository pfRepository) {
        this.pfRepository = pfRepository;
    }

    @PostMapping("/portfolio")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Portfolio getPortfolio(int customer_id) {
        Optional<Portfolio> pfEntity = pfRepository.findById(customer_id);
        if (!pfEntity.isPresent()) {
            System.out.println("DANGER");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return pfEntity.get();
    }
}