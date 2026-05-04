package com.coffee.backend.repository;

import com.coffee.backend.entity.Stocks;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stocks, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stocks> findByInsumos_IdInsumo(Long idInsumo);
}