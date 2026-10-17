package com.tomatoes.insumos;

import com.tomatoes.insumos.Dto.MovimientoRequest;
import com.tomatoes.insumos.Entidades.*;
import com.tomatoes.insumos.Excepciones.NegocioException;
import com.tomatoes.insumos.Repositorio.InsumoRepository;
import com.tomatoes.insumos.Repositorio.MovimientoRepository;
import com.tomatoes.insumos.Seguridad.UsuarioAutenticado;
import com.tomatoes.insumos.Servicio.MovimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock MovimientoRepository movimientos;
    @Mock InsumoRepository insumos;
    @Mock MongoTemplate mongo;
    @InjectMocks MovimientoService servicio;

    private final UsuarioAutenticado usuario = new UsuarioAutenticado("u1", "bodega@tomatoes.com", "Andrés Prada", "BODEGA", "token");
    private Insumo insumo;

    @BeforeEach
    void preparar() {
        insumo = new Insumo();
        insumo.setId("i1"); insumo.setNombre("Urea"); insumo.setUnidadMedida("kg");
        insumo.setStockActual(10); insumo.setEstado(Estado.ACTIVO);
    }

    @Test
    void salidaMayorAlStockSeRechazaYNoSeRegistra() {
        when(insumos.findById("i1")).thenReturn(Optional.of(insumo));
        when(mongo.findAndModify(any(Query.class), any(UpdateDefinition.class), any(FindAndModifyOptions.class), eq(Insumo.class)))
                .thenReturn(null); // la condición stockActual >= cantidad no se cumplió
        NegocioException ex = assertThrows(NegocioException.class,
                () -> servicio.salida(new MovimientoRequest("i1", 50.0, null, null), usuario));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(movimientos, never()).save(any());
    }

    @Test
    void entradaRegistraElMovimiento() {
        when(insumos.findById("i1")).thenReturn(Optional.of(insumo));
        when(movimientos.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));
        Movimiento m = servicio.entrada(new MovimientoRequest("i1", 5.0, null, "Compra"), usuario);
        assertEquals(TipoMovimiento.ENTRADA, m.getTipo());
        assertEquals(5.0, m.getCantidad());
        assertEquals("u1", m.getUsuarioId());
    }

    @Test
    void consumoExigeCosecha() {
        assertThrows(NegocioException.class,
                () -> servicio.consumo(new MovimientoRequest("i1", 1.0, "  ", null), usuario));
    }

    @Test
    void noSeMueveUnInsumoDesactivado() {
        insumo.setEstado(Estado.INACTIVO);
        when(insumos.findById("i1")).thenReturn(Optional.of(insumo));
        assertThrows(NegocioException.class, () -> servicio.entrada(new MovimientoRequest("i1", 1.0, null, null), usuario));
    }
}
