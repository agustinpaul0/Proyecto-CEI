# Generadores del corpus de `resources/`

Andamiaje, no parte del compilador. Los casos de `resources/lexico/conErrores/` y
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
