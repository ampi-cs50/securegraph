# UNIVERSIDAD TECNOLÓGICA NACIONAL
## FACULTAD REGIONAL LA PLATA (UTN - FRLP)
### Tecnicatura Universitaria en Programación — Trabajo Final Integrador (TFI)

---

# PROPUESTA DE PROYECTO (GUÍA 002)
## SECUREGRAPH
**Plataforma web para el análisis y la priorización explicable de vulnerabilidades conocidas en dependencias de software mediante grafos**

| Dato | Información |
| :--- | :--- |
| **Grupo** | Grupo N.° 9 |
| **Integrantes** | [Completar Integrante 1] · [Completar Integrante 2] |
| **Carrera** | Tecnicatura Universitaria en Programación |
| **Cuatrimestre** | Segundo cuatrimestre de 2026 |
| **Tutor/a asignado/a** | [COMPLETAR] |
| **Fecha de presentación** | 08/11/2026 |
| **Repositorio del proyecto** | `https://github.com/ampi-cs50/securegraph` |

---

## 1. Datos generales del proyecto

| Campo | Definición |
| :--- | :--- |
| **Nombre del proyecto** | SecureGraph: Plataforma web para el análisis y priorización de vulnerabilidades conocidas en dependencias de software mediante grafos |
| **Nombre alternativo / sigla** | SecureGraph |
| **Área temática** | Ciberseguridad; seguridad de la cadena de suministro de software; análisis de composición de software (SCA) |
| **Tipo de producto** | Aplicación web desacoplada: SPA + API REST |
| **Tecnologías principales** | Angular 17+ y TypeScript; Java 21 y Spring Boot 3.3.5; PostgreSQL 16; Maven; Flyway; Docker Compose; CycloneDX JSON; API OSV |
| **Año / cuatrimestre** | 2026 — Segundo cuatrimestre |
| **Fecha de presentación** | 08/11/2026 |

### 1.1 Integrantes del equipo

| Apellido y nombre | Legajo / DNI | Correo electrónico | Rol |
| :--- | :--- | :--- | :--- |
| **[Completar Integrante 1]** | [COMPLETAR] | [COMPLETAR] | Desarrollo frontend; interfaz, visualización de grafos y experiencia de usuario |
| **[Completar Integrante 2]** | [COMPLETAR] | [COMPLETAR] | Desarrollo backend; persistencia relacional, motor de priorización y APIs |

### 1.2 Tutor/a asignado/a

| Campo | Dato |
| :--- | :--- |
| **Apellido y nombre** | [COMPLETAR] |
| **Correo electrónico** | [COMPLETAR] |
| **Horario de consulta** | Semanal (a acordar con la cátedra) |

---

## 2. Descripción del proyecto

### 2.1 Descripción general
**SecureGraph** es una aplicación web orientada a apoyar la revisión de vulnerabilidades conocidas en dependencias de proyectos Java administrados con Apache Maven. El sistema recibirá inventarios de componentes de software (SBOM) en formato **CycloneDX JSON**, identificará los componentes y las relaciones de dependencia directa y transitiva, consultará información de vulnerabilidades en la API pública **OSV (Open Source Vulnerabilities)** y presentará los hallazgos mediante tablas comparativas, métricas e inspección en un grafo interactivo. Además, propondrá un índice de prioridad explicable (**SecureGraph Score**), basado en la severidad disponible y en características topológicas documentadas del grafo de dependencias. El backend se implementará con Spring Boot 3 y PostgreSQL 16; el frontend, con Angular 17+. El objetivo no será determinar automáticamente la explotabilidad real de una vulnerabilidad en tiempo de ejecución, sino proporcionar información contextual y criterios transparentes para facilitar su revisión por parte de usuarios técnicos.

### 2.2 Contexto y problemática actual
Los proyectos de software modernos incorporan librerías y dependencias de terceros, tanto directas como transitivas. Al aparecer vulnerabilidades conocidas, los equipos de desarrollo necesitan identificar qué componentes están afectados, cómo se relacionan con el proyecto y en qué orden revisar los hallazgos. Un listado plano de vulnerabilidades basado únicamente en el puntaje de severidad CVSS aporta información insuficiente sobre las rutas de dependencia y dificulta la interpretación de los resultados. Esta problemática afecta a desarrolladores, responsables técnicos y equipos que mantienen aplicaciones Java/Maven: sin una representación clara de las relaciones jerárquicas, el análisis manual demanda tiempo y aumenta la posibilidad de pasar por alto dependencias relevantes.

### 2.3 Propuesta de solución
Se propone desarrollar una plataforma que importe un archivo SBOM generado externamente, valide su estructura, persista los componentes y sus relaciones como un Grafo Acíclico Dirigido (DAG) en PostgreSQL y consulte la fuente pública OSV para identificar vulnerabilidades conocidas. El sistema ofrecerá un resumen métrico de hallazgos, un listado filtrable comparativo (Score contextual vs. CVSS puro) y una visualización interactiva del grafo. Un puntaje propio (**SecureGraph Score**), con fórmula y limitaciones documentadas, contribuirá a ordenar la revisión acompañando cada hallazgo con justificaciones legibles, sin reemplazar el juicio técnico humano sobre exposición o impacto. La solución incluirá reportes exportables en formato JSON y un entorno reproducible de ejecución local con Docker Compose.

### 2.4 Descripción del cliente o destinatario
Se trata de un proyecto de inventiva propia con un escenario de validación técnica reproducible. Sus destinatarios son desarrolladores Java, líderes técnicos, docentes y evaluadores de seguridad interesados en auditar dependencias Maven. La validación prevista utilizará casos de prueba representativos y las instancias de seguimiento con el docente tutor.

---

## 3. Objetivos

### 3.1 Objetivo general
Desarrollar una aplicación web que permita identificar vulnerabilidades conocidas en dependencias de proyectos Java/Maven y orientar su revisión mediante una priorización explicable basada en severidad, contexto y relaciones entre componentes.

### 3.2 Objetivos específicos
1. Implementar la importación y validación estructural de archivos SBOM en formato CycloneDX JSON.
2. Registrar proyectos, análisis, componentes y relaciones de dependencia en PostgreSQL 16, permitiendo consultas jerárquicas recursivas (`WITH RECURSIVE`).
3. Integrar consultas a la API pública de OSV y asociar los hallazgos detectados con los componentes identificados del inventario.
4. Definir, documentar e implementar un algoritmo reproducible (**SecureGraph Score**) que genere prioridades numéricas y razones explicables interpretables.
5. Construir una vista de grafo interactiva en Angular que represente nodos, aristas dirigidas y componentes afectados según su nivel de criticidad.
6. Implementar vistas de gestión de proyectos, panel de métricas, filtros por severidad, conmutación de criterio de ordenamiento y exportación de resultados.
7. Realizar pruebas funcionales y de integración, comparando el ordenamiento propuesto por *SecureGraph Score* frente al ordenamiento estándar por *CVSS* en casos reproducibles.

---

## 4. Alcance del proyecto

### 4.1 Incluye
* Registro, administración y consulta de múltiples proyectos Java/Maven junto con el historial completo de los análisis realizados sobre cada uno.
* Importación y validación estructural de archivos SBOM en formato CycloneDX JSON, extrayendo el componente principal, las librerías que lo componen y sus relaciones de dependencia directa y transitiva.
* Almacenamiento persistente de proyectos, análisis, componentes, vínculos del grafo y hallazgos de seguridad en una base de datos relacional PostgreSQL, resolviendo el recorrido jerárquico y el cálculo de profundidad mediante consultas recursivas.
* Consulta automatizada de vulnerabilidades conocidas a través de la API pública de OSV para cada dependencia detectada, contemplando el control de errores de comunicación y tiempos máximos de espera.
* Cálculo determinista de un índice de prioridad de revisión (*SecureGraph Score*) que combina la severidad reportada con la ubicación topológica del componente en el árbol de dependencias, acompañado de una explicación redactada en lenguaje claro sobre los factores considerados.
* Panel de control web (*Dashboard*) con indicadores globales de composición y criticidad, listado comparativo que permite alternar entre el ordenamiento por prioridad contextual y por puntaje CVSS tradicional, y filtros dinámicos por nivel de severidad.
* Visualización gráfica interactiva del grafo de dependencias que distingue visualmente el módulo raíz, las dependencias libres de hallazgos y los componentes vulnerables según su severidad, permitiendo inspeccionar el detalle de cada nodo y sus rutas de conexión.
* Generación y descarga de un informe técnico estructurado en formato JSON con los resultados consolidados del análisis, acompañado de su documentación técnica, pruebas de verificación y entorno de despliegue local reproducible mediante contenedores Docker.

### 4.2 No incluye
* Clonado, compilación o ejecución de código de proyectos de terceros en el servidor backend.
* Análisis estático del código fuente propio (SAST), pruebas dinámicas (DAST) o *pentesting* automatizado.
* Autenticación multiusuario, perfiles y gestión de roles en esta versión.
* Soporte garantizado para ecosistemas de paquetes distintos de Java/Maven.
* Alertas periódicas programadas o monitoreo continuo automatizado en segundo plano.
* Determinación concluyente de explotabilidad en tiempo de ejecución o impacto financiero empresarial.
* Uso de un motor de base de datos de grafos externo dedicado (como Neo4j).

---

## 5. Requerimientos principales del sistema

### 5.1 Requerimientos Funcionales (RF)
* **RF-01 (Gestión de proyectos):** Permitir el alta y consulta de proyectos Java/Maven junto con el resumen de sus análisis asociados.
* **RF-02 (Ingesta de SBOM CycloneDX):** Validar e importar archivos `.json` bajo especificación CycloneDX extrayendo metadatos del componente raíz, librerías (`groupId`, `artifactId`, `version`, `purl`, `scope`) y árbol de dependencias.
* **RF-03 (Persistencia y consulta del grafo):** Almacenar los componentes y aristas dirigidas en PostgreSQL diferenciando relaciones directas y transitivas, calculando la profundidad mediante consultas recursivas.
* **RF-04 (Consulta de vulnerabilidades en OSV):** Consultar la API de OSV para los paquetes del inventario y registrar los identificadores (`CVE` / `GHSA` / `OSV`), severidad y puntaje CVSS disponible.
* **RF-05 (Cálculo de prioridad explicable):** Calcular el *SecureGraph Score* ponderando severidad, profundidad en el grafo, carácter directo/transitivo y alcance, generando las razones explicables de cada hallazgo.
* **RF-06 (Panel comparativo y filtros):** Visualizar métricas globales y permitir ordenar la tabla de hallazgos tanto por *SecureGraph Score* como por *CVSS puro*, filtrando por nivel de severidad.
* **RF-07 (Exploración en grafo interactivo):** Representar visualmente el DAG de dependencias destacando los nodos vulnerables y mostrando el detalle topológico del nodo seleccionado.
* **RF-08 (Exportación de informe):** Generar y descargar un reporte técnico consolidado del análisis en formato JSON.

### 5.2 Requerimientos No Funcionales (RNF)
* **RNF-01 (Desacoplamiento contractual):** Comunicación estrictamente basada en API REST JSON conforme al contrato versionado en `docs/api-contract.md`.
* **RNF-02 (Reproducibilidad):** Base de datos PostgreSQL 16 contenerizada con Docker Compose y esquema versionado automáticamente mediante migraciones Flyway (`V1__init_schema.sql`).
* **RNF-03 (Resiliencia):** Configuración de tiempos máximos de espera en llamadas externas a OSV y soporte de operación desacoplada (*Modo Mock*) para pruebas de interfaz.

---

## 6. Supuestos y restricciones

### 6.1 Supuestos
1. Se dispondrá de archivos SBOM CycloneDX JSON válidos generados a partir de proyectos Maven para las pruebas funcionales y comparativas.
2. La API pública de OSV estará accesible durante las pruebas de integración o podrán utilizarse respuestas controladas ante eventuales fallas del servicio externo.
3. Ambos integrantes contarán con equipos de desarrollo aptos y acceso a las herramientas de código abierto propuestas.
4. El equipo dispondrá de instancias periódicas de revisión con el docente tutor para validar avances y entregas.

### 6.2 Restricciones
1. El desarrollo y sus entregas deberán ajustarse al calendario académico del cuatrimestre (8 semanas) y a las fechas aprobadas por la cátedra.
2. El proyecto será desarrollado por dos integrantes con responsabilidades principales diferenciadas en Frontend y Backend.
3. La primera versión estará acotada a inventarios CycloneDX JSON pertenecientes a proyectos Java/Maven.
4. La persistencia y consulta del grafo se resolverá íntegramente en PostgreSQL mediante tablas relacionales y consultas SQL recursivas, sin incorporar motores de grafos adicionales.
5. Se utilizarán exclusivamente herramientas gratuitas o de código abierto y despliegue local sin depender de servicios en la nube pagos.

---

## 7. Stack tecnológico propuesto

| Componente | Tecnología / herramienta |
| :--- | :--- |
| **Lenguajes** | Java 21; TypeScript 5.4; SQL |
| **Framework backend** | Spring Boot 3.3.5 (Spring Web, Spring Data JPA, Bean Validation) |
| **Framework frontend** | Angular 17+ (Standalone Components, Signals, RxJS) |
| **Base de datos** | PostgreSQL 16; migraciones automatizadas con Flyway |
| **Estándar / integración** | SBOM CycloneDX JSON; API REST pública OSV (`api.osv.dev`) |
| **Modelado y visualización de grafo** | Tablas relacionales con consultas SQL recursivas (`WITH RECURSIVE`) en PostgreSQL; renderizado interactivo SVG de grafos dirigidos (DAG) en Angular |
| **Control de versiones** | Git y GitHub; flujo de ramas `main`, `develop` y `feature/*` |
| **Contenedores** | Docker y Docker Compose |
| **Metodología** | Desarrollo iterativo incremental con tablero Kanban, contrato API previo y revisión por Pull Request |
| **Gestión y documentación** | Markdown en repositorio; documentación académica en PDF y DOCX |

---

## 8. Calendario de principales actividades

Las fechas de presentación, devoluciones e instancia de coloquio se encuentran sujetas a la confirmación del docente tutor y la cátedra.

| # | Actividad / hito | Fecha estimada | Área responsable | Entregable / resultado |
| :---: | :--- | :--- | :--- | :--- |
| **1** | Definición de tema, alcance y arquitectura base | 02–08/11/2026 | Equipo completo | Repositorio base y propuesta |
| **2** | **Presentación de Propuesta de Proyecto (Guía 002)** | **08/11/2026** | Equipo completo | Documento formal en PDF y editable |
| **3** | Especificación de requerimientos y contrato REST v1.0 | 02–08/11/2026 | Equipo completo | `docs/api-contract.md` |
| **4** | Esquema relacional de BD (Flyway V1) y diseño de módulos | 09–15/11/2026 | Backend / Frontend | Esquema SQL, DER y diagrama de módulos |
| **5** | Iteración 1: Parser CycloneDX JSON y vistas de proyectos/carga | 09–15/11/2026 | Backend / Frontend | Ingesta SBOM e interfaz de carga |
| **6** | Iteración 2: Integración con API OSV y panel de hallazgos | 16–22/11/2026 | Backend / Frontend | Consulta OSV y tabla de métricas |
| **7** | Iteración 3: Motor *SecureGraph Score* y grafo interactivo | 23–29/11/2026 | Backend / Frontend | Cálculo explicable y vista DAG |
| **8** | **Presentación de Informe de Avance N.° 1 (Guía 003)** | **29/11/2026** | Equipo completo | 1.° Informe de Avance + diagramas |
| **9** | Pruebas unitarias, de integración, filtros y exportación JSON | 30/11–13/12/2026 | Equipo completo | Suite de pruebas y exportador |
| **10** | **Presentación de Informe de Avance N.° 2 (Guía 003)** | **13/12/2026** | Equipo completo | 2.° Informe de Avance + matriz de pruebas |
| **11** | Evaluación comparativa (Score vs. CVSS) y manuales técnicos | 14–20/12/2026 | Equipo completo | Manual de usuario e implementación |
| **12** | **Entrega de Informe Técnico Final (Guía 004) y Video** | **27/12/2026** | Equipo completo | Informe Final, código en `main` y video |
| **13** | **Coloquio y defensa oral** | [A confirmar] | Equipo completo | Defensa oral del TFI |

### 8.1 Fechas clave del cuatrimestre

| Hito académico | Fecha prevista |
| :--- | :--- |
| **Entrega de Propuesta de Proyecto (Guía 002)** | 08/11/2026 (sujeta a aval de cátedra) |
| **Devolución / aprobación de propuesta** | [A confirmar por el tutor] |
| **Entrega de Informe de Avance N.° 1 (Guía 003 — Regularidad)** | 29/11/2026 |
| **Devolución de Informe de Avance N.° 1** | [A confirmar por el tutor] |
| **Entrega de Informe de Avance N.° 2 (Guía 003)** | 13/12/2026 |
| **Devolución de Informe de Avance N.° 2** | [A confirmar por el tutor] |
| **Entrega de Informe Técnico Final y Video (Guía 004)** | 27/12/2026 |
| **Coloquio final** | [A confirmar por la cátedra] |

---

## 9. Análisis de riesgos

| Riesgo identificado | Probabilidad | Impacto | Estrategia de mitigación |
| :--- | :---: | :---: | :--- |
| **Caídas, límites de tasa o latencia en la API de OSV** | Media | Alto | Configurar *timeouts* acotados, manejo explícito de errores HTTP y datos de prueba locales para garantizar la continuidad de las demostraciones. |
| **Incompatibilidades de integración entre Angular y Spring Boot** | Media | Alto | Mantener el contrato `docs/api-contract.md` sincronizado, emplear DTOs tipados y validar mediante el servicio dual (Mock / API Real). |
| **Archivos SBOM malformados o con referencias incompletas** | Alta | Medio | Validar el esquema JSON al inicio de la carga, rechazar entradas inválidas con código `400 Bad Request` y mensajes claros para el usuario. |
| **Complejidad en grafos extensos o con referencias cíclicas** | Media | Alto | Limitar la cota de profundidad en las consultas `WITH RECURSIVE` de PostgreSQL y organizar visualmente los nodos por nivel jerárquico. |
| **Sobrecarga de alcance o desfasajes en el cronograma** | Media | Alto | Priorizar las funcionalidades del producto mínimo viable (MVP) y realizar entregas incrementales revisadas semanalmente. |
| **Ausencia de puntaje CVSS numérico en hallazgos de OSV** | Media | Medio | Identificar explícitamente los datos ausentes sin fabricar vectores artificiales, aplicando una regla de ponderación documentada basada en la severidad disponible. |

---

## 10. Criterios de éxito y aceptación

1. El sistema permite registrar un proyecto e importar un archivo SBOM CycloneDX JSON válido, rechazando entradas vacías o malformadas con mensajes claros.
2. Las entidades de proyectos, análisis, componentes y relaciones importadas persisten y se recuperan correctamente desde PostgreSQL 16.
3. El sistema consulta la API de OSV y asocia los hallazgos conocidos cuando existen, diferenciándolos de componentes sin vulnerabilidades y de eventuales fallos de red.
4. El motor del **SecureGraph Score** produce resultados deterministas y justificaciones verificables para una misma entrada.
5. El frontend representa de forma interactiva los nodos y aristas provistos por la API y permite inspeccionar los componentes afectados y sus rutas de dependencia.
6. El sistema ofrece un reporte técnico exportable en JSON y cuenta con pruebas documentadas que verifican el flujo principal.
7. El equipo entrega la documentación académica reglamentaria y demuestra el despliegue local reproducible siguiendo el manual de implementación.

---

## 11. Declaración y firmas

Los abajo firmantes declaramos que la información contenida en este documento es correcta y que nos comprometemos a desarrollar el Trabajo Final Integrador en los términos aquí descriptos, bajo la supervisión del docente tutor asignado y con sujeción a las normas académicas de la Universidad Tecnológica Nacional — Facultad Regional La Plata.

| Integrante / responsable | Firma y aclaración | Fecha |
| :--- | :---: | :---: |
| **[Completar Integrante 1]** | ________________________ | ____/____/2026 |
| **[Completar Integrante 2]** | ________________________ | ____/____/2026 |
| **Tutor/a asignado/a** | ________________________ | ____/____/2026 |
| **Conformidad institucional (si corresponde)** | ________________________ | ____/____/2026 |

---

## 12. Glosario de términos

| Término | Definición |
| :--- | :--- |
| **SBOM (Software Bill of Materials)** | Inventario estructurado de los componentes, librerías y dependencias que integran un producto de software. |
| **CycloneDX** | Estándar abierto diseñado para la representación e intercambio de inventarios SBOM y análisis de seguridad en la cadena de suministro. |
| **SCA (Software Composition Analysis)** | Metodología automatizada para identificar componentes de código abierto de terceros y detectar vulnerabilidades o riesgos asociados. |
| **OSV (Open Source Vulnerabilities)** | Base de datos distribuida y API pública de vulnerabilidades conocidas en ecosistemas de código abierto impulsada por Google. |
| **CVSS (Common Vulnerability Scoring System)** | Estándar abierto que asigna una puntuación numérica (0.0 a 10.0) para caracterizar la severidad intrínseca de una vulnerabilidad. |
| **Dependencia transitiva** | Librería incorporada indirectamente en un proyecto por ser requerida por otra dependencia directa o de nivel intermedio. |
| **Grafo Acíclico Dirigido (DAG)** | Estructura de datos compuesta por nodos (componentes) y aristas orientadas (relaciones de dependencia) sin ciclos cerrados. |
| **API REST** | Interfaz de comunicación cliente-servidor basada en el protocolo HTTP para el intercambio estructurado de recursos en formato JSON. |
| **SecureGraph Score** | Índice de priorización contextual propuesto en este proyecto (escala 0 a 100) que combina severidad reportada y topología del grafo para orientar el orden de revisión técnica. |

---

## 13. Referencias y bibliografía

* Angular. (2024). *Angular documentation*. https://angular.dev/
* CycloneDX. (2024). *CycloneDX specification overview*. OWASP Foundation. https://cyclonedx.org/specification/overview/
* FIRST. (2023). *Common Vulnerability Scoring System (CVSS) v3.1 specification document*. Forum of Incident Response and Security Teams. https://www.first.org/cvss/
* Google. (2024). *OSV.dev: Open Source Vulnerabilities documentation*. https://osv.dev/
* PostgreSQL Global Development Group. (2024). *PostgreSQL 16 documentation: Queries with WITH (Common Table Expressions)*. https://www.postgresql.org/docs/16/queries-with.html
* Spring. (2024). *Spring Boot reference documentation*. VMware. https://docs.spring.io/spring-boot/
* Universidad Tecnológica Nacional — Facultad Regional La Plata [UTN FRLP]. (2026). *Guía para la elaboración de la propuesta del Trabajo Final Integrador (Guía 002)*. Tecnicatura Universitaria en Programación.
