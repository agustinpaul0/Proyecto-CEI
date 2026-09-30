# Comandos y formato de entrega

Índice: [`../AGENTS.md`](../../AGENTS.md).

## Comandos

```bash
cd compilador
mvn test                 # suite completa: la nuestra + los 6 testers de la cátedra (vintage engine)
mvn verify                # + chequeo de umbral de cobertura JaCoCo (falla si baja del umbral)
mvn -Pmutacion verify     # PIT — mutantes sobrevivientes = tests faltantes (verify, no test:
                          # PIT esta atado a esa fase y con `test` no muta nada)

# Cada caso del corpus vuelca a consola la salida real del compilador (tokens,
# [Error:...] y el reporte elegante) más un pie con los archivos procesados.
mvn test -Dvolcado=resumen   # una línea por caso + sólo sus códigos de error
mvn test -Dvolcado=off       # sin volcado, para una corrida limpia
mvn package                # genera target/Compilador.jar

# Sólo una fase de la suite. Los tests de comun/ corren en todas.
mvn test -Pfase-lexico
mvn test -Pfase-sintactico
mvn test -Pfase-semantico
```

### Invocar el compilador

```bash
# El contrato de la cátedra: un único argumento, cualquier extensión.
java -jar target/Compilador.jar programa1.java

# Etapa 1: el volcado de tokens del léxico.
java -jar target/Compilador.jar --etapa=1 resources/lexico/sinErrores/lexSinErrores01.java

# Etapa 2: sólo el sintáctico, sin tabla de símbolos.
java -jar target/Compilador.jar --etapa=2 resources/sintactico/sinErrores/todosLosLogrosJuntos.java
```

Sin `--etapa`, corre la Etapa 3.

`--etapa` es la única opción: **no hay forma de elegir con qué logros correr**.

Una opción desconocida se informa por `System.out` y **no** aborta: los testers de la cátedra
invocan `main()` in-process y una excepción mataría la corrida entera.

### Al armar el zip de la entrega

El zip es la descarga del repositorio desde GitHub, así que lleva lo versionado: el proyecto Maven
en `compilador/` y el informe en `etapas/etapa-N/entregables/`. Tiene que bajarse **después del
último push**.

El zip tiene que pesar menos de 20 MB. Por eso el `.gitattributes` de la raíz marca como
`export-ignore` las slides de clase y las Pautas de la cátedra (`etapas/**/Clase*.pdf` y
`etapas/**/Pautas*.pdf`): siguen versionadas, pero GitHub no las mete en la descarga.
`git archive HEAD --format=zip -o prueba.zip` arma el mismo zip localmente y sirve para medirlo
antes de pushear.

**No hay ningún paso de copia.** Los testers de la cátedra buscan el corpus en
`resources/sinErrores/` y `resources/conErrores/`, escrito a fuego y sin subcarpeta, así que el
corpus de la etapa **que se entrega** vive ahí: hoy, el de la Etapa 3. Los de las etapas anteriores
quedan archivados en `resources/lexico/` y `resources/sintactico/`, y sus testers los buscan ahí.
Cada uno se corre entero con su perfil (`-Pfase-lexico`, `-Pfase-sintactico`).

El corpus ajeno de `CorpusExternoTest` va adentro del proyecto, en
`compilador/resources/corpus-externo/`: son 29.101 fuentes y unos 10 megabytes. Los testers de la
cátedra no lo recorren.

**La contracara, que conviene tener presente:** el tester de la Etapa 1 que la cátedra tiene
también busca en `resources/sinErrores/`, así que sobre esta entrega encontraría los casos de la
Etapa 3. Es deliberado: la raíz le pertenece a la etapa que se está entregando, y las anteriores ya
tienen su corpus completo en su carpeta. Cuando arranque la Etapa 4, el corpus del semántico se
muda a `resources/semantico/` y el nuevo ocupa la raíz.

Java: `maven.compiler.release=25` (coincide con el JDK instalado; no hay ninguna directiva de la
cátedra que fije una versión distinta). JaCoCo en `0.8.15` — versiones anteriores (`0.8.12` y por
debajo) no leen bytecode de Java 25 (class file major version 69).

## Formato del informe entregable

Cada etapa entrega su informe como **PDF**, compilado desde LaTeX en
`etapas/etapa-N/entregables/informe/`. Qué tiene que contener lo fijan las Pautas de cada etapa: la
Etapa 1, slide 39 (los tokens con la ER que los define, las decisiones relevantes para probar el
compilador y los logros perseguidos); la Etapa 2, slide 33 (cómo compilar y correr, el proceso de
transformación de la gramática con su versión final, los tipos de errores sintácticos, la
documentación usual de un proyecto de software y los logros que se intentan); la Etapa 3, slide 36
(cómo compilar y correr, el diagrama de clases de la tabla de símbolos y los logros que se
intentan). Requisitos fijados por el usuario: **lo más breve y conciso posible**, con sólo lo que
exigen las Pautas y sin repetir `documentacion/`; **paleta de colores monocromática**, **portada
minimalista** e **índice/tabla de contenidos**.
