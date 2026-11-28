package ar.edu.utn.frlp.securegraph.repository;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalisisRepository extends JpaRepository<Analisis, Long> {
    List<Analisis> findByProyectoIdOrderByFechaAnalisisDesc(Long proyectoId);

    @EntityGraph(attributePaths = "proyecto")
    Optional<Analisis> findOneById(Long id);
}

