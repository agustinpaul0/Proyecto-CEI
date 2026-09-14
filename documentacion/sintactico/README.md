# Etapa 2 — Analizador Sintáctico

Documentación del **Analizador Sintáctico Descendente Recursivo Predictivo LL(1)**. Analizador
completo, con los diez logros de la etapa implementados; entrega 23/09/2026.

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
python3 herramientas/equivalencia_de_lenguajes.py    # mismo lenguaje que la de la cátedra
python3 herramientas/auditar_analizador.py           # 234/234 producciones citadas
bash herramientas/casos-gramatica.sh                                              # 87 casos
python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java   # 8/8
python3 herramientas/comparar_con_gramatica.py       # 1879 fuentes del corpus
```

Son **cuatro verificaciones independientes** y conviene tener claro qué prueba cada una:

| Herramienta | Qué garantiza |
|---|---|
| `primeros.py` | Que la gramática cumpla la condición LL(1) de la cátedra en **las 1024** configuraciones de logros, y que toda ambigüedad esté declarada con su regla de resolución. |
| `equivalencia_de_lenguajes.py` | Que transformar la gramática **no cambió el lenguaje**, comparándola con la de la cátedra mediante un reconocedor de Earley y una cadena por cada producción de cada una. |
| `auditar_analizador.py` | Que el código y la gramática **no se hayan separado**: cada producción la implementa un método y cada cita corresponde a una producción real. |
| `comparar_con_gramatica.py` | Que el analizador en Java y el simulador de la gramática den el **mismo veredicto y el mismo token** sobre el corpus. El tercer reconocedor, el oráculo JavaCC, ya intervino al etiquetar cada caso. |

Qué hace cada archivo de gramática está en
[`../../compilador/gramatica/README.md`](../../compilador/gramatica/README.md).

## Ver también

- [`../compartido/estrategia-de-testing.md`](../compartido/estrategia-de-testing.md)
- [`../proyecto/plan-de-accion-etapa-2.md`](../proyecto/plan-de-accion-etapa-2.md) — estado y backlog.
- [`../minijava/`](../minijava/README.md) — «Reglas de Sintaxis MiniJava 2026», el documento fuente.
