package com.tomatoes.empleados.Config;

import com.tomatoes.empleados.Entidades.Empleado;
import com.tomatoes.empleados.Entidades.Estado;
import com.tomatoes.empleados.Entidades.Rol;
import com.tomatoes.empleados.Repositorio.EmpleadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Crea un usuario por cada rol la primera vez que arranca el servicio (colección vacía).
 * Se desactiva con SEED_DEMO=false. Cambie estas contraseñas antes de usar datos reales.
 */
@Component
@ConditionalOnProperty(name = "tomatoes.seed.demo", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    private final EmpleadoRepository repo;
    private final PasswordEncoder encoder;

    public DatosIniciales(EmpleadoRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        crear("Ana María", "Rojas Pinto", "1000000001", "admin@tomatoes.com", "Admin123*", "Administradora del sistema", Rol.ADMINISTRADOR);
        crear("Carlos", "Mendoza Ruiz", "1000000002", "gerente@tomatoes.com", "Gerente123*", "Gerente general", Rol.GERENTE);
        crear("Laura", "Gómez Vargas", "1000000003", "supervisor@tomatoes.com", "Supervisor123*", "Supervisora de cosechas", Rol.SUPERVISOR);
        crear("Andrés", "Prada Silva", "1000000004", "bodega@tomatoes.com", "Bodega123*", "Encargado de bodega", Rol.BODEGA);
        crear("Juan", "Torres Díaz", "1000000005", "operario@tomatoes.com", "Operario123*", "Operario de campo", Rol.OPERARIO);
        crear("María", "Castro León", "1000000006", "operario2@tomatoes.com", "Operario123*", "Operaria de campo", Rol.OPERARIO);
        log.info("Se crearon los usuarios de demostración (uno por rol).");
    }

    private void crear(String nombres, String apellidos, String doc, String correo, String clave, String cargo, Rol rol) {
        Empleado e = new Empleado();
        e.setNombres(nombres); e.setApellidos(apellidos); e.setDocumento(doc); e.setCorreo(correo);
        e.setPasswordHash(encoder.encode(clave)); e.setCargo(cargo); e.setRol(rol);
        e.setEstado(Estado.ACTIVO); e.setFechaIngreso(LocalDate.now());
        repo.save(e);
    }
}
