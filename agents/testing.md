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
`compilador/resources/regresion/` (no se descarta al terminar la corrida de fuzzing). Ya encontró y
confirmó un bug real en esta sesión (el de los carets colgando — ver `manejo-de-errores.md`); el caso
que lo disparó quedó persistido como regresión permanente.

## Mutation testing (PIT) — score actual y por qué ciertos sobrevivientes no se cazan

Última corrida: **92% de mutantes matados** (238/259), *test strength* 96%. Comando:
`mvn -Pmutacion org.pitest:pitest-maven:mutationCoverage` (el goal no está bindeado a ninguna fase
del profile, hay que invocarlo explícito).

**Patrón recurrente de mutante equivalente en este código base**: varios estados hacen
`contexto.avanzar(); return new Continuar(SiguienteEstado);` justo antes de pasarle el control a un
estado cuya PROPIA lógica vuelve a chequear y consumir ese mismo carácter (por ejemplo,
`EstadoInicial` viendo un dígito pasa a `EstadoEntero`, que en su primer chequeo también acepta
dígitos). Si PIT elimina ese `avanzar()` puntual, el carácter se termina consumiendo de todos modos
—un paso después, por el chequeo duplicado del estado siguiente— y el lexema final resulta idéntico.
Esto hace que el mutante **sobreviva sin que sea un hueco real de testing**: es un mutante
equivalente, invisible a los filtros automáticos de PIT porque cruza el límite entre dos clases
distintas. Confirmado así en: `EstadoInicial` (dígito), `EstadoIdGenOClase`, `EstadoCharEscapado` y
`EstadoStringEscapado` (marcador `u`), `EstadoFloatExponenteInicio`,
`EstadoFloatExponenteRequiereDigito`, `EstadoBarra` (ambas ramas de comentario). **No intentar
"matarlos" agregando tests** — no hay ningún test posible que distinga el comportamiento mutado del
original, porque no lo hay.

**Sobrevivientes reales que sí se corrigieron** (agregando aserciones de lexema exacto donde sólo se
verificaba que lanzara excepción o el tipo de token): `EstadoCharAbierto`/`EstadoStringEscapado`
(agregarCaracterOfensivo en casos de error), y falta de cobertura completa de `EstadoInicial`
(puntuación) y `EstadoFloatExponenteDigitos` (exponentes de 2+ dígitos, nunca ejercitados).

**Sobrevivientes aceptados, no perseguidos**: `Main.main()` y `SalidaEstandar` tocan `System.out`
real — deliberadamente no se testean de forma directa (para eso existe la interfaz `Salida`, que sí
está 100% testeada vía `SalidaEnMemoria`); forzar un test ahí implicaría capturar stdout real o
lanzar un proceso, sin aportar valor proporcional. `ModuloPrincipal`'s `manejador.close()` no se
puede espiar sin inyectar `ManejadorDeArchivo` por constructor (hoy se instancia adentro del método)
— cambio de diseño no justificado sólo para este hueco, dado que `close()` únicamente libera memoria
en un proceso de corta vida (no hay handle de archivo del SO abierto de por medio). `SourceManagerImpl`
es código original de la cátedra sin tocar, no se le agregan tests propios.
