# RF-05 — SecureGraph Score

La documentación funcional de RF-05 define cuatro factores y la escala de
0 a 100. El contrato REST ilustra la salida con ejemplos. Esta especificación
define las ponderaciones y la fórmula de la primera versión del índice.
El resultado establece una prioridad de revisión técnica.

## Fórmula

`score = redondear(S × (70 + 15 × P + 10 × D + 5 × A), 1)`

| Factor | Cálculo |
| --- | --- |
| Severidad `S` | CVSS base válido dividido por 10, o una ponderación cualitativa si falta. |
| Proximidad `P` | `1 / profundidad` para profundidades positivas. Cero sin una ruta conocida desde la raíz. |
| Carácter directo `D` | Uno para dependencias directas, cero para las demás. |
| Alcance `A` | Uno para `required`, 0,5 para `optional` y cero para `excluded`. Para un alcance ausente o no reconocido, se usa 0,5. |

La severidad escala el total. El peso base de 70 conserva la influencia
principal del CVSS y los 30 puntos restantes distinguen proximidad,
dependencia directa y alcance. Se calcula con aritmética decimal y se
redondea al final a un decimal, con mitades hacia arriba. Un CVSS válido de
0 genera un score de 0. Los componentes desconectados conservan la profundidad
`NULL`. Su carácter directo proviene del grafo persistido. La raíz queda
fuera del escaneo de RF-04.

| Severidad disponible sin CVSS numérico | Factor `S` |
| --- | ---: |
| CRITICAL | 0,95 |
| HIGH | 0,80 |
| MEDIUM | 0,55 |
| LOW | 0,20 |
| NONE | 0 |
| UNKNOWN o cualquier etiqueta no reconocida | 0,35 |

Los factores cualitativos son ponderaciones propias de SecureGraph. El CVSS
ausente permanece `NULL`. Un CVSS inválido fuera de 0–10 se trata como ausente
para el cálculo. Una profundidad nula, negativa o cero no suma proximidad.

| Caso | Operación | Score |
| --- | --- | ---: |
| CVSS 10, directa, profundidad 1, required | 1 × (70 + 15 + 10 + 5) | 100,0 |
| CVSS 7,5, transitiva, profundidad 3, required | 0,75 × (70 + 5 + 0 + 5) | 60,0 |
| CVSS 5,9, transitiva, profundidad 2, required | 0,59 × (70 + 7,5 + 0 + 5) | 48,7 |
| Sin CVSS, HIGH, transitiva, profundidad 2, optional | 0,80 × (70 + 7,5 + 0 + 2,5) | 64,0 |
| Sin CVSS ni severidad reconocida, desconectada, scope desconocido | 0,35 × (70 + 0 + 0 + 2,5) | 25,4 |

## Explicaciones y persistencia

Cada hallazgo recibe cuatro razones en español, siempre en el orden
severidad, profundidad, carácter de la dependencia y alcance. Las razones
explican la procedencia de la entrada y el factor aplicado. Las razones
se guardan como líneas de texto en `razones_prioridad` y el puntaje en
`score_prioridad`, dentro de la transacción de importación SBOM. La
migración V4 calcula los valores para hallazgos históricos sin razones y
agrega una restricción de 0 a 100 en PostgreSQL. La migración usa el mismo
motor que las importaciones nuevas. Si cambia la fórmula, la migración V4
debe seguir usando esta versión del cálculo.

El alcance del SBOM es una señal declarada. El índice no mide
exposición en producción, probabilidad de explotación ni impacto de negocio.
La lectura REST de hallazgos persistidos y su ordenamiento corresponde a
RF-06. Las rutas y los DTO conservan el contrato actual.

Las pruebas PostgreSQL se habilitan con `SECUREGRAPH_TEST_POSTGRES_URL`
y las mismas credenciales de prueba descritas en [OSV-INTEGRATION.md](OSV-INTEGRATION.md).
