# DOCUMENTO FORMAL 3 — INFORME DE AVANCE N° 2 (GUÍA 003 - UTN FRLP)

* **Fecha límite de entrega:** 13/12/2026 (Semana 6)

---

## 1. Estado de Avance General
* **Avance planificado:** 80% | **Avance real:** 82% | **Diferencia:** +2% (Adelantado)
* **Resumen narrativo:** Se conectó el cálculo de priorización explicable con la interfaz de usuario. En el módulo Frontend se optimizó la interacción del grafo y la funcionalidad de exportación de reportes técnicos. En el módulo Backend se finalizaron las pruebas unitarias y se completó la batería de evaluación comparando el *SecureGraph Score* con el *CVSS puro* en proyectos de prueba Java, constatando que la priorización por grafo reduce la sobrecarga de alertas al contextualizar las librerías transitivas.

## 2. Matriz de Pruebas y Calidad de Software
| Módulo / Funcionalidad | Tipo de Prueba | Área Responsable | Resultado | Observaciones |
| :--- | :--- | :--- | :--- | :--- |
| Ingesta CycloneDX | Unitaria (JUnit) | Backend | **OK** | Se verificaron archivos válidos, incompletos y malformados. |
| Consulta a OSV | Integración | Backend | **OK** | Simulación de timeouts y respuestas 404/500 mediante mocks. |
| Motor de Score | Unitaria | Backend | **OK** | Se confirmó consistencia de puntajes y generación de explicaciones. |
| Grafo Interactivo | E2E / Usabilidad | Frontend | **OK** | Verificado en Chrome y Firefox con selección e inspección de nodos. |
| Formulario de Carga | Usabilidad | Frontend | **OK** | Validaciones claras ante formatos no admitidos o errores de red. |
