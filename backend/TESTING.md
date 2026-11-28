# SecureGraph — Auditoría de pruebas de la etapa 1

Base auditada: `develop` en `2fa2371` (PR #11). Alcance: backend de
RF-01 a RF-08. Se revisaron las 13 clases existentes, el código de los
controladores, el [contrato REST](../docs/api-contract.md) y la documentación
funcional del proyecto. Los cambios se limitan a pruebas, configuración de
JaCoCo y este documento.

## Estrategia y trazabilidad

JUnit 5 cubre reglas y casos de frontera. MockMvc verifica rutas, códigos,
filtros y cuerpos JSON. MockRestServiceServer simula respuestas HTTP de OSV
en forma determinista. Las pruebas PostgreSQL ejercitan SQL recursivo,
Flyway, escritura y rollback reales y lecturas mediante MockMvc. Sus
respuestas OSV son controladas para que el resultado no dependa de Internet.

| RF | Clases auditadas | Comportamientos cubiertos |
| --- | --- | --- |
| RF-01 | `ProyectoControllerTest` | Alta y listado, historial, proyecto sin análisis, validación de nombre y error de cinco campos. |
| RF-02 | `SbomControllerTest`, `DependencyGraphPostgresTest`, `VulnerabilityScanPostgresTest` | CycloneDX 1.2 a 1.6, esquema, archivo y JSON inválidos, claves y referencias duplicadas, extremos desconocidos, ciclos, proyecto inexistente y persistencia del SBOM de ejemplo. |
| RF-03 | `DependencyGraphServiceTest`, `DependencyGraphPostgresTest` | Raíz, menor profundidad, caminos múltiples, ciclos persistidos, desconectados, profundidad superior a 15 y aislamiento. |
| RF-04 | `OsvClientTest`, `VulnerabilityScanServiceTest`, `VulnerabilityScanPostgresTest` | Solicitud Maven, paginación y protección ante token repetido, severidad y CVSS, alias y deduplicación, respuesta vacía, persistencia, HTTP 502/503 y rollback. |
| RF-05 | `SecureGraphScoreEngineTest`, `SecureGraphScoreMigrationPostgresTest`, `VulnerabilityScanPostgresTest` | Límites 0 y 100, ponderaciones, profundidad ausente, valores inválidos, CVSS ausente, cuatro razones, reproducibilidad, persistencia y backfill V4. |
| RF-06 | `AnalisisServiceTest`, `AnalisisControllerTest`, `AnalysisQueryPostgresTest`, `VulnerabilityScanPostgresTest` | Resumen, orden score/CVSS, empate por ID, CVSS nulo al final, filtro sin alterar totales, cero vulnerabilidades con dependencias, parámetros, 404 y aislamiento. |
| RF-07 | `AnalisisServiceTest`, `AnalisisControllerTest`, `AnalysisQueryPostgresTest`, `DependencyGraphPostgresTest` | Nodos y aristas, severidad máxima, raíz, desconectados y exclusión de relaciones cruzadas. |
| RF-08 | `AnalisisControllerTest`, `AnalysisQueryPostgresTest` | Informe JSON, metadatos, orden, hallazgos y razones del análisis correcto, 400 y 404. |
| Salud | `HealthControllerTest` | Código y forma del JSON. La conectividad con la base es un defecto funcional documentado abajo. |

### Resultado por clase

| Clase | Base | Actual | Omitidas aquí |
| --- | ---: | ---: | ---: |
| `AnalisisServiceTest` | 2 | 2 | 0 |
| `AnalysisQueryPostgresTest` | 3 | 3 | 3 |
| `DependencyGraphPostgresTest` | 4 | 4 | 4 |
| `DependencyGraphServiceTest` | 1 | 1 | 0 |
| `OsvClientTest` | 5 | 14 | 0 |
| `SecureGraphScoreEngineTest` | 19 | 19 | 0 |
| `SecureGraphScoreMigrationPostgresTest` | 1 | 1 | 1 |
| `VulnerabilityScanPostgresTest` | 2 | 4 | 4 |
| `VulnerabilityScanServiceTest` | 1 | 1 | 0 |
| `AnalisisControllerTest` | 14 | 14 | 0 |
| `HealthControllerTest` | 1 | 1 | 0 |
| `ProyectoControllerTest` | 4 | 5 | 0 |
| `SbomControllerTest` | 22 | 25 | 0 |
| **Total** | **79** | **94** | **12** |

Se conservaron los 79 casos base y se agregaron 15. Pasaron 82 casos,
incluidos 69 de la base y 13 nuevos, con cero fallos y errores. Los 12
omitidos requieren PostgreSQL. Diez son de la base y dos son nuevos:
rollback por 502 después de una consulta OSV exitosa e importación con
dependencias y cero hallazgos. La validación anterior de 79 casos contra
PostgreSQL 16.16 pertenece a la base, según el resultado aportado para
esta auditoría.

## Contrato REST y hallazgos de auditoría

| Ruta | Revisión |
| --- | --- |
| `GET /api/health` | Coinciden la ruta, el código y los campos. El controlador no comprueba la conexión que describe el contrato. |
| `GET /api/proyectos`, `POST /api/proyectos` | Coinciden los campos y códigos 200/201/400 de los casos válidos y de Bean Validation. El ejemplo de `createdAt` tiene sufijo UTC y el DTO actual usa `LocalDateTime`. |
| `POST /api/proyectos/{id}/analisis` | Multipart `file`, 201, rechazo de SBOM con 400, proyecto ausente con 404 y traducción de OSV a 502/503. La regresión 502 con base real quedó pendiente de ejecución. |
| `GET /api/analisis/{id}/{resumen,hallazgos,grafo,reporte}` | Coinciden rutas, campos y errores contemplados. El orden, filtros y respuestas vacías tienen cobertura. CVSS y profundidad pueden ser nulos y la severidad puede ser `UNKNOWN`, casos que los ejemplos del contrato no ilustran. |

**Defectos funcionales.** `HealthController` responde 200 y `UP` sin
consultar PostgreSQL. Además, el contrato documenta un error uniforme de
cinco campos y el tratamiento global cubre excepciones específicas. Un
JSON ilegible en el alta de proyecto o un ID no numérico en una importación
SBOM queda a cargo del manejo estándar de Spring, sin garantía del campo
`message`. El `createdAt` de proyectos carece del offset que muestra el
ejemplo del contrato. La última diferencia es de representación y
requiere acordar la zona horaria antes de cambiarla.

**Brechas de testing.** Se agregaron casos que distinguen errores del
parser, límites de severidad de OSV, paginación repetida, proyectos sin
análisis y errores transaccionales intermedios. Quedan pendientes pruebas
de alcance más amplio para indisponibilidad real de la base, límites
multipart en un servidor embebido y contrato de errores de deserialización.
El listado de proyectos también hace una consulta de historial por
proyecto. Es un riesgo de rendimiento separado del cumplimiento REST.

## Ejecución y cobertura

JaCoCo 0.8.11 admite Java 21. `prepare-agent` instrumenta las pruebas y
`report` se ejecuta en `verify`. No hay exclusiones de paquetes ni clases.
El informe contiene todos los bytecodes de producción, incluida la
migración Java V4. Los SQL de Flyway no son bytecode Java.

```powershell
mvn -B -ntp -f backend/pom.xml clean verify
```

El comando genera `backend/target/site/jacoco/index.html` y
`backend/target/site/jacoco/jacoco.xml`. Para ejecutar además las
integraciones sobre una base desechable, desde la raíz:

```powershell
docker compose up -d --wait db
if ($LASTEXITCODE) { throw 'PostgreSQL no se inició' }
$db = "securegraph_testing_$(Get-Date -Format yyyyMMddHHmmssfff)"
docker compose exec -T db createdb -U securegraph_user $db
if ($LASTEXITCODE) { throw 'No se pudo crear la base de pruebas' }
$env:SECUREGRAPH_TEST_POSTGRES_URL = "jdbc:postgresql://localhost:5432/$db"
try {
    mvn -B -ntp -f backend/pom.xml clean verify
    if ($LASTEXITCODE) { throw 'Falló la suite de verificación' }
} finally {
    Remove-Item Env:SECUREGRAPH_TEST_POSTGRES_URL
}
```

En el entorno de auditoría, Docker rechazó el acceso al daemon y
Maven Central no resolvió por DNS. El plugin Maven no se pudo descargar.
Se ejecutó `clean test` con un POM temporal equivalente al original y
el agente JaCoCo 0.8.11. Los formatos HTML/XML se generaron con las
bibliotecas JaCoCo 0.8.11 sobre las mismas 56 clases de producción y los
datos de esa ejecución. La tabla mide la ejecución parcial. Los guiones
indican que el paquete no contiene elementos de ese tipo.

| Paquete | Instrucciones | Líneas | Ramas |
| --- | ---: | ---: | ---: |
| **Global** | 90.71% (3252/3585) | 87.50% (665/760) | 72.47% (229/316) |
| backend.service | 93.61% (2477/2646) | 91.03% (467/513) | 75.33% (226/300) |
| backend.web.controller | 94.17% (210/223) | 96.08% (49/51) | 50.00% (2/4) |
| backend.config | 100.00% (43/43) | 100.00% (6/6) | — |
| backend.web.dto | 93.33% (168/180) | 90.00% (9/10) | — |
| backend | 37.50% (3/8) | 33.33% (1/3) | — |
| backend.domain.entity | 86.06% (284/330) | 85.71% (120/140) | 0.00% (0/4) |
| backend.repository | — | — | — |
| db.migration | 0.00% (0/86) | 0.00% (0/23) | 0.00% (0/6) |
| backend.web.exception | 97.10% (67/69) | 92.86% (13/14) | 50.00% (1/2) |

## Correcciones de cumplimiento sobre `12a6104`

La auditoría anterior queda como referencia histórica. Las correcciones de
esta revisión se limitan a los tres hallazgos funcionales documentados.

| Hallazgo | Corrección y verificación |
| --- | --- |
| Salud reportaba `UP` sin consultar PostgreSQL | Toma una conexión del `DataSource`, ejecuta la validación JDBC con un límite de dos segundos y libera la conexión. Responde HTTP 200 / `UP` con una conexión válida y HTTP 503 / `DOWN` si falla la adquisición o validación. Mantiene los cuatro campos específicos de salud y no devuelve mensajes JDBC. MockMvc verifica ambos estados. |
| Errores MVC podían omitir `message` | El advice traduce errores de deserialización, tipos, parámetros, validación y los errores existentes de SBOM y OSV al cuerpo de cinco campos. Se mantienen los códigos del contrato y los encabezados MVC. Los mensajes de infraestructura y el stack trace quedan fuera de la respuesta. |
| `createdAt` carecía del offset del ejemplo del contrato | V5 añade `proyecto.created_at_utc` con tipo `timestamp with time zone` y deja las filas existentes con la columna nueva nula. Para cada alta de la API se captura el instante real y se serializa con `Z`. El valor histórico se devuelve como fecha local ISO 8601, sin asignarle una zona. |

### Procedencia de las fechas

V1 definió `proyecto.created_at` y `analisis.fecha_analisis` como
`timestamp without time zone`. Las entidades originales usaban
`LocalDateTime.now()` y los valores por defecto de SQL dependían de la
sesión que ejecutaba la inserción. El valor actual de `SHOW TimeZone` no
identifica la zona histórica de cada fila ni la de las JVM que la escribieron.
La revisión de esquema y código confirmó que esa información no se guardó.
La sesión real no pudo consultarse desde este entorno. V5 no ejecuta
backfill ni convierte campos existentes. `fecha_analisis` es interna.
`generatedAt` del reporte y el `timestamp` de salud y errores representan
instantes UTC.

### Conteo de clases de JaCoCo

En la base, el analizador preliminar procesó 56 archivos `.class`. El XML
contiene los mismos 56 elementos `class`, de los cuales nueve son
interfaces de repositorios y proyecciones sin contador de instrucciones.
El plugin Maven 0.8.11 informa el contador `CLASS` del informe y contabilizó
47 clases con código. La diferencia de nueve se comprobó en el XML, sin
exclusiones ni cambios al POM de cobertura. La unificación del DTO de error
elimina una clase con código en esta revisión.

### Resultados de esta revisión

Se conservaron los 94 casos base y se agregaron ocho regresiones. La
ejecución de esta revisión registró **102 casos, 88 aprobados, 0 fallos,
0 errores y 14 omitidos**. Los 14 requieren PostgreSQL. Son 12 de la base
y dos nuevos para salud con una conexión real y persistencia temporal V5.
Pasaron las seis pruebas nuevas que no requieren PostgreSQL.

| Métrica | Cobertura parcial | Cubiertos / total |
| --- | ---: | ---: |
| Instrucciones | 90,48 % | 3328 / 3678 |
| Líneas | 87,81 % | 677 / 771 |
| Ramas | 72,97 % | 243 / 333 |

Se analizaron 55 archivos `.class`, 46 con código y nueve sin instrucciones.

El `pom.xml` conserva JaCoCo 0.8.11. El intento de
`mvn clean verify` con ese POM no pudo resolver el plugin desde Maven
Central. El resultado anterior se obtuvo con `clean test`, un POM temporal
sin el binding del plugin y el agente y formateadores JaCoCo 0.8.11. Se
analizó todo `target/classes`. Los informes HTML/XML quedaron en
`backend/target/site/jacoco`. Esta medición parcial no se compara
directamente con la referencia completa en PostgreSQL de la PR #12
(95,40 % de instrucciones, 92,37 % de líneas y 76,90 % de ramas).

Queda pendiente ejecutar `mvn clean verify` con la base de pruebas definida
arriba y confirmar Flyway V5, la lectura y escritura de instantes UTC y el
sondeo HTTP con PostgreSQL 16.16 real.
