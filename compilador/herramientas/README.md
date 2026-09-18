# Herramientas

Andamiaje, no parte del compilador. Verifican la gramática, generan lo que el compilador consume
desde ella, y escriben casos de corpus en lote.

**Verificación de la gramática**

| Herramienta | Para qué |
|---|---|
| `primeros.py` | El verificador: vaciables, Primeros, LL(1), registro de ambigüedades, simulador del Esquema Simple. |
| `equivalencia_de_lenguajes.py` | Con un reconocedor de Earley, que el núcleo LL(1) sin logros (`minijava-ll1.g`) y la de la cátedra **generen el mismo lenguaje**. |
| `auditar_analizador.py` | Que las producciones que cada método del analizador cita en su comentario sean **exactamente** las de la gramática. |
| `comparar_con_gramatica.py` | Que el analizador en Java y el simulador de la gramática den el mismo veredicto y el mismo token de rechazo sobre el corpus. |
| `aceptar_fuente.py` | Corre la gramática sobre un fuente MiniJava real. |
| `casos-gramatica.sh` | Casos de aceptación y rechazo de la gramática, por logro. |

**Generación desde la gramática**

| Herramienta | Para qué |
|---|---|
| `primeros_a_java.py` | Las clases `PrimerosDeX` que usa el analizador, una por sección de la gramática. |
| `gramatica_a_markdown.py`, `gramatica_a_latex.py` | Los documentos de la gramática y las tablas del informe. |
| `explicacion_de_reglas.py` | La gramática explicada no terminal por no terminal: qué genera cada producción y por qué está escrita así. **No es material de entrega**, es un documento de estudio; sale a `~/downloads/explicacion-reglas.pdf`. |
| `gramatica_a_antlr.py` | La gramática ANTLR con la que Grammarinator deriva y muta fuentes. |
| `gramatica_a_javacc.py` | El oráculo JavaCC: un tercer reconocedor de la misma gramática. |
| `gramaticas_externas.py` | Lo que las dos anteriores comparten: el léxico de la Etapa 1 y la convención de nombres. |

**Corpus**

| Herramienta | Para qué |
|---|---|
| `generar_corpus_fuzz.py` | El corpus generado del sintáctico, de punta a punta. Se invoca con `make fuzz`. |
| `lote_sintactico.py` | El corpus del sintáctico **escrito a mano**, con las expectativas predichas. |
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
python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-final.g
```

Escribe `src/main/java/minijava/sintactico/primeros/Primeros.java`: una constante por producción
—220—, con su producción citada al lado. **Se genera y no se edita a mano**: es lo único que
garantiza que lo que el analizador usa para elegir producción sea exactamente lo que el verificador
calcula sobre la gramática. Si se toca el `.g`, hay que regenerarlo.

## `comparar_con_gramatica.py` — que el analizador y la gramática no se separen

```bash
python3 herramientas/comparar_con_gramatica.py
```

Corre el analizador sintáctico en Java y el simulador del Esquema Simple sobre los mismos fuentes y
exige el **mismo veredicto** y, cuando los dos rechazan, el **mismo token**. Responde la pregunta
que deja un analizador escrito a mano: ¿reconoce el mismo lenguaje que la gramática verificada, o
uno parecido?
