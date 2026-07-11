package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Producto vendible de la carta (ej.: Capuchino). Mapea la tabla {@code productos}.
 * Pertenece a una {@link Categorias}, tiene una {@link Recetas} (insumos que consume) y
 * aparece en los {@link DetallePedido}. El precio es {@code BigDecimal} para exactitud
 * monetaria.
 */
@Entity
@Table(name = "productos")
@Data
public class Productos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Long productoId;
    @Column(name = "nombre_producto",nullable = false)
    private String nombreProducto;
    @Column(name = "precio_actual", precision = 10, scale = 2)
    private BigDecimal precioActual;
    @ManyToOne
    @JoinColumn(name = "categoria_id",nullable = false)
    private Categorias categorias;
    @OneToMany(mappedBy = "productos")
    private List<DetallePedido> detallePedidos;
    @OneToMany(mappedBy = "productos",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE},orphanRemoval = true)
    private List<Recetas> recetas;
}
