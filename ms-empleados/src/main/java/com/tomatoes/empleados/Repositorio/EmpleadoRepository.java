package com.tomatoes.empleados.Repositorio;

import com.tomatoes.empleados.Entidades.Empleado;
import com.tomatoes.empleados.Entidades.Estado;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends MongoRepository<Empleado, String> {
    Optional<Empleado> findByCorreo(String correo);
    Optional<Empleado> findByDocumento(String documento);
    List<Empleado> findByEstado(Estado estado);
}
