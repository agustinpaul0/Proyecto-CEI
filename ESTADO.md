# Estado del proyecto

Dónde está cada etapa y qué queda por resolver. Se actualiza al cerrar cualquier tarea; el detalle
técnico vive en `documentacion/`, acá va sólo el estado.

**Última actualización:** 18/09/2026

---

## Etapa 1 — Analizador Léxico

**TERMINADA.** Entregada y corregida **sin errores ni observaciones**.

Los 8 logros están implementados: Floats I, Unicodes, Multi-detección de Errores, Reporte de Error
Elegante, Manejador de Archivos Eficiente, Imbatibilidad Léxica, Good Job! I y Entrega Anticipada.

Es ejecutable de forma aislada con `--etapa=1`, y su corpus completo (158 casos) vive en
`compilador/resources/lexico/`. `mvn test -Pfase-lexico` lo corre entero.

### Lo único abierto

**`--etapa=1` reconoce también los tres tokens que agrega la Etapa 2**: no hay forma de pedir
sólo el vocabulario de la Etapa 1.

| Entrada | Etapa 1 como se entregó | `--etapa=1` |
|---|---|---|
| `for` | `idMV` | `pr_for` |
| `private` | `idMV` | `pr_private` |
| `?` | error léxico | `signoPregunta` |

Para todo el resto del vocabulario el comportamiento es idéntico, y ningún caso del corpus léxico
usa el `?` como carácter inválido. **No afecta a la corrección ya hecha** —esa fue sobre el jar
entregado en su momento—, pero conviene tenerlo presente.

---

## Etapa 2 — Analizador Sintáctico

**TERMINADA.** Entrega 23/09/2026. Analizador Descendente Recursivo Predictivo LL(1), Esquema
Simple de Clase 5.

| | |
|---|---|
| Gramática | 97 no terminales, 198 producciones, 59 terminales; LL(1) verificada |
| Ambigüedades | 2, las dos declaradas con su regla de resolución |
| Analizador | una clase por sección; cita 198 de 198 producciones |
| Corpus propio | 10.251 fuentes (10.044 generadas con Grammarinator), 72 casos de gramática, 8 de la cátedra |
| Corpus externo | 16.512 fuentes de Giuliano Taormina; coinciden todos |
| Suite | 26.246 tests; 98,6 % de líneas, 94,1 % de ramas; mutación 91 % |
| Informe | `etapas/etapa-2/entregables/`, 31 páginas |

### Decisiones cerradas en esta etapa

- Los logros **no son configurables**.
- `public static` y `static public` son los dos válidos; repetir el modificador no. `protected` no
  se implementa.
- La recursión a derecha se traduce **con recursión**, no con `while`, en los diez lugares.
- La inicialización de arreglos es **la de Java**, como confirmó el profesor: `new int[][]{{1,2},{3,4}}`
  vale y `new int[2][2]{{1,2},{3,4}}` no. Sin inicializador rige la base: toda dimensión con tamaño.
- El lado izquierdo de un `=` es una `ExpresionCompuesta`, como en la base: `a ? b : c = d` no deriva.
- **No hay atributos estáticos**: `static` es `ModificadorOpcional` de `Metodo` y de nada más, como
  confirmó el profesor.
- La variable de control del `for` se declara **con `var` y nada más**, como confirmó el profesor:
  los logros son independientes entre sí, así que el `for` no toma el declarador tipado de
  Variables Locales Clásicas. `for(;;)` vale, como en Java (JLS §14.14.1).
- Cada declarador de una variable local clásica lleva su propia inicialización opcional, como en
  Java (JLS §14.4): `int x = 10, y = 11, z = 12;` e `int x, y, z = 10;` valen los dos.

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
está en el informe (§12.2). Lo más específico de esta etapa:

- que el padre de un `extends` sea una clase y lo de un `implements` una interfaz;
- que el lado izquierdo de un `=` y el operando de un `++` o `--` sean variables: `(a ? b : c) = d`
  y `m()++` pasan el sintáctico, igual que `a + b = c` en la gramática de la cátedra;
- qué variables inicializa `int x, y, z = 10;`: en Java, sólo `z`. El ejemplo del logro de
  Variables Locales Clásicas no lo aclara, así que conviene confirmarlo con el profesor antes del
  semántico.
