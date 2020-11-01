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
    private Portfolio portfolio;
    private AssetRepository assetRepository;

    @Autowired
    public PortfolioController(PortfolioRepository pfRepository, AssetRepository assetRepository) {
        this.pfRepository = pfRepository;
        this.assetRepository = assetRepository;
    }

    @GetMapping("/portfolio")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Portfolio getPortfolio(int customer_id) {

        handlePortfolioAssets();
        Optional<Portfolio> pfEntity = pfRepository.findById(customer_id);
        if (!pfEntity.isPresent()) {
            System.out.println("No portfolio found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return pfEntity.get();
    }

    // for each asset in assetRepo that has the same portfolio as portfolio??
    // add to Portfolio class
    // for each in pfrepo, for each in assetrepo
    public void handlePortfolioAssets() {
        Iterable<Portfolio> allPortfolios = pfRepository.findAll();
        Iterable<Asset> allAssets = assetRepository.findAll();

        // Iterator<Portfolio> pfIter = allPortfolios.iterator();
        // Iterator<Asset> asIter = allAssets.iterator();

        for (Portfolio pf : allPortfolios) {
            for (Asset asset : allAssets) {
                
            }
        }
    }

}