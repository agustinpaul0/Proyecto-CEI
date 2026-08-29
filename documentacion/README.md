# Compilador MiniJava — Documentación técnica

Documentación del proyecto de la materia **Compiladores e Intérpretes** (Departamento de Ciencias e
Ingeniería de la Computación, Universidad Nacional del Sur, 2° cuatrimestre 2026). Cubre la
**Etapa 1: Analizador Léxico + Módulo Principal**.

Toda esta carpeta es **autocontenida**: se puede leer entera sin abrir una sola línea de código y sin
consultar ningún otro archivo del repositorio. Está escrita para que un lector externo entienda no
sólo *qué* hace el compilador, sino **por qué hace cada cosa de la forma en que la hace**, y de dónde
sale cada decisión.

## Guía de lectura

| # | Documento | Qué responde |
|---|---|---|
| 1 | [`arquitectura.md`](arquitectura.md) | Qué hace el proyecto, qué piezas lo componen, cómo se comunican, qué pasa de punta a punta cuando se lo ejecuta. **Empezar acá.** |
| 2 | [`especificacion-lexica.md`](especificacion-lexica.md) | La expresión regular exacta de los 56 tokens, sus autómatas, y qué queda deliberadamente afuera del lenguaje. |
| 3 | [`patron-state.md`](patron-state.md) | Cómo se implementó la técnica *Método por Estado* que exige la cátedra, pieza por pieza, con trazados completos. |
| 4 | [`manejo-de-errores.md`](manejo-de-errores.md) | Cómo se detecta un error, cómo se recupera el análisis, y los dos formatos de reporte. |
| 5 | [`manejador-de-archivo.md`](manejador-de-archivo.md) | Los requisitos del *Manejador de Archivos Eficiente* y cómo los resuelve el diseño, incluido el costo de ubicar un error. |
| 6 | [`decisiones.md`](decisiones.md) | **El catálogo completo de decisiones**: cada caso ambiguo, la alternativa descartada, la fuente que lo resuelve y la justificación. |
| 7 | [`testing.md`](testing.md) | Qué garantiza cada suite de tests, cómo están organizados y cómo correrlos. |
| 8 | [`logros.md`](logros.md) | Los 8 logros de la Etapa 1: qué exige cada uno, dónde está implementado y cómo verificarlo. |

## Resumen en una página

El programa toma un archivo fuente MiniJava y produce, por consola, la secuencia de sus componentes
léxicos, un token por línea, en el formato exacto que fija la cátedra:

```console
$ java -jar Compilador.jar programa1.java
(pr_class,class,1)
(idClase,Alpha,1)
(llaveA,{,1)
(pr_var,var,2)
(idMV,texto,2)
(op_asignacion,=,2)
(litString,"hola, mundo",2)
(puntoYComa,;,2)
...
(EOF,$,22)

[SinErrores]
```

Si el fuente tiene errores léxicos, en lugar de `[SinErrores]` aparece un reporte por cada error, y
el análisis **no se detiene en el primero** (logro *Multi-detección de Errores*):

```console
$ java -jar Compilador.jar conErrores.java
(idMV,v1,2)
Error Léxico en línea 2, columna 4: # no es un símbolo válido
Detalle: v1 # v2
            ^

[Error:#|2]
(idMV,v2,2)
(EOF,$,3)
```

Las tres primeras líneas del reporte son un agregado propio (logro *Reporte de Error Elegante*); la
última, `[Error:Lexema|NroLinea]`, es el contrato exacto que verifican los testers de la cátedra. Los
renglones en blanco antes de `[SinErrores]` y de `[Error:...]` son los de los ejemplos de Pautas
Etapa 1 (slides 25 y 26).

### Datos del proyecto

| | |
|---|---|
| Lenguaje reconocido | MiniJava — subconjunto estricto de Java, sin excepciones ni hilos |
| Técnica de implementación | Método por Estado (patrón State), **sin generadores automáticos** |
| Tokens reconocidos | 56 (21 palabras clave, 3 familias de identificadores, 4 de literales, 10 símbolos de puntuación, 17 operadores, EOF) |
| Estados del autómata | 22 clases en `minijava.lexico.estados`, una por estado |
| Clases de producción | 49 |
| Suites de test | 22 (20 propias + los 2 testers de la cátedra), con **627 casos** ejecutables |
| Casos de fuente | 76 sin errores + 82 con errores + 1 de regresión de fuzzing |
| Logros perseguidos | los 8 de la Etapa 1 |
| Java | 25 (`maven.compiler.release=25`) |
| Build | Maven; `mvn package` genera `target/Compilador.jar` |

### Cómo correrlo

```bash
cd compilador
mvn package                 # genera target/Compilador.jar
java -jar target/Compilador.jar resources/sinErrores/lexSinErrores01.java

mvn test                    # suite propia + los 2 testers de la cátedra, en la misma pasada
mvn verify                  # además exige el umbral de cobertura JaCoCo
mvn -Pmutacion test         # análisis de mutación con PIT (no corre en cada cambio)
```

El archivo fuente puede tener **cualquier extensión** (`.java`, `.mjava`, `.txt`, ninguna): el
compilador no inspecciona el nombre, sólo abre la ruta que recibe.

Desde IntelliJ IDEA: abrir la carpeta `compilador/`, que es donde está el `pom.xml`. El *working
directory* de los tests debe ser esa misma carpeta, porque los testers de la cátedra usan rutas
relativas duras (`resources/sinErrores/`, `resources/conErrores/`).

## Cómo se tomaron las decisiones

Dos reglas gobiernan todo el proyecto, y explican la mayoría de las decisiones documentadas acá:

1. **Lo que la cátedra especifica, se respeta al pie de la letra.** El formato de salida, el código
   de error, la técnica de implementación y las expresiones regulares de los tokens salen de
   `Aspectos Lexicos MiniJava 2026.pdf`, `Pautas Etapa 1.pdf` y las clases 01 y 02. Cuando este
   documento afirma algo de la cátedra, cita la fuente.
2. **Lo que la cátedra no especifica, se resuelve como en Java** — regla escrita explícitamente por
   la cátedra: *"todo lo que NO esté especificado para MiniJava tiene comportamiento por defecto
   igual al de Java"*. Y "como en Java" nunca se resolvió de memoria: se verificó contra la
   especificación (JLS) o **ejecutando `javac`** sobre el caso concreto, y el resultado de esa
   ejecución quedó registrado en el documento y fijado en un test.

Las dos reglas dejan un residuo: casos donde MiniJava **diverge** de Java a propósito, porque la
cátedra especificó algo distinto. Están todos enumerados en [`decisiones.md`](decisiones.md) §7,
porque son exactamente los casos que un evaluador querría revisar.
