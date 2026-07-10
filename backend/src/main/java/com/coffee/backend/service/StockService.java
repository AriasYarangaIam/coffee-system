package com.coffee.backend.service;

import com.coffee.backend.dto.request.StockRequestDTO;
import com.coffee.backend.dto.response.MovimientoStockResponseDTO;
import com.coffee.backend.dto.response.StockResponseDTO;

import java.util.List;

// Gestión de stock para administración (ADMIN): listado, ingreso, histórico y deshacer.
public interface StockService {
    List<StockResponseDTO> listar();

    // 'correo' es el usuario autenticado que registra el ingreso (se guarda en la traza).
    StockResponseDTO registrarIngreso(StockRequestDTO dto, String correo);

    // "Últimos Ingresos": los movimientos vigentes más recientes.
    List<MovimientoStockResponseDTO> listarMovimientos();

    // Deshace el último ingreso (LIFO): revierte el saldo y marca el movimiento REVERSADO.
    StockResponseDTO deshacerUltimoIngreso();
}
