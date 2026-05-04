package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "pedidos")
public class Pedidos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pedido_id")
    private Long pedidoId;
    @Column(name = "alias_ticket",nullable = false,unique = true)
    private String aliasTicket;
    @CurrentTimestamp
    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;
    @ManyToOne
    @JoinColumn(name = "usuario_id",nullable = false)
    private Usuarios usuario;
    @OneToMany(mappedBy = "pedidos", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

}
