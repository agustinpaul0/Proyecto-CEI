# Arquitectura — decisiones de implementación

Índice: [`../AGENTS.md`](../AGENTS.md). Para el onboarding narrativo y los diagramas, ver
[`../documentacion/arquitectura.md`](../documentacion/arquitectura.md) y
[`../documentacion/patron-state.md`](../documentacion/patron-state.md) — este archivo es la versión
operativa/decisional, no el tutorial.

## Técnica de implementación — obligatoria, no es una elección abierta

La cátedra presenta tres estrategias en Clase 02 (Método por Estado, Switch/Case, Tabla) y aclara
"nosotros vamos a concentrarnos en esta estrategia" refiriéndose a Método por Estado. **El usuario
confirmó explícitamente que para este proyecto la técnica es Método por Estado y ninguna otra.**

Se implementa como **patrón State (GoF)**: un componente por cada estado del AF que simula el
comportamiento del autómata estando en ese estado — el esquema exacto de las slides 40-41 de Clase 02
(`Si caracterActual = c1 → agregar a lexema, avanzar, retornar resultado de e1() ...`).

**Único margen de decisión propio**: la cátedra encadena llamadas recursivamente (`return e1()`,
slides 38-58 de Clase 02); eso sigue siendo Método por Estado, pero tiene profundidad de pila
proporcional al largo del lexema (`StackOverflowError` real con un string o comentario largo). Se
sustituye el encadenamiento recursivo por un **driver iterativo** (`while`) que invoca la lógica de
transición de cada estado sin llamada recursiva — sigue siendo un componente por estado simulando el
AF, sólo cambia el mecanismo de encadenamiento. Esta decisión y su justificación van explícitas en el
informe para que quede claro que sigue siendo Método por Estado.

```java
public Token proximoToken() throws ExcepcionLexica {
    contexto.iniciarNuevoToken();
    EstadoLexico estado = EstadoInicial.INSTANCIA;
    while (true) {
        ResultadoDeTransicion resultado = estado.procesar(contexto);
        switch (resultado) {
            case Emitir emitir -> { return emitir.token(); }
            case Continuar continuar -> estado = continuar.siguiente();
        }
    }
}
```

`ResultadoDeTransicion` es una `sealed interface` con únicamente estos dos `permits`, por eso el
`switch` de arriba es exhaustivo sin `default` — el compilador exige cubrir ambos casos (requiere
`maven.compiler.release=25`, ver [`comandos-y-entrega.md`](comandos-y-entrega.md)). Detalle completo
de esta interacción (`EstadoLexico`, `ContextoLexico` y sus dos piezas, criterio singleton-vs-`new`
por estado, trazado paso a paso) en [`../documentacion/patron-state.md`](../documentacion/patron-state.md).

## Paquetes (`compilador/src/main/java/`)

```
minijava/
├── aplicacion/     Main (punto de entrada), ModuloPrincipal
├── config/          ConfiguracionDelCompilador (flags de logros)
├── io/              SourceManager + SourceManagerImpl (interfaz e impl ORIGINALES de la
│                     cátedra, contenido sin tocar — sólo se movió el `package` al mudarlas
│                     acá junto a la interfaz/impl propia que las extiende), ManejadorDeArchivo
│                     (interfaz propia) y ManejadorDeArchivoEficiente (logro, implementación real)
├── errores/         ExcepcionLexica, TipoDeErrorLexico, FormateadorDeError, PosicionLexica
│                     (las coordenadas del reporte), EscapadorDeNoImprimibles
├── tokens/          Token (record inmutable), TipoDeToken, TablaDePalabrasClave
├── salida/          interfaz Salida + implementaciones (real / en memoria para tests)
└── lexico/          AnalizadorLexico (el driver)
    ├── automata/       EstadoLexico, ResultadoDeTransicion + Continuar/Emitir
    ├── contexto/       ContextoLexico (fachada de BuilderDeLexema +
    │                    FabricaDeExcepcionesLexicas), AnclaDeError
    ├── recuperacion/   RecuperacionDeLitString (el único modo pánico que hay)
    └── estados/        EstadoInicial (el despachador) y un componente por estado del AF
                         (patrón State), agrupados por familia léxica: comentarios/,
                         identificadores/, literales/, numeros/, operadores/
```

**Criterio de ubicación, para no volver a discutirlo caso por caso**: `PosicionLexica` vive en
`errores/` porque línea + columna + línea fuente son las coordenadas del reporte, no del autómata;
`TablaDePalabrasClave` en `tokens/` porque su única razón de existir es decidir qué `TipoDeToken`
corresponde a un lexema; `RecuperacionDeLitString` fuera de `estados/` porque no es un estado del AF.
`FabricaDeExcepcionesLexicas` se queda del lado de `lexico/` a propósito: moverla a `errores/`
invertiría la dirección de la dependencia entre los dos paquetes.

El corte de `estados/` en familias deja package-private a `EstadosFloat.esSufijoFloat()` y a
`EstadoIdMV.esAlfanumericoOGuion()` (sus llamadores caen dentro de su propia familia). El único que
pierde encapsulamiento es `RecuperacionDeLitString`, que pasa a `public` — a cambio queda
directamente testeable en vez de sólo a través de un litString completo.

Tests bajo `compilador/src/test/java/`: `catedra/` (los 2 testers de la cátedra, paquete propio —
no son entregables, se pueden editar libremente) y `minijava/...` (espejo de la estructura de
producción).

**Por qué `Salida` es una interfaz** (no `System.out` llamado directo): es la decisión de mayor
impacto en la testeabilidad del proyecto — permite testear el Módulo Principal completo sin capturar
el stream global de la JVM, con una implementación `SalidaEnMemoria` para asserts directos.

**Reglas de diseño**: toda dependencia externa a una clase se inyecta por constructor (`Salida`,
`AnalizadorLexico`, `SourceManager`, formateador de error) — permite sustituir cualquier pieza en
tests sin mocks frameworks pesados. **Modularidad sobre tamaño de archivo**: no hay un límite
numérico de líneas por archivo — la regla es que cada clase tenga una responsabilidad clara y
acotada (un estado del AF, un formateador, una excepción); si una clase crece porque mezcla
responsabilidades, se separa por diseño, no para cumplir una cifra. Reglas de estilo de código
(comentarios, etc.) están en [`workflow.md`](workflow.md), no acá — esto es sólo estructura.

**Reuso concreto para no explotar en clases**: un solo componente `EstadoOperadorConSufijoOpcional`
parametrizado cubre los seis pares `>`/`>=`, `<`/`<=`, `!`/`!=`, `=`/`==`, `+`/`++`, `-`/`--`; los
símbolos de puntuación de un carácter se resuelven con una tabla `Map<Character,TipoDeToken>` en el
estado inicial, sin una clase por símbolo.

El manejo de errores (`ExcepcionLexica`, `PosicionLexica`, `FormateadorDeError`) tiene su propio
archivo: [`manejo-de-errores.md`](manejo-de-errores.md).

## Por qué el `SourceManagerImpl` de la cátedra no sirve tal cual

`SourceManagerImpl.java` (contenido original de la cátedra, hoy en `minijava.io`) lee **por líneas**
(`reader.readLine()`) y usa `reader.ready()` para decidir si hay más contenido. Ese diseño **falla
contra los propios casos de ejemplo de la cátedra**: `testersLexico/resources/sinErrores/
lexSinErrores01.java` tiene 21 saltos de línea y termina en newline; el oráculo (`//#$,22`) espera el
EOF en la línea 22, pero `reader.ready()` hace que el manejador devuelva EOF en la línea 21. También
lanza `NullPointerException` con un archivo vacío (`currentLine` queda `null` tras el primer
`readLine()`).

**Conclusión**: el logro "Manejador de Archivos Eficiente" (leer char a char con un buffer propio,
sólo `java.io`) es un requisito para pasar los tests de la cátedra, no un extra opcional. La interfaz
`SourceManager` se extiende (autorizado explícitamente en Pautas, slide 45: *"puede que por algún
otro logro sea necesario expandir la interface provista"*) con `getColumnNumber()` y
`getCurrentLineText()`, necesarios para el Reporte de Error Elegante.

**Semántica a implementar en el reemplazo**: `getLineNumber()`, `getColumnNumber()` y
`getCurrentLineText()` describen **el mismo carácter, el último devuelto** — los tres se fotografían
al entrar a `getNextChar()`, antes de mutar los contadores. El contador interno se sigue
incrementando al devolver el `\n`, pero lo observable es la foto: mientras el `\n` es el último
devuelto la línea es todavía la suya, y con eso los tres casos de EOF de arriba salen correctos sin
casos especiales. Esa simetría es la que hace innecesario el contador propio que `BuilderDeLexema`
mantenía en paralelo. Los
tres convenios de salto de línea (`\n`, `\r`, `\r\n`) se normalizan a `\n` antes de entregar el
carácter al léxico, así el autómata sólo ve un tipo de salto y cuenta líneas igual sin importar el
convenio del archivo fuente.
