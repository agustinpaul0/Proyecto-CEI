# Gramáticas de MiniJava

Las tres versiones de la gramática, en texto plano, en el formato que consume
`herramientas/primeros.py`. **Éste es el único lugar donde se edita la gramática**:
`documentacion/sintactico/gramatica-ll1-final.md` y las tablas del informe se generan desde acá.

| Archivo | Qué es |
|---|---|
| `minijava-base.g` | Transcripción de la gramática de la cátedra, tal cual el PDF. **No es LL(1)**: existe para que el verificador lo demuestre — reporta 16 conflictos en 10 no terminales. |
| `minijava-ll1.g` | El núcleo corregido a LL(1), **sin logros**. El compilador no lo consume: existe para que `equivalencia_de_lenguajes.py` demuestre que corregir la gramática de la cátedra no cambió el lenguaje. |
| `minijava-ll1-final.g` | **La gramática vigente, y la única que el compilador implementa.** |

## El formato

Una producción por línea. Un símbolo es **no terminal** si aparece alguna vez a la izquierda de una
flecha; todo lo demás es terminal. `EPSILON` es la cadena vacía.

```
Inicial -> ListaClases eof
ListaClases -> Clase ListaClases | Interfaz ListaClases | EPSILON
```

Los símbolos van **siempre separados por espacios**: el `|` separador se distingue del terminal
`||` porque la línea se tokeniza antes de partirla en alternativas.

## Verificación

Desde `compilador/`:

```bash
python3 herramientas/primeros.py gramatica/minijava-base.g          # NO ES LL(1): 16 conflictos
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g    # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g --ambiguedades   # registro
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g --nt Miembro     # un NT
bash herramientas/casos-gramatica.sh                                # 75 casos
python3 herramientas/aceptar_fuente.py src/test/resources/catedra/*/*.java
python3 herramientas/equivalencia_de_lenguajes.py   # mismo lenguaje que la de la cátedra
python3 herramientas/auditar_analizador.py          # el código cita exactamente estas producciones
```

## Al tocar la gramática

Después de cualquier cambio hay que **regenerar** los documentos derivados y el código que sale de
la gramática, nunca editarlos a mano:

```bash
python3 herramientas/gramatica_a_markdown.py gramatica/minijava-ll1-final.g \
    > ../documentacion/sintactico/gramatica-ll1-final.md
python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-final.g producciones \
    > ../etapas/etapa-2/entregables/informe/generado/gramatica-final.tex
python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-final.g primeros \
    > ../etapas/etapa-2/entregables/informe/generado/tabla-primeros.tex
python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-final.g   # las clases PrimerosDeX
```

Los **banners de sección** (`# ==== NOMBRE ====`) no son decorativos: `primeros_a_java.py` los usa
para decidir en qué clase va cada constante, y tiene que conocer el nombre. Agregar una sección
nueva al `.g` exige agregarla también a `POR_SECCION` en ese script.

Y volver a correr la verificación completa.
