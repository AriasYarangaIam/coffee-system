package com.coffee.backend.repository;

import com.coffee.backend.entity.MovimientoStock;
import com.coffee.backend.entity.MovimientoStock.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    // "Últimos ingresos" del panel de stock: los N más recientes vigentes.
    List<MovimientoStock> findTop20ByEstadoOrderByFechaDesc(Estado estado);

    // Rehidratación de la Pila de deshacer al arrancar: orden ASC ⇒ el último queda en el tope.
    List<MovimientoStock> findByEstadoOrderByFechaAsc(Estado estado);
}
