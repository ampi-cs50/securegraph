ALTER TABLE componente ADD COLUMN es_raiz BOOLEAN NOT NULL DEFAULT FALSE;

-- RF-02 saved the metadata component first, preserving insertion order.
UPDATE componente AS c
SET es_raiz = TRUE
FROM (
    SELECT DISTINCT ON (analisis_id) id
    FROM componente
    ORDER BY analisis_id, id
) AS primera_raiz
WHERE c.id = primera_raiz.id;

CREATE UNIQUE INDEX uq_componente_raiz_por_analisis
    ON componente (analisis_id) WHERE es_raiz;

CREATE INDEX idx_relacion_analisis_padre
    ON relacion_dependencia (analisis_id, padre_componente_id);
