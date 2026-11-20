# UNIVERSIDAD TECNOLÓGICA NACIONAL
## FACULTAD REGIONAL LA PLATA (UTN - FRLP)
### Tecnicatura Universitaria en Programación — Trabajo Final Integrador (TFI)

---

# INFORME DE AVANCE — N° 2 (GUÍA 003)

| Campo | Detalle |
| :--- | :--- |
| **Nombre del proyecto** | SecureGraph: Plataforma web para el análisis y priorización de vulnerabilidades conocidas en dependencias de software mediante grafos |
| **Grupo** | Grupo N.° 9 |
| **Número de informe** | N° 2 |
| **Periodo que cubre** | Desde: 30/11/2026 — Hasta: 13/12/2026 |
| **Fecha de presentación** | 13/12/2026 |
| **Integrantes del equipo** | [Integrante 1 — Frontend] -- [Integrante 2 — Backend] |
| **Docente / Tutor asignado** | Ing. Antonini, Sergio |

---

## 1. Identificación del Proyecto

### 1.1 Datos generales del proyecto

| Campo | Definición |
| :--- | :--- |
| **Nombre del proyecto** | SecureGraph: Plataforma web para el análisis y priorización de vulnerabilidades conocidas en dependencias de software mediante grafos |
| **Descripción breve** | Aplicación web que importa inventarios SBOM en formato CycloneDX JSON de proyectos Java/Maven, persiste su árbol de dependencias como un grafo dirigido en PostgreSQL, consulta vulnerabilidades conocidas en la API pública de OSV y calcula un puntaje de priorización explicable (*SecureGraph Score*) acompañado de visualización interactiva del grafo. |
| **Tecnologías principales** | Angular 17+, TypeScript 5.4, Java 21, Spring Boot 3.3.5, PostgreSQL 16, Flyway, Maven, Docker Compose, CycloneDX JSON, API OSV |
| **Repositorio / URL** | `https://github.com/ampi-cs50/securegraph` |

### 1.2 Equipo de trabajo

| Integrante | Rol / Área | Correo de contacto |
| :--- | :--- | :--- |
| **[Integrante 1]** | Frontend, UI/UX, Visualización de Grafos y Pruebas de Usabilidad | [Completar en documento de entrega] |
| **[Integrante 2]** | Backend, Persistencia Relacional, Motor de Score y Pruebas de Integración | [Completar en documento de entrega] |

---

## 2. Estado General del Avance

### 2.1 Porcentaje de avance global

| Avance planificado al cierre del periodo | Avance real al cierre del periodo | Diferencia (+ adelantado / - atrasado) |
| :---: | :---: | :---: |
| **80 %** | **85 %** | **+5 % (Adelantado)** |

### 2.2 Estado del proyecto respecto a la propuesta original

| Indicador | Estado | Observaciones |
| :--- | :--- | :--- |
| **Cronograma** | `[X]` Adelantado `[ ]` En término `[ ]` Atrasado | Se completó la integración extremo a extremo de todos los módulos funcionales y el módulo de exportación JSON antes de la fecha límite del periodo. |
| **Alcance** | `[X]` Sin cambios `[ ]` Ampliado `[ ]` Reducido | Se cumplieron la totalidad de los requerimientos funcionales (RF-01 a RF-08) establecidos en la propuesta. |
| **Calidad** | `[X]` Conforme `[ ]` Con desvíos | Suite de pruebas unitarias, de integración y de usabilidad ejecutada con resultados satisfactorios. |

### 2.3 Resumen narrativo del periodo
Durante este segundo periodo (semanas 5 y 6 del cronograma), el equipo completó la integración final entre el motor de cálculo explicable del backend y las vistas interactivas del frontend, alcanzando un producto funcional completo de punta a punta.

En el módulo Frontend (Angular 17+), se refinó la experiencia de usuario en el visualizador del Grafo Acíclico Dirigido (`GraphViewComponent`), asegurando el resaltado visual del nodo activo y la inspección detallada de las aristas directas y transitivas. Asimismo, se completó la integración del módulo de exportación de reportes técnicos en formato JSON (`GET /api/analisis/{id}/reporte`) desde el Dashboard y se validó el flujo completo de selección de proyecto y subida de archivos SBOM CycloneDX con retroalimentación clara ante errores de formato.

En el módulo Backend (Spring Boot 3 + PostgreSQL 16), se consolidó la capa de servicios (`AnalisisService`), la validación estandarizada de errores HTTP `400 Bad Request` en la ingesta de archivos y la batería de pruebas automatizadas con JUnit 5 y `MockMvc`. Además, se llevó a cabo la evaluación comparativa experimental entre el ordenamiento propuesto por *SecureGraph Score* y el ordenamiento tradicional por *Severidad CVSS Pura*, verificando cómo el algoritmo contextual prioriza dependencias directas críticas en producción y penaliza dependencias transitivas profundas.

Para la etapa final (semanas 7 y 8), el equipo se enfocará exclusivamente en la redacción y revisión de formato APA 7.ª edición del Informe Técnico Final (Guía 004), la elaboración de los manuales de usuario y despliegue, y la grabación del video demostrativo de 5 a 10 minutos.

---

## 3. Actividades del Periodo

### 3.1 Actividades planificadas para el periodo

| Actividad / Tarea | Fecha inicio planeada | Fecha fin planeada | Estado |
| :--- | :---: | :---: | :--- |
| 1. Finalización de Iteración 3: ajuste fino de visualizador DAG y explicabilidad | 30/11/2026 | 04/12/2026 | `[ ]` No iniciada `[ ]` En curso `[X]` Finalizada |
| 2. Implementación y prueba del módulo de exportación de reporte técnico JSON | 02/12/2026 | 07/12/2026 | `[ ]` No iniciada `[ ]` En curso `[X]` Finalizada |
| 3. Ejecución de pruebas unitarias, de integración y de usabilidad | 05/12/2026 | 11/12/2026 | `[ ]` No iniciada `[ ]` En curso `[X]` Finalizada |
| 4. Evaluación comparativa experimental (*SecureGraph Score* vs. *CVSS Puro*) | 08/12/2026 | 13/12/2026 | `[ ]` No iniciada `[ ]` En curso `[X]` Finalizada |
| 5. Redacción preliminar de manuales y estructura del Informe Técnico Final | 10/12/2026 | 20/12/2026 | `[ ]` No iniciada `[X]` En curso `[ ]` Finalizada |

### 3.2 Actividades iniciadas en el periodo

| Actividad | Fecha inicio real | Responsable(s) | Observaciones |
| :--- | :---: | :--- | :--- |
| Integración de endpoint `/api/analisis/{id}/reporte` con descarga Blob en Angular | 30/11/2026 | Ambos integrantes | Respeta el criterio de ordenamiento activo (`score` o `cvss`). |
| Pruebas automatizadas de controladores REST (`AnalisisControllerTest`) | 03/12/2026 | Backend | Cobertura de `/resumen`, `/hallazgos`, `/grafo` y `/reporte`. |
| Validación experimental comparativa con `sample-maven-cyclonedx.json` | 07/12/2026 | Ambos integrantes | Documentación del comportamiento en dependencias de nivel 1, 2 y 3. |

### 3.3 Actividades finalizadas en el periodo

| Actividad | Fecha finalización | Responsable(s) | Entregable generado |
| :--- | :---: | :--- | :--- |
| Visualizador de grafo interactivo con cálculo dinámico de coordenadas SVG | 04/12/2026 | Frontend | `graph-view.component.ts` |
| Servicio y endpoint de reporte técnico consolidado | 06/12/2026 | Backend | `ReportResponseDto.java` y `AnalisisService.java` |
| Batería de pruebas unitarias, de integración y usabilidad | 11/12/2026 | Ambos integrantes | Matriz de pruebas (Sección 7.3) y tests JUnit |

### 3.4 Actividades pendientes o en curso al cierre del periodo

| Actividad | % completado | Responsable(s) | Próximos pasos |
| :--- | :---: | :--- | :--- |
| Redacción del Informe Técnico Final (Guía 004) y Manuales | **45 %** | Ambos integrantes | Completar capturas numeradas del Manual de Usuario y revisión de estilo APA 7. |
| Producción y grabación del Video de Presentación (5 a 10 min) | **20 %** | Ambos integrantes | Grabar demostración en vivo del sistema y publicar enlace para la portada del informe. |

---

## 4. Contribución Individual de Cada Integrante

### Integrante 1 (Desarrollo Frontend y Visualización)

| Aspecto | Detalle |
| :--- | :--- |
| **Tareas realizadas en el periodo** | 1. Optimización del componente `GraphViewComponent` con señales computadas (`positionedNodes` y `positionedEdges`) y resaltado de selección.<br>2. Conexión del selector de proyectos en `UploadSbomComponent` con el listado dinámico proveniente del servicio.<br>3. Integración de la descarga de reportes JSON en `DashboardComponent` consumiendo `getReporte()`.<br>4. Ejecución de pruebas de usabilidad y validación visual en navegadores Chrome y Firefox. |
| **Horas dedicadas (estimadas)** | **Total: 32 hs** |
| **Dificultades encontradas** | Asegurar que las etiquetas de texto y flechas orientadas en el SVG mantuvieran legibilidad al variar la longitud de los nombres de artefactos Maven. |
| **Logros / aprendizajes destacados** | Truncamiento inteligente de etiquetas en el lienzo manteniendo el nombre completo y metadatos en el inspector lateral de topología. |

### Integrante 2 (Desarrollo Backend y Persistencia)

| Aspecto | Detalle |
| :--- | :--- |
| **Tareas realizadas en el periodo** | 1. Implementación de `ReportResponseDto` y del método `obtenerReporte()` en `AnalisisService`.<br>2. Estandarización de respuestas de error HTTP `400 Bad Request` en `ProyectoController` ante archivos vacíos o sin extensión `.json`.<br>3. Desarrollo de pruebas unitarias y de tajada web en `AnalisisControllerTest` y `HealthControllerTest`.<br>4. Sistematización de los resultados comparativos entre *SecureGraph Score* y *CVSS* para el capítulo de validación del Informe Final. |
| **Horas dedicadas (estimadas)** | **Total: 32 hs** |
| **Dificultades encontradas** | Garantizar la consistencia del formato de fechas ISO-8601 (`Instant` / `LocalDateTime`) entre las respuestas de Spring Boot y las interfaces de Angular. |
| **Logros / aprendizajes destacados** | Uso de `java.time.Instant` en los Records DTO para serialización directa compatible con el cliente TypeScript. |

---

## 5. Aspectos Positivos y Negativos del Periodo

### 5.1 Aspectos positivos

| # | Descripción del aspecto positivo | Área / Categoría |
| :---: | :--- | :--- |
| **1** | La totalidad de los endpoints definidos en el contrato API v1.0 se encuentran implementados y verificados con pruebas automatizadas. | Técnico |
| **2** | La comparación experimental en el Dashboard demuestra claramente cómo la priorización por grafo evita que vulnerabilidades transitivas lejanas desplacen a dependencias directas críticas. | Técnico |
| **3** | El cumplimiento anticipado del desarrollo deja las últimas dos semanas libres para dedicarlas a la calidad documental del Informe Final y al video. | Gestión / Equipo |

### 5.2 Aspectos negativos o dificultades

| # | Descripción del aspecto negativo / dificultad | Área | Acción tomada o planificada |
| :---: | :--- | :--- | :--- |
| **1** | Variabilidad en la estructura de severidades devuelta por entradas históricas de OSV. | Técnico / Externo | Se documentó en las limitaciones del sistema y en la regla de cálculo del score el tratamiento determinista de severidades. |

---

## 6. Gestión de Riesgos

### 6.1 Riesgos ocurridos durante el periodo

| Descripción del riesgo ocurrido | Probabilidad | Impacto | Plan de mitigación / Acción tomada |
| :--- | :---: | :---: | :--- |
| **Ingreso de archivos vacíos o con extensiones distintas a `.json` en el endpoint de análisis** | Alta | Medio | Se implementó doble validación (en el cliente Angular y en `ProyectoController` de Spring Boot) devolviendo mensajes claros sin interrumpir el servicio. |

### 6.2 Riesgos identificados para el próximo periodo

| Descripción del riesgo potencial | Probabilidad | Impacto | Plan de prevención / mitigación |
| :--- | :---: | :---: | :--- |
| **Extensión o formato fuera de norma en el Informe Técnico Final (Guía 004)** | Media | Alto | Aplicar estrictamente la lista de verificación de la Guía 004 (20 a 60 páginas, APA 7, numeración de figuras/tablas y 3 ADRs obligatorios). |

---

## 7. Estado de los Entregables y Calidad

### 7.1 Entregables del periodo

| Entregable | Estado | Versión / Fecha | Link / Ubicación |
| :--- | :--- | :---: | :--- |
| **Módulo de Visualización Interactiva de Grafo (DAG)** | `[ ]` Pendiente `[ ]` En progreso `[X]` Completo | v1.0 — 04/12/2026 | `frontend/src/app/pages/graph-view/` |
| **Módulo de Exportación de Reportes Técnicos JSON** | `[ ]` Pendiente `[ ]` En progreso `[X]` Completo | v1.0 — 06/12/2026 | `AnalisisController.java` y `dashboard.component.ts` |
| **Suite de Pruebas Automatizadas Backend** | `[ ]` Pendiente `[ ]` En progreso `[X]` Completo | v1.0 — 11/12/2026 | `backend/src/test/java/ar/edu/utn/frlp/securegraph/` |
| **Inventario SBOM CycloneDX de Prueba Reproducible** | `[ ]` Pendiente `[ ]` En progreso `[X]` Completo | v1.0 — 07/12/2026 | `samples/sample-maven-cyclonedx.json` |

### 7.2 Diagramas obligatorios (Informe de Avance N° 1)
*No aplica modificación en este periodo: tanto el Esquema de la Base de Datos (DER) como el Diagrama de Módulos presentados y aprobados en el Informe de Avance N° 1 se mantienen vigentes sin alteraciones estructurales.*

### 7.3 Calidad del software — Pruebas realizadas

| Módulo / Funcionalidad | Tipo de prueba | Responsable | Resultado | Observaciones |
| :--- | :--- | :--- | :---: | :--- |
| Ingesta y validación de archivo SBOM (`POST /api/proyectos/{id}/analisis`) | Unitaria / Integración | Backend | `[X]` OK `[ ]` Fallo `[ ]` Parcial | Verifica aceptación de `.json` válido (`201 Created`) y rechazo de archivo vacío o inválido (`400 Bad Request`). |
| Consulta de Resumen y Hallazgos Priorizados (`GET /api/analisis/{id}/hallazgos`) | Unitaria (`MockMvc`) | Backend | `[X]` OK `[ ]` Fallo `[ ]` Parcial | Comprueba ordenamiento descendente por `score` (98.5, 62.0, 54.0) y por `cvss` (10.0, 7.5, 5.9), más filtro por severidad. |
| Consulta de Grafo y Reporte Consolidado (`/grafo` y `/reporte`) | Unitaria (`MockMvc`) | Backend | `[X]` OK `[ ]` Fallo `[ ]` Parcial | Verifica integridad de nodos, aristas (`DIRECT`/`TRANSITIVE`) y metadatos de exportación. |
| Flujo E2E: Alta de Proyecto → Carga de SBOM → Dashboard → Exportación JSON | Funcional / E2E | Ambos integrantes | `[X]` OK `[ ]` Fallo `[ ]` Parcial | Recorrido completo verificado tanto en Modo Mock como conectado al backend local. |
| Interacción con el Grafo de Dependencias e Inspector de Topología | Usabilidad | Frontend | `[X]` OK `[ ]` Fallo `[ ]` Parcial | Verificado en Chrome y Firefox con respuesta inmediata al seleccionar cada nodo. |

---

## 8. Comunicación y Reuniones

### 8.1 Reuniones del equipo

| Fecha | Duración | Participantes | Principales temas tratados |
| :---: | :---: | :--- | :--- |
| 02/12/2026 | 45 minutos | Ambos integrantes | Integración del endpoint de reportes y revisión de estilos visuales sin emojis ni datos innecesarios. |
| 08/12/2026 | 60 minutos | Ambos integrantes | Ejecución de la matriz de pruebas y análisis comparativo de resultados sobre `sample-maven-cyclonedx.json`. |
| 12/12/2026 | 50 minutos | Ambos integrantes | Cierre del Informe de Avance N° 2 y distribución de capítulos del Informe Técnico Final (Guía 004). |

### 8.2 Reuniones / consultas con el tutor

| Fecha | Temas abordados | Acuerdos / Decisiones tomadas |
| :---: | :--- | :--- |
| 29/11/2026 | Entrega y revisión del Informe de Avance N° 1 (con DER y diagrama de módulos). | Continuidad hacia la etapa de pruebas integrales y preparación del Informe de Avance N° 2. |

---

## 9. Planificación del Próximo Periodo

### 9.1 Objetivos para el próximo periodo

| Objetivo / Tarea planificada | Responsable(s) | Fecha estimada de finalización |
| :--- | :--- | :---: |
| 1. Redactar los capítulos 4 a 10 del Informe Técnico Final (Introducción, Requerimientos, Marco Teórico, Proceso, Arquitectura con 3 ADRs e Implementación) | Ambos integrantes | 20/12/2026 |
| 2. Elaborar el Manual de Usuario (con capturas numeradas y 5 FAQ) y el Manual de Implementación (con comandos Docker y verificación) | Ambos integrantes | 22/12/2026 |
| 3. Grabar y editar el Video de Presentación (5 a 10 minutos, mostrando el sistema en funcionamiento) | Ambos integrantes | 24/12/2026 |
| 4. Revisión final de formato APA 7.ª edición, fusión a rama `main` y entrega formal del Informe Técnico Final (Guía 004) | Ambos integrantes | 27/12/2026 |

### 9.2 Recursos o apoyos que el equipo necesita del tutor o de la cátedra
Confirmación de las pautas y fechas disponibles para la instancia de coloquio y defensa oral una vez entregado el Informe Técnico Final y el video demostrativo.

---

## 10. Reflexión del Equipo

### 10.1 ¿Qué aprendimos como equipo en este periodo?
Comprobamos empíricamente cómo la representación en grafo cambia la comprensión de la seguridad de un proyecto Java: al visualizar que una vulnerabilidad media o alta está a 2 o 3 saltos transitivos detrás de un starter de Spring Boot mientras que otra de máxima severidad es dependencia directa del módulo principal, la toma de decisiones del desarrollador se vuelve inmediata y fundamentada.

### 10.2 ¿Qué haríamos diferente?
Automatizaríamos desde la primera semana la generación de reportes de cobertura de pruebas en el pipeline de GitHub para contar con métricas históricas de evolución en cada Pull Request.

### 10.3 Nivel de satisfacción del equipo con el periodo

| Dimensión | Puntuación | Comentarios |
| :--- | :--- | :--- |
| **Avance técnico** | `[ ]`1 `[ ]`2 `[ ]`3 `[ ]`4 `[X]`5 | Todos los módulos previstos en el alcance funcionan de manera integrada y estable. |
| **Trabajo en equipo** | `[ ]`1 `[ ]`2 `[ ]`3 `[ ]`4 `[X]`5 | Colaboración equitativa en las pruebas de integración y en la documentación técnica. |
| **Comunicación interna** | `[ ]`1 `[ ]`2 `[ ]`3 `[ ]`4 `[X]`5 | Reuniones breves y efectivas apoyadas en el tablero de tareas. |
| **Gestión del tiempo** | `[ ]`1 `[ ]`2 `[ ]`3 `[ ]`4 `[X]`5 | El proyecto se encuentra un 5 % adelantado respecto al cronograma original. |

---

## 11. Declaración y Firmas

Los integrantes del equipo declaramos que la información contenida en este informe es veraz y refleja fielmente el estado real del proyecto al cierre del periodo indicado.

| Integrante / Rol | Firma y aclaración | Fecha |
| :--- | :---: | :---: |
| **[Integrante 1 — Frontend]** | ________________________ | 13/12/2026 |
| **[Integrante 2 — Backend]** | ________________________ | 13/12/2026 |

### RECIBIDO Y REVISADO POR EL TUTOR

| Firma del tutor | Aclaración | Fecha de revisión |
| :---: | :---: | :---: |
| ________________________ | Ing. Sergio Antonini | ____/____/2026 |

* **Observaciones del tutor:** ____________________________________________________________________
