# SecureGraph — Backend

Servicio backend encargado de la ingesta de inventarios SBOM CycloneDX, persistencia relacional y de grafos, consulta de vulnerabilidades en OSV y cálculo del índice de priorización SecureGraph Score.

* **Tecnologías principales:** Java 21, Spring Boot 3, Spring Data JPA, Flyway, PostgreSQL 16
* **Paquete base:** `ar.edu.utn.frlp.securegraph`
* **Puerto por defecto:** `8080`

---

## Arquitectura de Paquetes

```text
src/main/java/ar/edu/utn/frlp/securegraph/
├── SecureGraphApplication.java
├── config/
│   └── WebCorsConfig.java                 # Configuración de políticas CORS
├── domain/
│   └── entity/                            # Entidades JPA mapeadas al esquema relacional
│       ├── Proyecto.java
│       ├── Analisis.java
│       ├── Componente.java
│       ├── RelacionDependencia.java       # Aristas dirigidas del grafo de dependencias (DAG)
│       └── HallazgoVulnerabilidad.java    # Hallazgos OSV y puntaje de priorización
├── repository/                            # Repositorios Spring Data JPA y consultas recursivas (CTE)
│   ├── ProyectoRepository.java
│   ├── AnalisisRepository.java
│   ├── ComponenteRepository.java
│   ├── RelacionDependenciaRepository.java
│   └── HallazgoVulnerabilidadRepository.java
├── service/                               # Lógica de negocio, análisis y generación de reportes
│   └── AnalisisService.java
└── web/
    ├── controller/                        # Controladores REST según especificación en docs/api-contract.md
    │   ├── AnalisisController.java
    │   ├── HealthController.java
    │   └── ProyectoController.java
    └── dto/                               # Objetos de transferencia de datos (Records inmutables)
        ├── FindingsResponseDto.java
        ├── GraphResponseDto.java
        ├── HealthResponseDto.java
        ├── ProyectoDto.java
        └── ReportResponseDto.java
```

---

## Ejecución en Entorno Local

### Requisitos Previos

* **Java Development Kit (JDK):** versión 21
* **Apache Maven:** versión 3.9 o superior
* **Docker y Docker Compose:** para la instancia local de PostgreSQL 16

### Pasos de Ejecución

1. Iniciar el contenedor de PostgreSQL desde el directorio raíz del repositorio:
   ```bash
   docker compose up -d db
   ```

2. Compilar e iniciar la aplicación Spring Boot:
   ```bash
   mvn spring-boot:run
   ```

3. Verificar la disponibilidad del servicio:
   ```bash
   curl http://localhost:8080/api/health
   ```
