# SecureGraph

Plataforma web para el análisis y la priorización explicable de vulnerabilidades conocidas en dependencias de software Java/Maven mediante grafos.

**Trabajo Final Integrador (TFI)** — Tecnicatura Universitaria en Programación  
Facultad Regional La Plata — Universidad Tecnológica Nacional (UTN-FRLP)

---

## Descripción del Proyecto

Los proyectos Java modernos gestionados con Apache Maven incorporan dependencias directas que, a su vez, arrastran decenas o cientos de dependencias transitivas. Los escáneres tradicionales ordenan los hallazgos únicamente por puntaje CVSS sin contemplar la topología ni el contexto del sistema.

**SecureGraph** importa un inventario estandarizado **SBOM CycloneDX (JSON)**, construye y almacena el **Grafo Acíclico Dirigido (DAG)** de dependencias sobre **PostgreSQL 16** (explorándolo mediante consultas jerárquicas `WITH RECURSIVE`), consulta vulnerabilidades conocidas en la API pública de **OSV (Open Source Vulnerabilities)** y calcula el **SecureGraph Score**: un índice de priorización explicable que pondera severidad CVSS, carácter directo o transitivo, profundidad en el árbol y alcance (*scope*) del componente.

---

## Arquitectura y Módulos

| Módulo | Tecnologías | Directorio |
| :--- | :--- | :--- |
| **Backend & Persistencia** | Java 21, Spring Boot 3, PostgreSQL 16, Flyway | [`backend/`](./backend) |
| **Frontend & Visualización** | Angular 17+, TypeScript, Visualización de Grafos | [`frontend/`](./frontend) |

Los directorios compartidos ([`docs/`](./docs), [`samples/`](./samples), [`docker-compose.yml`](./docker-compose.yml), [`.gitignore`](./.gitignore)) contienen las especificaciones técnicas, contratos de integración y recursos comunes del sistema.

---

## Estructura del Repositorio

```text
securegraph/
├── backend/                  # API REST en Java 21 + Spring Boot 3 + Maven + Flyway
├── frontend/                 # SPA en Angular 17+ + TypeScript + Servicios HTTP/Mock
├── docs/                     # Contratos API, flujo Git y documentación técnica
│   ├── api-contract.md       # Contrato REST v1.0 entre Frontend y Backend
│   ├── git-workflow.md       # Convenciones de ramas, commits semánticos y Pull Requests
│   └── entregables/          # Documentación académica (Guías 002, 003 y 004)
├── samples/                  # Archivos SBOM CycloneDX JSON de prueba
│   └── sample-maven-cyclonedx.json
├── docker-compose.yml        # Entorno local reproducible (PostgreSQL 16)
├── .env.example              # Plantilla de variables de entorno
└── README.md
```

---

## Configuración y Ejecución del Entorno de Desarrollo

### 1. Base de Datos (PostgreSQL)

Iniciar el contenedor de base de datos mediante Docker Compose desde la raíz del proyecto:

```bash
cp .env.example .env
docker compose up -d db
```

Esto disponibiliza una instancia de **PostgreSQL 16** en el puerto `5432` con la base de datos `securegraph_db`.

### 2. Backend (Spring Boot 3)

**Requisitos previos:** JDK 21 y Apache Maven 3.9+.

```bash
cd backend
mvn spring-boot:run
```

* El servidor HTTP iniciará en `http://localhost:8080`.
* Flyway ejecutará automáticamente el esquema inicial definido en [`V1__init_schema.sql`](./backend/src/main/resources/db/migration/V1__init_schema.sql).
* Verificación de estado del servicio: `GET http://localhost:8080/api/health` (respuesta esperada: `{"status":"UP"}`).

### 3. Frontend (Angular 17+)

**Requisitos previos:** Node.js 20+ y npm 10+.

```bash
cd frontend
npm install
npm start
```

* La aplicación web estará disponible en `http://localhost:4200`.
* Incluye soporte para operación desacoplada mediante datos simulados basados en [`docs/api-contract.md`](./docs/api-contract.md), además de la integración directa con la API REST del backend.

---

## Flujo de Trabajo con Git

El repositorio sigue un modelo de ramificación estructurado:

1. **`main`**: Rama principal con versiones estables y entregas formales.
2. **`develop`**: Rama de integración continua.
3. **`feature/*`**: Ramas de desarrollo para nuevas funcionalidades o módulos (`feature/backend-*`, `feature/frontend-*`).

Consultar [`docs/git-workflow.md`](./docs/git-workflow.md) para mayor detalle sobre las normas de versionado, convenciones de commits y revisión de código.
