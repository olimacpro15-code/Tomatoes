package com.tomatoes.cosechas.Repositorio;

import com.tomatoes.cosechas.Entidades.Cosecha;
import com.tomatoes.cosechas.Entidades.EstadoCosecha;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CosechaRepository extends MongoRepository<Cosecha, String> {
    boolean existsByLoteIdAndEstadoNot(String loteId, EstadoCosecha estado);

    @Query("{ 'empleadosIds': ?0 }")
    List<Cosecha> buscarPorEmpleado(String empleadoId);
}
