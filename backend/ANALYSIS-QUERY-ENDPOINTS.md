# RF-06 a RF-08 — Consultas de análisis

Los endpoints de lectura usan datos persistidos por la importación SBOM,
la detección OSV y el cálculo de SecureGraph Score. El identificador es el
`analysisId` devuelto por la importación del SBOM.

| Endpoint | Parámetros | Resultado |
| --- | --- | --- |
| `GET /api/analisis/{id}/resumen` | — | Contadores de componentes y hallazgos del análisis. |
| `GET /api/analisis/{id}/hallazgos` | `orden`, `severidad` | Resumen global y lista de hallazgos ordenada y filtrada. |
| `GET /api/analisis/{id}/grafo` | — | Nodos y aristas, incluidos los componentes desconectados. |
| `GET /api/analisis/{id}/reporte` | `orden` | Informe JSON con metadatos del proyecto, resumen global y todos los hallazgos. |

- `orden` admite `score` por defecto o `cvss`. Ambos ordenan de mayor a menor.
  Los CVSS ausentes van al final cuando se ordena por CVSS. Los empates se
  resuelven por ID ascendente del hallazgo.
- `severidad` admite `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `NONE` y
  `UNKNOWN`. Los valores se interpretan sin distinguir mayúsculas y se
  recortan los espacios. Omitir el filtro o enviarlo vacío devuelve todos los
  hallazgos. El filtro afecta sólo a la lista.
- `components` cuenta las dependencias importadas, excluida la raíz.
  `vulnerabilities` incluye los hallazgos `NONE` y `UNKNOWN`. Los cuatro
  contadores por nivel mantienen los nombres del contrato.
- Los campos `cvss` y `depth` conservan `null` cuando falta una puntuación
  numérica o una ruta desde la raíz. Un CVSS real de cero se devuelve como
  `0.0`. `reasons` contiene las cuatro razones guardadas por RF-05.
- Los IDs de nodos y extremos de aristas son los `bom-ref` del análisis.
  La raíz usa `MODULE`. Un nodo con hallazgos usa la mayor severidad
  conocida y tiene `vulnerable: true`.
- Un identificador mal formado, no positivo o fuera del rango de entero
  de 64 bits, o un valor de orden o severidad inválido, devuelve HTTP 400.
  Un identificador válido que no existe devuelve HTTP 404. El JSON de error
  usa `timestamp`, `status`, `error`, `message` y `path`.

## Ejemplos

`GET /api/analisis/3/resumen`

```json
{"components":2,"vulnerabilities":1,"critical":1,"high":0,"medium":0,"low":0}
```

`GET /api/analisis/3/hallazgos?orden=cvss&severidad=CRITICAL`

```json
{
  "analysisId": 3,
  "summary": {"components":2,"vulnerabilities":1,"critical":1,"high":0,"medium":0,"low":0},
  "findings": [{
    "id": 27,
    "vulnerabilityId": "GHSA-jfh8-c2jp-5v3q",
    "cveId": "CVE-2021-44228",
    "component": "org.apache.logging.log4j:log4j-core",
    "version": "2.14.1",
    "cvss": 10.0,
    "severity": "CRITICAL",
    "direct": true,
    "depth": 1,
    "secureGraphScore": 100.0,
    "reasons": [
      "CVSS base 10. Factor de severidad 1.",
      "Profundidad mínima 1 desde la raíz. Aporta 15 / 1 puntos antes de aplicar severidad.",
      "Dependencia directa del proyecto. Aporta 10 puntos antes de aplicar severidad.",
      "Alcance required. Aporta 5 puntos antes de aplicar severidad."
    ]
  }]
}
```

`GET /api/analisis/3/grafo`

```json
{
  "analysisId": 3,
  "nodes": [
    {"id":"app","label":"app:1.0","type":"MODULE","vulnerable":false,"severity":"NONE"},
    {"id":"log4j","label":"log4j-core:2.14.1","type":"DEPENDENCY","vulnerable":true,"severity":"CRITICAL"},
    {"id":"helper","label":"helper:1.0","type":"DEPENDENCY","vulnerable":false,"severity":"NONE"}
  ],
  "edges": [
    {"source":"app","target":"log4j","relation":"DIRECT"},
    {"source":"log4j","target":"helper","relation":"TRANSITIVE"}
  ]
}
```

`GET /api/analisis/4/reporte?orden=score`, para un análisis sin hallazgos

```json
{
  "analysisId": 4,
  "projectName": "Inventory API",
  "sbomFormat": "CycloneDX-JSON",
  "generatedAt": "2026-11-28T14:00:00Z",
  "orderingCriterion": "score",
  "summary": {"components":2,"vulnerabilities":0,"critical":0,"high":0,"medium":0,"low":0},
  "findings": []
}
```

## Pruebas

Las pruebas HTTP con repositorios simulados corren con `mvn clean test`.
La suite comprueba las consultas y el aislamiento sobre PostgreSQL cuando
`SECUREGRAPH_TEST_POSTGRES_URL` apunta a una base de pruebas. La
configuración es la misma que en [OSV-INTEGRATION.md](OSV-INTEGRATION.md).
