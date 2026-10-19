package com.tomatoes.empleados.Repositorio;

import com.tomatoes.empleados.Entidades.Jornada;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface JornadaRepository extends MongoRepository<Jornada, String> {
    List<Jornada> findByEmpleadoIdOrderByFechaDesc(String empleadoId);
   
}
