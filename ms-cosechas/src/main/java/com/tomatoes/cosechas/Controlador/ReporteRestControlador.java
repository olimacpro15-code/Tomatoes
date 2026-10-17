package com.tomatoes.cosechas.Controlador;

import com.tomatoes.cosechas.Dto.ReporteProduccion;
import com.tomatoes.cosechas.Servicio.CosechaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Reportes de producción")
public class ReporteRestControlador {

    private final CosechaService servicio;

    public ReporteRestControlador(CosechaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Reporte de producción por lote", description = "Total de kg, número de cosechas y costo de insumos por lote. Filtros: desde, hasta y loteId.")
    @GetMapping("/produccion")
    @PreAuthorize("hasAnyRole('GERENTE','SUPERVISOR','ADMINISTRADOR')")
    public List<ReporteProduccion> produccion(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                              @RequestParam(required = false) String loteId) {
        return servicio.reporteProduccion(desde, hasta, loteId);
    }
}
