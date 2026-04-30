package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;

import java.sql.Date;
import java.util.List;

@Entity
@Data
@Table(name = "pedidos")
public class Pedidos {
    @Id
    @Column(name = "pedido_id")
    @SequenceGenerator(name = "pedido_rol",sequenceName ="pedido_rol",allocationSize = 1)
    @GeneratedValue(generator = "pedido_rol",strategy = GenerationType.SEQUENCE)
    private Long pedidoId;
    @Column(name = "alias_ticket",nullable = false,unique = true)
    private String aliasTicket;
    @CurrentTimestamp
    @Column(name = "fecha_pedido")
    private Date fechaPedido;
    @ManyToOne
    @JoinColumn(name = "usuario_id",nullable = false)
    private Usuarios usuario;
    @OneToMany (mappedBy = "pedidos", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE} )
    private List<DetallePedido> detallePedido;
}
