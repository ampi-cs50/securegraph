# RF-03 - Profundidad mínima del grafo

`DependencyGraphService.findMinimumDepths(analysisId)` devuelve el ID, el
`bom-ref` y la profundidad mínima de cada componente del análisis. La raíz
tiene profundidad `0`, sus dependencias directas `1`, las transitivas
alcanzables `2` o más y los componentes desconectados `null`. Si el
análisis no tiene componentes, devuelve una lista vacía.

## Recorrido en PostgreSQL

- La migración Flyway V2 marca la raíz de cada importación existente de RF-02.
  RF-02 guardaba primero el componente de metadatos, por eso la migración usa
  el menor ID de componente por análisis. Las importaciones nuevas marcan
  la raíz explícitamente. Un índice único parcial limita el esquema a una raíz
  por análisis.
- `WITH RECURSIVE` empieza en la raíz y avanza por niveles. En cada iteración,
  `frontera` contiene los IDs del nivel actual y `visitados` reúne los IDs
  alcanzados hasta ese nivel. `LATERAL` reúne los hijos de toda la frontera y
  descarta duplicados y visitados antes de avanzar.
- El primer encuentro con cada componente fija su distancia mínima aunque
  existan varios caminos. Como ningún visitado vuelve a la frontera, un ciclo
  no prolonga el recorrido. El recorrido termina cuando la frontera siguiente
  está vacía, sin un límite fijo de niveles.
- La consulta restringe la raíz, las aristas, sus destinos y el resultado al
  mismo análisis. Cada origen proviene de la frontera ya restringida. El
  `LEFT JOIN` final conserva los componentes que quedaron desconectados.

La consulta canónica está en `RelacionDependenciaRepository.buscarProfundidadesMinimas`.
El servicio devuelve resultados internos tipados que puede consumir el motor
de Score. Las rutas y las respuestas de la API conservan su contrato.

## Verificación en PowerShell

Desde la raíz de la rama `feature/backend-dependency-graph`, levantá
PostgreSQL 16 e iniciá el backend de esa rama. El ejemplo usa el puerto
`8082` para permitir otra instancia local en `8081`.

```powershell
docker compose up -d --wait db
$env:SERVER_PORT = '8082'
mvn -B -ntp -f backend/pom.xml spring-boot:run
```

En otra terminal, desde la raíz, importá el SBOM existente y consultá la
profundidad con la misma consulta del repositorio.

```powershell
$base = 'http://localhost:8082/api/proyectos'
$project = Invoke-RestMethod -Uri $base -Method Post -ContentType 'application/json' -Body (
    @{ nombre = "rf03-$([DateTime]::UtcNow.ToString('yyyyMMdd-HHmmssfff'))" } | ConvertTo-Json
)
$importBody = curl.exe -fsS -F 'file=@samples/sample-maven-cyclonedx.json;type=application/json' "$base/$($project.id)/analisis"
if ($LASTEXITCODE -ne 0) { throw 'Falló la importación SBOM' }
$import = $importBody | ConvertFrom-Json
if (-not $import.analysisId) { throw 'La API no devolvió un ID de análisis' }
$import | Format-List

$repository = 'backend/src/main/java/ar/edu/utn/frlp/securegraph/repository/RelacionDependenciaRepository.java'
$query = [regex]::Match((Get-Content -LiteralPath $repository -Raw),
    '(?s)WITH RECURSIVE niveles.*?ORDER BY a\.profundidad NULLS LAST, c\.id')
if (-not $query.Success) { throw 'No se encontró la consulta del repositorio' }
($query.Value.Replace(':analisisId', ([long]$import.analysisId).ToString()) + ';') |
    docker compose exec -T db psql -U securegraph_user -d securegraph_db -v ON_ERROR_STOP=1
```

El SBOM de ejemplo debe devolver seis filas. La raíz
`facturacion-core-service` tiene profundidad 0. `log4j-core` y
`spring-boot-starter` tienen profundidad 1. `log4j-api`, `snakeyaml` y
`jackson-databind` tienen profundidad 2.

## Pruebas de integración

Usá una base de pruebas separada. Flyway aplica las migraciones al iniciar y
JUnit revierte los datos insertados por cada caso.

```powershell
$testDb = "securegraph_rf03_$([DateTime]::UtcNow.ToString('yyyyMMddHHmmssfff'))"
docker compose exec -T db createdb -U securegraph_user $testDb
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear la base de pruebas' }
$env:SECUREGRAPH_TEST_POSTGRES_URL = "jdbc:postgresql://localhost:5432/$testDb"
try {
    mvn -B -ntp -f backend/pom.xml test '-Dtest=DependencyGraphPostgresTest'
} finally {
    Remove-Item Env:SECUREGRAPH_TEST_POSTGRES_URL
}
```

Los ejemplos usan los datos de conexión por defecto de Docker Compose. Si
configuraste credenciales distintas, ajustá el usuario y la base en los
comandos y definí `SECUREGRAPH_TEST_POSTGRES_USER` y
`SECUREGRAPH_TEST_POSTGRES_PASSWORD` para las pruebas. Si Maven no figura
en el PATH, reemplazá `mvn` por `& $maven` en la terminal donde lo configuraste.
