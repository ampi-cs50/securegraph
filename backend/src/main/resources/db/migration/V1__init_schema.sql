-- =============================================================================
-- SecureGraph - Migración Inicial V1 (PostgreSQL 16)
-- Modela proyectos, análisis SBOM, componentes, hallazgos y aristas del grafo.
-- =============================================================================

CREATE TABLE proyecto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analisis (
    id BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT NOT NULL REFERENCES proyecto(id) ON DELETE CASCADE,
    fecha_analisis TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sbom_formato VARCHAR(50) NOT NULL DEFAULT 'CycloneDX-JSON',
    estado VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    total_componentes INT NOT NULL DEFAULT 0,
    total_vulnerabilidades INT NOT NULL DEFAULT 0
);

CREATE TABLE componente (
    id BIGSERIAL PRIMARY KEY,
    analisis_id BIGINT NOT NULL REFERENCES analisis(id) ON DELETE CASCADE,
    bom_ref VARCHAR(255) NOT NULL,
    group_id VARCHAR(150),
    artifact_id VARCHAR(150) NOT NULL,
    version VARCHAR(50),
    purl VARCHAR(255),
    scope VARCHAR(30) DEFAULT 'required',
    es_directa BOOLEAN NOT NULL DEFAULT FALSE,
    modulo_contexto VARCHAR(100)
);

CREATE TABLE relacion_dependencia (
    id BIGSERIAL PRIMARY KEY,
    analisis_id BIGINT NOT NULL REFERENCES analisis(id) ON DELETE CASCADE,
    padre_componente_id BIGINT NOT NULL REFERENCES componente(id) ON DELETE CASCADE,
    hijo_componente_id BIGINT NOT NULL REFERENCES componente(id) ON DELETE CASCADE,
    tipo_transitiva BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE hallazgo_vulnerabilidad (
    id BIGSERIAL PRIMARY KEY,
    analisis_id BIGINT NOT NULL REFERENCES analisis(id) ON DELETE CASCADE,
    componente_id BIGINT NOT NULL REFERENCES componente(id) ON DELETE CASCADE,
    osv_id VARCHAR(100) NOT NULL,
    cve_id VARCHAR(50),
    severidad_cvss NUMERIC(3,1) NOT NULL DEFAULT 0.0,
    nivel_severidad VARCHAR(20) NOT NULL,
    profundidad INT NOT NULL DEFAULT 1,
    score_prioridad NUMERIC(4,1) NOT NULL DEFAULT 0.0,
    razones_prioridad TEXT
);

-- Índices para optimizar consultas recursivas (WITH RECURSIVE) y filtros por análisis
CREATE INDEX idx_analisis_proyecto ON analisis(proyecto_id);
CREATE INDEX idx_componente_analisis ON componente(analisis_id);
CREATE INDEX idx_relacion_analisis ON relacion_dependencia(analisis_id);
CREATE INDEX idx_relacion_padre ON relacion_dependencia(padre_componente_id);
CREATE INDEX idx_relacion_hijo ON relacion_dependencia(hijo_componente_id);
CREATE INDEX idx_hallazgo_analisis_score ON hallazgo_vulnerabilidad(analisis_id, score_prioridad DESC);

