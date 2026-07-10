package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

/**
 * Traza de un ingreso de stock (RF-DS-03). A diferencia de {@link Stocks} (saldo),
 * cada fila es un evento con fecha y autor. Deshacer no borra: pasa a
 * {@link Estado#REVERSADO}, preservando la auditoría.
 */
@Entity
@Data
@Table(name = "movimientos_stock")
public class MovimientoStock {

    public enum Tipo { INGRESO }

    public enum Estado { ACTIVO, REVERSADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumos insumo;

    @ManyToOne
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacenes almacen;

    @Column(nullable = false)
    private Long cantidad;

    @CurrentTimestamp
    @Column(name = "fecha")
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuarios usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    // Fila de stocks afectada, para revertir el saldo directo al deshacer.
    @Column(name = "codigo_stock")
    private Long codigoStock;
}
