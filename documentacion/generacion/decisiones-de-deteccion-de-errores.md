# Decisiones de detección de errores

La Etapa 5 suma dos clases de errores: el del punto de entrada, que es de compilación, y los de
ejecución, que detecta el código generado o la CeIVM. Todos los de las etapas anteriores se
reportan igual que antes (Pautas Etapa 5, slide 26).

## 1. El punto de entrada

El documento completo (§1, §8 y §14) deja para la Etapa 5 el control del punto de entrada, y el
Apunte §15 dice que todo programa MiniJava declara un `static void main()`. No dice qué error dar;
lo resolvió el profesor ([`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §1):

- Cuenta sólo un método `main` **estático, `void` y sin parámetros**, declarado en una clase. Uno
  heredado no cuenta otra vez: es el mismo método.
- Si no hay ninguno, el error va en el **fin de archivo**, como el del sintáctico cuando falta algo
  al final: `[Error:$|N]`, con N la línea del EOF.
- Si hay más de uno, el error va en el **nombre del segundo**, en el orden del fuente, como un
  nombre repetido (Pautas Etapa 3, slide 30).
- Se controla después del chequeo de sentencias: un error en un cuerpo se reporta antes.

| Fuente | Resultado |
|---|---|
| `class Aa { void m() { } }` | `[Error:$\|2]` |
| `class Init { void main() { } }` | `[Error:$\|2]`: el de instancia no cuenta |
| `class Init { static int main() { return 1; } }` | `[Error:$\|2]` |
| `class Aa { static void main() { } }` y `class Bb { static void main() { } }`, en las líneas 1 y 2 | `[Error:main\|2]` |
| `class Base { static void main() { } }` y `class Der extends Base { }` | `[SinErrores]` |

Los casos están en `resources/conErrores/`.

## 2. Los errores de ejecución

El Apunte §17 los agrupa en cuatro categorías. Los de las dos primeras (código CeIASM mal formado,
recursos de la CeIVM) no deberían ocurrir con un programa correcto: la robustez prueba que todo lo
generado ensambla. Los de las otras dos los detecta la CeIVM, o el código generado.

| Error | Lo detecta | Cómo termina |
|---|---|---|
| Referencia nula: atributo, llamada, `length`, índice, `printS` | la CeIVM, en `LOADREF`, `STOREREF` o `DEREF` | el error de la CeIVM |
| División o módulo por cero | la CeIVM, en `DIV` o `MOD` | el error de la CeIVM |
| Índice negativo o mayor o igual a la longitud | la rutina `componente` | `Error en tiempo de ejecucion: indice fuera de rango` y `HALT` |
| Tamaño negativo en cualquier dimensión | `nuevo_arreglo` y `nuevo_arreglo_multi` | `Error en tiempo de ejecucion: tamanio de arreglo negativo` y `HALT` |
| Desborde de la pila o del heap | la CeIVM | el error de la CeIVM |

La referencia nula de un arreglo se detecta sobre la referencia original, antes de sumarle el
índice, como pide el Apunte §12: la rutina `componente` lee la longitud con `LOADREF 1`.

Un error propio termina con un mensaje y `HALT`, como sugiere el Apunte. Tras el `HALT` la CeIVM
informa que la ejecución terminó exitosamente, pero la cátedra busca en la salida `Error` y
`Exitosamente` ([`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §2). Los casos
están en `resources/errores-de-ejecucion/`.
