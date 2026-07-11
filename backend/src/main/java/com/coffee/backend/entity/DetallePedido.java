package com.coffee.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detallePedido_id")
    private Long detallePedidoId;
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(name = "cantidad_pedida",nullable = false)
    private Long cantidadPedida;
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    @ManyToOne
    @JoinColumn(name = "producto_id",nullable = false)
    private Productos productos;
    @ManyToOne
    @JoinColumn(name = "pedido_id",nullable = false)
    private Pedidos pedidos;
}

