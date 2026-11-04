package ar.edu.utn.frlp.securegraph.repository;

import ar.edu.utn.frlp.securegraph.domain.entity.RelacionDependencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelacionDependenciaRepository extends JpaRepository<RelacionDependencia, Long> {

    List<RelacionDependencia> findByAnalisisId(Long analisisId);

    /**
     * Consulta recursiva (WITH RECURSIVE) sobre PostgreSQL para explorar el DAG de dependencias
     * y calcular las profundidades desde los componentes directos de un análisis.
     */
    @Query(value = """
            WITH RECURSIVE grafo_dependencias AS (
                SELECT
                    r.padre_componente_id,
                    r.hijo_componente_id,
                    1 AS profundidad
                FROM relacion_dependencia r
                JOIN componente c_padre ON c_padre.id = r.padre_componente_id
                WHERE r.analisis_id = :analisisId AND c_padre.es_directa = TRUE
                UNION ALL
                SELECT
                    r.padre_componente_id,
                    r.hijo_componente_id,
                    g.profundidad + 1
                FROM relacion_dependencia r
                INNER JOIN grafo_dependencias g ON r.padre_componente_id = g.hijo_componente_id
                WHERE r.analisis_id = :analisisId AND g.profundidad < 15
            )
            SELECT DISTINCT padre_componente_id, hijo_componente_id, MIN(profundidad) AS profundidad
            FROM grafo_dependencias
            GROUP BY padre_componente_id, hijo_componente_id
            """, nativeQuery = true)
    List<Object[]> calcularGrafoRecursivoPorAnalisis(@Param("analisisId") Long analisisId);
}

