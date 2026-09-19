# Decisiones de detección de errores del Analizador Sintáctico

Qué se considera un error sintáctico, en qué punto exacto se lo detecta y qué se reporta.

El mecanismo general de reporte —común a todas las etapas— está en
[`../compartido/manejo-de-errores.md`](../compartido/manejo-de-errores.md).

## 1. Los dos puntos donde el análisis falla

El Esquema Simple de la cátedra sólo produce error en dos lugares, y no hay ningún tercero.

**En `match`,** cuando el token actual no es el terminal que la producción exige:

```java
public void match(TipoDeToken esperado) throws ExcepcionLexica, ExcepcionSintactica {
    if (actual.tipo() != esperado) {
        throw new ExcepcionSintactica(actual, nombreDe(esperado));
    }
    avanzar();
}
```

**Al elegir producción,** cuando el token actual no está en los Primeros de ninguna alternativa
**y** el no terminal no es anulable:

```java
private void unNoTerminal() throws ExcepcionLexica, ExcepcionSintactica {
    if (estaEn(PRIMEROS_A)) {
        /* traducción de α */
    } else if (estaEn(PRIMEROS_B)) {
        /* traducción de β */
    } else {
        throw errorEsperando(PRIMEROS_A, PRIMEROS_B);
    }
}
```

Si el no terminal **sí** es anulable, el `else` **no hace nada** —queda escrito, vacío, con la
producción `ε` citada al lado—: eso significa que derivó a vacío,
y el error —si lo hay— lo va a encontrar el método que llamó, cuando intente su propio `match`.

## 2. Qué se reporta

Formato fijado por las Pautas, slide 25:

```
Error Sintactico en linea N: se esperaba X se encontro "Y"

[Error:Y|N]
```

donde `Y` es el lexema del token con el que se produjo el error y `N` su línea. El lexema del
token `eof` es `$` (Pautas, slide 26).

> **Verificado contra los casos de la cátedra.** Los cuatro fuentes con error de
> `compilador/resources/catedra/` declaran en su primera línea el código que esperan.
> Simulando el Esquema Simple sobre la gramática, el rechazo cae en el token exacto de los cuatro:
> `[Error:,|3]`, `[Error:clasesita|3]`, `[Error:hola|5]`, `[Error:else|7]`. Reproducible con
> `python3 herramientas/aceptar_fuente.py resources/catedra/*/*.java`.

## 3. Ambigüedades resueltas por regla de implementación

La gramática tiene dos ambigüedades declaradas, y ninguna se resuelve reescribiéndola: las dos se
resuelven con el **orden de las ramas** en el analizador, que es la segunda de las dos vías que
ofrece Clase 4, slide 49. En los dos casos la regla es la misma —**voraz**— y coincide con Java.

| Ambigüedad | Regla | Efecto |
|---|---|---|
| *dangling else* | tomar la rama `else` apenas se ve el token | liga al `if` más cercano |
| `new int[3][4]` | consumir el `[` como otra dimensión de la creación | es una creación 2-D, no un acceso |

Las dos están implementadas: el `else` en `AnalizadorDeSentencias` y el `new int[3][4]` en
`AnalizadorDeCreacion`. El detalle de cada una,
con las derivaciones que las exhiben, está en
[`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md). El listado se
mantiene verificado: `python3 herramientas/primeros.py gramatica/minijava-ll1-final.g
--ambiguedades` **falla** si aparece una ambigüedad que no esté declarada con su regla.

> **Los Siguientes no son parte del método.** Ese diagnóstico los calcula para poder detectar
> ambigüedades, que es un ítem del checklist de Clase 4, slide 41. Pero el analizador decide
> **sólo con Primeros**, como enseña la cátedra: los Siguientes no aparecen en la gramática, ni en
> la elección de producciones, ni en una línea del compilador.

## 4. Recuperación en Modo Pánico

Logro de la etapa, **implementado**. Al detectar un error, en lugar de terminar, el analizador
descarta tokens hasta uno donde pueda reanudar y sigue, de modo que un fuente con varios errores
los reporte todos. Está siempre activa.

### 4.1. Dónde se recupera

En **cuatro lugares y sólo cuatro**: las cuatro listas de la gramática que repiten un elemento
—`ListaClases`, `ListaMiembros`, `ListaMetodosInterfaz` y `ListaSentencias`—. Es donde tiene
sentido descartar un elemento roto y seguir con el siguiente, y mantenerlo ahí evita que el
`try/catch` se desparrame por las nueve secciones del analizador. Las de miembros y de métodos de
interfaz sincronizan igual —adentro de una llave, contra `;` y `}`— y comparten contexto, así que
los contextos de sincronización son tres.

### 4.2. Con qué se sincroniza

Pautas slide 40 nombra el punto y coma, la llave de cierre y la llave que abre «según el contexto».
A esos se les suman **los Primeros del elemento de la lista**, y hacen falta:

```java
class C1 {
  int x        // falta el `;`
  void m(){ }  // este método es correcto
}
```

El análisis falla en `void`, que es justamente donde empieza el miembro siguiente y correcto. Sin
los Primeros como punto de reanudación, la recuperación se llevaría puesto el método entero.

Las **llaves se cuentan** mientras se descarta: un `{` que no arranca un elemento de la lista abre
algo anidado —el cuerpo del método cuya firma está rota— y se saltea entero, en vez de leer cada
sentencia de adentro como si fuera otro miembro y producir un error inventado por cada una.

### 4.3. La invariante de progreso

Cada vuelta del bucle consume al menos un token. Hay que decirlo explícito porque el error puede
caer justo en un token que también arranca otro elemento: `class C { int int; }` falla en el
segundo `int`, que es un comienzo válido de miembro, y sin la garantía el analizador quedaría dando
vueltas sobre el mismo token. Está cubierto con un test con límite de tiempo.

### 4.4. El primer error no cambia

La recuperación **agrega errores después del primero, nunca cambia cuál es el primero**: el primer
código reportado es exactamente el que daría un analizador que cortara ahí, que es la convención
base de Pautas slide 25. Cada caso del corpus con errores declara ese primer código en su primera
línea, y el tester estricto verifica que sea el primero que el compilador emite.

### 4.5. El formato del reporte

Se repite el reporte completo de Pautas slide 25 —mensaje, renglón en blanco, código— por cada
error, en orden. Es el mismo formato con el que la Etapa 1 reporta varios errores léxicos con
*Multi-detección*, que se corrigió sin observaciones.
