# Cómo revertir el criterio de escapes mal formados en `litString`

> **Nota operativa, no material de entrega.** A diferencia del resto de esta carpeta, este documento
> no explica qué hace el compilador: explica cómo deshacer un cambio concreto si hiciera falta. Por
> eso no figura en el índice del [`README.md`](README.md). Cuando el criterio quede confirmado, este
> archivo se borra.

## 1. Por qué existe

El 26/08/2026 el criterio de recuperación ante un escape mal formado dentro de un `litString` cambió
por **indicación del profesor de la materia**: reportar el primer escape y descartar lo que queda del
literal hasta su comilla de cierre, sin emitir token. Está documentado como decisión firme en
[`decisiones.md`](decisiones.md) §4.3 y [`manejo-de-errores.md`](manejo-de-errores.md) §2.1.

La indicación fue verbal y se confirma con el profesor el **viernes 28/08/2026**. La entrega de la
Etapa 1 cierra el **02/09/2026 a las 16:00**, así que entre la confirmación y el cierre quedan tres
días hábiles. Si en esa conversación el criterio resultara ser otro, este documento es el
procedimiento para volver atrás sin reconstruirlo de memoria.

**Lo que se revierte es sólo el criterio de error.** El refactor de `ContextoLexico` en fachada +
`BuilderDeLexema` + `FabricaDeExcepcionesLexicas`, y la corrección del bug por el que la
configuración de `ModuloPrincipal` no llegaba al léxico, son independientes y **se quedan**.

## 2. Los dos comportamientos, lado a lado

Salidas reales, no reconstruidas: la columna "antes" se obtuvo compilando el commit `484d0fc`
(*"Emprolija los testers de la cátedra y corrige los punteros colgados"*), que es el último estado
del repositorio con el criterio anterior.

### `"a\q b\z c" + v1`

| Antes — error no fatal acumulable | Ahora — descarte hasta la comilla de cierre |
|---|---|
| <pre>[Error:"a\q\|1]<br>[Error:"a\q b\z\|1]<br>(litString,"a\q b\z c",1)<br>(op+,+,1)<br>(idMV,v1,1)<br>(EOF,$,2)</pre> | <pre>[Error:"a\q\|1]<br>(op+,+,1)<br>(idMV,v1,1)<br>(EOF,$,2)</pre> |

### `"a\u12G4" + v1`

| Antes — el unicode ya era fatal, pero sin resincronizar | Ahora — mismo criterio que el escape inválido |
|---|---|
| <pre>[Error:"a\u12\|1]<br>(idClase,G4,1)<br>[Error:" + v1\|1]<br>(EOF,$,2)</pre> | <pre>[Error:"a\u12\|1]<br>(op+,+,1)<br>(idMV,v1,1)<br>(EOF,$,2)</pre> |

En el segundo caso la versión anterior perdía `+` y `v1`: la comilla de **cierre** del literal roto
abría un literal nuevo que se los comía y moría sin cerrar. Es el argumento más fuerte a favor del
criterio actual, y conviene tenerlo a mano el viernes.

## 3. Ruta A — revertir el commit

El cambio de criterio está aislado en un único commit, separado a propósito del refactor que lo
precede y de la limpieza de comentarios y documentación que lo sigue. Identificarlo y revertirlo:

```bash
git log --oneline --grep='escape'      # el commit del criterio de escapes
git revert <sha>
cd compilador && python3 herramientas/lote2.py && mvn test
```

**Alcance exacto de ese commit**: código de producción, los cuatro tests, los cinco oráculos de
`resources/conErrores/` y el bloque de `herramientas/lote2.py`. O sea, todo lo mecánico — los pasos
§4.1 a §4.8 de la Ruta B en una sola orden.

**La documentación queda afuera** y se revierte a mano siguiendo §4.9. Está así a propósito: los
archivos de `documentacion/` y `agents/` mezclan este cambio con el refactor de `ContextoLexico` y
con la limpieza de comentarios, que **no** hay que revertir, así que un revert automático los
dejaría describiendo una arquitectura que no existe.

Es la ruta preferida mientras no haya commits posteriores que toquen los mismos archivos; si el
`revert` conflictúa, se pasa a la Ruta B.

## 4. Ruta B — reconstrucción manual

Nueve pasos. Ninguno toca la fachada ni el flujo de configuración.

### 4.1. `ContextoLexico` — recuperar el colector de errores no fatales

Reponer el campo, los dos métodos y los dos imports (`java.util.ArrayList`, `java.util.List`):

```java
private final List<ExcepcionLexica> erroresNoFatales = new ArrayList<>();

public void registrarError(ExcepcionLexica error) {
    erroresNoFatales.add(error);
}

public List<ExcepcionLexica> tomarErroresNoFatales() {
    List<ExcepcionLexica> tomados = List.copyOf(erroresNoFatales);
    erroresNoFatales.clear();
    return tomados;
}
```

Ahora que la separación de responsabilidades existe, la alternativa más limpia es una clase
`ColectorDeErrores` en `minijava.lexico` con esos dos métodos, inyectada en la fachada igual que
`BuilderDeLexema` y `FabricaDeExcepcionesLexicas`. Era la "Clase 3" del plan de refactor original,
que no se creó justamente porque este cambio la volvía innecesaria.

### 4.2. `AnalizadorLexico` — reexponer el drenaje

```java
public List<ExcepcionLexica> tomarErroresNoFatales() {
    return contexto.tomarErroresNoFatales();
}
```

Más el import de `java.util.List`.

### 4.3. `ModuloPrincipal` — volver a drenar antes de imprimir el token

```java
Token token = analizador.proximoToken();
boolean huboNoFatales = reportarErroresNoFatales(analizador);
huboErrores |= huboNoFatales;
imprimirToken(token);
termino = token.tipo() == TipoDeToken.EOF
        || (huboNoFatales && !configuracion.multiDeteccionDeErrores());
} catch (ExcepcionLexica excepcion) {
    huboErrores |= reportarErroresNoFatales(analizador);
    huboErrores = true;
```

y el método privado, más el import de `java.util.List`:

```java
private boolean reportarErroresNoFatales(AnalizadorLexico analizador) {
    List<ExcepcionLexica> pendientes = analizador.tomarErroresNoFatales();
    pendientes.forEach(this::reportarError);
    return !pendientes.isEmpty();
}
```

El orden importa: los errores no fatales ocurren **dentro** del token, así que se imprimen antes que
él. Los oráculos de `resources/conErrores/` codifican ese orden.

### 4.4. `EstadoStringEscapado` — registrar en vez de lanzar

Reemplazar el bloque del escape inválido por:

```java
if (ESCAPES_DE_UN_CARACTER.indexOf(actual) < 0 && !esDigitoOctal(actual)) {
    contexto.avanzar();
    contexto.registrarError(contexto.crearExcepcionLexica(
            TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, AnclaDeError.ULTIMO_CONSUMIDO,
            "\\" + actual));
    return new Continuar(EstadoStringAbierto.INSTANCIA);
}
```

### 4.5. Los dos estados de Unicode — sacar `dentroDeLitString`

`EstadoMarcadorUnicode` y `EstadoEscapeUnicode` vuelven a recibir sólo `siguienteAlTerminar`, y
`EstadoEscapeUnicode` vuelve a lanzar sin resincronizar:

```java
throw contexto.crearExcepcionLexica(TipoDeErrorLexico.UNICODE_MAL_FORMADO,
        AnclaDeError.LOOKAHEAD);
```

Actualizar los dos call sites: `EstadoStringEscapado` y `EstadoCharEscapado`.

### 4.6. Borrar `RecuperacionDeLitString`

`compilador/src/main/java/minijava/lexico/recuperacion/RecuperacionDeLitString.java` queda sin
usar, y con él todo el paquete `lexico/recuperacion/`.

### 4.7. Los cuatro tests

| Archivo | Qué revertir |
|---|---|
| `AnalizadorLexicoTest` | `stringLiteralConEscapeInvalidoSegunJavaEsErrorFatal` vuelve a esperar token + un error no fatal; `dosEscapesInvalidosEnUnStringReportanSoloElPrimero` vuelve a esperar dos errores y un token; `conUnicodeDeshabilitadoElEscapeDeUnicodeEnStringEsError` vuelve a la lista de no fatales. **Borrar** los cinco tests nuevos del descarte (`trasElDescarte…`, `unSegundoLitStringRoto…`, `elDescarteNoSeComeLaLineaSiguiente…`, `escapeUnicodeMalFormadoEnUnString…`, `escapeUnicodeMalFormadoEnUnCharLiteral…`) |
| `TipoDeErrorLexicoTest` | reponer la rama especial de `STRING_ESCAPE_INVALIDO` en el test parametrizado; `escapeInvalidoDeStringDescartaElLiteralYNoEmiteToken` vuelve a ser `escapeInvalidoDeStringEsElUnicoErrorNoFatal`; `elEscapeInvalidoViajaConLaSecuenciaOfensiva…` vuelve a leer de `tomarErroresNoFatales()` |
| `PosicionDelCaretTest` | `cadaLitStringRotoDeUnaMismaLineaReportaSuPropiaColumna` vuelve a ser `cadaEscapeInvalidoDeUnMismoLitStringReportaSuPropiaColumna` sobre `"a\q b\z c"`, esperando dos errores en las columnas 4 y 8; el helper `primerError()` vuelve a priorizar los no fatales sobre el fatal |
| `PropiedadesDelReporteTest` | quitar `resincronizoElLitString()` y `descarteDeLitString()` de la propiedad de reconstrucción, y volver a descartar los no fatales con `analizador.tomarErroresNoFatales()` dentro del bucle |

### 4.8. Los oráculos de `resources/conErrores/`

En `compilador/herramientas/lote2.py`, el bloque de escapes inválidos vuelve a:

```python
con_errores("stringConDosEscapesInvalidos.java",
            [E('"a\\q', 1), E('"a\\q b\\z', 1), T('"a\\q b\\z c"', 1)],
            ['"a\\q b\\z c"'])

con_errores("stringConEscapeOctalFueraDeRango.java",
            [E('"a\\8', 1), T('"a\\8"', 1)],
            ['"a\\8"'])

con_errores("stringConEscapeInvalidoYLuegoCodigoValido.java",
            [E('"x\\q', 1), T('"x\\q"', 1), T("+", 1), T("v1", 1)],
            ['"x\\q" + v1'])

con_errores("escapeUnicodeEnStringConDigitoInvalido.java",
            [E('"a\\u12', 1), T("G4", 1), E('"', 1)],
            ['"a\\u12G4"'])
```

`stringConEscapeInvalido.java` era anterior al generador y estaba escrito a mano con sólo dos líneas
(`// [Error:"hola \q|2]` y el fuente). Al revertir puede quedarse en el lote con sus expectativas
viejas —`[E('"hola \\q', 1), T('"hola \\q mundo"', 1)]`— o volver a su forma manual; las dos pasan.

Después: `cd compilador && python3 herramientas/lote2.py`.

### 4.9. La documentación

Revertir, en este orden:

- [`decisiones.md`](decisiones.md) §4.3 y la fila de `"a\q b\z c"` de la tabla de divergencias §7;
- [`manejo-de-errores.md`](manejo-de-errores.md) §2 (la excepción a la regla de recuperación), §2.1
  entera, las dos filas de la tabla de tipos §2.2 y la nota bajo la tabla de `javac` en §4;
- [`arquitectura.md`](arquitectura.md) §3 (el párrafo de la excepción) y el paso 4 del ciclo de vida;
- [`logros.md`](logros.md) §3, "El caso especial";
- [`especificacion-lexica.md`](especificacion-lexica.md) §3.3, el párrafo final;
- [`testing.md`](testing.md) §3.2 y los conteos de §5;
- `agents/manejo-de-errores.md`, `agents/especificacion-lexica.md`, `agents/testing.md`.

Y borrar este archivo.

## 5. Verificación del rollback

```bash
cd compilador
python3 herramientas/lote2.py
mvn test                                                  # suite completa + los 2 testers de la cátedra
mvn verify                                                # umbral de cobertura JaCoCo
mvn -Pmutacion org.pitest:pitest-maven:mutationCoverage    # score de mutación, referencia 92%
```

Y la comprobación manual que da el resultado de la tabla de §2:

```bash
printf '"a\\q b\\z c" + v1\n' > /tmp/caso.mjava
java -jar target/Compilador.jar /tmp/caso.mjava
```

Tras el rollback tiene que imprimir **dos** errores y el `litString` completo como token.
