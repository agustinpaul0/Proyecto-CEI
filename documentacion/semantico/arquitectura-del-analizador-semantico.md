# Arquitectura del analizador semántico

## Qué hace y qué no

El análisis semántico de un lenguaje con tipado estático tiene dos mitades (Clase 6, slide 3). El
**chequeo de declaraciones** controla todo lo que está fuera de un bloque: clases, interfaces,
atributos, encabezados de métodos y constructores, y parámetros. El **chequeo de sentencias**
controla lo que está dentro de los cuerpos. Esta etapa hace sólo la primera mitad. Ningún cuerpo
se mira, y tampoco la expresión que inicializa un atributo.

## El flujo de una compilación

Es el esquema de Pautas Etapa 3, slides 15-25:

```mermaid
flowchart LR
    P[Módulo Principal] -->|1. inicializar| TS[(Tabla de Símbolos)]
    P -->|2. analizar| S[Sintáctico]
    S --> L[Léxico]
    S -->|acciones de la EDT| TS
    P -->|3. chequearDeclaraciones<br/>4. consolidar| TS
```

1. `ModuloPrincipalSemantico` inicializa la tabla, que queda con `Object`, `String` y `System`.
2. El sintáctico reconoce el programa y sus acciones van llenando la tabla
   ([`esquema-de-traduccion.md`](esquema-de-traduccion.md)).
3. Si hubo un error léxico o sintáctico, se reporta igual que en la Etapa 2 y el semántico no
   corre.
4. Si no hubo errores, el Módulo Principal le pide a la tabla `chequearDeclaraciones()` y después
   `consolidar()`. El primer error semántico corta el análisis. Si no hay ninguno, la salida es
   `Compilacion Exitosa` y `[SinErrores]`.

`--etapa=3` corre esta etapa. `--etapa=1` y `--etapa=2` corren el léxico y el sintáctico solos, y
`--etapa=4`, la etapa por defecto, sigue con el chequeo de sentencias
([`../sentencias/README.md`](../sentencias/README.md)).

## La tabla de símbolos

Es anidada, porque un mismo nombre significa cosas distintas en contextos distintos (Clase 7,
slides 13-15). La tabla global guarda las clases y las interfaces: cada una tiene sus
propias tablas de miembros, y cada método o constructor, su lista de parámetros.

Los diagramas son UML y muestran todos los campos, constructores y métodos del código:

- lo subrayado es estático, y lo que está en cursiva, abstracto;
- la visibilidad va adelante: `+` pública, `#` protegida, `~` de paquete y `-` privada;
- una referencia a otra clase dibujada es una relación, rotulada con el nombre del campo y su
  multiplicidad: `0..1` si puede faltar, `*` si es una colección, y sin multiplicidad si es
  exactamente una;
- una caja sin miembros es una clase que se detalla en otro diagrama.

`herramientas/diagramas_de_clases.py` verifica que cada diagrama coincida con el código y los
renderiza para el informe, que muestra estos mismos diagramas.

### La tabla

Todos sus miembros son estáticos: es la tabla global sin singleton que recomendó el profesor.
`EntidadesPredefinidas` carga `Object`, `String` y `System` cada vez que se inicializa.

<!-- diagrama: tabla -->
```mermaid
---
config:
  layout: elk
---
classDiagram
    direction TB
    class TablaDeSimbolos {
        -tipos : Map~String, ClaseOInterfaz~$
        -claseActual : Clase$
        -interfazActual : Interfaz$
        -tipoActual : ClaseOInterfaz$
        -unidadActual : Unidad$
        -primerRepetido : ExcepcionSemantica$
        -TablaDeSimbolos()
        +inicializar()$
        +declararClase(Clase)$
        +declararInterfaz(Interfaz)$
        -declarar(ClaseOInterfaz, String)$
        +claseActual()$ Clase
        +interfazActual()$ Interfaz
        +tipoActual()$ ClaseOInterfaz
        +unidadActual()$ Unidad
        +setUnidadActual(Unidad)$
        +tipo(String)$ Optional~ClaseOInterfaz~
        +object()$ ClaseOInterfaz
        +referenciaAObject(int)$ TipoReferencia
        +referenciaAString(int)$ TipoReferencia
        -referenciaA(String, int)$ TipoReferencia
        +tipos()$ List~ClaseOInterfaz~
        +registrarRepetido(ExcepcionSemantica)$
        +chequearDeclaraciones()$
        +consolidar()$
        +chequearSentencias()$
    }
    class EntidadesPredefinidas {
        ~OBJECT : String$
        -STRING : String$
        -SYSTEM : String$
        -LINEA_PREDEFINIDA : int$
        -EntidadesPredefinidas()
        ~cargar()$
        -cargarObject()$
        -cargarString()$
        -cargarSystem()$
        -metodo(String, Tipo)$ Metodo
        -metodo(String, Tipo, String, Tipo)$ Metodo
        -estatico(Metodo)$ Metodo
        -referencia(String)$ TipoReferencia
        -idClase(String)$ Token
    }
    class ExcepcionSemantica {
        +ExcepcionSemantica(Token, String)
        +getLexemaOfensivo() String
        +getLinea() int
    }
    class Token {
        <<record>>
        -tipo : TipoDeToken
        -lexema : String
        -linea : int
    }
    class ClaseOInterfaz {
        <<abstract>>
    }
    TablaDeSimbolos *-- ClaseOInterfaz : *
    TablaDeSimbolos ..> EntidadesPredefinidas : carga
    TablaDeSimbolos ..> ExcepcionSemantica : lanza
    ExcepcionSemantica --> Token : token
```

### Las clases y las interfaces

`metodosPropios` son los métodos declarados en el fuente, y `metodos` es la tabla consolidada, que
suma los heredados. Con `Clase` pasa lo mismo con `atributosPropios` y `atributos`.

<!-- diagrama: clases-e-interfaces -->
```mermaid
---
config:
  layout: elk
---
classDiagram
    direction LR
    class ClaseOInterfaz {
        <<abstract>>
        -consolidado : boolean
        #ClaseOInterfaz(Token)
        +token() Token
        +nombre() String
        +setParametroDeTipo(Token)
        +parametroDeTipo() Optional~Token~
        +esGenerico() boolean
        +declaraParametroDeTipo(String) boolean
        +agregarMetodo(Metodo)
        +metodo(ClaveDeMetodo) Optional~Metodo~
        +metodos() List~Metodo~
        #metodosPropios() List~Metodo~
        #esPropio(Metodo) boolean
        +toString() String
        +estaBienDeclarado()*
        +consolidar()
        #consolidarConAncestros()*
        #heredarMetodos(ClaseOInterfaz, Sustitucion, ControlDeRedefinicion)
        +sustitucionHacia(TipoReferencia, ClaseOInterfaz)$ Sustitucion
        +ancestrosDirectos()* List~TipoReferencia~
        #verificarQueNoHayaCiclo()
        #ancestroDirecto()* Optional~ClaseOInterfaz~
        #verificarUnSoloParametroDeTipo()
        -ancestroQueAportaUnParametro(Set~ClaseOInterfaz~) Optional~TipoReferencia~
        -tieneParametroDeTipo(Set~ClaseOInterfaz~) boolean
    }
    class ControlDeRedefinicion {
        <<interface>>
        +controlar(Metodo, Metodo, ClaseOInterfaz)*
    }
    class Clase {
        +Clase(Token)
        +setPadre(TipoReferencia)
        +setInterfaz(TipoReferencia)
        +padre() Optional~TipoReferencia~
        +interfaz() Optional~TipoReferencia~
        +agregarMiembro(Miembro)
        +agregarAtributo(Atributo)
        +agregarConstructor(Constructor)
        +cerrar()
        +atributo(String) Optional~Atributo~
        +atributos() List~Atributo~
        +constructores() List~Constructor~
        +constructor(int) Optional~Constructor~
        +ancestrosDirectos() List~TipoReferencia~
        +chequearSentencias()
        +estaBienDeclarado()
        -verificarPadre(TipoReferencia)
        -verificarInterfaz(TipoReferencia)
        #ancestroDirecto() Optional~ClaseOInterfaz~
        #consolidarConAncestros()
        -heredarAtributos(Clase, Sustitucion)
        -controlarRedefinicion(Metodo, Metodo, ClaseOInterfaz)$
        -verificarImplementacion(Interfaz, Sustitucion)
    }
    class Interfaz {
        +Interfaz(Token)
        +setExtendida(TipoReferencia)
        +extendida() Optional~TipoReferencia~
        +ancestrosDirectos() List~TipoReferencia~
        +estaBienDeclarado()
        #ancestroDirecto() Optional~ClaseOInterfaz~
        #consolidarConAncestros()
        -controlarRedefinicion(Metodo, Metodo, ClaseOInterfaz)$
        -verificarContraObject()
    }
    class Token {
        <<record>>
    }
    class TipoReferencia
    class Metodo
    class Atributo {
        <<record>>
    }
    class Constructor
    ClaseOInterfaz <|-- Clase
    ClaseOInterfaz <|-- Interfaz
    ClaseOInterfaz ..> ControlDeRedefinicion : usa
    ClaseOInterfaz --> Token : token
    ClaseOInterfaz --> Token : parametroDeTipo 0..1
    ClaseOInterfaz *-- Metodo : metodosPropios *
    ClaseOInterfaz o-- Metodo : metodos *
    Clase --> TipoReferencia : padre 0..1
    Clase --> TipoReferencia : interfaz 0..1
    Clase *-- Atributo : atributosPropios *
    Clase o-- Atributo : atributos *
    Clase *-- Constructor : constructores *
    Clase o-- Unidad : unidadesEnOrden *
    Interfaz --> TipoReferencia : extendida 0..1
```

### Los miembros

`Unidad` es la base de métodos y constructores. De cada una cuelga su cuerpo, el AST que chequea la
Etapa 4, y guarda su `declarante`, la clase o interfaz que la declara.

<!-- diagrama: miembros -->
```mermaid
---
config:
  layout: elk
---
classDiagram
    direction TB
    class Miembro {
        <<interface>>
    }
    class Atributo {
        <<record>>
        +nombre() String
        ~estaBienDeclarado(ClaseOInterfaz)
        ~instanciado(Sustitucion) Atributo
    }
    class Unidad {
        <<abstract>>
        #Unidad(Token)
        #Unidad(Token, List~Parametro~)
        #Unidad(Token, List~Parametro~, ClaseOInterfaz, NodoBloque)
        +token() Token
        +nombre() String
        +parametros() List~Parametro~
        +aridad() int
        ~setDeclarante(ClaseOInterfaz)
        +declarante() ClaseOInterfaz
        +setCuerpo(NodoBloque)
        +cuerpo() NodoBloque
        ~chequearCuerpo(Clase)
        +agregarParametro(Token, Tipo)
        #verificarParametrosEn(ClaseOInterfaz, boolean)
    }
    class Metodo {
        -estatico : boolean
        +Metodo(Token)
        -Metodo(Token, List~Parametro~, boolean, Tipo, ClaseOInterfaz, NodoBloque)
        +hacerEstatico()
        +setRetorno(Tipo)
        +esEstatico() boolean
        +retorno() Tipo
        +clave() ClaveDeMetodo
        +mismaSignaturaQue(Metodo) boolean
        ~estaBienDeclarado(ClaseOInterfaz)
        ~instanciado(Sustitucion) Metodo
    }
    class Constructor {
        +Constructor(Token)
        ~estaBienDeclarado(Clase)
    }
    class Parametro {
        <<record>>
        -posicion : int
        +nombre() String
        ~instanciado(Sustitucion) Parametro
    }
    class ClaveDeMetodo {
        <<record>>
        -nombre : String
        -aridad : int
        +toString() String
    }
    class Token {
        <<record>>
    }
    class Tipo {
        <<interface>>
    }
    class ClaseOInterfaz {
        <<abstract>>
    }
    class NodoBloque
    Miembro <|.. Atributo
    Miembro <|.. Metodo
    Miembro <|.. Constructor
    Unidad <|-- Metodo
    Unidad <|-- Constructor
    Unidad --> Token : token
    Unidad --> ClaseOInterfaz : declarante
    Unidad --> NodoBloque : cuerpo 0..1
    Unidad *-- Parametro : parametros *
    Metodo --> Tipo : retorno
    Metodo ..> ClaveDeMetodo : clave
    Atributo --> Token : token
    Atributo --> Tipo : tipo
    Parametro --> Token : token
    Parametro --> Tipo : tipo
```

Cada entidad guarda **el token de su declaración**. El chequeo corre con el parseo terminado, sin
tokens a mano, y cada error se reporta con lexema y línea (Clase 8, slide 29). Las tablas son
`LinkedHashMap`: se recorren en orden de declaración, así que el error reportado es determinista.

La clave de un método es `(nombre, aridad)`, porque MiniJava tiene sobrecarga simple (documento
§6.2). Los constructores se identifican por la aridad.

## Los tipos

Primitivos y referencias son semánticamente distintos y se modelan por separado, pero bajo una
jerarquía uniforme (Clase 7, slides 29-31). `Tipo` es una interfaz sellada:

| Tipo | Ejemplos | Válido si |
|---|---|---|
| `TipoPrimitivo` | `boolean`, `char`, `int`, `float` | siempre |
| `TipoVoid` | `void` | siempre; la gramática lo admite sólo como retorno |
| `TipoReferencia` | `Aa`, `Caja<T>`, `Caja<String>` | el nombre está en la tabla; lleva argumento si y sólo si el tipo es genérico, salvo en `extends` e `implements`, donde es optativo; el argumento es una `T` válida o una clase o interfaz no genérica |
| `VariableDeTipo` | `T` | la clase o interfaz actual declara esa `T`, y el método no es estático |
| `TipoArreglo` | `int[][]`, `T[]` | su tipo base es válido |

Cada tipo sabe tres cosas:

- **validarse** en un contexto (`verificarEn`);
- **nombrarse**: dos tipos son iguales si tienen la misma `identidad()`, que es como se escriben
  salvo para un parámetro de tipo, que suma la clase o interfaz que lo declara (documento completo
  §5.1). El léxico garantiza que el nombre no confunde una clase con un parámetro, porque una clase
  tiene al menos dos caracteres y un parámetro de tipo tiene uno solo;
- **instanciarse**: reemplazar la `T` por un argumento. `Sustitucion` es ese reemplazo.

<!-- diagrama: tipos -->
```mermaid
---
config:
  layout: elk
---
classDiagram
    direction LR
    class Tipo {
        <<interface>>
        +nombre()* String
        +verificarEn(ClaseOInterfaz, boolean)*
        +instanciar(Sustitucion)* Tipo
        +conformaCon(Tipo)* boolean
        +identidad() String
        +esIgualA(Tipo) boolean
    }
    class TipoPrimitivo {
        <<enumeration>>
        BOOLEAN
        CHAR
        INT
        FLOAT
        -nombre : String
        -TipoPrimitivo(String)
        +nombre() String
        +verificarEn(ClaseOInterfaz, boolean)
        +instanciar(Sustitucion) Tipo
        +conformaCon(Tipo) boolean
    }
    class TipoVoid {
        <<enumeration>>
        VOID
        +nombre() String
        +verificarEn(ClaseOInterfaz, boolean)
        +instanciar(Sustitucion) Tipo
        +conformaCon(Tipo) boolean
    }
    class TipoNull {
        <<enumeration>>
        NULL
        +nombre() String
        +verificarEn(ClaseOInterfaz, boolean)
        +instanciar(Sustitucion) Tipo
        +conformaCon(Tipo) boolean
    }
    class TipoReferencia {
        +TipoReferencia(Token)
        +TipoReferencia(Token, Tipo)
        +token() Token
        +argumento() Optional~Tipo~
        +nombre() String
        +identidad() String
        +nombreDelTipo() String
        +esObject() boolean
        +verificarEn(ClaseOInterfaz, boolean)
        +verificarComoAncestroEn(ClaseOInterfaz, boolean)
        +declaracion() ClaseOInterfaz
        +instanciar(Sustitucion) TipoReferencia
        +conformaCon(Tipo) boolean
        -conParametroConservado() TipoReferencia
    }
    class VariableDeTipo {
        <<record>>
        -declarante : String
        +nombre() String
        +identidad() String
        +verificarEn(ClaseOInterfaz, boolean)
        +instanciar(Sustitucion) Tipo
        +conformaCon(Tipo) boolean
    }
    class TipoArreglo {
        <<record>>
        -dimensiones : int
        +de(Tipo, int)$ Tipo
        +nombre() String
        +identidad() String
        +verificarEn(ClaseOInterfaz, boolean)
        +instanciar(Sustitucion) Tipo
        +conformaCon(Tipo) boolean
        +componente() Tipo
    }
    class Sustitucion {
        <<interface>>
        +reemplazoDe(VariableDeTipo)* Optional~Tipo~
        +identidad()$ Sustitucion
        +de(String, String, Tipo)$ Sustitucion
    }
    class Token {
        <<record>>
    }
    class ClaseOInterfaz {
        <<abstract>>
    }
    Tipo <|.. TipoPrimitivo
    Tipo <|.. TipoVoid
    Tipo <|.. TipoNull
    Tipo <|.. TipoReferencia
    Tipo <|.. VariableDeTipo
    Tipo <|.. TipoArreglo
    Tipo ..> Sustitucion : instanciar
    TipoReferencia --> Token : token
    TipoReferencia --> Tipo : argumento 0..1
    TipoReferencia ..> ClaseOInterfaz : declaracion
    VariableDeTipo --> Token : token
    TipoArreglo --> Tipo : base
```

## El chequeo

Funciona por delegación (Pautas slide 8): la tabla le pide a cada tipo que se chequee, y cada tipo
chequea lo suyo y le pide lo mismo a cada miembro.

**Primera pasada, al parsear.** Cada `agregar…` controla el nombre repetido en su tabla. El
repetido no se inserta, se **registra**, y `chequearDeclaraciones()` lo lanza antes que ningún otro
error ([`decisiones-de-diseno.md`](decisiones-de-diseno.md) §2).

**Segunda pasada, `estaBienDeclarado()`**, sobre cada tipo en orden de declaración: la herencia
(que el tipo exista y que sea del tipo correcto), la herencia circular, y los
tipos de los atributos, los constructores y los métodos.

**Consolidación, `consolidar()`.** Un tipo se consolida después de su ancestro: si el ancestro no
está consolidado, primero le pide que se consolide (Clase 8, slide 26). La recursión termina porque
la segunda pasada ya descartó los ciclos, y por eso todos los tipos se chequean antes de consolidar
cualquiera. Los atributos se consolidan por separado de los métodos (misma slide). Lo heredado entra
**instanciado**, y cada miembro propio se compara contra lo heredado: ahí se controlan las
redefiniciones. Una clase que implementa una interfaz se compara, al final, contra la interfaz ya
consolidada.

La lista completa de controles, con su token, está en
[`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md).

## Código

| Paquete | Qué hay |
|---|---|
| `minijava.semantico.tabla` | `TablaDeSimbolos` y `EntidadesPredefinidas` |
| `minijava.semantico.entidades` | `ClaseOInterfaz`, `Clase`, `Interfaz`, `Unidad`, `Metodo`, `Constructor`, `Atributo`, `Parametro`, `ClaveDeMetodo`, `Miembro` |
| `minijava.semantico.tipos` | `Tipo` y sus cinco variantes, `Sustitucion` |
| `minijava.semantico.errores` | `ExcepcionSemantica` |
| `minijava.sintactico.partes` | `AnalizadorDeClases`, `AnalizadorDeMiembros` y `AnalizadorDeTipos`, con las acciones de la EDT |
| `minijava.aplicacion` | `ModuloPrincipalSemantico` |

`Unidad` es la base de métodos y constructores, y de cada una cuelga su cuerpo.
