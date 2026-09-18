# Comandos y formato de entrega

Índice: [`../AGENTS.md`](../../AGENTS.md).

## Comandos

```bash
cd compilador
mvn test                 # suite completa: la nuestra + los 4 testers de la cátedra (vintage engine)
mvn verify                # + chequeo de umbral de cobertura JaCoCo (falla si baja del umbral)
mvn -Pmutacion verify     # PIT — mutantes sobrevivientes = tests faltantes (verify, no test:
                          # PIT esta atado a esa fase y con `test` no muta nada)

# Cada caso del corpus vuelca a consola la salida real del compilador (tokens,
# [Error:...] y el reporte elegante) más un pie con los archivos procesados.
mvn test -Dvolcado=resumen   # una línea por caso + sólo sus códigos de error
mvn test -Dvolcado=off       # sin volcado, para una corrida limpia
mvn package                # genera target/Compilador.jar

# Sólo una fase de la suite. Los tests de comun/ corren en las dos.
mvn test -Pfase-lexico
mvn test -Pfase-sintactico
```

### Invocar el compilador

```bash
# El contrato de la cátedra: un único argumento, cualquier extensión.
java -jar target/Compilador.jar programa1.java

# Etapa 1: el volcado de tokens del léxico.
java -jar target/Compilador.jar --etapa=1 resources/lexico/sinErrores/lexSinErrores01.java
```

`--etapa` es la única opción: **no hay forma de elegir con qué logros correr**.

Una opción desconocida se informa por `System.out` y **no** aborta: los testers de la cátedra
invocan `main()` in-process y una excepción mataría la corrida entera.

### Al armar el zip de la Etapa 2

El zip es la descarga del repositorio desde GitHub, así que lleva exactamente lo versionado: el
proyecto Maven en `compilador/` y el informe en `etapas/etapa-2/entregables/`. Tiene que bajarse
**después del último push**.

**No hay ningún paso de copia.** El corpus de la etapa **activa** vive directamente en
`resources/sinErrores/` y `resources/conErrores/`, que es exactamente donde los testers de la
cátedra lo buscan —lo tienen escrito a fuego, sin subcarpeta—; el de la Etapa 1 queda archivado en
`resources/lexico/`.

Son **10.251 archivos y unos 2,6 megabytes** de corpus, y los testers de la cátedra los procesan en
unos 3 segundos. Todos están verificados con el compilador tal cual, que es la única forma en que corre.

**La contracara, que conviene tener presente:** el tester de la Etapa 1 también busca en
`resources/sinErrores/`, así que sobre esta entrega encontraría los casos del sintáctico. Es
deliberado: la raíz le pertenece a la etapa que se está entregando, y la Etapa 1 ya está corregida.
Su corpus está completo en `resources/lexico/` y `mvn test -Pfase-lexico` lo corre entero. Cuando
arranque la Etapa 3, el corpus del sintáctico se muda a `resources/sintactico/` y el nuevo ocupa la
raíz.

Java: `maven.compiler.release=25` (coincide con el JDK instalado; no hay ninguna directiva de la
cátedra que fije una versión distinta). JaCoCo en `0.8.15` — versiones anteriores (`0.8.12` y por
debajo) no leen bytecode de Java 25 (class file major version 69).

## Formato del informe entregable

Cada etapa entrega su informe como **PDF**, compilado desde LaTeX en
`etapas/etapa-N/entregables/informe/`. Qué tiene que contener lo fijan las Pautas de cada etapa: la
Etapa 1, slide 39 (los tokens con la ER que los define, las decisiones relevantes para probar el
compilador y los logros perseguidos); la Etapa 2, slide 33 (cómo compilar y correr, el proceso de
transformación de la gramática con su versión final, los tipos de errores sintácticos, la
documentación usual de un proyecto de software y los logros que se intentan). Requisitos de diseño
fijados por el usuario: **paleta de colores monocromática**, **portada minimalista**,
**índice/tabla de contenidos**.
