# El lenguaje MiniJava

Los documentos que publica la cátedra para describir **el lenguaje**, no una etapa del compilador.
Viven acá y no dentro de `etapas/` porque los usan todas las etapas.

| Documento | Qué contiene |
|---|---|
| `Aspectos Lexicos MiniJava 2026.pdf` | Palabras clave, identificadores, literales, símbolos de puntuación y operadores. Es la fuente de los 56 tokens. |
| `Reglas de Sintaxis MiniJava 2026.pdf` | La gramática del lenguaje en notación BNF-extendida. Es la fuente de la Etapa 2. |
| `MiniJava-Semantica-2026-Parte 1.pdf` | Los controles semánticos de las declaraciones: tabla de símbolos, tipos, genericidad, herencia y entidades predefinidas. Es la fuente de la Etapa 3. |

## Cómo leerlos

**Como imágenes, página por página.** No con extracción de texto: estos PDF y los de las clases
tienen diagramas y capturas de código con globos de explicación que `pdftotext` pierde. Está
permitido usar extracción de texto **sólo** para ubicar en qué página está un tema.

## Dónde está trabajado cada uno

- Aspectos léxicos →
  [`../lexico/especificacion-lexica-de-minijava.md`](../lexico/especificacion-lexica-de-minijava.md)
- Reglas de sintaxis →
  [`../sintactico/gramatica-original-de-la-catedra.md`](../sintactico/gramatica-original-de-la-catedra.md),
  que es su transcripción fiel, y el resto de
  [`../sintactico/`](../sintactico/README.md)
- Semántica, Parte I → [`../semantico/`](../semantico/README.md)

Una nota sobre la transcripción: el PDF de sintaxis escribe `[]` como si fuera un símbolo, pero
léxicamente son **dos** tokens — «Aspectos Léxicos» no define ningún token `[]`. La transcripción
lo registra como `[ ]`, y es la única desviación deliberada de su literalidad.
