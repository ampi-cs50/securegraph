# RF-04 — Detección de vulnerabilidades con OSV

Al importar un SBOM, el backend usa [POST /v1/query](https://google.github.io/osv.dev/post-v1-query/)
por cada paquete Maven y versión. Lee las coordenadas de un PURL Maven o de
`groupId:artifactId` con versión cuando el SBOM carece de PURL. Excluye la raíz
e incluye dependencias desconectadas con profundidad `NULL`. La profundidad
se lee de `DependencyGraphService`.

El cliente sigue la paginación y reutiliza los resultados para coordenadas
repetidas en el mismo análisis. Los avisos que comparten un identificador o
alias se consolidan por componente. V3 conserva el identificador principal,
el primer CVE y GHSA en orden alfabético y todos los identificadores del grupo
en `osv_identifiers`. La base impone unicidad por análisis, componente e ID
principal. El contador registra pares componente–vulnerabilidad en la misma
transacción.

Se conserva el mayor puntaje base CVSS válido del grupo. El cliente acepta
puntajes numéricos y vectores completos v2.0, v3.0, v3.1 y v4.0. Sin puntaje,
guarda `NULL` y usa la severidad publicada en `database_specific.severity`
cuando está disponible, o `UNKNOWN`. Los componentes sin coordenadas o
versión utilizables quedan fuera del escaneo.

Una consulta exitosa sin avisos produce cero hallazgos. Una respuesta inválida
o un error de upstream produce `502`. Un timeout, un error de conexión o una
respuesta `429`, `503` o `504` de OSV produce `503`. El error usa el JSON
estándar y revierte la importación completa. El POST conserva su cuerpo de
éxito. Los GET de análisis existentes siguen sirviendo datos de ejemplo.
Esta entrega guarda los hallazgos para la lectura y priorización posteriores.

El escaneo es síncrono y no hace reintentos automáticos. Los timeouts se
aplican por conexión y lectura. La consulta transmite a OSV únicamente el
nombre Maven del paquete y su versión.

| Variable | Valor predeterminado |
| --- | --- |
| `OSV_API_BASE_URL` | `https://api.osv.dev/v1` |
| `OSV_CONNECT_TIMEOUT_SECONDS` | `3` |
| `OSV_READ_TIMEOUT_SECONDS` | `10` |

## Verificación en PowerShell

Para consultar directamente el servicio público con el componente Log4j
del SBOM de ejemplo:

```powershell
$payload = @{
    package = @{ ecosystem = 'Maven'; name = 'org.apache.logging.log4j:log4j-core' }
    version = '2.14.1'
} | ConvertTo-Json -Depth 4 -Compress
$osv = Invoke-RestMethod -Uri 'https://api.osv.dev/v1/query' -Method Post -ContentType 'application/json' -Body $payload
$osv.vulns | Select-Object id, aliases, severity
```

Desde la raíz del repositorio, para ejecutar pruebas con PostgreSQL 16 y
respuestas OSV controladas:

```powershell
docker compose up -d --wait db
$testDb = "securegraph_rf04_$(Get-Date -Format yyyyMMddHHmmssfff)"
docker compose exec -T db createdb -U securegraph_user $testDb
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear la base de pruebas' }
$env:SECUREGRAPH_TEST_POSTGRES_URL = "jdbc:postgresql://localhost:5432/$testDb"
try {
    mvn -B -ntp -f backend/pom.xml clean test
} finally {
    Remove-Item Env:SECUREGRAPH_TEST_POSTGRES_URL
}
```

Para verificar la importación con OSV real, levantá el backend de esta rama
en una terminal con `mvn -B -ntp -f backend/pom.xml spring-boot:run`.
Desde otra terminal, ejecutá el bloque siguiente. El total exacto depende
de los avisos que devuelva OSV en el momento de la prueba.

```powershell
$base = 'http://localhost:8080/api/proyectos'
$project = Invoke-RestMethod -Uri $base -Method Post -ContentType 'application/json' -Body (
    @{ nombre = "rf04-$(Get-Date -Format yyyyMMddHHmmssfff)" } | ConvertTo-Json -Compress
)
$body = curl.exe -fsS -F 'file=@samples/sample-maven-cyclonedx.json;type=application/json' "$base/$($project.id)/analisis"
if ($LASTEXITCODE -ne 0) { throw 'Falló el análisis del SBOM' }
$analysis = $body | ConvertFrom-Json
$analysis | Format-List
$sql = @"
SELECT a.id, a.total_vulnerabilidades, count(h.id) AS hallazgos
FROM analisis a LEFT JOIN hallazgo_vulnerabilidad h ON h.analisis_id = a.id
WHERE a.id = :analysis_id GROUP BY a.id;
SELECT c.artifact_id, h.osv_id, h.cve_id, h.ghsa_id,
       h.severidad_cvss, h.nivel_severidad, h.profundidad
FROM hallazgo_vulnerabilidad h JOIN componente c ON c.id = h.componente_id
WHERE h.analisis_id = :analysis_id ORDER BY h.profundidad NULLS LAST, c.artifact_id, h.osv_id;
"@
$sql | docker compose exec -T db psql -U securegraph_user -d securegraph_db -v ON_ERROR_STOP=1 -v "analysis_id=$($analysis.analysisId)"
```

Los comandos usan las credenciales y el puerto por defecto de Docker Compose.
Si Maven no figura en el PATH, reemplazá `mvn` por `& $maven` en la terminal
donde configuraste esa variable.
