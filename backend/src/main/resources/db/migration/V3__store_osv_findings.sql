ALTER TABLE hallazgo_vulnerabilidad
    ADD COLUMN ghsa_id VARCHAR(50),
    ADD COLUMN osv_identifiers TEXT,
    ALTER COLUMN severidad_cvss DROP DEFAULT,
    ALTER COLUMN severidad_cvss DROP NOT NULL,
    ALTER COLUMN profundidad DROP DEFAULT,
    ALTER COLUMN profundidad DROP NOT NULL;

ALTER TABLE hallazgo_vulnerabilidad
    ADD CONSTRAINT uq_hallazgo_analisis_componente_osv
        UNIQUE (analisis_id, componente_id, osv_id);
