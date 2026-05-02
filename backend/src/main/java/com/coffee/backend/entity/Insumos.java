package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "insumos")
@Data
public class Insumos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Long idInsumo;
    @Column(name = "nombre_insumo",nullable = false)
    private String nombreInsumo;
    @OneToMany(mappedBy = "insumos",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List<Recetas> recetas;
    @OneToMany(mappedBy = "insumos", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List<Stocks> stocks;
}

