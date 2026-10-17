package com.tomatoes.insumos.Repositorio;

import com.tomatoes.insumos.Entidades.Proveedor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProveedorRepository extends MongoRepository<Proveedor, String> {
    Optional<Proveedor> findByNit(String nit);
}
