# DOCUMENTO FORMAL 1 — PROPUESTA DE PROYECTO (GUÍA 002 - UTN FRLP)

* **Fecha límite de entrega:** 08/11/2026 (Semana 1)
* **Formato de presentación:** Digital en PDF y editable

---

## 1. Datos Generales del Proyecto
* **Nombre del proyecto:** SecureGraph: Plataforma web para el análisis y priorización de vulnerabilidades conocidas en dependencias de software mediante grafos.
* **Nombre alternativo / Sigla:** SecureGraph
* **Área temática / Dominio:** Seguridad en el Ciclo de Vida del Software / Análisis de Composición de Software (SCA)
* **Tipo de producto:** Aplicación Web (SPA + API REST)
* **Tecnologías principales:** Java 21, Spring Boot 3, Angular 17+, PostgreSQL 16, Docker Compose, CycloneDX, OSV API
* **Año / Cuatrimestre:** 2026 — 2° Cuatrimestre
* **Fecha de presentación:** 08/11/2026

## 2. Integrantes del Equipo
| Integrante | Legajo / DNI | Correo Electrónico | Rol en el Proyecto |
| :--- | :--- | :--- | :--- |
| **[Integrante 1]** | [Completar] | [Completar] | Desarrollo Frontend & UI/UX |
| **[Integrante 2]** | [Completar] | [Completar] | Desarrollo Backend & Base de Datos |

## 3. Datos del Tutor Asignado
* **Apellido y Nombre:** [Completar con Docente Tutor de UTN]
* **Correo electrónico:** [Completar]
* **Horario de consulta acordado:** Semanal

## 4. Descripción del Proyecto
* **4.1 Descripción General:**  
  SecureGraph es una aplicación web destinada a orientar a los desarrolladores en la revisión de vulnerabilidades de seguridad en aplicaciones Java gestionadas con Apache Maven. A partir de un inventario estructurado de software (SBOM CycloneDX JSON), el sistema identifica dependencias directas y transitivas, consulta vulnerabilidades conocidas en la fuente pública OSV, almacena las relaciones jerárquicas en PostgreSQL y calcula un puntaje de prioridad explicable (*SecureGraph Score*). Este índice combina severidad, profundidad en el árbol y contexto del módulo. La plataforma presenta un panel interactivo con visualización en grafo que expone gráficamente las rutas hacia los componentes afectados, optimizando los tiempos de análisis frente a listados tradicionales basados únicamente en CVSS.
* **4.2 Contexto y Problemática Actual:**  
  El desarrollo moderno se basa ampliamente en componentes de terceros. Un proyecto promedio incorpora decenas de dependencias directas que arrastran cientos de dependencias transitivas. Los escáneres actuales generan alertas ordenadas por severidad CVSS sin considerar la arquitectura del sistema: una vulnerabilidad crítica en una librería transitiva lejana o utilizada en un módulo aislado puede tener un impacto real menor que una vulnerabilidad media en una dependencia directa de producción. Esto produce fatiga por alertas y dificulta decidir qué hallazgos revisar primero.
* **4.3 Propuesta de Solución:**  
  Construir una solución que transforme el listado plano en una representación de grafo de dependencias, integrando la severidad de OSV con la topología de la aplicación para ofrecer una priorización con justificaciones legibles.
* **4.4 Descripción del Cliente:**  
  Proyecto de inventiva propia orientado a desarrolladores, líderes técnicos y evaluadores de seguridad de software. Cliente de prueba simulado para validación de escenarios de desarrollo.

## 5. Objetivos
* **5.1 Objetivo General:**  
  Desarrollar una aplicación web que permita identificar vulnerabilidades conocidas en las dependencias de un proyecto Java con Maven y orientar su revisión mediante una priorización explicable basada en severidad, contexto y relaciones entre componentes.
* **5.2 Objetivos Específicos:**
  1. Importar un inventario de componentes en formato SBOM CycloneDX JSON con dependencias directas y transitivas.
  2. Consultar la API pública de OSV y asociar los hallazgos de seguridad con los componentes del proyecto.
  3. Almacenar y consultar el modelo de grafo de dependencias utilizando PostgreSQL.
  4. Calcular una prioridad de revisión explicable (*SecureGraph Score*) basada en severidad, contexto y topología.
  5. Visualizar interactivamente mediante un grafo en Angular las relaciones y los caminos hacia los componentes afectados.
  6. Presentar los resultados en un panel de control web y permitir la exportación de un informe técnico.
  7. Comparar la priorización propuesta frente al ordenamiento estándar por CVSS mediante casos de prueba reproducibles.

## 6. Alcance del Proyecto
* **6.1 Lo que incluye:** Registro de proyectos y análisis; importación de SBOMs CycloneDX JSON; consulta automática a OSV; motor de cálculo de score explicable; visualización gráfica interactiva; panel de métricas; exportación de informe; despliegue con Docker Compose.
* **6.2 Lo que NO incluye:** Compilación o clonado de repositorios en el servidor; análisis de código fuente interno (SAST); ecosistemas distintos de Java/Maven; gestión de usuarios y roles; bases de datos de grafos dedicadas.

## 7. Supuestos del Proyecto
1. Los proyectos a analizar cuentan con un SBOM CycloneDX generado previamente mediante Maven.
2. La API pública de OSV se mantendrá operativa y accesible sin restricciones críticas de servicio.
3. El equipo de desarrollo dispone de infraestructura local con soporte para Docker y conexión estable a internet.
4. Las revisiones con el tutor docente se llevarán a cabo en los plazos previstos en el calendario académico.

## 8. Restricciones del Proyecto
1. Plazo improrrogable de desarrollo de 8 semanas correspondiente al cuatrimestre académico.
2. Uso exclusivo de tecnologías de código abierto sin costos de licencias ni servicios en la nube de pago.
3. El modelado y consulta de grafos debe resolverse sobre PostgreSQL mediante sentencias relacionales y recursivas.
4. Aplicación orientada a navegadores web estándar sin requerimientos de extensiones o plugins adicionales.

## 9. Matriz de Riesgos Inicial
| Riesgo Identificado | Probabilidad | Impacto | Estrategia de Mitigación |
| :--- | :--- | :--- | :--- |
| Tiempos lentos o fallas en la API de OSV | Media | Alto | Implementar caché local de vulnerabilidades en PostgreSQL y configurar timeouts estrictos. |
| Dificultad visual al renderizar árboles con muchos nodos | Alta | Medio | Diseñar en Angular filtros por severidad, zoom interactivo y opción de colapsar ramas secundarias. |
| Incompatibilidades en el contrato API entre frontend y backend | Media | Alto | Definir y congelar `docs/api-contract.md` en la Semana 1 y utilizar servicios mock en Angular. |
| Ausencia de puntaje CVSS numérico en ciertas entradas de OSV | Media | Medio | Definir reglas de cálculo por defecto documentadas para asignar una severidad estimativa justificada. |

## 10. Criterios de Éxito Medibles
1. Procesamiento y guardado de un SBOM de hasta 200 dependencias en menos de 5 segundos.
2. Mapeo automático de vulnerabilidades desde OSV sin interrupciones del flujo ante paquetes limpios.
3. Renderizado interactivo del grafo de dependencias en el navegador destacando visualmente los nodos afectados.
4. Cada vulnerabilidad priorizada presenta un puntaje numérico y una explicación textual comprensible de los factores aplicados.
5. Ejecución completa del sistema mediante un único comando (`docker compose up`) en una máquina limpia.
