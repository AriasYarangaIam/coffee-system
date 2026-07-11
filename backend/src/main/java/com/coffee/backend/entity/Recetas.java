package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Receta: cuánto de un {@link Insumos} consume un {@link Productos} (línea producto↔insumo
 * con {@code cantidadUsada}). Al vender el producto, el sistema descuenta esa cantidad del
 * stock. Mapea la tabla {@code recetas}.
 */
@Entity
@Table(name = "recetas")
@Data
public class Recetas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "receta_id")
    private Long recetaID;
    @Column(name = "nombre_receta",nullable = false)
    private String nombreReceta;
    @Column(name = "cantidad_usada",nullable = false)
    private Long cantidadUsada;
    @ManyToOne
    @JoinColumn(name = "producto_id",nullable = false)
    private Productos productos;
    @ManyToOne
    @JoinColumn(name = "insumo_id",nullable = false)
    private Insumos insumos;

}
