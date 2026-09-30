# Esquema de traducción

La tabla de símbolos se construye mientras el sintáctico reconoce el programa (Pautas Etapa 3,
slide 6). El diseño es un **Esquema de Traducción (EDT)** sobre la gramática LL(1) final de la
Etapa 2 ([`../sintactico/gramatica-ll1-final.md`](../sintactico/gramatica-ll1-final.md)): las
acciones `{ … }` se ejecutan de izquierda a derecha, en el lugar que ocupan en la producción
(Clase 6, slides 13-14).

En el analizador descendente recursivo, la traducción es directa (Clase 6, slides 20-21):
- una **acción** es código en el método del no terminal, en su lugar;
- un **atributo de salida** (`X.a`) es el valor de retorno del método;
- un **atributo de entrada** sería un parámetro.

`TS` es la tabla de símbolos, global y estática. Sólo llevan acciones las producciones de las
declaraciones: las de sentencias y expresiones quedan como en la Etapa 2.

## Clases e interfaces

```
Clase → class idClase { c = new Clase(idClase); TS.declararClase(c) }
        GenericidadOpcional { c.setParametroDeTipo(GenericidadOpcional.parametro) }
        HerenciaOpcional { ListaMiembros } { c.cerrar() }

Interfaz → interface idClase { i = new Interfaz(idClase); TS.declararInterfaz(i) }
           GenericidadOpcional { i.setParametroDeTipo(GenericidadOpcional.parametro) }
           ExtensionOpcional { ListaMetodosInterfaz }

GenericidadOpcional → < idGen > { GenericidadOpcional.parametro = idGen }
GenericidadOpcional → ε         { GenericidadOpcional.parametro = ninguno }

HerenciaOpcional → extends TipoReferencia { TS.claseActual().setPadre(TipoReferencia.tipo) }
HerenciaOpcional → implements { TS.claseActual().setPadre(Object) }
                   TipoReferencia { TS.claseActual().setInterfaz(TipoReferencia.tipo) }
HerenciaOpcional → ε { TS.claseActual().setPadre(Object) }

ExtensionOpcional → extends TipoReferencia { TS.interfazActual().setExtendida(TipoReferencia.tipo) }
ExtensionOpcional → ε
```

`cerrar()` agrega el constructor por defecto si la clase no declaró ninguno (documento §6.3). La
herencia implícita de `Object` la pone la producción ε de `HerenciaOpcional`, y también la rama de
`implements`, porque una clase que implementa una interfaz extiende `Object` (documento §4).

## Miembros

```
Miembro → Visibilidad MiembroTrasVisibilidad
          { TS.claseActual().agregarMiembro(MiembroTrasVisibilidad.miembro) }
Miembro → static VisibilidadOpcional TipoMetodo Metodo
          { Metodo.metodo.hacerEstatico(); Metodo.metodo.setRetorno(TipoMetodo.tipo);
            TS.claseActual().agregarMiembro(Metodo.metodo) }
Miembro → MiembroSinModificadores
          { TS.claseActual().agregarMiembro(MiembroSinModificadores.miembro) }

MiembroTrasVisibilidad → static TipoMetodo Metodo
          { Metodo.metodo.hacerEstatico(); Metodo.metodo.setRetorno(TipoMetodo.tipo);
            MiembroTrasVisibilidad.miembro = Metodo.metodo }
MiembroTrasVisibilidad → MiembroSinModificadores
          { MiembroTrasVisibilidad.miembro = MiembroSinModificadores.miembro }

MiembroSinModificadores → void Metodo
          { Metodo.metodo.setRetorno(void); MiembroSinModificadores.miembro = Metodo.metodo }
MiembroSinModificadores → TipoPrimitivo DimensionesOpcionales idMetVar
          { AtributoOMetodo.tipo = arreglo(TipoPrimitivo.tipo, DimensionesOpcionales.cantidad);
            AtributoOMetodo.nombre = idMetVar }
          AtributoOMetodo { MiembroSinModificadores.miembro = AtributoOMetodo.miembro }
MiembroSinModificadores → idGen DimensionesOpcionales idMetVar
          { AtributoOMetodo.tipo = arreglo(new VariableDeTipo(idGen), DimensionesOpcionales.cantidad);
            AtributoOMetodo.nombre = idMetVar }
          AtributoOMetodo { MiembroSinModificadores.miembro = AtributoOMetodo.miembro }
MiembroSinModificadores → idClase { MiembroDesdeIdClase.idClase = idClase }
          MiembroDesdeIdClase { MiembroSinModificadores.miembro = MiembroDesdeIdClase.miembro }

MiembroDesdeIdClase → { c = new Constructor(MiembroDesdeIdClase.idClase); TS.setUnidadActual(c) }
          CuerpoDeMetodo { MiembroDesdeIdClase.miembro = c }
MiembroDesdeIdClase → TipoGenericoOpcional DimensionesOpcionales idMetVar
          { AtributoOMetodo.tipo = arreglo(new TipoReferencia(MiembroDesdeIdClase.idClase,
                TipoGenericoOpcional.argumento), DimensionesOpcionales.cantidad);
            AtributoOMetodo.nombre = idMetVar }
          AtributoOMetodo { MiembroDesdeIdClase.miembro = AtributoOMetodo.miembro }

AtributoOMetodo → InicializacionDeAtributo ;
          { AtributoOMetodo.miembro = new Atributo(AtributoOMetodo.nombre, AtributoOMetodo.tipo) }
AtributoOMetodo → { m = new Metodo(AtributoOMetodo.nombre); m.setRetorno(AtributoOMetodo.tipo);
                    TS.setUnidadActual(m) }
          CuerpoDeMetodo { AtributoOMetodo.miembro = m }

Metodo → idMetVar { m = new Metodo(idMetVar); TS.setUnidadActual(m) }
         CuerpoDeMetodo { Metodo.metodo = m }

MetodoInterfaz → PublicOpcional TipoMetodo idMetVar
          { m = new Metodo(idMetVar); m.setRetorno(TipoMetodo.tipo); TS.setUnidadActual(m) }
          ArgsFormales ; { TS.interfazActual().agregarMetodo(m) }

TipoMetodo → Tipo { TipoMetodo.tipo = Tipo.tipo }
TipoMetodo → void { TipoMetodo.tipo = void }
```

`MiembroDesdeIdClase.idClase`, `AtributoOMetodo.tipo` y `AtributoOMetodo.nombre` son los únicos
atributos de entrada de toda la EDT
([`decisiones-de-diseno.md`](decisiones-de-diseno.md) §4). `Visibilidad`, `VisibilidadOpcional`,
`PublicOpcional` e `InicializacionDeAtributo` no llevan acciones: en esta etapa no se registran
([`decisiones-de-logica.md`](decisiones-de-logica.md) §1).

## Tipos y parámetros

```
Tipo → TipoBase DimensionesOpcionales
       { Tipo.tipo = arreglo(TipoBase.tipo, DimensionesOpcionales.cantidad) }

TipoBase → TipoPrimitivo  { TipoBase.tipo = TipoPrimitivo.tipo }
TipoBase → TipoReferencia { TipoBase.tipo = TipoReferencia.tipo }
TipoBase → idGen          { TipoBase.tipo = new VariableDeTipo(idGen) }

DimensionesOpcionales → [ ] DimensionesOpcionales₁
       { DimensionesOpcionales.cantidad = 1 + DimensionesOpcionales₁.cantidad }
DimensionesOpcionales → ε { DimensionesOpcionales.cantidad = 0 }

TipoReferencia → idClase TipoGenericoOpcional
       { TipoReferencia.tipo = new TipoReferencia(idClase, TipoGenericoOpcional.argumento) }

TipoPrimitivo → boolean { TipoPrimitivo.tipo = boolean }      (lo mismo con char, int y float)

TipoGenericoOpcional → < InstanciadoOParametrico >
       { TipoGenericoOpcional.argumento = InstanciadoOParametrico.tipo }
TipoGenericoOpcional → ε { TipoGenericoOpcional.argumento = ninguno }

InstanciadoOParametrico → idGen { InstanciadoOParametrico.tipo = new VariableDeTipo(idGen) }
InstanciadoOParametrico → idClase TipoGenericoOpcional
       { InstanciadoOParametrico.tipo = new TipoReferencia(idClase, TipoGenericoOpcional.argumento) }

ArgFormal → Tipo idMetVar { TS.unidadActual().agregarParametro(idMetVar, Tipo.tipo) }
```

`arreglo(t, 0)` es `t`: sin corchetes no hay arreglo.

Las producciones de tipos también se usan dentro de los cuerpos (variables locales, `new`), donde
sus valores de retorno se descartan. `ArgFormal` sólo aparece en encabezados, así que
`TS.unidadActual()` es siempre el método o constructor que se está declarando.
