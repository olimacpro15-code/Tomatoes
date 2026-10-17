package com.tomatoes.empleados;

import com.tomatoes.empleados.Dto.EmpleadoRequest;
import com.tomatoes.empleados.Entidades.Empleado;
import com.tomatoes.empleados.Entidades.Estado;
import com.tomatoes.empleados.Entidades.Rol;
import com.tomatoes.empleados.Excepciones.NegocioException;
import com.tomatoes.empleados.Repositorio.EmpleadoRepository;
import com.tomatoes.empleados.Servicio.EmpleadoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock EmpleadoRepository repo;
    @Mock PasswordEncoder encoder;
    @InjectMocks EmpleadoService servicio;

    private EmpleadoRequest solicitud(String correo, String documento) {
        return new EmpleadoRequest("Luis", "Pérez", documento, correo, "Clave1234", "Operario de campo", Rol.OPERARIO, LocalDate.now());
    }

    @Test
    void crearRechazaCorreoDuplicado() {
        when(repo.findByCorreo("luis@tomatoes.com")).thenReturn(Optional.of(new Empleado()));
        assertThrows(NegocioException.class, () -> servicio.crear(solicitud("luis@tomatoes.com", "12345678")));
        verify(repo, never()).save(any());
    }

    @Test
    void crearGuardaContrasenaCifradaYEstadoActivo() {
        when(encoder.encode("Clave1234")).thenReturn("HASH");
        when(repo.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
        Empleado e = servicio.crear(solicitud("LUIS@Tomatoes.com", "12345678"));
        assertEquals("HASH", e.getPasswordHash());
        assertEquals(Estado.ACTIVO, e.getEstado());
        assertEquals("luis@tomatoes.com", e.getCorreo());
    }

    @Test
    void noPermiteDesactivarSuPropioUsuario() {
        assertThrows(NegocioException.class, () -> servicio.desactivar("abc", "abc"));
    }
}
