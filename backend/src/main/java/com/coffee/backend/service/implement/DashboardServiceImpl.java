package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.DashboardResponseDTO;
import com.coffee.backend.dto.response.StockBajoDTO;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.repository.StockRepository;
import com.coffee.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    // El modelo no persiste un mínimo de stock por insumo; usamos un umbral fijo de
    // alerta. Suficiente para la cafetería MYPE (una sede). Si más adelante se necesita
    // por insumo, se añade una columna y se reemplaza esta constante.
    private static final long UMBRAL_STOCK_BAJO = 10;

    private final PedidoRepository pedidoRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDTO obtenerResumen() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.plusDays(1).atStartOfDay();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = inicioMes.plusMonths(1);

        double totalVentasDia = pedidoRepository.sumarVentasEntre(inicioDia, finDia);
        long totalPedidosDia = pedidoRepository.contarPedidosEntre(inicioDia, finDia);

        List<String> masVendidos = pedidoRepository.productosMasVendidos(
                inicioMes, finMes, PageRequest.of(0, 1));
        String productoEstrella = masVendidos.isEmpty() ? null : masVendidos.get(0);

        List<StockBajoDTO> stockBajo = stockRepository.findByCantidadLessThan(UMBRAL_STOCK_BAJO)
                .stream()
                .map(s -> new StockBajoDTO(
                        s.getInsumos().getNombreInsumo(),
                        s.getCantidad(),
                        s.getInsumos().getUnidad()))
                .toList();

        return new DashboardResponseDTO(totalVentasDia, totalPedidosDia, productoEstrella, stockBajo);
    }
}
