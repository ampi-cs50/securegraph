package ar.edu.utn.frlp.securegraph.repository;

import ar.edu.utn.frlp.securegraph.domain.entity.Componente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponenteRepository extends JpaRepository<Componente, Long> {
    List<Componente> findByAnalisisId(Long analisisId);
}

