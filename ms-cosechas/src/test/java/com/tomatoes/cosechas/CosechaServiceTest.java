package com.tomatoes.cosechas;

import com.tomatoes.cosechas.Dto.AsignacionRequest;
import com.tomatoes.cosechas.Dto.EstadoCosechaRequest;
import com.tomatoes.cosechas.Entidades.Cosecha;
import com.tomatoes.cosechas.Entidades.EstadoCosecha;
import com.tomatoes.cosechas.Excepciones.NegocioException;
import com.tomatoes.cosechas.Repositorio.CosechaRepository;
import com.tomatoes.cosechas.Repositorio.LoteRepository;
import com.tomatoes.cosechas.Servicio.ClienteMicroservicios;
import com.tomatoes.cosechas.Servicio.CosechaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosechaServiceTest {

    @Mock CosechaRepository repo;
    @Mock LoteRepository lotes;
    @Mock ClienteMicroservicios clientes;
    @InjectMocks CosechaService servicio;

    private Cosecha cosecha;

    @BeforeEach
    void preparar() {
        cosecha = new Cosecha();
        cosecha.setId("c1");
        cosecha.setLoteId("l1");
        cosecha.setFechaInicio(LocalDate.now().minusDays(10));
        cosecha.setEstado(EstadoCosecha.EN_CURSO);
    }

    @Test
    void noSePuedeFinalizarSinCantidadCosechada() {
        when(repo.findById("c1")).thenReturn(Optional.of(cosecha));
        assertThrows(NegocioException.class,
                () -> servicio.cambiarEstado("c1", new EstadoCosechaRequest(EstadoCosecha.FINALIZADA, null, null, null)));
        verify(repo, never()).save(any());
    }

    @Test
    void noSePuedeRetrocederElEstado() {
        when(repo.findById("c1")).thenReturn(Optional.of(cosecha));
        assertThrows(NegocioException.class,
                () -> servicio.cambiarEstado("c1", new EstadoCosechaRequest(EstadoCosecha.PLANEADA, null, null, null)));
    }

    @Test
    void soloSeAsignanEmpleadosActivos() {
        when(repo.findById("c1")).thenReturn(Optional.of(cosecha));
        when(clientes.empleado("e1")).thenReturn(Map.of("estado", "INACTIVO", "nombres", "Ana", "apellidos", "Ruiz"));
        assertThrows(NegocioException.class, () -> servicio.asignarEmpleados("c1", new AsignacionRequest(List.of("e1"))));
        assertTrue(cosecha.getEmpleadosIds().isEmpty());
    }
}
