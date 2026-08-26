# Estrategia de testing

Documento autocontenido: qué garantiza cada suite de tests, cómo están organizadas y cómo correrlas.

En este proyecto **los tests son la prioridad número uno, por encima del código de producción**. La
razón es específica de un compilador: un analizador léxico tiene una superficie de entrada infinita
y llena de bordes (literales sin cerrar, escapes raros, caracteres invisibles, archivos truncados),
y la única forma de saber que funciona es ejercitarlo sistemáticamente. Un bug léxico no se
manifiesta como una excepción ruidosa: se manifiesta como un token de menos, silenciosamente, tres
etapas más adelante.

## 1. Las reglas del proyecto

1. **Todo código nuevo lleva tests.** No se escriben tests para subir un número de cobertura; se
   escriben para fijar comportamiento.
2. **Los tests verifican comportamiento, contratos e invariantes — nunca implementación interna.**
   Concretamente: un error se valida por **tipo y localización**, jamás por el texto completo del
   mensaje. Cambiar la redacción de un mensaje no debe romper ningún test.
3. **Todo bug encontrado se convierte en test de regresión permanente.** El ciclo es: reproducir →
   escribir el test que falla → arreglar → confirmar que pasa → el caso queda en la suite para
   siempre. Los casos de regresión son **append-only**: no se borran ni se renumeran.
4. **Nunca se ajusta un test para acomodar una implementación equivocada.** Si un test falla, se
   arregla el código. Si la especificación cambió legítimamente, se actualiza el test **y se
   documenta el motivo**.
5. **Un error interno del compilador es siempre un defecto.** Un error léxico ante un programa
   inválido es comportamiento **esperado**; cualquier otra cosa —`NullPointerException`,
   `StackOverflowError`, `ClassCastException`— hace fallar el test, sin excepción.
6. Deterministas, independientes entre sí, sin depender del orden de ejecución, sin `Thread.sleep`.
   El fuzzing usa **semilla fija** justamente por esto.

## 2. Los siete tipos de test y qué aporta cada uno

| Tipo | Qué verifica | Dónde |
|---|---|---|
| **Unitario** | Unidades aisladas y deterministas: el manejador de archivo, los tipos de token, el formateador de errores. | `ManejadorDeArchivoEficienteTest`, `TipoDeTokenTest`, `FormateadorDeErrorTest` |
| **Data-driven** | Muchas entradas evaluadas con la misma lógica: fuente → stream de tokens esperado. | `AnalizadorLexicoTest`, `LimitesNumericosTest`, `PosicionDelCaretTest` |
| **De integración** | Interacción entre piezas: manejador → analizador, analizador → módulo principal. | `ModuloPrincipalTest`, `ReporteDeErrorEleganteTest` |
| **End-to-end** | El pipeline completo, entrando por `Main.main()` sobre archivos reales. | los 2 testers de la cátedra, `TesterDeCasosConErroresEstricto`, `MainTest` |
| **Negativo** | Un caso por cada tipo de error léxico: que el programa inválido se rechace **correctamente**. | `TipoDeErrorLexicoTest`, `resources/conErrores/` |
| **Basado en propiedades** | Invariantes que deben valer para **cualquier** entrada, no para casos elegidos. | `AnalizadorLexicoPropiedadesTest`, `PropiedadesDelReporteTest` |
| **Fuzzing** | Robustez ante entradas sin ninguna estructura: bytes arbitrarios. | `AnalizadorLexicoFuzzTest`, `CorpusDeRegresionTest` |

A eso se suma el **análisis de mutación** (PIT), que no es un tipo de test sino una forma de medir
qué tan buena es la suite: introduce mutaciones en el código de producción y comprueba que algún test
las detecte. Un mutante que sobrevive señala un test faltante. Corre en un perfil aparte
(`mvn -Pmutacion test`) porque es lento; no en cada cambio.

## 3. Las invariantes, que son lo más valioso de la suite

Un caso concreto prueba que **una** entrada funciona. Una invariante prueba que **todas** funcionan.
Estas son las que sostiene el proyecto:

### 3.1. El análisis siempre termina

Para cualquier entrada, el compilador llega al `EOF` en una cantidad finita de pasos. Está
garantizado estructuralmente: el lexema de un error nunca es vacío, así que cada error consumió al
menos un carácter y la posición de lectura siempre avanza.

### 3.2. Los lexemas reconstruyen el fuente

Sobre un fuente sin blancos ni comentarios, **la concatenación de los lexemas de todos los tokens
emitidos y de todos los errores fatales reconstruye el fuente completo**, carácter por carácter.

Esta es la invariante más fuerte de la suite, porque cierra de un plumazo toda una familia de bugs:
si la recuperación se comiera un carácter, o si un estado consumiera algo que no reporta, la
concatenación no daría igual al original. Fue exactamente el bug que se encontró de esta forma —ver
[`decisiones.md`](decisiones.md) §4.2.

### 3.3. Las líneas nunca decrecen

El número de línea reportado por tokens y errores sucesivos es monótono no decreciente. Un fuente no
puede "volver" a una línea anterior.

### 3.4. El `^` nunca cae fuera de la línea impresa

En un reporte de error elegante, la columna del `^` siempre corresponde a un carácter que la línea
mostrada debajo de `Detalle:` realmente contiene. Un caret una posición más allá del último carácter
señala el aire — fue un bug real, y ahora es una invariante verificada tanto caso por caso
(`PosicionDelCaretTest`) como sobre entradas generadas (`PropiedadesDelReporteTest`).

### 3.5. Ningún fallo es un crash

Sobre bytes UTF-8 arbitrarios, cualquier fallo debe ser una `ExcepcionLexica` controlada. Cualquier
otra excepción es un defecto del compilador y hace fallar el test.

### 3.6. El formato de salida siempre se respeta

Cada línea de la salida es un token `(nombre,lexema,línea)`, un código `[Error:lexema|línea]`, una
línea del reporte elegante, o `[SinErrores]`. **Nunca** una línea partida en dos — que es lo que
pasaba cuando un lexema de error contenía un salto de línea crudo.

## 4. Los archivos de caso: `resources/`

Además de los tests en Java, el proyecto mantiene un corpus de archivos fuente MiniJava con su
oráculo escrito en las primeras líneas, en el formato que definen los testers de la cátedra.

```
resources/
├── sinErrores/    65 archivos   oráculo: //#lexema,nroLinea   (un token esperado, en orden)
├── conErrores/    77 archivos   oráculo: // [Error:lexema|nroLinea]
└── regresion/                    entradas que rompieron una corrida de fuzzing
```

### El tester estricto, y por qué hubo que escribirlo

El tester de errores que provee la cátedra sólo verifica que el **primer** código de error declarado
aparezca en algún lugar de la salida. Eso deja dos agujeros grandes:

- un caso con cinco errores queda casi sin verificar;
- **nada comprueba qué tokens salen entre medio** — y ahí es exactamente donde se escondía el bug de
  la recuperación tragándose caracteres.

Por eso el proyecto agrega `TesterDeCasosConErroresEstricto`, que corre sobre **los mismos archivos**
y verifica la salida **completa**, en orden: todos los códigos de error y todos los tokens. Los dos
testers de la cátedra quedan intactos al lado, corriendo en la misma pasada.

La cabecera de un caso estricto se ve así:

```java
// [Error:"a\q|7]          ← primera línea: lo que verifica el tester de la cátedra
// [Completo]              ← activa la verificación estricta de todo lo que sigue
// [Error:"a\q|7]
// [Error:"a\q b\z|7]
//#"a\q b\z c",7
//#$,8
"a\q b\z c"
```

### El corpus de regresión de fuzzing

`AnalizadorLexicoFuzzTest` persiste en `resources/regresion/` cualquier entrada aleatoria que haya
hecho fallar una corrida. Durante un tiempo esos archivos **no los leía nadie**: quedaban de adorno,
sin proteger de nada. `CorpusDeRegresionTest` los reprocesa todos en cada corrida.

No hay oráculo por caso —son entradas aleatorias, nadie sabe qué tokens "deberían" salir—; lo que se
verifica es el contrato que vale para cualquier entrada: que el compilador termine, que no lance nada
que no sea una `ExcepcionLexica`, y que la salida respete el formato.

## 5. Suites, una por una

| Suite | Casos | Qué cubre |
|---|---|---|
| `AnalizadorLexicoTest` | 146 | El grueso del reconocimiento: un caso por familia léxica y sus bordes. |
| `catedra.TesterDeCasosConErrores` | 77 | El tester original de la cátedra sobre `resources/conErrores/`. |
| `TesterDeCasosConErroresEstricto` | 77 | Los **mismos** archivos, pero verificando la salida completa. |
| `catedra.TesterDeCasosSinErrores` | 65 | El tester original de la cátedra sobre `resources/sinErrores/`. |
| `TipoDeTokenTest` | 22 | Que los nombres impresos coincidan con los que fija la cátedra. |
| `PosicionDelCaretTest` | 21 | Dónde apunta el `^`, con el diagnóstico de `javac` citado en cada caso. |
| `ManejadorDeArchivoEficienteTest` | 19 | EOF en los tres casos de ejemplo, archivo vacío, los tres convenios de fin de línea, `0x1A` embebido, UTF-8 inválido, columnas y línea-fuente. |
| `TipoDeErrorLexicoTest` | 16 | Un caso por cada tipo de error, validado por tipo y localización. |
| `CriterioDeRecuperacionTest` | 14 | Que tras cada error el análisis retome en el carácter correcto. |
| `LimitesNumericosTest` | 12 | El límite de 9 dígitos y su interacción con los floats. |
| `DivergenciasConJavaTest` | 9 | Los puntos donde MiniJava se aparta de Java **a propósito**. |
| `ModuloPrincipalTest` | 7 | El cliente completo, incluidas las combinaciones de configuración. |
| `FormateadorDeErrorTest` | 6 | Las cuatro líneas del reporte, y el `^` con tabuladores y caracteres CJK. |
| `PropiedadesDelReporteTest` | 5 | Las invariantes de §3.2, §3.4 y §3.6 sobre entradas generadas. |
| `AnalizadorLexicoPropiedadesTest` | 4 | Las invariantes de §3.1 y §3.3. |
| `AnalizadorLexicoFuzzTest` | 3 | Bytes UTF-8 arbitrarios con semilla fija. |
| `DefectosDeRobustezTest` | 3 | Caracteres de control e invisibles en lexemas y mensajes. |
| `MainTest` | 3 | El punto de entrada: sin argumentos, archivo inexistente, caso feliz. |
| `CorpusDeRegresionTest` | 2 | Reprocesa el corpus de fuzzing acumulado. |
| `ReporteDeErrorEleganteTest` | 1 | El ejemplo textual de la consigna, línea por línea. |

**Total: 512 casos ejecutables.**

## 6. Cómo correr

```bash
cd compilador

mvn test                 # suite propia + los 2 testers de la cátedra, en la misma pasada
mvn verify               # además exige el umbral de cobertura de JaCoCo
mvn -Pmutacion test      # análisis de mutación con PIT
```

Los testers de la cátedra están escritos con JUnit 4; corren junto a los propios (JUnit 5) gracias al
*vintage engine*, sin necesidad de tocarlos.

**El *working directory* debe ser `compilador/`**, porque los testers de la cátedra usan rutas
relativas duras (`resources/sinErrores/`, `resources/conErrores/`). Es el valor por defecto cuando
IntelliJ importa el proyecto Maven correctamente.

## 7. Ver también

- [`decisiones.md`](decisiones.md) — cada decisión, con el test que la fija.
- [`manejo-de-errores.md`](manejo-de-errores.md) — el detalle de las invariantes de error.
- [`arquitectura.md`](arquitectura.md) — por qué `Salida` es una interfaz, que es la decisión de
  diseño que hace posible casi toda esta suite.
