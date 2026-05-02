package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

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
