package ar.edu.utn.frlp.securegraph.repository;

import ar.edu.utn.frlp.securegraph.domain.entity.HallazgoVulnerabilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallazgoVulnerabilidadRepository extends JpaRepository<HallazgoVulnerabilidad, Long> {

    @Query("""
            select h from HallazgoVulnerabilidad h join fetch h.componente c
            where h.analisis.id = :id and c.analisis.id = :id
            """)
    List<HallazgoVulnerabilidad> buscarConComponente(@Param("id") Long id);

    @Query("""
            select h.nivelSeveridad as nivel, count(h) as total
            from HallazgoVulnerabilidad h join h.componente c
            where h.analisis.id = :id and c.analisis.id = :id
            group by h.nivelSeveridad
            """)
    List<ConteoSeveridad> contarPorSeveridad(@Param("id") Long id);

    @Query("""
            select distinct c.id as componentId, h.nivelSeveridad as nivel
            from HallazgoVulnerabilidad h join h.componente c
            where h.analisis.id = :id and c.analisis.id = :id
            """)
    List<SeveridadComponente> buscarSeveridadesDeComponentes(@Param("id") Long id);

    interface ConteoSeveridad {
        String getNivel();
        Long getTotal();
    }

    interface SeveridadComponente {
        Long getComponentId();
        String getNivel();
    }
}
