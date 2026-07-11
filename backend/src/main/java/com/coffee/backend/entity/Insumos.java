package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Insumo: materia prima que se consume al preparar productos (leche, café, azúcar...).
 * Mapea la tabla {@code insumos}. Un insumo aparece en varias {@link Recetas} y tiene su
 * saldo en {@link Stocks}. {@code @Data} (Lombok) genera getters/setters.
 */
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
    // Unidad de medida (ej.: ml, g, kg, L, unidad). La cantidad de stock y de receta se
    // interpretan en esta unidad.
    @Column(name = "unidad")
    private String unidad;
    @OneToMany(mappedBy = "insumos",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List<Recetas> recetas;
    @OneToMany(mappedBy = "insumos", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List<Stocks> stocks;
}

