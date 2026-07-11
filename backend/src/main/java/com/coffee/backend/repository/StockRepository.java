package com.coffee.backend.repository;

import com.coffee.backend.entity.Stocks;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stocks, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stocks> findByInsumos_IdInsumo(Long idInsumo);

    // Dashboard admin (B-05): insumos con stock por debajo del umbral de alerta.
    List<Stocks> findByCantidadLessThan(Long umbral);
}