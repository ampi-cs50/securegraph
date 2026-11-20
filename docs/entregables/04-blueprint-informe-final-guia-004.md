# UNIVERSIDAD TECNOLÓGICA NACIONAL
## FACULTAD REGIONAL LA PLATA (UTN - FRLP)
### Tecnicatura Universitaria en Programación — Trabajo Final Integrador (TFI)

---

# ESTRUCTURA Y CONTENIDO BASE DEL INFORME TÉCNICO FINAL (GUÍA 004 v2.0)

* **Grupo:** Grupo N.° 9
* **Fecha límite de entrega:** 27/12/2026
* **Especificaciones formales exigidas (Sección 2.1 de Guía 004):**
  * **Extensión:** Mínimo 20 páginas / máximo 60 páginas (sin contar anexos).
  * **Tipografía e interlineado:** Arial o Calibri, 11–12 pt, color negro; interlineado 1,5 líneas.
  * **Márgenes:** Izquierdo 3 cm (para anillado); superior, inferior y derecho 2,5 cm.
  * **Numeración:** Romana en preliminares (portada e índices); arábiga centrada al pie en el cuerpo.
  * **Figuras y Tablas:** Todas numeradas y tituladas (`Figura 1...`, `Tabla 1...`), resolución mínima 96 dpi.
  * **Citado:** Formato **APA 7.ª edición** estricto.
  * **Entregables finales:** Archivo PDF único + código fuente en `.zip` + enlace al video de presentación (5 a 10 minutos).

---

## Mapa de Secciones Obligatorias (Guía 004 — Secciones 4 a 16)

### Sección 4. Portada y Resumen Ejecutivo
* **4.1 Portada:**
  * Universidad Tecnológica Nacional — Facultad Regional La Plata — Tecnicatura Universitaria en Programación.
  * **Título:** *SecureGraph: Plataforma web para el análisis y la priorización explicable de vulnerabilidades conocidas en dependencias de software mediante grafos*.
  * **Grupo:** Grupo N.° 9.
  * **Integrantes:** Nombres completos y Legajo / DNI de ambos integrantes.
  * **Docente Tutor:** Ing. Sergio Antonini.
  * **Fecha de entrega:** Diciembre 2026.
  * **Enlace al video de presentación:** `[Insertar URL de YouTube / Google Drive con permisos de lectura]`.
* **4.2 Resumen Ejecutivo (Abstract) — (200 a 300 palabras en 3.ª persona, en español e inglés):**
  * **Resumen (Español):** El desarrollo moderno de aplicaciones empresariales en el ecosistema Java con Apache Maven se apoya extensamente en componentes de código abierto que incorporan múltiples niveles de dependencias transitivas. Las herramientas tradicionales de análisis de composición de software suelen presentar las vulnerabilidades detectadas mediante listados planos ordenados exclusivamente por su severidad intrínseca, sin contemplar la distancia topológica ni el alcance operativo de cada librería dentro de la arquitectura del sistema, lo que ocasiona sobrecarga de alertas y demora la remediación. Para abordar esta problemática, se desarrolló SecureGraph, una plataforma web desacoplada orientada a la identificación, visualización y priorización explicable de vulnerabilidades conocidas en dependencias de software mediante grafos. La solución fue construida utilizando Angular y TypeScript en el cliente, Spring Boot con Java en el servidor y PostgreSQL con migraciones Flyway para la persistencia relacional y la ejecución de consultas jerárquicas recursivas, integrándose con la interfaz de programación de aplicaciones pública de Open Source Vulnerabilities a partir de inventarios estandarizados Software Bill of Materials en formato CycloneDX. El sistema reconstruye el grafo acíclico dirigido de dependencias, asocia los hallazgos de seguridad y calcula el índice SecureGraph Score, proporcionando justificaciones legibles por cada factor ponderado, un tablero comparativo frente al sistema de puntuación de vulnerabilidades comunes, un visualizador interactivo del grafo y exportación de reportes técnicos. Las pruebas de validación demostraron que el enfoque contextual permite distinguir de forma transparente las vulnerabilidades críticas en dependencias directas de aquellas alojadas en niveles transitivos profundos, optimizando la toma de decisiones técnicas durante la auditoría de software.
  * **Abstract (English):** Modern enterprise application development within the Java and Apache Maven ecosystem relies heavily on open-source components that introduce multiple layers of transitive dependencies. Traditional software composition analysis tools typically report detected vulnerabilities as flat lists sorted solely by intrinsic severity, disregarding the topological distance and operational scope of each library within the system architecture, which leads to alert fatigue and delayed remediation. To address this challenge, SecureGraph was developed as a decoupled web platform designed for the identification, visualization, and explainable prioritization of known software dependency vulnerabilities through graphs. The solution was engineered using Angular and TypeScript on the client side, Spring Boot with Java on the server side, and PostgreSQL with Flyway migrations for relational persistence and recursive hierarchical queries, integrating with the public Open Source Vulnerabilities application programming interface from standardized Software Bill of Materials inventories in CycloneDX format. The system reconstructs the directed acyclic dependency graph, maps security findings, and computes the SecureGraph Score index, providing human-readable justifications for each weighted factor, a comparative dashboard against the Common Vulnerability Scoring System, an interactive graph visualizer, and technical report exporting. Validation testing demonstrated that this contextual approach transparently differentiates critical vulnerabilities in direct dependencies from those located in deep transitive layers, streamlining technical decision-making during software security audits.

---

### Sección 5. Introducción (En prosa continua, sin listas)
Debe desarrollar en párrafos continuos: contexto de la seguridad en la cadena de suministro de software, motivación y partes afectadas, objetivo general, metas específicas planteadas, alcance y limitaciones del producto, y organización capitular del informe.

---

### Sección 6. Descripción del Problema y Análisis de Requerimientos
* **6.1 Descripción del problema y Stakeholders:** Desarrolladores Java, líderes técnicos, equipos de DevSecOps y auditores académicos.
* **6.2 Requerimientos Funcionales (Tabla obligatoria):**

| ID | Descripción | Prior. | Criterio de aceptación |
| :---: | :--- | :---: | :--- |
| **RF-01** | Gestión y listado de proyectos Java/Maven con su historial de análisis. | Alta | El usuario registra un proyecto con nombre válido y este persiste en PostgreSQL, listándose con código `200 OK`. |
| **RF-02** | Importación y validación estructural de inventarios SBOM CycloneDX JSON. | Alta | El sistema acepta archivos `.json` válidos (`201 Created`) y rechaza archivos vacíos o de otro formato con error `400 Bad Request`. |
| **RF-03** | Persistencia relacional del grafo de dependencias (nodos y aristas) y cálculo de profundidad. | Alta | Las relaciones padre-hijo se almacenan en `relacion_dependencia` y la consulta `WITH RECURSIVE` retorna la profundidad mínima de cada nodo. |
| **RF-04** | Consulta automatizada de vulnerabilidades conocidas en la API pública de OSV. | Alta | Cada componente es consultado contra OSV registrando `osv_id`, `cve_id`, puntaje CVSS y nivel de severidad. |
| **RF-05** | Cálculo determinista del *SecureGraph Score* y generación de razones explicables. | Alta | Para una misma entrada, el motor calcula idéntico puntaje (0–100) y al menos dos justificaciones textuales verificables. |
| **RF-06** | Panel de control con métricas globales, filtro por severidad y comparación Score vs. CVSS. | Alta | Al alternar el selector entre *Score* y *CVSS*, la tabla reordena dinámicamente la prioridad de los hallazgos. |
| **RF-07** | Visualización interactiva del Grafo Acíclico Dirigido (DAG) e inspector de topología. | Alta | El lienzo SVG dibuja el módulo raíz, dependencias directas y transitivas con colores por severidad y muestra el detalle al seleccionar un nodo. |
| **RF-08** | Exportación de informe técnico consolidado en formato JSON. | Media | Al accionar el botón de exportación, el navegador descarga un archivo `.json` válido con el resumen y hallazgos del análisis. |

* **6.3 Requerimientos No Funcionales (Los 6 atributos de calidad exigidos en pág. 9):**
  1. **Rendimiento:** Respuesta de consultas de grafo y hallazgos en menos de 2 segundos para proyectos de hasta 200 componentes.
  2. **Seguridad:** Ingesta estática de archivos JSON sin ejecución de código arbitrario, validación de tamaño máximo (`15 MB`) y políticas CORS restringidas.
  3. **Disponibilidad:** Tolerancia a fallos de la API externa OSV mediante *timeouts* configurables (`10 s`) y soporte de modo simulado (*Mock Mode*).
  4. **Escalabilidad:** Esquema relacional indexado por claves foráneas (`idx_relacion_padre`, `idx_relacion_hijo`, `idx_hallazgo_analisis_score`).
  5. **Usabilidad:** Interfaz intuitiva con codificación cromática semántica por severidad (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `NONE`).
  6. **Mantenibilidad:** Separación estricta en capas (Controller, Service, Repository, Entity, DTO) y componentes Angular *Standalone*.
* **6.4 Casos de Uso / Historias de Usuario:** Especificar Actores, Precondiciones, Flujo Principal, Flujos Alternativos y Postcondiciones para: *CU-01 Importar SBOM y Ejecutar Análisis*, *CU-02 Comparar Priorización Explicable vs. CVSS* y *CU-03 Explorar Rutas de Afectación en el Grafo*.

---

### Sección 7. Marco Teórico y Tecnologías Seleccionadas
* **7.1 Marco Teórico:** Análisis de Composición de Software (SCA), estándar SBOM CycloneDX, sistema CVSS v3.1, Teoría de Grafos (DAG y recorrido en anchura/profundidad mediante CTE recursivas en bases relacionales).
* **7.2 Tecnologías Seleccionadas y Justificación (Tabla obligatoria por tecnología según pág. 10):**

| Tecnología | Nombre y versión | Propósito | Justificación frente a alternativas | Alternativas consideradas | Licencia |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Backend** | Spring Boot 3.3.5 (Java 21) | API REST, lógica de negocio, cliente OSV y acceso a datos. | Tipado fuerte con Records inmutables en Java 21, ecosistema maduro con Spring Data JPA y Flyway integrado. | Node.js + Express; Python + FastAPI. | Apache 2.0 |
| **Frontend** | Angular 17.3 (TypeScript 5.4) | Aplicación SPA, gestión de estado con Signals y grafo SVG. | Arquitectura empresarial modular, componentes *Standalone*, reactividad nativa con *Signals* y tipado estricto. | React 18; Vue.js 3. | MIT |
| **Base de Datos** | PostgreSQL 16 | Persistencia transaccional y consultas recursivas del grafo (`WITH RECURSIVE`). | Soporte robusto de estándar SQL:1999 para CTE recursivas, evitando mantener dos motores de bases de datos distintos. | Neo4j (Grafo dedicado); MySQL 8. | PostgreSQL License (Open Source) |
| **Migraciones** | Flyway 10 | Control de versiones del esquema SQL. | Ejecución automática y determinista de scripts SQL (`V1__init_schema.sql`) al iniciar el backend. | Liquibase; Hibernate `ddl-auto: update`. | Apache 2.0 |
| **Contenedores** | Docker & Docker Compose | Aislamiento y reproducibilidad de la base de datos local. | Despliegue en un comando con verificación de salud (`pg_isready`) y persistencia en volumen. | Instalación manual local de PostgreSQL. | Apache 2.0 |

---

### Sección 8. Descripción del Proceso de Desarrollo
* **8.1 Metodología adoptada:** Desarrollo iterativo e incremental apoyado en tablero Kanban (Trello / GitHub Projects) y diseño guiado por contrato (*Contract-First API*).
* **8.2 Planificación y cronograma:** Tabla comparativa de las 3 iteraciones y 8 semanas (tareas planificadas vs. completadas).
* **8.3 Narrativa del proceso (en tiempo pasado, 8.3.1 a 8.3.5):** Relevamiento, Diseño, Implementación (Iteraciones 1, 2 y 3), Pruebas y Despliegue.
* **8.4 Control de versiones:** Estrategia Gitflow simplificada (`main`, `develop`, `feature/frontend-*`, `feature/backend-*`) y estándar de *Conventional Commits*.

---

### Sección 9. Arquitectura y Decisiones de Diseño (Peso 20 % en la rúbrica)
* **9.1 y 9.2 Diagrama y descripción de Arquitectura General:** Arquitectura cliente-servidor desacoplada en 3 niveles (SPA Angular, API REST Spring Boot en capas, PostgreSQL 16) con integración externa HTTPS hacia OSV API.
* **9.3 Decisiones de Diseño Relevantes — Los 3 ADRs Obligatorios (Formato oficial de 5 campos de pág. 13):**

#### ADR-01: Persistencia y consulta del grafo de dependencias mediante PostgreSQL y CTEs recursivas
| Campo del ADR | Contenido |
| :--- | :--- |
| **Título** | Uso de PostgreSQL 16 con consultas `WITH RECURSIVE` para modelar y recorrer el grafo de dependencias |
| **Contexto** | Las dependencias de un proyecto Maven conforman un Grafo Acíclico Dirigido (DAG). Se requería almacenar tanto metadatos transaccionales (proyectos, análisis, hallazgos) como relaciones jerárquicas padre-hijo para calcular niveles de profundidad. |
| **Opciones consideradas** | **Opción A:** Utilizar una base de datos relacional (PostgreSQL) junto con un motor de grafos dedicado (Neo4j).<br>**Opción B:** Utilizar exclusivamente PostgreSQL 16 modelando una tabla de aristas (`relacion_dependencia`) y explorándola con consultas SQL recursivas (`WITH RECURSIVE`). |
| **Decisión adoptada** | Se eligió la **Opción B** (PostgreSQL 16 con CTEs recursivas). |
| **Consecuencias** | **Ventajas:** Simplifica drásticamente el despliegue en Docker Compose a un único contenedor de datos, reduce el consumo de memoria RAM y mantiene integridad referencial ACID en una sola transacción.<br>**Desventajas / Deuda técnica:** Requiere establecer una cota máxima de recursión (`profundidad < 15`) e índices explícitos sobre las claves foráneas de origen y destino. |

#### ADR-02: Ingesta estática mediante inventarios SBOM CycloneDX JSON en lugar de ejecución de Maven en servidor
| Campo del ADR | Contenido |
| :--- | :--- |
| **Título** | Adopción de archivos SBOM CycloneDX JSON como único mecanismo de entrada de proyectos |
| **Contexto** | Para conocer las dependencias directas y transitivas de un proyecto Java/Maven, existen dos caminos: clonar el repositorio y ejecutar Maven en el backend, o recibir un inventario SBOM pregenerado. |
| **Opciones consideradas** | **Opción A:** Recibir una URL de Git o un archivo `pom.xml` y ejecutar `mvn dependency:tree` en el servidor backend.<br>**Opción B:** Recibir un archivo estandarizado **SBOM CycloneDX JSON** generado por el usuario con `cyclonedx-maven-plugin`. |
| **Decisión adoptada** | Se eligió la **Opción B** (Ingesta exclusiva de SBOM CycloneDX JSON). |
| **Consecuencias** | **Ventajas:** Elimina riesgos críticos de seguridad por ejecución remota de código arbitrario en el servidor, evita descargas masivas de artefactos Maven durante el análisis y alinea el proyecto con estándares internacionales de ciberseguridad (OWASP CycloneDX).<br>**Desventajas:** Requiere que el usuario genere previamente el archivo JSON desde su entorno de compilación. |

#### ADR-03: Arquitectura SPA desacoplada con servicio dual (Mock/API) y renderizado SVG nativo
| Campo del ADR | Contenido |
| :--- | :--- |
| **Título** | Arquitectura desacoplada Angular 17+ / Spring Boot 3 con contrato REST previo y grafo SVG nativo |
| **Contexto** | El equipo de dos integrantes necesitaba desarrollar en paralelo la interfaz de visualización de grafos y el motor backend de análisis sin bloquearse mutuamente dentro del plazo de 8 semanas. |
| **Opciones consideradas** | **Opción A:** Aplicación monolítica con plantillas renderizadas en el servidor (Spring Boot + Thymeleaf) y librería externa de grafos.<br>**Opción B:** SPA en Angular 17+ conectada vía API REST a Spring Boot, con un servicio conmutable (*Modo Mock / API Real*) y renderizado reactivo de grafos en SVG nativo. |
| **Decisión adoptada** | Se eligió la **Opción B**. |
| **Consecuencias** | **Ventajas:** División limpia de directorios (`frontend/` vs. `backend/`) con cero conflictos de Git, posibilidad de probar y demostrar la interfaz en modo autónomo y control total sobre la estética y rendimiento del grafo SVG sin dependencias pesadas de terceros. |

* **9.4 Modelo de Datos:** Incluye el DER aprobado en el Informe de Avance N° 1 (sin cambios estructurales posteriores) y la descripción de las 5 tablas.
* **9.5 Diagramas de Secuencia o Flujo (Mínimo 2 obligatorios según pág. 14):**
  1. *Diagrama de Secuencia 1:* Flujo de importación de SBOM CycloneDX, persistencia de componentes/aristas, consulta a OSV y cálculo del *SecureGraph Score*.
  2. *Diagrama de Secuencia 2:* Flujo de consulta comparativa de hallazgos (*Score* vs. *CVSS*), renderizado del grafo interactivo y exportación de reporte JSON.
* **9.6 Diseño de la Interfaz de Usuario:** Decisiones de UI/UX (tema oscuro de alto contraste para resaltar colores semánticos de severidad) y capturas numeradas de las 4 vistas.

---

### Sección 10. Implementación del Sistema
* **10.1 Estructura del proyecto:** Árbol de directorios de `securegraph/` (`backend/`, `frontend/`, `docs/`, `samples/`, `docker-compose.yml`).
* **10.2 Módulos principales y fragmentos de código (máx. 20 líneas por fragmento):**
  * Fragmento 1: Consulta SQL `WITH RECURSIVE` en `RelacionDependenciaRepository.java`.
  * Fragmento 2: Lógica de ordenamiento comparativo y filtrado en `AnalisisService.java`.
  * Fragmento 3: Cálculo reactivo de coordenadas del DAG con `computed()` Signals en `GraphViewComponent`.
* **10.3 Integración con sistemas externos:** Especificación de la integración con **OSV API (`https://api.osv.dev/v1`)**, formato de consulta por paquete Maven, ausencia de API Key requerida (pública), *timeouts* y manejo de fallos.

---

### Sección 11. Manual de Usuario (Escrito en imperativo, sin jerga técnica)
* **11.1 Introducción** y captura general de la pantalla principal.
* **11.2 Acceso al sistema** (`http://localhost:4200`) y descripción del selector de origen de datos en la barra superior.
* **11.3 Paso a paso de las 4 funcionalidades:**
  1. *Cómo registrar y consultar un proyecto* ("Haga clic en Gestión de Proyectos...", "Complete el campo Nombre...").
  2. *Cómo importar un archivo SBOM CycloneDX* ("Seleccione el proyecto asociado...", "Adjunte el archivo `.json`...").
  3. *Cómo analizar y comparar vulnerabilidades en el Panel de Hallazgos* ("Alterne entre SecureGraph Score y Severidad CVSS Pura...", "Filtre por severidad...", "Haga clic en Exportar Informe JSON...").
  4. *Cómo explorar el Grafo de Dependencias* ("Haga clic sobre cualquier nodo del diagrama para visualizar su detalle en el panel derecho...").
* **11.4 Preguntas Frecuentes (Las 5 FAQ obligatorias de pág. 16):**
  1. *¿Cómo genero el archivo SBOM CycloneDX JSON de mi proyecto Maven?*
  2. *¿Por qué el sistema rechaza el archivo que intento subir?*
  3. *¿Cuál es la diferencia entre ordenar por SecureGraph Score y ordenar por Severidad CVSS Pura?*
  4. *¿Qué significa que una dependencia sea "Transitiva (Profundidad 3)"?*
  5. *¿Para qué sirve el botón "Cambiar a Modo Mock / API Real" de la barra superior?*

---

### Sección 12. Manual de Implementación (Despliegue)
* **12.1 Tabla de Requisitos del Entorno:** SO (Linux / macOS / Windows 10+), Hardware mínimo (2 CPU, 4 GB RAM, 2 GB disco), Software base (JDK 21, Maven 3.9+, Node.js 20+, Docker 24+), Base de datos (PostgreSQL 16-alpine), Puertos (`4200`, `8080`, `5432`) y Conectividad HTTPS a `api.osv.dev`.
* **12.2 Variables de Entorno (`.env.example`):** `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`, `SERVER_PORT`, `SPRING_DATASOURCE_URL`, `OSV_API_BASE_URL`, `CORS_ALLOWED_ORIGINS`.
* **12.3 Procedimiento de Instalación en 7 pasos verificables** (con comandos exactos y salida esperada como `{"status":"UP"}`).
* **12.4 Actualización y Rollback:** Gestión de nuevas migraciones `V2__*.sql` con Flyway y reversión mediante etiquetas de Git y contenedores.
* **12.5 Respaldo y Recuperación:** Comandos exactos de `docker exec securegraph-postgres pg_dump` y `pg_restore`.
* **12.6 Monitoreo y Logs:** Consulta de logs con `docker compose logs -f db`, salida de consola de Spring Boot y endpoint `/api/health`.

---

### Sección 13. Pruebas y Validación
* **13.1 Tabla de Estrategia de Pruebas** (Unitarias: Sí; Integración: Sí; Sistema E2E: Sí; Usabilidad: Sí; Rendimiento: Sí; Seguridad de entrada: Sí).
* **13.2 Casos de Prueba Documentados (`CP-01` a `CP-06`):** Con Precondiciones, Pasos, Resultado Esperado y Resultado Obtenido.
* **13.3 y 13.4 Métricas de Calidad y Evaluación Comparativa Experimental:** Tabla comparativa del caso `facturacion-core-service` (`log4j-core` directo nivel 1 vs. `snakeyaml` transitivo nivel 3 vs. `jackson-databind` transitivo nivel 2).

---

### Sección 14. Conclusiones
* **14.1 Resultados obtenidos:** Verificación del cumplimiento del Objetivo General y de cada uno de los 7 Objetivos Específicos.
* **14.2 Lecciones aprendidas:** Valor del enfoque *Contract-First*, consultas recursivas SQL y visualización reactiva en Angular.
* **14.3 Limitaciones del sistema actual:** Alcance acotado a Maven/CycloneDX JSON y análisis estático topológico (no evalúa alcanzabilidad de métodos en bytecode o explotabilidad dinámica).
* **14.4 Trabajo futuro:** Soporte para ecosistemas npm/PyPI, análisis de alcanzabilidad (*reachability analysis*) a nivel de llamadas a funciones, e integración directa como plugin en pipelines de CI/CD (GitHub Actions / GitLab CI).

---

### Sección 15. Referencias Bibliográficas (APA 7.ª edición)
Listado alfabético completo coincidente con las citas en el cuerpo del texto.
