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

Tres corridas con semilla fija, cada una tapando un hueco distinto: alfabeto sesgado a caracteres
"interesantes", **bytes crudos** (la primera nunca puede generar UTF-8 inválido porque codifica un
`String`, y ahí se escondía un `UncheckedIOException` que reventaba el compilador con un archivo
binario), y construcciones sin cerrar (donde vive la mayor parte de la lógica de error).

Todo input que produzca un fallo se persiste como caso permanente en
`compilador/resources/regresion/`, y `CorpusDeRegresionTest` **reprocesa ese directorio entero** en
cada corrida — antes no lo leía ningún tester, así que los casos guardados no protegían de nada. Ya encontró y
confirmó un bug real en esta sesión (el de los carets colgando — ver `manejo-de-errores.md`); el caso
que lo disparó quedó persistido como regresión permanente.

## Verificación cruzada contra la suite de un compañero (26/08/2026)

Un compañero compartió su suite (3 testers JUnit 4 + 24 casos). Sirvió como **oráculo
independiente**, que es el uso que vale la pena darle a material ajeno: los 24 casos se corrieron
contra este compilador con un script descartable —no se copió nada al proyecto para probarlos— y
**pasaron los 24**, incluida la secuencia exacta de tokens y errores intercalados. Ninguna
divergencia, ningún bug encontrado.

Qué era cada cosa, para no volver a evaluarlo desde cero:

- Sus `TesterDeCasosSinErrores` y `TesterDeCasosConErrores` son los mismos archivos de la cátedra
  que ya están en `src/test/java/catedra/` (idénticos salvo espacios).
- Su `TesterDeCasosConErroresYTokens` cumple la función de nuestro
  `minijava.casos.TesterDeCasosConErroresEstricto` y usa **el mismo formato de anotación**
  (`// [Error:lex|linea]` + `//#lex,linea`) — el nuestro verifica además la ausencia de
  `[SinErrores]` y que ninguna línea de salida lleve un carácter de control. No se integró.
- Sus casos son casi un subconjunto de `resources/`: sólo cuatro huecos reales, que quedaron
  cubiertos con casos propios escritos en el formato del corpus —
  `sinErrores/puntoSeguidoDeSufijoDeFloat.java` (`1.f`: la forma `Digito+ . Digito* Sufijo` con
  parte fraccionaria vacía), `sinErrores/igualdadCuadrupleYBarraAsteriscoSeparados.java` (`a====b`,
  `a!=!b`, y `/` seguido de espacio y `*`, que **no** abre un comentario),
  `sinErrores/charsConDolarYControlesEscapados.java` (`'$'` — adversarial, porque `$` es el lexema
  del `EOF` — más `'\r'`, `'\f'`, `'\b'`) y `conErrores/pipeTriple.java` (`|||`, la simetría que
  faltaba con `ampersandTriple.java`).

**Lección para la próxima**: el aporte de una suite ajena está en la verificación, no en el volumen
de casos. Correrla antes de copiar nada evita ensuciar el corpus con duplicados, y un caso ajeno
cuya expectativa contradice una decisión ya documentada se informa —verificándolo contra `javac`
como manda `AGENTS.md` §0.1— pero no se adopta.

## El oráculo externo: `javac`

Cuando la cátedra no especifica un comportamiento, `AGENTS.md` §0.1 manda hacer lo que hace Java,
**verificado, no de memoria**. En la práctica eso significa escribir el caso en un `.java` y correr
`javac`. Ya cambió dos decisiones de diseño:

- el criterio de recuperación (`javac` no sincroniza hasta el delimitador y nunca descarta un
  carácter ajeno al lexema reportado — ver `documentacion/manejo-de-errores.md` §2);
- dónde apunta el `^` en cada diagnóstico.

Y confirmó una tercera: los floats **no** tienen límite de dígitos, ni en la cátedra ni en Java; lo
que Java rechaza es un valor fuera de rango, y eso es semántico, no léxico.

**Regla**: todo test cuyo oráculo sea "así hace Java" lleva en su comentario la salida **real** de
`javac`, copiada de la corrida, para que sea rastreable y no una afirmación de memoria.

**Y el límite del oráculo**: `javac` vale mientras la cátedra calle. Donde el profesor fijó un
criterio, ése manda aunque Java haga otra cosa — hoy pasa en un solo lugar, el escape mal formado
dentro de un `litString` (`documentacion/decisiones.md` §4.3), donde `javac` sigue leyendo el
literal y MiniJava lo descarta hasta la comilla de cierre. Los tests de ese caso citan la indicación
del profesor, no a `javac`.

## Los dos testers de casos de `resources/conErrores/`

Corren sobre los **mismos archivos** y conviven:

- `catedra.TesterDeCasosConErrores` — el de la cátedra, intacto. Lee **sólo la primera línea** y hace
  `containsString` del código declarado.
- `minijava.casos.TesterDeCasosConErroresEstricto` — propio. Con la marca `// [Completo]` en la
  cabecera exige la secuencia **exacta y ordenada** de errores **y de tokens**, sin nada de más;
  verifica además que no aparezca `[SinErrores]` y que ninguna línea lleve un carácter de control.

Declarar los tokens y no sólo los errores es lo que hace verificable que la recuperación no se coma
nada: con el criterio viejo, `v1 &v2` imprimía `(intLiteral,2,1)` y ningún tester lo notaba.

Los casos nuevos se generan con `compilador/herramientas/lote*.py`, que calculan la línea del `EOF` y
el desplazamiento de la cabecera. **Las expectativas se predicen antes de correr**: si un caso falla,
lo primero es decidir si el equivocado es el oráculo o el compilador. Varios casos del corpus salen
de haber predicho mal y haber ido a mirar por qué.

## Mutation testing (PIT) — score actual y por qué ciertos sobrevivientes no se cazan

Última corrida: **92% de mutantes matados** (271/293), *test strength* 95%. Comando:
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

**Sobrevivientes aceptados, no perseguidos**: `SalidaEstandar` toca `System.out` real —
deliberadamente no se testea de forma directa (para eso existe la interfaz `Salida`, que sí está 100%
testeada vía `SalidaEnMemoria`). `Main.main()` **sí** se testea ahora (`MainTest`), pero sólo en sus
modos de falla —sin argumentos, archivo inexistente, extensión rara— capturando `System.out`: son los
casos en los que una excepción sin atrapar mataría la corrida in-process de los testers de la
cátedra, así que valen el costo. `ModuloPrincipal`'s `manejador.close()` no se
puede espiar sin inyectar `ManejadorDeArchivo` por constructor (hoy se instancia adentro del método)
— cambio de diseño no justificado sólo para este hueco, dado que `close()` únicamente libera memoria
en un proceso de corta vida (no hay handle de archivo del SO abierto de por medio). `SourceManagerImpl`
es código original de la cátedra sin tocar, no se le agregan tests propios.
