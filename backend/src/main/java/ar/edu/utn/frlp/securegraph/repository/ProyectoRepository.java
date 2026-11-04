package ar.edu.utn.frlp.securegraph.repository;

import ar.edu.utn.frlp.securegraph.domain.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
}

