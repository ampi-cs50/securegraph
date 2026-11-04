# Contrato API REST v1.0 — SecureGraph

* **Versión:** 1.0.0
* **URL Base Local:** `http://localhost:8080/api`

---

## 1. Convenciones Generales

* **Formato de intercambio:** `application/json` (excepto la carga de archivos SBOM, que utiliza `multipart/form-data`).
* **Nomenclatura de propiedades JSON:** `camelCase` estricto en todas las peticiones y respuestas.
* **Códigos de estado HTTP estándar:**
  * `200 OK`: Consulta procesada con éxito.
  * `201 Created`: Recurso creado exitosamente (proyecto o nuevo análisis).
  * `400 Bad Request`: Error de validación o archivo SBOM CycloneDX inválido.
  * `404 Not Found`: Proyecto o análisis inexistente.
  * `502 Bad Gateway` / `503 Service Unavailable`: Error de comunicación con la API externa de OSV.

### Estructura Estándar de Error JSON
```json
{
  "timestamp": "2026-11-04T17:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "El archivo proporcionado no cumple con el esquema CycloneDX JSON válido.",
  "path": "/api/proyectos/1/analisis"
}
```

---

## 2. Catálogo de Endpoints

| Método | Endpoint | Parámetros / Body | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Ninguno | Verifica el estado del servidor y la conexión a PostgreSQL. |
| `GET` | `/api/proyectos` | Ninguno | Retorna el listado de proyectos registrados. |
| `POST` | `/api/proyectos` | JSON: `{ "nombre": "...", "descripcion": "..." }` | Registra un nuevo proyecto en el sistema. |
| `POST` | `/api/proyectos/{id}/analisis` | `multipart/form-data` con archivo `file` (SBOM JSON) | Importa un SBOM CycloneDX, lo parsea y ejecuta el análisis. |
| `GET` | `/api/analisis/{id}/resumen` | Path: `id` del análisis | Devuelve contadores globales de componentes y severidades. |
| `GET` | `/api/analisis/{id}/hallazgos` | Query params: `?orden=score&severidad=CRITICAL` | Devuelve el listado priorizado de vulnerabilidades con razones explicables. |
| `GET` | `/api/analisis/{id}/grafo` | Path: `id` del análisis | Devuelve nodos y aristas para renderizar el grafo interactivo. |
| `GET` | `/api/analisis/{id}/reporte` | Path: `id` del análisis | Devuelve el informe técnico consolidado para exportación. |

---

## 3. Ejemplos de Payloads JSON

### 3.1 `GET /api/health`
```json
{
  "status": "UP",
  "service": "securegraph-backend",
  "database": "PostgreSQL",
  "timestamp": "2026-11-04T17:30:00Z"
}
```

### 3.2 `GET /api/proyectos`
```json
[
  {
    "id": 1,
    "nombre": "facturacion-core-service",
    "descripcion": "Microservicio principal de facturación electrónica (Java 21 / Maven)",
    "createdAt": "2026-11-04T14:20:00Z",
    "ultimoAnalisisId": 42,
    "totalAnalisis": 2
  }
]
```

### 3.3 `GET /api/analisis/{id}/resumen`
```json
{
  "components": 25,
  "vulnerabilities": 3,
  "critical": 1,
  "high": 1,
  "medium": 1,
  "low": 0
}
```

### 3.4 `GET /api/analisis/{id}/hallazgos`
```json
{
  "analysisId": 42,
  "summary": {
    "components": 25,
    "vulnerabilities": 3,
    "critical": 1,
    "high": 1,
    "medium": 1,
    "low": 0
  },
  "findings": [
    {
      "id": 1,
      "vulnerabilityId": "GHSA-jfh8-c2jp-5v3q",
      "cveId": "CVE-2021-44228",
      "component": "org.apache.logging.log4j:log4j-core",
      "version": "2.14.1",
      "cvss": 10.0,
      "severity": "CRITICAL",
      "direct": true,
      "depth": 1,
      "secureGraphScore": 98.5,
      "reasons": [
        "Dependencia declarada directa en el módulo principal",
        "Severidad máxima CVSS (10.0)",
        "Componente con alcance (scope) de ejecución en tiempo de producción"
      ]
    },
    {
      "id": 2,
      "vulnerabilityId": "OSV-2022-1234",
      "cveId": "CVE-2022-25857",
      "component": "org.yaml:snakeyaml",
      "version": "1.30",
      "cvss": 7.5,
      "severity": "HIGH",
      "direct": false,
      "depth": 3,
      "secureGraphScore": 62.0,
      "reasons": [
        "Dependencia transitiva de nivel 3",
        "Severidad CVSS elevada (7.5)",
        "Penalización de prioridad por lejanía del nodo raíz"
      ]
    },
    {
      "id": 3,
      "vulnerabilityId": "GHSA-rgv9-q543-rqg4",
      "cveId": "CVE-2022-42003",
      "component": "com.fasterxml.jackson.core:jackson-databind",
      "version": "2.13.4.2",
      "cvss": 5.9,
      "severity": "MEDIUM",
      "direct": false,
      "depth": 2,
      "secureGraphScore": 54.0,
      "reasons": [
        "Dependencia transitiva de nivel 2 a través de spring-boot-starter",
        "Severidad CVSS media (5.9)",
        "Alcance de compilación y ejecución estándar"
      ]
    }
  ]
}
```

### 3.5 `GET /api/analisis/{id}/grafo`
```json
{
  "analysisId": 42,
  "nodes": [
    { "id": "mod-root", "label": "facturacion-core-service (Root)", "type": "MODULE", "vulnerable": false, "severity": "NONE" },
    { "id": "dep-1", "label": "log4j-core:2.14.1", "type": "DEPENDENCY", "vulnerable": true, "severity": "CRITICAL" },
    { "id": "dep-2", "label": "spring-boot-starter:3.1.0", "type": "DEPENDENCY", "vulnerable": false, "severity": "NONE" },
    { "id": "dep-3", "label": "snakeyaml:1.30", "type": "DEPENDENCY", "vulnerable": true, "severity": "HIGH" },
    { "id": "dep-4", "label": "jackson-databind:2.13.4.2", "type": "DEPENDENCY", "vulnerable": true, "severity": "MEDIUM" }
  ],
  "edges": [
    { "source": "mod-root", "target": "dep-1", "relation": "DIRECT" },
    { "source": "mod-root", "target": "dep-2", "relation": "DIRECT" },
    { "source": "dep-2", "target": "dep-3", "relation": "TRANSITIVE" },
    { "source": "dep-2", "target": "dep-4", "relation": "TRANSITIVE" }
  ]
}
```

### 3.6 `GET /api/analisis/{id}/reporte`
```json
{
  "analysisId": 42,
  "projectName": "facturacion-core-service",
  "sbomFormat": "CycloneDX-JSON",
  "generatedAt": "2026-11-04T17:30:00Z",
  "orderingCriterion": "score",
  "summary": {
    "components": 25,
    "vulnerabilities": 3,
    "critical": 1,
    "high": 1,
    "medium": 1,
    "low": 0
  },
  "findings": []
}
```
