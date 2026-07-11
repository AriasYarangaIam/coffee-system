package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Saldo de un {@link Insumos} en un {@link Almacenes} (cuánto hay disponible). Mapea la
 * tabla {@code stocks}. Es un saldo, no un historial: los ingresos individuales se guardan
 * aparte en {@link MovimientoStock}. Se incrementa al registrar ingresos y se descuenta al
 * vender productos que usan el insumo.
 */
@Entity
@Data
@Table(name = "stocks")
public class Stocks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_stock")
    private Long codigoStock;
    @Column(nullable = false)
    private Long cantidad;
    @ManyToOne
    @JoinColumn(name = "insumo_id",nullable = false)
    private Insumos insumos;
    @ManyToOne
    @JoinColumn(name = "almacen_id",nullable = false)
    private Almacenes almacenes;
}
