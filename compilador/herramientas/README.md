# Herramientas

Andamiaje, no parte del compilador. Verifican la gramática, generan lo que el compilador consume
desde ella, y escriben casos de corpus en lote.

| Herramienta | Para qué |
|---|---|
| `primeros.py` | El verificador de la gramática: vaciables, Primeros, LL(1), combinatoria de logros, registro de ambigüedades, simulador del Esquema Simple. |
| `primeros_a_java.py` | Genera las constantes de Primeros que usa el analizador. |
| `comparar_con_gramatica.py` | Exige que el analizador en Java y la gramática reconozcan el mismo lenguaje. |
| `aceptar_fuente.py` | Corre la gramática sobre un fuente MiniJava real. |
| `casos-gramatica.sh` | Casos de aceptación y rechazo de la gramática, por logro. |
| `gramatica_a_markdown.py`, `gramatica_a_latex.py` | Generan los documentos de la gramática. |
| `lote*.py`, `generar_casos.py` | Escriben casos del corpus léxico en lote. |

## Los generadores del corpus léxico

Los casos de `resources/lexico/conErrores/` y
`resources/lexico/sinErrores/` son archivos comunes y corrientes que se pueden editar
a mano; estos scripts existen sólo para poder escribirlos en lote sin
equivocarse con dos cuentas tediosas y fáciles de errar:

- la línea del token `EOF`, que es «saltos de línea consumidos + 1» y por lo
  tanto depende de cuántas líneas de expectativa lleva el propio archivo;
- el desplazamiento que la cabecera de comentarios le mete al fuente en los
  casos de `conErrores/`.

Uso, siempre desde `compilador/`:

```bash
python3 herramientas/lote1.py
```

Las expectativas se escriben a mano y **se predicen antes de correr**: si un
caso falla, primero hay que decidir si el equivocado es el oráculo o el
compilador. Varios casos de estos lotes salieron de haber predicho mal y
haber ido a mirar por qué.

## `primeros_a_java.py` — genera los Primeros que usa el analizador

```bash
python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-logros.g
```

Escribe `src/main/java/minijava/sintactico/primeros/Primeros.java`: una constante por producción
—220—, con su producción citada al lado. **Se genera y no se edita a mano**: es lo único que
garantiza que lo que el analizador usa para elegir producción sea exactamente lo que el verificador
calcula sobre la gramática. Si se toca el `.g`, hay que regenerarlo.

## `comparar_con_gramatica.py` — que el analizador y la gramática no se separen

```bash
python3 herramientas/comparar_con_gramatica.py 'resources/sintactico/*/*.java'
```

Corre el analizador sintáctico en Java y el simulador del Esquema Simple sobre los mismos fuentes y
exige el **mismo veredicto** y, cuando los dos rechazan, el **mismo token**. Responde la pregunta
que deja un analizador escrito a mano: ¿reconoce el mismo lenguaje que la gramática verificada, o
uno parecido?

El analizador es deliberadamente más restrictivo en los chequeos que una gramática libre de contexto
no puede expresar —los del logro MiniLambdas—. Esas divergencias van **declaradas** en la
herramienta con su motivo; una sin declarar la hace fallar, igual que el registro de ambigüedades
del verificador.
