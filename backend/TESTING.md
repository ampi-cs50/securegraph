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
