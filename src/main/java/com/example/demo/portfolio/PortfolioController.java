package com.example.demo.portfolio;

import com.example.demo.security.AuthorizedUser;
import com.example.demo.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioController {
    @Autowired
    AssetRepository assets;

    User currUser;

    public PortfolioController(AssetRepository assets) {
        this.assets = assets;
    }

    public void authenticateCurrentUser() {
        AuthorizedUser context = new AuthorizedUser();
        this.currUser = context.getUser();
    }

    @PostMapping(path = "/portfolio/{id}")
    public @ResponseBody Portfolio getPortfolio(@PathVariable int id) {
        authenticateCurrentUser();
        return new Portfolio(id, assets.findAssetByCustomerId(id));
    }
}