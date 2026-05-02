package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "almacenes")
@Data
public class Almacenes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_almacen",nullable = false)
    private Long codigoAlmacen;
    @Column(name = "nombre_almacen",nullable = false)
    private String nombreAlmacen;
    @Column(name = "direccion_almacen")
    private String direcciónAlmacen;
    @OneToMany(mappedBy = "almacenes", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List<Stocks> stocks;
}
