# Etapa 2 — Analizador Sintáctico

Documentación del **Analizador Sintáctico Descendente Recursivo Predictivo LL(1)**. Etapa en curso;
entrega 23/09/2026.

El objeto central de esta etapa es la **gramática**: se parte de la que publica la cátedra, se la
diagnostica, se la corrige a LL(1) y se la extiende con los logros. Los cuatro primeros documentos
recorren ese camino en orden.

## Documentos

### La gramática, en el orden en que conviene leerla

| Documento | Qué responde |
|---|---|
| [`gramatica-original-de-la-catedra.md`](gramatica-original-de-la-catedra.md) | Transcripción fiel del PDF, sin corregir nada. Es la fuente de verdad contra la que se verifica todo lo demás. |
| [`defectos-de-la-gramatica-original.md`](defectos-de-la-gramatica-original.md) | La gramática original explicada **regla por regla**: para qué sirve cada una, por qué está escrita así y dónde está rota. **Empezar acá.** |
| [`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md) | El paso a paso **atómico** de cada corrección y de cada extensión por logro: regla original, defecto, transformación, resultado verificado. |
| [`gramatica-ll1-final.md`](gramatica-ll1-final.md) | La gramática resultante. **Se genera** desde `compilador/gramatica/minijava-ll1-logros.g`; no se edita a mano. |

### El analizador

| Documento | Qué responde |
|---|---|
| [`arquitectura-del-analizador-sintactico.md`](arquitectura-del-analizador-sintactico.md) | La técnica, las tres formas de método, `match`, el flujo completo y cómo queda organizado el código para las cinco etapas. |
| [`decisiones-de-diseno.md`](decisiones-de-diseno.md) | Los criterios que gobernaron las transformaciones: el orden, no factorizar de más, insertar el no terminal en vez de fusionar. |
| [`decisiones-de-logica.md`](decisiones-de-logica.md) | Qué lenguaje reconoce exactamente: las ambigüedades y cómo se resuelven, el alcance del ternario, los modificadores, las lambdas. |
| [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) | Dónde falla el análisis, qué se reporta, y los chequeos que hace el analizador porque la gramática no puede expresarlos. |
| [`resolucion-de-logros.md`](resolucion-de-logros.md) | Los 10 logros: qué exige cada uno, cómo se resolvió y cómo verificarlo. |

## Verificación

La gramática no se afirma correcta: se comprueba. Desde `compilador/`:

```bash
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g                  # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --combinatoria   # 1024/1024
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --ambiguedades   # todas declaradas
bash herramientas/casos-gramatica.sh                                              # 84 casos
python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java   # 8/8
```

Qué hace cada archivo de gramática está en
[`../../compilador/gramatica/README.md`](../../compilador/gramatica/README.md).

## Ver también

- [`../compartido/estrategia-de-testing.md`](../compartido/estrategia-de-testing.md)
- [`../proyecto/plan-de-accion-etapa-2.md`](../proyecto/plan-de-accion-etapa-2.md) — estado y backlog.
- [`../minijava/`](../minijava/README.md) — «Reglas de Sintaxis MiniJava 2026», el documento fuente.
