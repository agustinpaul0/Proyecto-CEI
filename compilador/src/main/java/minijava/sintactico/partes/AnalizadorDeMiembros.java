package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeMiembros;

// Atributos, métodos y constructores. Es donde vive la factorización que decide
// entre constructor y miembro con tipo de referencia, y entre atributo y método.
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
            miembroTrasVisibilidad();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_STATIC)) {
            match(TipoDeToken.PR_STATIC);
            visibilidadOpcional();
            tipoMetodo();
            metodo();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_MIEMBRO_SIN_MODIFICADORES)) {
            miembroSinModificadores();
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_VISIBILIDAD,
                    PrimerosDeMiembros.MIEMBRO_STATIC,
                    PrimerosDeMiembros.MIEMBRO_MIEMBRO_SIN_MODIFICADORES);
        }
    }

    // MiembroTrasVisibilidad -> static TipoMetodo Metodo
    // MiembroTrasVisibilidad -> MiembroSinModificadores
    private void miembroTrasVisibilidad() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_STATIC)) {
            match(TipoDeToken.PR_STATIC);
            tipoMetodo();
            metodo();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_MIEMBRO_SIN_MODIFICADORES)) {
            miembroSinModificadores();
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_STATIC,
                    PrimerosDeMiembros.MIEMBRO_TRAS_VISIBILIDAD_MIEMBRO_SIN_MODIFICADORES);
        }
    }

    // MetodoInterfaz -> PublicOpcional TipoMetodo idMetVar ArgsFormales ;
    //
    // Los métodos de interfaz son siempre abstractos y admiten `public` pero no
    // `private`: en Java un método de interfaz privado exige cuerpo, y MiniJava
    // no tiene forma de escribirlo.
    public void metodoInterfaz() throws ExcepcionLexica, ExcepcionSintactica {
        publicOpcional();
        tipoMetodo();
        match(TipoDeToken.ID_MV);
        sintaxis.tipos().argsFormales();
        match(TipoDeToken.PUNTO_Y_COMA);
    }

    // TipoMetodo -> Tipo | void
    public void tipoMetodo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.TIPO_METODO_VOID)) {
            match(TipoDeToken.PR_VOID);
        } else if (estaEn(PrimerosDeMiembros.TIPO_METODO_TIPO)) {
            sintaxis.tipos().tipo();
        } else {
            throw errorEsperando(PrimerosDeMiembros.TIPO_METODO_TIPO, PrimerosDeMiembros.TIPO_METODO_VOID);
        }
    }

    // CuerpoDeMetodo -> ArgsFormales Bloque
    //
    // Se comparte a propósito: es la misma construcción en método, método static
    // y constructor.
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
    private void miembroSinModificadores() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_VOID)) {
            match(TipoDeToken.PR_VOID);
            metodo();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            declaradorDeMiembro();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
            declaradorDeMiembro();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            miembroDesdeIdClase();
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_VOID,
                    PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_TIPO_PRIMITIVO,
                    PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_GEN,
                    PrimerosDeMiembros.MIEMBRO_SIN_MODIFICADORES_ID_CLASE);
        }
    }

    private void declaradorDeMiembro() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.tipos().dimensionesOpcionales();
        match(TipoDeToken.ID_MV);
        atributoOMetodo();
    }

    // MiembroDesdeIdClase -> CuerpoDeMetodo
    // MiembroDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar AtributoOMetodo
    //
    // El constructor ya no lleva `public` propio —lo absorbió <Visibilidad>—, así
    // que compite con el atributo o método de tipo referencia por el token idClase.
    private void miembroDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_DESDE_ID_CLASE_CUERPO_DE_METODO)) {
            cuerpoDeMetodo();
            return;
        }
        sintaxis.tipos().tipoGenericoOpcional();
        declaradorDeMiembro();
    }

    // AtributoOMetodo -> InicializacionDeAtributo ; | CuerpoDeMetodo
    private void atributoOMetodo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.ATRIBUTO_OMETODO_CUERPO_DE_METODO)) {
            cuerpoDeMetodo();
        } else {
            inicializacionDeAtributo();
            match(TipoDeToken.PUNTO_Y_COMA);
        }
    }

    // InicializacionDeAtributo -> = ExpresionCondicionalOLambda
    // InicializacionDeAtributo -> EPSILON
    private void inicializacionDeAtributo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.INICIALIZACION_DE_ATRIBUTO_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicionalOLambda();
        } else {
            // InicializacionDeAtributo -> EPSILON
        }
    }

    // Metodo -> idMetVar CuerpoDeMetodo
    private void metodo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.ID_MV);
        cuerpoDeMetodo();
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
