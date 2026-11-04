# SecureGraph — Frontend

Aplicación web de página única (SPA) para la gestión de proyectos, importación de archivos SBOM CycloneDX, visualización interactiva de grafos de dependencias y análisis priorizado de vulnerabilidades.

* **Tecnologías principales:** Angular 17+ (Standalone Components), TypeScript, RxJS
* **Puerto por defecto:** `4200` (`http://localhost:4200`)

---

## Estructura de la Aplicación

```text
src/app/
├── app.component.ts                 # Estructura principal de navegación y selector de origen de datos
├── app.config.ts                    # Configuración global de enrutamiento y cliente HTTP
├── app.routes.ts                    # Definición de rutas de la aplicación
├── models/
│   └── securegraph.models.ts        # Interfaces y tipos de datos alineados con docs/api-contract.md
├── services/
│   └── securegraph-data.service.ts  # Capa de acceso a datos (soporte HTTP API y datos simulados)
└── pages/
    ├── projects/                    # Administración y listado de proyectos registrados
    ├── upload-sbom/                 # Carga y validación de archivos SBOM CycloneDX
    ├── dashboard/                   # Indicadores generales, tabla de hallazgos priorizados y detalle técnico
    └── graph-view/                  # Representación interactiva del grafo de dependencias
```

---

## Ejecución en Entorno Local

### Requisitos Previos

* **Node.js:** versión 20 o superior
* **npm:** versión 10 o superior

### Pasos de Ejecución

1. Instalar las dependencias del proyecto desde el directorio `frontend/`:
   ```bash
   npm install
   ```

2. Iniciar el servidor de desarrollo:
   ```bash
   npm start
   ```

3. Acceder a la aplicación desde el navegador en `http://localhost:4200`.

---

## Modos de Operación (Origen de Datos)

La barra superior de la interfaz dispone de un selector de entorno que permite alternar entre dos modos de funcionamiento:

* **Modo Mock (Simulado):** Opera de manera autónoma utilizando los conjuntos de datos definidos en el contrato de interfaz (`docs/api-contract.md`), sin requerir conexión con el backend.
* **Modo API Backend (`localhost:8080`):** Establece comunicación HTTP directa con los servicios REST provistos por el servidor Spring Boot.
