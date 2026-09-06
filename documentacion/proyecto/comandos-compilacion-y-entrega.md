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
java -jar target/Compilador.jar resources/sinErrores/lexSinErrores01.java
```

Java: `maven.compiler.release=25` (coincide con el JDK instalado; no hay ninguna directiva de la
cátedra que fije una versión distinta). JaCoCo en `0.8.15` — versiones anteriores (`0.8.12` y por
debajo) no leen bytecode de Java 25 (class file major version 69).

## Formato del informe entregable (para cuando se redacte)

El informe pedido por la cátedra (tokens + ER que los define, decisiones relevantes para probar el
compilador, logros perseguidos — Pautas, slide 39) se entrega como **PDF final**; si el paso
intermedio Markdown→PDF resulta más trabajoso que escribir el PDF directamente, se genera el PDF
directamente, sin pasar por Markdown. Requisitos de diseño fijados por el usuario:
**paleta de colores monocromática**, **portada minimalista**, **índice/tabla de contenidos**.
