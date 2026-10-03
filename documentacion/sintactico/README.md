# Etapa 2 — Analizador Sintáctico

Documentación del **Analizador Sintáctico Descendente Recursivo Predictivo LL(1)**. Analizador
completo; entrega 23/09/2026.

| | |
|---|---|
| Gramática | 97 no terminales, 197 producciones, 59 terminales; **única** y verificada LL(1) |
| Ambigüedades | 2, las dos declaradas con su regla de resolución |
| Analizador | una clase por sección de la gramática; cita 197 de 197 producciones |
| Corpus | 10.405 fuentes propias (10.061 generadas con Grammarinator y 184 importadas de un corpus ajeno), más 75 casos de gramática, los 8 de la cátedra y 29.101 externos |
| Suite | 26.648 tests; cobertura 98,4 % de líneas y 93,8 % de ramas; mutación 90 % |
| Informe | `etapas/etapa-2/entregables/`, 22 páginas |

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
| [`gramatica-ll1-final.md`](gramatica-ll1-final.md) | La gramática resultante. **Se genera** desde `compilador/gramatica/minijava-ll1-final.g`; no se edita a mano. |

### El analizador

| Documento | Qué responde |
|---|---|
| [`arquitectura-del-analizador-sintactico.md`](arquitectura-del-analizador-sintactico.md) | La técnica, las tres formas de método, `match`, el flujo completo y cómo queda organizado el código para las cinco etapas. |
| [`decisiones-de-diseno.md`](decisiones-de-diseno.md) | Los criterios que gobernaron las transformaciones: el orden, no factorizar de más, insertar el no terminal en vez de fusionar. |
| [`decisiones-de-logica.md`](decisiones-de-logica.md) | Qué lenguaje reconoce exactamente: las ambigüedades y cómo se resuelven, el ternario, los modificadores, el `for` y la inicialización de arreglos. |
| [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) | Dónde falla el análisis, qué se reporta y cómo se recupera en Modo Pánico. |
| [`resolucion-de-logros.md`](resolucion-de-logros.md) | Los 9 logros: qué exige cada uno, cómo se resolvió y cómo verificarlo. |

## Verificación

La gramática no se afirma correcta: se comprueba. Desde `compilador/`:

```bash
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g                  # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g --ambiguedades   # todas declaradas
python3 herramientas/equivalencia_de_lenguajes.py    # mismo lenguaje que la de la cátedra
python3 herramientas/auditar_analizador.py           # 197/197 producciones citadas
bash herramientas/casos-gramatica.sh                                              # 75 casos
python3 herramientas/aceptar_fuente.py resources/catedra/sintactico/*/*.java   # 8/8
python3 herramientas/comparar_con_gramatica.py       # 10.405 fuentes del corpus, después de mvn test
```

Son **cinco verificaciones independientes** y conviene tener claro qué prueba cada una:

| Herramienta | Qué garantiza |
|---|---|
| `primeros.py` | Que la gramática cumpla la condición LL(1) de la cátedra, y que toda ambigüedad esté declarada con su regla de resolución. |
| `equivalencia_de_lenguajes.py` | Que corregir la gramática **no cambió el lenguaje**: compara el núcleo LL(1) sin logros (`minijava-ll1.g`) con la de la cátedra mediante un reconocedor de Earley y una cadena por cada producción de cada una. Y que la gramática final **contenga** a la de la cátedra: los logros agregan construcciones, ninguno quita una que la base define. |
| `auditar_analizador.py` | Que el código y la gramática **no se hayan separado**: cada producción la implementa un método y cada cita corresponde a una producción real. |
| `comparar_con_gramatica.py` | Que el analizador en Java y el simulador de la gramática den el **mismo veredicto y el mismo token** sobre las 10.405 fuentes del corpus. El tercer reconocedor, el oráculo JavaCC, ya intervino al etiquetar cada caso. |
| `CorpusExternoTest` | Que el analizador coincida con **29.101 fuentes generados a partir de otra gramática** — el corpus y su *fuzzer* son de Giuliano Taormina, compañero de la materia (`compilador/resources/corpus-externo/`, empaquetado). En todos, el primer código que emite es el que el caso declara. Es la única verificación que no comparte decisiones con este proyecto. |

Qué hace cada archivo de gramática está en
[`../../compilador/gramatica/README.md`](../../compilador/gramatica/README.md).

## Ver también

- [`../compartido/estrategia-de-testing.md`](../compartido/estrategia-de-testing.md)
- [`../proyecto/README.md`](../proyecto/README.md) — las tres reglas que no se negocian.
- [`../minijava/`](../minijava/README.md) — «Reglas de Sintaxis MiniJava 2026», el documento fuente.
