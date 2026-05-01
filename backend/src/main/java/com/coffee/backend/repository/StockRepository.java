package com.coffee.backend.repository;

import com.coffee.backend.entity.Stocks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stocks,Long> {
}
