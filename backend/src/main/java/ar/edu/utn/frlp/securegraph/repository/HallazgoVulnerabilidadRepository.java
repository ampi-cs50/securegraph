package ar.edu.utn.frlp.securegraph.repository;

import ar.edu.utn.frlp.securegraph.domain.entity.HallazgoVulnerabilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallazgoVulnerabilidadRepository extends JpaRepository<HallazgoVulnerabilidad, Long> {
    List<HallazgoVulnerabilidad> findByAnalisisIdOrderByScorePrioridadDesc(Long analisisId);
    List<HallazgoVulnerabilidad> findByAnalisisIdOrderBySeveridadCvssDesc(Long analisisId);
}

