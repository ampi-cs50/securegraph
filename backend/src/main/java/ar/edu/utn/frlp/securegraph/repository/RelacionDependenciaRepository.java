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

    @Query(value = """
            WITH RECURSIVE niveles(profundidad, frontera, visitados) AS (
                SELECT 0, ARRAY[c.id], ARRAY[c.id]
                FROM componente c
                WHERE c.analisis_id = :analisisId AND c.es_raiz

                UNION ALL

                SELECT
                    n.profundidad + 1,
                    siguiente.ids,
                    n.visitados || siguiente.ids
                FROM niveles n
                CROSS JOIN LATERAL (
                    SELECT array_agg(DISTINCT r.hijo_componente_id) AS ids
                    FROM relacion_dependencia r
                    JOIN componente hijo ON hijo.id = r.hijo_componente_id
                        AND hijo.analisis_id = :analisisId
                    WHERE r.analisis_id = :analisisId
                        AND r.padre_componente_id = ANY(n.frontera)
                        AND NOT (r.hijo_componente_id = ANY(n.visitados))
                ) siguiente
                WHERE cardinality(siguiente.ids) > 0
            ), alcanzados AS (
                SELECT unnest(frontera) AS id, profundidad
                FROM niveles
            )
            SELECT c.id AS "componentId", c.bom_ref AS "bomRef", a.profundidad AS "depth"
            FROM componente c
            LEFT JOIN alcanzados a ON a.id = c.id
            WHERE c.analisis_id = :analisisId
            ORDER BY a.profundidad NULLS LAST, c.id
            """, nativeQuery = true)
    List<ProfundidadComponente> buscarProfundidadesMinimas(@Param("analisisId") Long analisisId);

    @Query("""
            select p.bomRef as source, c.bomRef as target, r.tipoTransitiva as transitive
            from RelacionDependencia r join r.padreComponente p join r.hijoComponente c
            where r.analisis.id = :id and p.analisis.id = :id and c.analisis.id = :id
            order by r.id
            """)
    List<AristaAnalisis> buscarAristas(@Param("id") Long id);

    interface AristaAnalisis {
        String getSource();
        String getTarget();
        boolean getTransitive();
    }

    interface ProfundidadComponente {
        Long getComponentId();

        String getBomRef();

        Integer getDepth();
    }
}

