# Gramáticas de MiniJava

Las tres versiones de la gramática, en texto plano, en el formato que consume
`herramientas/primeros.py`. **Éste es el único lugar donde se edita la gramática**: los documentos
de `documentacion/sintactico/` y las tablas del informe se generan desde acá.

| Archivo | Qué es |
|---|---|
| `minijava-base.g` | Transcripción de la gramática de la cátedra, tal cual el PDF. **No es LL(1)**: existe para que el verificador lo demuestre — reporta 16 conflictos en 10 no terminales. |
| `minijava-ll1.g` | El núcleo corregido a LL(1), **sin logros**. Es la gramática base del lenguaje, con la recursión a izquierda eliminada y factorizada. |
| `minijava-ll1-logros.g` | **La gramática vigente.** El núcleo más los 9 logros de la Etapa 2 y el *carry-over* de floats. Es la que se implementa. |

## El formato

Una producción por línea. Un símbolo es **no terminal** si aparece alguna vez a la izquierda de una
flecha; todo lo demás es terminal. `EPSILON` es la cadena vacía.

```
Inicial -> ListaClases eof
ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON
```

Los símbolos van **siempre separados por espacios**: el `|` separador se distingue del terminal
`||` porque la línea se tokeniza antes de partirla en alternativas.

### Marcas de logro

Una producción puede llevar una marca al final:

```
VisibilidadOpcional -> public | private | EPSILON     @+visibilidad
Constructor -> public idClase CuerpoDeMetodo          @-visibilidad
```

- `@+nombre` — la producción existe **sólo si el logro está prendido**
- `@-nombre` — existe **sólo si está apagado**
- sin marca — existe siempre

Es lo que permite verificar que la gramática sea LL(1) en **cualquier** subconjunto de logros,
incluido el vacío, que tiene que reproducir exactamente la especificación de la cátedra.

## Verificación

Desde `compilador/`:

```bash
python3 herramientas/primeros.py gramatica/minijava-base.g          # NO ES LL(1): 16 conflictos
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g    # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --combinatoria   # las 1024
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --ambiguedades   # registro
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --nt Miembro     # un NT
bash herramientas/casos-gramatica.sh                                # 84 casos
python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java
```

## Al tocar la gramática

Después de cualquier cambio hay que **regenerar** los documentos derivados, nunca editarlos a mano:

```bash
python3 herramientas/gramatica_a_markdown.py gramatica/minijava-ll1-logros.g --sin-cabecera \
    > ../documentacion/sintactico/gramatica-ll1-final.md      # (con su cabecera, ver el archivo)
python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g producciones \
    > ../etapas/etapa-2/entregables/informe/generado/gramatica-final.tex
python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g primeros \
    > ../etapas/etapa-2/entregables/informe/generado/tabla-primeros.tex
```

Y volver a correr la verificación completa.
