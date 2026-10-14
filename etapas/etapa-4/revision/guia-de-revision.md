# Guía de revisión — Etapa 4

Qué leer y en qué orden. Las rutas del código son relativas a `compilador/src/main/java/minijava/`.
Unas 1.900 líneas nuevas o cambiadas, 1.340 de ellas en el paquete del AST; cada clase es chica.

Antes de empezar, dos lecturas cortas:
1. `documentacion/sentencias/arquitectura-del-chequeo-de-sentencias.md`: el flujo y los
   diagramas. Tenelos abiertos mientras leés.
2. `documentacion/sentencias/decisiones-de-deteccion-de-errores.md`, la tabla de tokens.

## 1. Los tipos y la conformidad (§9)

- `semantico/tipos/Tipo.java`: `conformaCon` e `identidad`.
- `semantico/tipos/TipoPrimitivo.java`, `TipoVoid.java`, `TipoNull.java` (nuevo),
  `TipoArreglo.java`, `VariableDeTipo.java` (ahora con `declarante`).
- `semantico/tipos/TipoReferencia.java`: `conformaCon`, que recorre los ancestros, y el chequeo raw
  en `verificarEn` y `verificarComoAncestroEn`.
- `semantico/tipos/Sustitucion.java`: ahora reemplaza sólo la `T` del tipo indicado.

**Qué mirar:** que cada regla de `conformaCon` sea una frase del documento §9.

## 2. Lo que cambió en la Etapa 3

- `semantico/entidades/Unidad.java`: `declarante`, `cuerpo` y `chequearCuerpo`.
- `semantico/entidades/Metodo.java`: la copia heredada conserva `declarante` y `cuerpo`.
- `semantico/entidades/ClaseOInterfaz.java`: `agregarMetodo` fija el declarante,
  `sustitucionHacia` es pública y aparece `ancestrosDirectos`.
- `semantico/entidades/Clase.java`: `constructor(int)`, `ancestrosDirectos` y
  `chequearSentencias()`, que saltea lo heredado.
- `semantico/tabla/TablaDeSimbolos.java`: `tipoActual`, `referenciaAString` y `chequearSentencias()`.

## 3. El contexto

- `semantico/ast/Contexto.java` y `VariableLocal.java`: los bloques, `declararLocal` y la búsqueda
  local → parámetro (§10.1, §12.3).

## 4. Las expresiones, de las hojas hacia arriba (`semantico/ast/expresiones/`)

1. `NodoExpresion.java`: el contrato.
2. `NodoLiteral`, `NodoVariable`, `NodoThis`, `NodoParentizada`.
3. `NodoUnario`, `NodoBinario` y `NodoAsignacion`.
4. `Argumentos`, `NodoLlamada`, `NodoLlamadaEstatica`.
5. `NodoEncadenado`, `NodoAccesoAtributo`, `NodoLlamadaConReceptor`, `NodoAccesoArreglo`.
6. `NodoCreacionObjeto`, `NodoCreacionArreglo`.
7. `ExpresionNoControlada`.

**Qué mirar:** en cada `chequear`, que se chequeen primero los hijos y que cada `throw` use el
token de la tabla.

## 5. Las sentencias (`semantico/ast/sentencias/`)

`NodoSentencia`, `NodoBloque`, `NodoVacia`, `NodoVarLocal`, `NodoReturn`, `NodoIf`, `NodoWhile`,
`NodoSentenciaExpresion`, `SentenciaNoControlada`.

## 6. El parser: dónde se arma el AST (`sintactico/partes/`)

Leelo junto a `documentacion/sentencias/esquema-de-traduccion.md`. Con `git diff` se ve que en
cada método se cambió `void` por el nodo y se agregó el parámetro heredado.

1. `AnalizadorDeExpresiones.java`: `restoExpresionCompuesta` es el único lugar donde se decide la
   precedencia (duda 1).
2. `AnalizadorDeReferencias.java`, `AnalizadorDeArgumentos.java` y `AnalizadorDeCreacion.java`.
3. `AnalizadorDeSentencias.java` y `AnalizadorDeFors.java`.
4. `AnalizadorDeMiembros.cuerpoDeMetodo()` y `AnalizadorDeTipos.variableDeTipo()`.

## 7. El driver

`aplicacion/ModuloPrincipalSemantico.java` (`conChequeoDeSentencias`), `Main.java` y
`OpcionesDeInvocacion.java` (la etapa por defecto es la 4).

## 8. Los tests (`compilador/src/test/java/minijava/sentencias/`)

1. `EsquemaDeTraduccionDeSentenciasTest`: el árbol esperado de cada producción.
2. `ConformidadTest`, `ContextoTest` y `EstructuraDeLosNodosTest`.
3. Una muestra del corpus: `herramientas/sentencias/casos_*.py`. Elegí unos casos de cada archivo y
   predecí vos el código antes de mirar la marca `// <-`.
4. `herramientas/sentencias/generar_corpus_sentencias.py`: los dos modelos, `conforma_minijava` y
   `conforma_java`, contra el documento §9.
5. `SentenciasPropiedadesTest`, `ExpresionAleatoria`, `SentenciasFuzzTest`,
   `RobustezDelChequeoDeSentenciasTest` y `ConsistenciaEntreEtapasTest`.

## Para verificar

```bash
cd compilador
mvn verify                       # la suite completa y la cobertura
mvn -Pmutacion verify            # mutantes (tarda)
python3 herramientas/diagramas_de_clases.py --verificar
java -jar target/Compilador.jar resources/catedra/sentencias/conErrores/semIIError04.java
```
