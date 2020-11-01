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
import java.lang.Iterable;

@RestController
public class PortfolioController {
    private PortfolioRepository pfRepository;
    private AssetRepository assetRepository;

    @Autowired
    public PortfolioController(PortfolioRepository pfRepository, AssetRepository assetRepository) {
        this.pfRepository = pfRepository;
        this.assetRepository = assetRepository;
    }


}