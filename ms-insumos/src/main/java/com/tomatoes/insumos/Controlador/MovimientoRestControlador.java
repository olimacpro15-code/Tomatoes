package com.tomatoes.insumos.Controlador;

import com.tomatoes.insumos.Dto.MovimientoRequest;
import com.tomatoes.insumos.Entidades.Movimiento;
import com.tomatoes.insumos.Entidades.TipoMovimiento;
import com.tomatoes.insumos.Seguridad.UsuarioAutenticado;
import com.tomatoes.insumos.Servicio.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@Tag(name = "Movimientos", description = "Entradas, salidas e historial de inventario")
public class MovimientoRestControlador {

    private final MovimientoService servicio;

    public MovimientoRestControlador(MovimientoService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Registrar entrada", description = "Compra o ingreso de insumo: suma la cantidad al stock automáticamente.")
    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BODEGA')")
    public Movimiento entrada(@Valid @RequestBody MovimientoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.entrada(request, usuario);
    }

    @Operation(summary = "Registrar salida", description = "Entrega a campo: falla con error 400 si la cantidad supera el stock disponible.")
    @PostMapping("/salida")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BODEGA')")
    public Movimiento salida(@Valid @RequestBody MovimientoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.salida(request, usuario);
    }

    @Operation(summary = "Registrar consumo en una cosecha",
               description = "Lo invoca el microservicio de cosechas con el token del supervisor. Equivale a una salida con cosechaId obligatorio.")
    @PostMapping("/consumo")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPERVISOR','BODEGA')")
    public Movimiento consumo(@Valid @RequestBody MovimientoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.consumo(request, usuario);
    }

    @Operation(summary = "Historial de movimientos", description = "Filtros opcionales: insumoId, tipo (ENTRADA/SALIDA), desde y hasta (AAAA-MM-DD).")
    @GetMapping
    @PreAuthorize("hasAnyRole('BODEGA','GERENTE','ADMINISTRADOR')")
    public List<Movimiento> historial(@RequestParam(required = false) String insumoId,
                                      @RequestParam(required = false) TipoMovimiento tipo,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.historial(insumoId, tipo, desde, hasta);
    }
}
