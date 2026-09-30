package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.Atributo;
import minijava.semantico.entidades.Constructor;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.entidades.Miembro;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoReferencia;
import minijava.semantico.tipos.TipoVoid;
import minijava.semantico.tipos.VariableDeTipo;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeMiembros;

// Atributos, métodos y constructores. Es donde vive la factorización que decide
// entre constructor y miembro con tipo de referencia, y entre atributo y método.
//
// Cada miembro se crea apenas se sabe qué es y se agrega a la clase actual de la
// tabla de símbolos. La visibilidad se reconoce y no se registra: es un logro de
// la Etapa 2, y los logros no suman controles en la Etapa 3.
//
// `miembroDesdeIdClase` y `atributoOMetodo` son los únicos métodos que reciben
// atributos de entrada: la factorización consumió el tipo y el nombre antes de
// saber si el miembro es un atributo, un método o un constructor.
public final class AnalizadorDeMiembros extends ParteDelAnalizador {

    public AnalizadorDeMiembros(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Miembro -> Visibilidad MiembroTrasVisibilidad
    // Miembro -> static VisibilidadOpcional TipoMetodo Metodo
    // Miembro -> MiembroSinModificadores
    //
    // Los dos órdenes valen, como en Java: `public static` y `static public`. Que
    // la visibilidad aparezca a un solo lado de `static` es lo que impide repetir
    // el modificador, sin necesitar un chequeo fuera de la gramática.
    public void miembro() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_VISIBILIDAD)) {
            visibilidad();
            TablaDeSimbolos.claseActual().agregarMiembro(miembroTrasVisibilidad());
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_STATIC)) {
            match(TipoDeToken.PR_STATIC);
            visibilidadOpcional();
            Tipo retorno = tipoMetodo();
            Metodo metodo = metodo();
            metodo.hacerEstatico();
            metodo.setRetorno(retorno);
            TablaDeSimbolos.claseActual().agregarMiembro(metodo);
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_MIEMBRO_SIN_MODIFICADORES)) {
            TablaDeSimbolos.claseActual().agregarMiembro(miembroSinModificadores());
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_VISIBILIDAD,
                    PrimerosDeMiembros.MIEMBRO_STATIC,
                    PrimerosDeMiembros.MIEMBRO_MIEMBRO_SIN_MODIFICADORES);
        }
    }

    // MiembroTrasVisibilidad -> static TipoMetodo Metodo
    // MiembroTrasVisibilidad -> MiembroSinModificadores
    private Miembro miembroTrasVisibilidad() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_STATIC)) {
            match(TipoDeToken.PR_STATIC);
            Tipo retorno = tipoMetodo();
            Metodo metodo = metodo();
            metodo.hacerEstatico();
            metodo.setRetorno(retorno);
            return metodo;
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_MIEMBRO_SIN_MODIFICADORES)) {
            return miembroSinModificadores();
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_STATIC,
                    PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_MIEMBRO_SIN_MODIFICADORES);
        }
    }

    // MetodoInterfaz -> PublicOpcional TipoMetodo idMetVar ArgsFormales ;
    //
    // Admite `public` y no `private`: en Java un método de interfaz privado exige
    // cuerpo, y éste termina en `;`.
    public void metodoInterfaz() throws ExcepcionLexica, ExcepcionSintactica {
        publicOpcional();
        Tipo retorno = tipoMetodo();
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        Metodo metodo = new Metodo(nombre);
        metodo.setRetorno(retorno);
        TablaDeSimbolos.setUnidadActual(metodo);
        sintaxis.tipos().argsFormales();
        match(TipoDeToken.PUNTO_Y_COMA);
        TablaDeSimbolos.interfazActual().agregarMetodo(metodo);
    }

    // TipoMetodo -> Tipo | void
    public Tipo tipoMetodo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.TIPO_METODO_VOID)) {
            match(TipoDeToken.PR_VOID);
            return TipoVoid.VOID;
        } else if (estaEn(PrimerosDeMiembros.TIPO_METODO_TIPO)) {
            return sintaxis.tipos().tipo();
        } else {
            throw errorEsperando(PrimerosDeMiembros.TIPO_METODO_TIPO, PrimerosDeMiembros.TIPO_METODO_VOID);
        }
    }

    // CuerpoDeMetodo -> ArgsFormales Bloque
    public void cuerpoDeMetodo() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.tipos().argsFormales();
        sintaxis.sentencias().bloque();
    }

    // Visibilidad -> public | private
    private void visibilidad() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.VISIBILIDAD_PUBLIC)) {
            match(TipoDeToken.PR_PUBLIC);
        } else if (estaEn(PrimerosDeMiembros.VISIBILIDAD_PRIVATE)) {
            match(TipoDeToken.PR_PRIVATE);
        } else {
            throw errorEsperando(PrimerosDeMiembros.VISIBILIDAD_PUBLIC,
                    PrimerosDeMiembros.VISIBILIDAD_PRIVATE);
        }
    }

    // VisibilidadOpcional -> Visibilidad | EPSILON
    private void visibilidadOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.VISIBILIDAD_OPCIONAL_VISIBILIDAD)) {
            visibilidad();
        } else {
            // VisibilidadOpcional -> EPSILON
        }
    }

    // MiembroSinModificadores -> void Metodo
    //                          | TipoPrimitivo DimensionesOpcionales idMetVar AtributoOMetodo
    //                          | idGen DimensionesOpcionales idMetVar AtributoOMetodo
    //                          | idClase MiembroDesdeIdClase
    private Miembro miembroSinModificadores() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_VOID)) {
            match(TipoDeToken.PR_VOID);
            Metodo metodo = metodo();
            metodo.setRetorno(TipoVoid.VOID);
            return metodo;
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_TIPO_PRIMITIVO)) {
            Tipo base = sintaxis.tipos().tipoPrimitivo();
            Tipo tipo = TipoArreglo.de(base, sintaxis.tipos().dimensionesOpcionales());
            Token nombre = actual();
            match(TipoDeToken.ID_MV);
            return atributoOMetodo(tipo, nombre);
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_GEN)) {
            Token parametro = actual();
            match(TipoDeToken.ID_GEN);
            Tipo tipo = TipoArreglo.de(new VariableDeTipo(parametro), sintaxis.tipos().dimensionesOpcionales());
            Token nombre = actual();
            match(TipoDeToken.ID_MV);
            return atributoOMetodo(tipo, nombre);
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_CLASE)) {
            Token idClase = actual();
            match(TipoDeToken.ID_CLASE);
            return miembroDesdeIdClase(idClase);
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_VOID,
                    PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_TIPO_PRIMITIVO,
                    PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_GEN,
                    PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_CLASE);
        }
    }

    // MiembroDesdeIdClase -> CuerpoDeMetodo
    // MiembroDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar AtributoOMetodo
    //
    // El constructor no lleva un `public` propio —lo absorbe <Visibilidad>—, así
    // que compite con el atributo o método de tipo referencia por el token idClase.
    private Miembro miembroDesdeIdClase(Token idClase) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_DESDE_ID_CLASE_CUERPO_DE_METODO)) {
            Constructor constructor = new Constructor(idClase);
            TablaDeSimbolos.setUnidadActual(constructor);
            cuerpoDeMetodo();
            return constructor;
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL)) {
            TipoReferencia base = new TipoReferencia(idClase, sintaxis.tipos().tipoGenericoOpcional());
            Tipo tipo = TipoArreglo.de(base, sintaxis.tipos().dimensionesOpcionales());
            Token nombre = actual();
            match(TipoDeToken.ID_MV);
            return atributoOMetodo(tipo, nombre);
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_DESDE_ID_CLASE_CUERPO_DE_METODO,
                    PrimerosDeMiembros.MIEMBRO_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL);
        }
    }

    // AtributoOMetodo -> InicializacionDeAtributo ; | CuerpoDeMetodo
    private Miembro atributoOMetodo(Tipo tipo, Token nombre) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.ATRIBUTO_OMETODO_INICIALIZACION_DE_ATRIBUTO)) {
            inicializacionDeAtributo();
            match(TipoDeToken.PUNTO_Y_COMA);
            return new Atributo(nombre, tipo);
        } else if (estaEn(PrimerosDeMiembros.ATRIBUTO_OMETODO_CUERPO_DE_METODO)) {
            Metodo metodo = new Metodo(nombre);
            metodo.setRetorno(tipo);
            TablaDeSimbolos.setUnidadActual(metodo);
            cuerpoDeMetodo();
            return metodo;
        } else {
            throw errorEsperando(PrimerosDeMiembros.ATRIBUTO_OMETODO_INICIALIZACION_DE_ATRIBUTO,
                    PrimerosDeMiembros.ATRIBUTO_OMETODO_CUERPO_DE_METODO);
        }
    }

    // InicializacionDeAtributo -> = ExpresionCondicional
    // InicializacionDeAtributo -> EPSILON
    private void inicializacionDeAtributo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.INICIALIZACION_DE_ATRIBUTO_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicional();
        } else {
            // InicializacionDeAtributo -> EPSILON
        }
    }

    // Metodo -> idMetVar CuerpoDeMetodo
    private Metodo metodo() throws ExcepcionLexica, ExcepcionSintactica {
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        Metodo metodo = new Metodo(nombre);
        TablaDeSimbolos.setUnidadActual(metodo);
        cuerpoDeMetodo();
        return metodo;
    }

    // PublicOpcional -> public
    // PublicOpcional -> EPSILON
    private void publicOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.PUBLIC_OPCIONAL_PUBLIC)) {
            match(TipoDeToken.PR_PUBLIC);
        } else {
            // PublicOpcional -> EPSILON
        }
    }
}
