# Estrategia de testing

Índice: [`../AGENTS.md`](../AGENTS.md). Los tests son la prioridad número uno del proyecto — más
importantes que el código de producción en sí. Este es el archivo que más se va a consultar durante
el desarrollo.

## Fases del compilador (marco para las 5 etapas)

```
Fuente → [Léxico] → [Sintáctico] → [AST] → [Tabla de Símbolos / Resolución de Nombres]
       → [Semántico] → [Chequeo de Tipos] → [Generación de Código] → [Ejecución]
```

Etapa 1 cubre sólo **Léxico**; el resto queda `N/A todavía — Etapa N` hasta que se publique esa
etapa. Cuando falla un test end-to-end, tiene que ser posible aislar en qué fase está el problema
usando los tests de esa fase específica — de ahí que los tests se ubiquen y nombren por fase.

## Tipos de test — cuándo corresponde cada uno (no se aplican todos a todo)

| Tipo | Cuándo usarlo | En Etapa 1 |
|---|---|---|
| **Unit** | unidades aisladas y deterministas | cada estado del AF, `Token`, tabla de palabras clave, clasificador de caracteres, formateadores de error, manejador de archivo |
| **Data-driven** | muchas entradas evaluadas con la misma lógica | fuente → stream de tokens esperado, un archivo de tabla por familia léxica (identificadores, enteros, chars, strings, operadores, comentarios, floats, unicode) |
| **Integration** | interacción entre componentes/fases | `SourceManager → AnalizadorLexico`, `AnalizadorLexico → ModuloPrincipal` |
| **E2E** | pipeline completo | `Main.main()` sobre los archivos de `resources/`, más los 2 testers de la cátedra sin modificar |
| **Negative** | programas inválidos deben rechazarse correctamente | un caso por cada `TipoDeErrorLexico` |
| **Regression** | todo bug relevante encontrado | append-only, nunca se borra ni se renumera un caso |
| **Golden/snapshot** | resultados estructurados grandes | salida completa del compilador por archivo de `resources/`, comparada entera |
| **Property-based** | invariantes que deben sostenerse sobre muchas entradas | terminación garantizada, líneas nunca decrecen, round-trip lexema+blancos = fuente original |
| **Fuzz** | robustez del léxico ante entradas inesperadas | bytes/UTF-8 aleatorios: cualquier resultado que no sea `ExcepcionLexica` (o éxito) es un fallo |
| **Mutation** | medir qué tan buena es la suite, no correr en cada cambio | PIT en un profile Maven aparte (`-Pmutacion`) |

**Prioridad**: Nivel 1 obligatorio siempre (unit, data-driven, integration, negative, regression);
Nivel 2 obligatorio cuando existe pipeline completo (E2E — ya existe en Etapa 1); Nivel 3
complementario (golden, property-based, fuzz); Nivel 4 evaluación periódica, no continua (mutation).
No convertir esto en regla rígida si la arquitectura real de una etapa futura pide otra distribución.

## Reglas generales

- Todo código nuevo lleva tests apropiados; no se escriben tests sólo para subir el número de
  cobertura.
- Los tests verifican comportamiento, contratos e invariantes — no implementación interna.
- Deterministas, independientes entre sí, sin depender del orden de ejecución, sin `Thread.sleep`.
- Nunca ocultar un error para hacer pasar un test, ni modificar un test para acomodar una
  implementación que está mal — si la especificación cambia legítimamente, se actualiza el test y se
  documenta el motivo (mensaje de commit descriptivo).
- Los tests viven cerca de la fase que validan (mismo paquete espejado bajo `src/test/java`), salvo
  los E2E que viven en su propio módulo de tests de integración completa.
- **Todo bug relevante se convierte en test de regresión permanente**: reproducir → escribir el test
  que reproduce el bug → confirmar que falla → arreglar → confirmar que pasa → el caso queda en la
  suite para siempre.

## Taxonomía de errores (severidad distinta para el test)

`error léxico` / `sintáctico` (etapa 2+) / `semántico` (etapa 3+) / `de tipos` (etapa 3+) /
**`interno del compilador`**. Los primeros cuatro son comportamiento **esperado** ante un programa
inválido — nunca deben tratarse como crash. Un **error interno del compilador es siempre un defecto**
(una excepción no controlada, un `NullPointerException`, cualquier cosa que no sea
`ExcepcionLexica`) y **debe hacer fallar el test**, sin excepción.

Los errores esperados se validan por **tipo y localización** (línea, columna), nunca por el texto
completo del mensaje — salvo el código de contrato de la cátedra `[Error:Lexema|NroLinea]`, que sí
se compara literal porque es lo que verifica el tester.

## Fuzzing reproducible

Semilla fija por corrida; todo input que produzca un fallo se persiste como caso permanente en
`compilador/resources/regresion/` (no se descarta al terminar la corrida de fuzzing).
