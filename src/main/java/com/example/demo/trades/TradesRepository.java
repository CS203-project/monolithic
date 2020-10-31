package com.example.demo.trades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradesRepository extends JpaRepository <Trade, Integer> {
    // implement only custom needs here, CRUD is handled by JPA
}