# Reglas de Trabajo Colaborativo y Flujo de Git — SecureGraph

## 1. Organización y División de Directorios

* **`backend/`**: Módulo de servidor REST, lógica de negocio y persistencia relacional (Java 21 / Spring Boot 3 / PostgreSQL 16).
* **`frontend/`**: Aplicación cliente SPA y visualización de grafos (Angular 17+ / TypeScript).
* **`docs/`, `samples/`, `docker-compose.yml`, `.gitignore`**: Recursos y especificaciones compartidas; cualquier modificación se coordina previamente y se integra mediante Pull Request con revisión cruzada.

---

## 2. Estrategia de Ramas (Gitflow Simplificado)

* **`main`**: Rama estable de producción. Solo recibe integraciones desde `develop` al cierre de cada hito o entrega formal.
* **`develop`**: Rama de integración continua.
* **`feature/<area>-<descripcion>`**: Ramas de trabajo por funcionalidad creadas a partir de `develop`.
  * Ejemplos de Backend:
    * `feature/backend-init-schema`
    * `feature/backend-parser-cyclonedx`
    * `feature/backend-osv-client`
    * `feature/backend-score-engine`
  * Ejemplos de Frontend:
    * `feature/frontend-layout-routing`
    * `feature/frontend-sbom-upload`
    * `feature/frontend-findings-dashboard`
    * `feature/frontend-graph-visualizer`

---

## 3. Estándar de Commits Semánticos

Formato requerido:
```text
<tipo>(<alcance>): <descripción breve en imperativo>
```

* **Tipos permitidos:**
  * `feat`: Nueva funcionalidad (`feat(backend): implementar parser CycloneDX JSON`)
  * `fix`: Corrección de error (`fix(frontend): corregir filtro por severidad en tabla`)
  * `docs`: Cambios en documentación técnica o entregables (`docs(entregables): actualizar DER en informe de avance 1`)
  * `refactor`: Mejora estructural de código sin alterar la funcionalidad externa
  * `test`: Adición o ajuste de pruebas unitarias o de integración
  * `chore`: Configuración de construcción, dependencias o contenedores Docker

---

## 4. Protocolo de Pull Request (PR)

1. Antes de abrir el Pull Request hacia `develop`:
   * **Backend:** Verificar que `mvn clean test` finalice sin errores.
   * **Frontend:** Verificar que `npm run build` compile sin errores de TypeScript.
2. Asignar un revisor técnico del equipo (*Reviewer*).
3. Si se modifica algún DTO o ruta REST, actualizar simultáneamente [`docs/api-contract.md`](./api-contract.md).
