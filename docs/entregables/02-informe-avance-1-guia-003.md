# DOCUMENTO FORMAL 2 — INFORME DE AVANCE N° 1 (GUÍA 003 - UTN FRLP)

* **Fecha límite de entrega:** 29/11/2026 (Semana 4 — Otorga la Regularidad)

---

## 1. Identificación y Estado General
* **Proyecto:** SecureGraph | **Período:** Semanas 1 a 4 (02/11/2026 al 29/11/2026)
* **Avance planificado:** 50% | **Avance real:** 50% | **Diferencia:** 0% (En término)
* **Estado:** Cronograma en término, Alcance sin variaciones, Calidad conforme.
* **Resumen narrativo:** Se construyó la infraestructura base del sistema. En el área de Backend se implementó la ingesta de inventarios CycloneDX JSON en Spring Boot y la consulta automática a OSV, modelando los componentes y relaciones en PostgreSQL. En el área de Frontend se desarrolló la arquitectura en Angular, el formulario de importación, el panel de hallazgos y la visualización interactiva del grafo de dependencias. Ambos módulos se integraron a través del contrato API acordado.

## 2. Horas y Aportes por Área
* **Desarrollo Backend y Persistencia:** 48 horas reloj dedicadas. Configuración de Spring Boot, esquema relacional en PostgreSQL, parser CycloneDX, cliente HTTP para OSV y formulación del cálculo del score.
* **Desarrollo Frontend y Visualización:** 48 horas reloj dedicadas. Configuración de Angular, diseño de interfaz principal, pantalla de proyectos y carga de archivos, tabla dinámica de vulnerabilidades y componente de visualización del grafo.

## 3. Diagramas Obligatorios (Excluyentes)

### 3.1 Esquema Completo de la Base de Datos (DER Relacional para Grafo)
```text
+---------------------------------------------------------------------------------+
|                        ESQUEMA DE BASE DE DATOS (POSTGRESQL)                    |
+---------------------------------------------------------------------------------+

 [ PROYECTO ]
   - id: BIGSERIAL (PK)
   - nombre: VARCHAR(100) NOT NULL
   - descripcion: TEXT
   - created_at: TIMESTAMP NOT NULL
        | 1
        | N
 [ ANALISIS ]
   - id: BIGSERIAL (PK)
   - proyecto_id: BIGINT (FK -> PROYECTO.id) ON DELETE CASCADE
   - fecha_analisis: TIMESTAMP NOT NULL
   - sbom_formato: VARCHAR(50)
   - estado: VARCHAR(30)
   - total_componentes: INT
   - total_vulnerabilidades: INT
        | 1
        +-----------------------------------+
        | N                                 | N
 [ COMPONENTE ]                      [ HALLAZGO_VULNERABILIDAD ]
   - id: BIGSERIAL (PK)                - id: BIGSERIAL (PK)
   - analisis_id: BIGINT (FK)          - analisis_id: BIGINT (FK)
   - bom_ref: VARCHAR(255)             - componente_id: BIGINT (FK -> COMPONENTE.id)
   - group_id: VARCHAR(150)            - osv_id: VARCHAR(100)
   - artifact_id: VARCHAR(150)         - cve_id: VARCHAR(50)
   - version: VARCHAR(50)              - severidad_cvss: NUMERIC(3,1)
   - purl: VARCHAR(255)                - nivel_severidad: VARCHAR(20)
   - scope: VARCHAR(30)                - profundidad: INT
   - es_directa: BOOLEAN NOT NULL      - score_prioridad: NUMERIC(4,1)
   - modulo_contexto: VARCHAR(100)     - razones_prioridad: TEXT
        | 1 (padre)     | 1 (hijo)
        +-------+-------+
                | N
 [ RELACION_DEPENDENCIA ] (Aristas del Grafo)
   - id: BIGSERIAL (PK)
   - analisis_id: BIGINT (FK -> ANALISIS.id)
   - padre_componente_id: BIGINT (FK -> COMPONENTE.id)
   - hijo_componente_id: BIGINT (FK -> COMPONENTE.id)
   - tipo_transitiva: BOOLEAN NOT NULL
```

### 3.2 Diagrama de Módulos del Sistema y Responsabilidades
```text
+---------------------------------------------------------------------------------+
|                        DIAGRAMA DE MÓDULOS DEL SISTEMA                          |
+---------------------------------------------------------------------------------+

 FRONTEND (ANGULAR 17+ SPA):
  - Módulo Proyectos: Administración de proyectos y carga del archivo SBOM.
  - Módulo Dashboard: Métricas consolidadas, contadores de criticidad y resumen.
  - Módulo Grafo: Renderizado visual interactivo del árbol con rutas de afectación.
  - Módulo Reportes: Visualización tabular detallada y exportación.
  - Core HTTP: Consumo de API REST, soporte de simulación y manejo de errores.

        | (Peticiones HTTP / JSON - Contrato REST v1.0)
        v

 BACKEND (SPRING BOOT 3 REST API):
  - Ingestion Module: Validación de esquemas CycloneDX y parseo de dependencias.
  - OSV Client: Consulta a API pública con control de resiliencia y timeouts.
  - Prioritization Engine: Algoritmo de cálculo del SecureGraph Score y razones.
  - Graph Service: Consultas SQL jerárquicas recursivas en PostgreSQL.
  - Project & Analysis Service: Gestión de proyectos, análisis y reportes.

        | (Spring Data JPA / JDBC Driver)
        v

 BASE DE DATOS (POSTGRESQL 16):
  - Almacena entidades maestras, relaciones de aristas y hallazgos registrados.
```
