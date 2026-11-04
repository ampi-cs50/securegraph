# DOCUMENTO FORMAL 4 — BLUEPRINT DEL INFORME TÉCNICO FINAL (GUÍA 004 - UTN FRLP)

* **Fecha límite de entrega:** 27/12/2026 (Semana 8)
* **Extensión:** 20 a 60 páginas | **Formato:** APA 7.ª edición

---

## 1. Estructura Obligatoria de Capítulos
1. **Portada Reglamentaria:** Institución, título, datos de los autores, Tutor, Fecha y **Enlace al Video Demostrativo**.
2. **Resumen Ejecutivo (Abstract):** 200 a 300 palabras en español e inglés.
3. **Índices:** General, Figuras y Tablas.
4. **Introducción:** Contexto de seguridad en software, motivación, objetivos y alcance.
5. **Descripción del Problema y Requerimientos:** Requerimientos Funcionales (RF-01 a RF-10) y No Funcionales (Rendimiento, Usabilidad, Portabilidad, Mantenibilidad).
6. **Marco Teórico y Tecnologías:** SCA, SBOM, CycloneDX, CVSS, Teoría de Grafos y justificación comparada de tecnologías.
7. **Proceso de Desarrollo:** Metodología iterativa, seguimiento por semanas y resolución de desvíos.
8. **Arquitectura y Decisiones de Diseño:** Diagramas de arquitectura, modelo de datos y **los 3 ADRs obligatorios**.
9. **Implementación del Sistema:** Estructura de código en `backend/` y `frontend/`, componentes principales.
10. **Manual de Usuario:** Guía ilustrada con capturas de pantalla numeradas y preguntas frecuentes (FAQ).
11. **Manual de Implementación (Despliegue):** Requisitos, variables de entorno y comandos verificables con Docker Compose.
12. **Pruebas y Validación:** Casos de prueba, métricas de cobertura y evaluación experimental comparativa.
13. **Conclusiones y Trabajos Futuros:** Cumplimiento de objetivos, limitaciones reconocidas y líneas futuras.
14. **Referencias Bibliográficas:** Formato APA 7.ª edición.
15. **Anexos:** Scripts SQL y contratos JSON.

---

## 2. Los 3 Registros de Decisión de Arquitectura (ADRs Obligatorios)

### ADR 01: Persistencia y Análisis de Grafos en PostgreSQL Relacional
* **Contexto:** Las dependencias del software constituyen un grafo dirigido. Se debía elegir entre montar un motor específico de grafos (Neo4j) o utilizar PostgreSQL.
* **Opciones:** 1) Base de datos de grafos externa (Neo4j). 2) Modelado relacional con consultas recursivas (`WITH RECURSIVE`) en PostgreSQL.
* **Decisión:** Opción 2. PostgreSQL resuelve la topología sin incorporar un segundo motor de datos, optimizando el consumo de hardware y simplificando el despliegue con Docker Compose.
* **Consecuencias:** Despliegue más liviano y atómico. Requiere indexación sobre claves foráneas para árboles profundos.

### ADR 02: Ingesta Exclusiva mediante SBOM CycloneDX JSON
* **Contexto:** Mecanismo de entrada para analizar proyectos Java/Maven.
* **Opciones:** 1) Clonar código desde Git y ejecutar Maven en el servidor. 2) Importar un SBOM CycloneDX JSON generado previamente.
* **Decisión:** Opción 2.
* **Consecuencias:** Elimina riesgos de ejecución de código arbitrario en el backend y evita un consumo elevado de recursos en compilaciones, circunscribiendo el alcance al análisis estático de inventarios estandarizados.

### ADR 03: Arquitectura SPA Desacoplada (Angular + Spring Boot REST)
* **Contexto:** Patrón de arquitectura cliente-servidor.
* **Opciones:** 1) Monolito con plantillas del lado del servidor (Spring Boot + Thymeleaf). 2) Arquitectura desacoplada: SPA en Angular conectada vía REST a Spring Boot.
* **Decisión:** Opción 2.
* **Consecuencias:** Permite separar las responsabilidades de presentación (Frontend) y lógica de negocio/persistencia (Backend), facilitando el desarrollo modular mediante el contrato `api-contract.md` y la representación interactiva en el cliente.
