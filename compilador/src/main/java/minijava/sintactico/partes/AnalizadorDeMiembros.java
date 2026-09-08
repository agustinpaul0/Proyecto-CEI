package minijava.sintactico.partes;

import minijava.comun.config.Logro;
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

    // Miembro -> VisibilidadOpcional MiembroSinVisibilidad   [+visibilidad]
    // Miembro -> MiembroSinVisibilidad | Constructor         [-visibilidad]
    //
    // Sin el logro, el `public` sólo aparece en el constructor y es lo que lo
    // distingue del resto: es la regla original de la cátedra.
    public void miembro() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.VISIBILIDAD)) {
            visibilidadOpcional();
            miembroSinVisibilidad();
            return;
        }
        if (estaEn(PrimerosDeMiembros.MIEMBRO_CONSTRUCTOR)) {
            constructor();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_MIEMBRO_SIN_VISIBILIDAD)) {
            miembroSinVisibilidad();
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_CONSTRUCTOR,
                    PrimerosDeMiembros.MIEMBRO_MIEMBRO_SIN_VISIBILIDAD);
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

    // VisibilidadOpcional -> public | private | EPSILON   [+visibilidad]
    private void visibilidadOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.VISIBILIDAD_OPCIONAL_PUBLIC)) {
            match(TipoDeToken.PR_PUBLIC);
        } else if (estaEn(PrimerosDeMiembros.VISIBILIDAD_OPCIONAL_PRIVATE)) {
            match(TipoDeToken.PR_PRIVATE);
        }
    }

    // Constructor -> public idClase CuerpoDeMetodo   [-visibilidad]
    private void constructor() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_PUBLIC);
        match(TipoDeToken.ID_CLASE);
        cuerpoDeMetodo();
    }

    // MiembroSinVisibilidad -> static TipoMetodo Metodo
    //                        | void Metodo
    //                        | TipoPrimitivo DimensionesOpcionales idMetVar AtributoOMetodo
    //                        | idGen DimensionesOpcionales idMetVar AtributoOMetodo
    //                        | idClase MiembroDesdeIdClase
    private void miembroSinVisibilidad() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_STATIC)) {
            match(TipoDeToken.PR_STATIC);
            tipoMetodo();
            metodo();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_VOID)) {
            match(TipoDeToken.PR_VOID);
            metodo();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            declaradorDeMiembro();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
            declaradorDeMiembro();
        } else if (estaEn(PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            miembroDesdeIdClase();
        } else {
            throw errorEsperando(PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_STATIC,
                    PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_VOID,
                    PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_TIPO_PRIMITIVO,
                    PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_ID_GEN,
                    PrimerosDeMiembros.MIEMBRO_SIN_VISIBILIDAD_ID_CLASE);
        }
    }

    private void declaradorDeMiembro() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.tipos().dimensionesOpcionales();
        match(TipoDeToken.ID_MV);
        atributoOMetodo();
    }

    // MiembroDesdeIdClase -> CuerpoDeMetodo   [+visibilidad]
    // MiembroDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar AtributoOMetodo
    //
    // Con visibilidad el constructor ya no lleva `public` propio, así que compite
    // con el atributo o método de tipo referencia por el token idClase.
    private void miembroDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.VISIBILIDAD) && estaEn(PrimerosDeMiembros.MIEMBRO_DESDE_ID_CLASE_CUERPO_DE_METODO)) {
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

    // InicializacionDeAtributo -> = ExpresionCondicional   [+atrinit]
    // InicializacionDeAtributo -> EPSILON
    private void inicializacionDeAtributo() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.ATRINIT) && estaEn(PrimerosDeMiembros.INICIALIZACION_DE_ATRIBUTO_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicional();
        }
    }

    // Metodo -> idMetVar CuerpoDeMetodo
    private void metodo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.ID_MV);
        cuerpoDeMetodo();
    }

    // PublicOpcional -> public   [+visibilidad]
    // PublicOpcional -> EPSILON
    private void publicOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.VISIBILIDAD) && estaEn(PrimerosDeMiembros.PUBLIC_OPCIONAL_PUBLIC)) {
            match(TipoDeToken.PR_PUBLIC);
        }
    }
}
