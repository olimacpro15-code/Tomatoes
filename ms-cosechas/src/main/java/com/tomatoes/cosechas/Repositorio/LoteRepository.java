package com.tomatoes.cosechas.Repositorio;

import com.tomatoes.cosechas.Entidades.Estado;
import com.tomatoes.cosechas.Entidades.Lote;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LoteRepository extends MongoRepository<Lote, String> {
    Optional<Lote> findByNombreIgnoreCase(String nombre);
    List<Lote> findByEstado(Estado estado);
}
