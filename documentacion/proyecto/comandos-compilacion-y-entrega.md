# Comandos y formato de entrega

Índice: [`../AGENTS.md`](../../AGENTS.md).

## Comandos

```bash
cd compilador
mvn test                 # suite completa: la nuestra + los 2 testers de la cátedra (vintage engine)
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

# Etapa 1: vuelve al volcado de tokens.
java -jar target/Compilador.jar --etapa=1 resources/lexico/sinErrores/lexSinErrores01.java
```

`--etapa` es la única opción: **no hay forma de elegir con qué logros correr**, porque el
compilador reconoce MiniJava con todos los de la Etapa 1 y la Etapa 2 incorporados, siempre.

Una opción desconocida se informa por `System.out` y **no** aborta: los testers de la cátedra
invocan `main()` in-process y una excepción mataría la corrida entera.

### Al armar el zip de la Etapa 2

**No hay ningún paso de copia.** El corpus de la etapa **activa** vive directamente en
`resources/sinErrores/` y `resources/conErrores/`, que es exactamente donde los testers de la
cátedra lo buscan —lo tienen escrito a fuego, sin subcarpeta—; el de la Etapa 1 queda archivado en
`resources/lexico/`. Armar la entrega es comprimir la carpeta:

```bash
cd compilador
mvn clean package                 # deja target/Compilador.jar
zip -r ../Etapa2-Paul.zip . -x 'target/*' '.venv/*' 'herramientas/fuzz/jars/*' \
                                'herramientas/fuzz/trabajo/*'
```

Son **1953 archivos y medio megabyte** de corpus, y los testers de la cátedra los procesan en unos
4 segundos. Todos están verificados con el compilador tal cual, que es la única forma en que corre.

**La contracara, que conviene tener presente:** el tester de la Etapa 1 también busca en
`resources/sinErrores/`, así que sobre esta entrega encontraría los casos del sintáctico. Es
deliberado: la raíz le pertenece a la etapa que se está entregando, y la Etapa 1 ya está corregida.
Su corpus sigue completo en `resources/lexico/` y `mvn test -Pfase-lexico` lo corre entero. Cuando
arranque la Etapa 3, el corpus del sintáctico se muda a `resources/sintactico/` y el nuevo ocupa la
raíz.

Java: `maven.compiler.release=25` (coincide con el JDK instalado; no hay ninguna directiva de la
cátedra que fije una versión distinta). JaCoCo en `0.8.15` — versiones anteriores (`0.8.12` y por
debajo) no leen bytecode de Java 25 (class file major version 69).

## Formato del informe entregable (para cuando se redacte)

El informe pedido por la cátedra (tokens + ER que los define, decisiones relevantes para probar el
compilador, logros perseguidos — Pautas, slide 39) se entrega como **PDF final**; si el paso
intermedio Markdown→PDF resulta más trabajoso que escribir el PDF directamente, se genera el PDF
directamente, sin pasar por Markdown. Requisitos de diseño fijados por el usuario:
**paleta de colores monocromática**, **portada minimalista**, **índice/tabla de contenidos**.
