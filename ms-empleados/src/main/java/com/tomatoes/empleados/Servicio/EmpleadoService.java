package com.tomatoes.empleados.Servicio;

import com.tomatoes.empleados.Dto.EmpleadoRequest;
import com.tomatoes.empleados.Entidades.Empleado;
import com.tomatoes.empleados.Entidades.Estado;
import com.tomatoes.empleados.Entidades.Rol;
import com.tomatoes.empleados.Excepciones.NegocioException;
import com.tomatoes.empleados.Excepciones.NoEncontradoException;
import com.tomatoes.empleados.Repositorio.EmpleadoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository repo;
    private final PasswordEncoder encoder;

    public EmpleadoService(EmpleadoRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public List<Empleado> listar(Estado estado) {
        return estado == null ? repo.findAll(Sort.by("apellidos", "nombres")) : repo.findByEstado(estado);
    }

    public Empleado obtener(String id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el empleado con id " + id));
    }

    public Empleado crear(EmpleadoRequest r) {
        if (r.password() == null || r.password().isBlank()) {
            throw new NegocioException("La contraseña es obligatoria al crear un empleado.");
        }
        validarUnicos(r, null);
        Empleado e = new Empleado();
        aplicar(e, r);
        e.setPasswordHash(encoder.encode(r.password()));
        e.setEstado(Estado.ACTIVO);
        return repo.save(e);
    }

    public Empleado actualizar(String id, EmpleadoRequest r) {
        Empleado e = obtener(id);
        validarUnicos(r, id);
        aplicar(e, r);
        if (r.password() != null && !r.password().isBlank()) {
            e.setPasswordHash(encoder.encode(r.password()));
        }
        return repo.save(e);
    }

    public Empleado cambiarRol(String id, Rol rol) {
        Empleado e = obtener(id);
        e.setRol(rol);
        return repo.save(e);
    }

    /** Borrado lógico: el empleado se conserva para mantener el historial de jornadas y cosechas. */
    public Empleado desactivar(String id, String idSolicitante) {
        if (id.equals(idSolicitante)) {
            throw new NegocioException("No puede desactivar su propio usuario.");
        }
        Empleado e = obtener(id);
        e.setEstado(Estado.INACTIVO);
        return repo.save(e);
    }

    public Empleado activar(String id) {
        Empleado e = obtener(id);
        e.setEstado(Estado.ACTIVO);
        return repo.save(e);
    }

    private void validarUnicos(EmpleadoRequest r, String idActual) {
        repo.findByDocumento(r.documento().trim()).filter(o -> esOtro(o, idActual))
            .ifPresent(o -> { throw new NegocioException("Ya existe un empleado con el documento " + r.documento() + "."); });
        repo.findByCorreo(r.correo().trim().toLowerCase()).filter(o -> esOtro(o, idActual))
            .ifPresent(o -> { throw new NegocioException("Ya existe un empleado con el correo " + r.correo() + "."); });
    }

    private boolean esOtro(Empleado o, String idActual) {
        return idActual == null || !idActual.equals(o.getId());
    }

    private void aplicar(Empleado e, EmpleadoRequest r) {
        e.setNombres(r.nombres().trim());
        e.setApellidos(r.apellidos().trim());
        e.setDocumento(r.documento().trim());
        e.setCorreo(r.correo().trim().toLowerCase());
        e.setCargo(r.cargo().trim());
        e.setRol(r.rol());
        e.setFechaIngreso(r.fechaIngreso() != null ? r.fechaIngreso() : (e.getFechaIngreso() != null ? e.getFechaIngreso() : LocalDate.now()));
    }
}
