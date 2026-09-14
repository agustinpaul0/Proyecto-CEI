# Cómo se trabaja en este proyecto

Documentos **normativos**: no explican qué hace el compilador sino cómo se lo construye. Son los
que hay que leer antes de escribir código, y los que fijan qué se consulta y qué se decide solo.

| Documento | Qué responde |
|---|---|
| [`flujo-de-trabajo.md`](flujo-de-trabajo.md) | El paso a paso antes de implementar y ante un bug, el estilo de comentarios, y las reglas que aplican a todas las etapas. |
| [`protocolo-de-investigacion.md`](protocolo-de-investigacion.md) | Qué hacer ante una duda de comportamiento no resuelta: MiniJava primero, Java verificado después, informar siempre. |
| [`convenciones-de-la-catedra.md`](convenciones-de-la-catedra.md) | Lo no negociable: contrato de salida, códigos de error, invariantes duras. |
| [`comandos-compilacion-y-entrega.md`](comandos-compilacion-y-entrega.md) | Comandos Maven, versión de Java, formato del informe entregable. |

## Las cuatro reglas que no se negocian

Están desarrolladas en [`flujo-de-trabajo.md`](flujo-de-trabajo.md); acá el resumen:

1. **Todo lo que no esté especificado se comporta como en Java**, verificado contra la
   especificación o ejecutando `javac`, nunca de memoria, e informado antes de implementar.
2. **La robustez del testing es lo primero.** Los tests importan más que el código; buscar
   activamente casos extremos y de borde es parte del trabajo.
3. **Cada logro es un interruptor independiente**, de punta a punta: el compilador tiene que poder
   correrse y testearse con 0, 1 o N logros en cualquier combinación.
4. **En los entregables no se deja constancia de los errores del desarrollo.** Sí de los defectos
   del material de la cátedra, de las alternativas descartadas y de las decisiones.
