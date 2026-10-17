package com.tomatoes.insumos.Repositorio;

import com.tomatoes.insumos.Entidades.Estado;
import com.tomatoes.insumos.Entidades.Insumo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InsumoRepository extends MongoRepository<Insumo, String> {
    List<Insumo> findByEstado(Estado estado);
    boolean existsByProveedorId(String proveedorId);
}
