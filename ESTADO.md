# Estado del proyecto

Dónde está cada etapa y qué queda por resolver. Se actualiza al cerrar cualquier tarea; el detalle
técnico vive en `documentacion/`, acá va sólo el estado.

**Última actualización:** 17/09/2026

---

## Etapa 1 — Analizador Léxico

**TERMINADA.** Entregada y corregida **sin errores ni observaciones**.

Los 8 logros están implementados: Floats I, Unicodes, Multi-detección de Errores, Reporte de Error
Elegante, Manejador de Archivos Eficiente, Imbatibilidad Léxica, Good Job! I y Entrega Anticipada.

Sigue siendo ejecutable de forma aislada con `--etapa=1`, y su corpus completo (158 casos) vive en
`compilador/resources/lexico/`. `mvn test -Pfase-lexico` lo corre entero.

### Lo único abierto

**El léxico de `--etapa=1` creció.** Al sacar la configurabilidad de logros —orden del profesor— ya
no hay forma de decir «sólo el vocabulario de la Etapa 1», así que `--etapa=1` hoy reconoce también
los cuatro tokens que agrega la Etapa 2:

| Entrada | Etapa 1 como se entregó | Hoy |
|---|---|---|
| `for` | `idMV` | `pr_for` |
| `private` | `idMV` | `pr_private` |
| `?` | error léxico | `signoPregunta` |
| `->` | `op-` + `op_mayor` | `op_flecha` |

Para todo el resto del vocabulario el comportamiento es idéntico. Tres casos del corpus léxico se
ajustaron por esto, y son exactamente los tres que ejercitaban esos tokens. **No afecta a la
corrección ya hecha** —esa fue sobre el jar entregado en su momento—, pero conviene tenerlo
presente y está anotado para consultar (ver *Dudas abiertas*).

---

## Etapa 2 — Analizador Sintáctico

**TERMINADA.** Entrega 23/09/2026. Analizador Descendente Recursivo Predictivo LL(1), Esquema
Simple de Clase 5, con los **diez logros implementables por código** más el *carry-over* de floats.

| | |
|---|---|
| Gramática | 110 no terminales, 230 producciones, 60 terminales; LL(1) verificada |
| Ambigüedades | 3, todas declaradas con su regla de resolución |
| Analizador | una clase por sección; cita 230 de 230 producciones |
| Corpus propio | 1953 fuentes, 81 casos de gramática, 8 de la cátedra |
| Corpus externo | 28.089 fuentes de Giuliano Taormina; 28.086 coinciden, 1 familia declarada |
| Suite | 5904 tests; 98,8 % de líneas, 94,5 % de ramas; mutación 89 % |
| Informe | `etapas/etapa-2/entregables/`, 41 páginas |

### Decisiones cerradas en esta etapa

- Los logros **no son configurables**: el compilador reconoce MiniJava con todos los de la Etapa 1
  y la Etapa 2 incorporados, siempre.
- `public static` y `static public` son los dos válidos; repetir el modificador no. `protected` no
  se implementa.
- La recursión a derecha se traduce **con recursión**, no con `while`, en los diez lugares.
- El `this`, la captura de variables y los parámetros repetidos de una lambda **van al semántico**.
- La lambda entra donde la base la deja entrar: entre paréntesis es operando y suelta es sentencia.
- **No hay atributos estáticos**: `static` es `ModificadorOpcional` de `Metodo` y de nada más.

### Lo que queda abierto

Tres puntos de lenguaje donde la especificación admite más de una lectura. Los tres están
implementados y declarados, pero **conviene confirmarlos** (ver *Dudas abiertas*):

1. **Inicialización de arreglos.** Hoy toda dimensión conserva su tamaño y el inicializador se
   cuelga al final: `new int[2][2]{{1,2},{3,4}}` vale y `new int[]{1,2,3}` no. Diverge de Java.
2. **La lambda donde la base la deja entrar.** `p -> x;` como sentencia y `(x -> y).m()` como
   operando se **aceptan**: los dos salen solos de la base, que define `Asignacion -> Expresion`
   y `ExpresionParentizada -> ( Expresion )`. Divergen de Java, que rechaza los dos. Sin
   paréntesis la lambda sigue sin ser operando: `1 + x -> x` no deriva.

---

## Etapas 3, 4 y 5

**SIN EMPEZAR.** No hay especificación publicada todavía; aparecerá en `etapas/etapa-3/` y
siguientes.

### Lo que la Etapa 2 ya le dejó preparado al semántico

- **Un no terminal de decisión por contexto.** Al factorizar se insertaron NT nuevos en vez de
  fusionar, así que cada punto donde se decide *qué construcción era* tiene nombre propio y método
  propio en el analizador.
- **El `else` vacío escrito explícitamente**, con la producción `ε` citada al lado: es el lugar
  donde va a colgar la traducción de la producción vacía.
- **El código sigue el Esquema Simple sin desvíos**, así que extenderlo es agregar acciones a los
  métodos que ya están.

### Lo que la Etapa 2 le dejó pendiente

Todo lo que exige resolver un nombre contra su declaración. El inventario completo, con ejemplos,
está en el informe (§12.2) y en
[`documentacion/sintactico/decisiones-de-deteccion-de-errores.md`](documentacion/sintactico/decisiones-de-deteccion-de-errores.md).
Lo más específico de esta etapa:

- `this` explícito e implícito dentro de una lambda;
- captura de variables locales y parámetros dentro de una lambda;
- parámetros de una lambda con nombre repetido;
- que el padre de un `extends` sea una clase y lo de un `implements` una interfaz;
- que la cantidad de elementos de un inicializador de arreglo coincida con el tamaño declarado.

---

## Dudas abiertas para consultar

Están desarrolladas en `~/downloads/dudas.pdf`. En una línea cada una:

1. **Arreglos** — ¿el inicializador se cuelga de dimensiones con tamaño (base MiniJava) o exige
   corchete vacío (Java)?
2. **La lambda** — `p -> x;` y `(x -> y).m()` se aceptan, respetando el molde de la base; ¿está
   bien, o el «como en Java» del logro pesa más? Incluye qué costaría el cambio contrario.
