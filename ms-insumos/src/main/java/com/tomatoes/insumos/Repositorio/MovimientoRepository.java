package com.tomatoes.insumos.Repositorio;

import com.tomatoes.insumos.Entidades.Movimiento;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovimientoRepository extends MongoRepository<Movimiento, String> {
}
