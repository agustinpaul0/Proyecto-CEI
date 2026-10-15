# Etapa 4 — Analizador Semántico, Parte II: chequeo de sentencias

Documentación de los **AST** y del **chequeo de sentencias**. Entrega 30/10/2026 a las 16:00. La
especificación es «MiniJava-Semantica-Completa-2026», en [`../minijava/`](../minijava/README.md),
junto con Pautas Etapa 4 y las Clases 09, 10 y 11 en `etapas/etapa-4/`.

| | |
|---|---|
| Alcance | Los cuerpos de métodos y constructores: resolución de nombres, tipos y forma de cada sentencia y expresión. |
| Construcción | Los AST, con acciones de un Esquema de Traducción sobre la gramática LL(1) de la Etapa 2; cada uno se cuelga de su método o constructor. |
| Chequeo | Después de las declaraciones y la consolidación, `chequear` en cada nodo, por delegación. |
| Logros | Sólo los que no piden código: Imbatibilidad Semántica II, Entrega Anticipada y Grandioso Laburo! |
| Lo que contestó el profesor | [`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §1 a §6 |

## Documentos

| Documento | Qué responde |
|---|---|
| [`arquitectura-del-chequeo-de-sentencias.md`](arquitectura-del-chequeo-de-sentencias.md) | El flujo, los diagramas de clases del AST, el contexto y la conformidad. |
| [`esquema-de-traduccion.md`](esquema-de-traduccion.md) | La EDT: qué nodo arma cada producción. |
| [`decisiones-de-diseno.md`](decisiones-de-diseno.md) | Delegación, receptores anidados, información estructural, el declarante de cada unidad. |
| [`decisiones-de-logica.md`](decisiones-de-logica.md) | Qué se controla, qué no, los cambios al chequeo de declaraciones y lo que se resolvió como en Java. |
| [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) | El orden de los controles y el token de cada error. |
| [`resolucion-de-logros.md`](resolucion-de-logros.md) | Los logros de la etapa y por qué se persiguen sólo tres. |

## Verificación

Desde `compilador/`:

```bash
mvn test -Pfase-sentencias    # los tests de la etapa y los comunes
mvn test                      # la suite completa, las cuatro etapas
java -jar target/Compilador.jar resources/catedra/sentencias/conErrores/semIIError04.java
```

| Batería | Casos |
|---|---|
| Corpus a mano (`resources/sinErrores`, `resources/conErrores`) | 394 propios y 126 de Santiago Salamanca |
| Corpus generado (`resources/sentencias-generado.tar.gz`) | 2.905, contrastados con `javac` 25 |
| Cátedra (`resources/catedra/sentencias`) | 10, con `semIICorrecto03` corregido por el profesor |
| Propiedades y fuzz | 4.500 expresiones al azar y 3.000 mutaciones |
| Robustez | los unos 40.000 fuentes de las etapas anteriores |
