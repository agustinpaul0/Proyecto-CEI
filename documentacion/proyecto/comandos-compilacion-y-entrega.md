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

# Etapa 3: el chequeo de declaraciones. Es también la etapa por defecto.
java -jar target/Compilador.jar --etapa=3 resources/sinErrores/herenciaGenericaConArgumento.java
```

Sin `--etapa`, corre la última etapa, la 3. Cada etapa tiene su opción explícita, y los testers de
la cátedra de cada una la pasan (`--etapa=1`, `--etapa=2`, `--etapa=3`), así que una etapa nueva
no obliga a tocarlos. `Main` elige el módulo principal en un mapa de etapa a módulo: una etapa
nueva es una entrada más, y `MainTest` controla que el mapa cubra exactamente las etapas que
acepta la opción.

`--etapa` es la única opción: **no hay forma de elegir con qué logros correr**.

Una opción desconocida se informa por `System.out` y **no** aborta: los testers de la cátedra
invocan `main()` in-process y una excepción mataría la corrida entera.

### Al armar el zip de la entrega

El zip es la descarga del repositorio desde GitHub, así que lleva lo versionado: el proyecto Maven
en `compilador/` y el informe en `etapas/etapa-N/entregables/`. Tiene que bajarse **después del
último push**.

Se versiona sólo lo que hace a la entrega:
- el proyecto Maven, con su corpus;
- la documentación y los informes;
- todo lo usado para desarrollar con IA, que el profesor pidió revisar: `AGENTS.md`, `CLAUDE.md`,
  `.claude/`, `.agents/` y `.codex/`.

No se versionan las slides, las Pautas ni la información administrativa de la cátedra
(`etapas/*.pdf`, `etapas/**/Clase*.pdf` y `etapas/**/Pautas*.pdf`, en el `.gitignore`): viven
sólo en la copia local, donde se leen. Tampoco `vendor/`. Los PDF de `documentacion/minijava/`,
que describen el lenguaje, sí se versionan: la documentación los cita.

El zip tiene que pesar menos de 20 MB; el de la Etapa 3 pesa 7,3 MB. `git archive HEAD --format=zip -o prueba.zip` arma el mismo
zip localmente y sirve para medirlo antes de pushear.

**No hay ningún paso de copia.** Los testers de la cátedra buscan el corpus en
`resources/sinErrores/` y `resources/conErrores/`, escrito a fuego y sin subcarpeta, así que el
corpus de la etapa **que se entrega** vive ahí: hoy, el de la Etapa 3. Los de las etapas anteriores
quedan archivados en `resources/lexico/` y `resources/sintactico/`, y sus testers los buscan ahí.
Cada uno se corre entero con su perfil (`-Pfase-lexico`, `-Pfase-sintactico`).

El corpus ajeno de `CorpusExternoTest` va adentro del proyecto, en
`compilador/resources/corpus-externo/`. Son 29.101 fuentes, empaquetados en un `.tar.gz` de 4,4 MB,
y los testers de la cátedra no lo recorren. Los generados del sintáctico van empaquetados de la
misma forma: sueltos, las cabeceras del zip pesaban tanto como los fuentes
([`estrategia-de-testing.md`](../compartido/estrategia-de-testing.md) §2).

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
