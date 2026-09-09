# Comandos y formato de entrega

Índice: [`../AGENTS.md`](../../AGENTS.md).

## Comandos

```bash
cd compilador
mvn test                 # suite completa: la nuestra + los 2 testers de la cátedra (vintage engine)
mvn verify                # + chequeo de umbral de cobertura JaCoCo (falla si baja del umbral)
mvn -Pmutacion test       # PIT — mutantes sobrevivientes = tests faltantes

# Cada caso del corpus vuelca a consola la salida real del compilador (tokens,
# [Error:...] y el reporte elegante) más un pie con los archivos procesados.
mvn test -Dvolcado=resumen   # una línea por caso + sólo sus códigos de error
mvn test -Dvolcado=off       # sin volcado, para una corrida limpia
mvn package                # genera target/Compilador.jar

# Sólo una fase de la suite. Los tests de comun/ corren en las dos.
mvn test -Pfase-lexico
mvn test -Pfase-sintactico

# La suite en otra configuración de logros. Un test o un caso del corpus que no
# aplique se SALTEA, no falla.
mvn test -Dlogros=ninguno
mvn test -Dlogros=todos
mvn test -Dlogros=etapa1
mvn test -Dlogros=ternario,lambda
```

### Invocar el compilador

```bash
# El contrato de la cátedra: un único argumento, cualquier extensión.
java -jar target/Compilador.jar programa1.java

# Etapa 1: vuelve al volcado de tokens, con los logros de esa etapa.
java -jar target/Compilador.jar --etapa=1 resources/lexico/sinErrores/lexSinErrores01.java

# Prender y apagar logros. Sin --logros manda la etapa: --etapa=1 trae los de la
# Etapa 1, y la Etapa 2 los trae todos.
java -jar target/Compilador.jar --logros=ninguno programa1.java
java -jar target/Compilador.jar --logros=ternario,lambda programa1.java
```

Un nombre de logro o una opción desconocidos se informan por `System.out` y **no** abortan: los
testers de la cátedra invocan `main()` in-process y una excepción mataría la corrida entera.

### Al armar el zip de la Etapa 2

Los testers de la cátedra hardcodean `resources/sinErrores/` y `resources/conErrores/`, y en el
repositorio esos directorios están separados por fase. Al empaquetar la entrega de la Etapa 2 hay
que copiar `resources/sintactico/{sinErrores,conErrores}` a `resources/{sinErrores,conErrores}`:

```bash
cd compilador
mkdir -p entrega/resources
cp -r resources/sintactico/sinErrores resources/sintactico/conErrores entrega/resources/
```

Son **1861 archivos y medio megabyte**. Todos están verificados con el compilador tal cual, o sea
con todos los logros prendidos, que es como la cátedra corre sus testers: ningún caso del corpus
depende de tener un logro apagado. Lo que **no** va en el zip es el andamiaje para regenerarlo
—`.venv/`, `herramientas/fuzz/jars/` y `herramientas/fuzz/trabajo/`, todos ignorados por git—: el
corpus está versionado y `mvn verify` no lo necesita.

Java: `maven.compiler.release=25` (coincide con el JDK instalado; no hay ninguna directiva de la
cátedra que fije una versión distinta). JaCoCo en `0.8.15` — versiones anteriores (`0.8.12` y por
debajo) no leen bytecode de Java 25 (class file major version 69).

## Formato del informe entregable (para cuando se redacte)

El informe pedido por la cátedra (tokens + ER que los define, decisiones relevantes para probar el
compilador, logros perseguidos — Pautas, slide 39) se entrega como **PDF final**; si el paso
intermedio Markdown→PDF resulta más trabajoso que escribir el PDF directamente, se genera el PDF
directamente, sin pasar por Markdown. Requisitos de diseño fijados por el usuario:
**paleta de colores monocromática**, **portada minimalista**, **índice/tabla de contenidos**.
