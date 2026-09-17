package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.ConjuntoDePrimeros;
import minijava.sintactico.primeros.PrimerosDeTipos;

// Tipos, dimensiones, genéricos y argumentos formales.
public final class AnalizadorDeTipos extends ParteDelAnalizador {

    public AnalizadorDeTipos(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Tipo -> TipoBase DimensionesOpcionales
    public void tipo() throws ExcepcionLexica, ExcepcionSintactica {
        tipoBase();
        dimensionesOpcionales();
    }

    // TipoBase -> TipoPrimitivo | TipoReferencia | idGen
    private void tipoBase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.TIPO_BASE_TIPO_PRIMITIVO)) {
            tipoPrimitivo();
        } else if (estaEn(PrimerosDeTipos.TIPO_BASE_TIPO_REFERENCIA)) {
            tipoReferencia();
        } else if (estaEn(PrimerosDeTipos.TIPO_BASE_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
        } else {
            throw errorEsperando(PrimerosDeTipos.TIPO_BASE_TIPO_PRIMITIVO,
                    PrimerosDeTipos.TIPO_BASE_TIPO_REFERENCIA, PrimerosDeTipos.TIPO_BASE_ID_GEN);
        }
    }

    // DimensionesOpcionales -> [ ] DimensionesOpcionales | EPSILON
    public void dimensionesOpcionales() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.DIMENSIONES_OPCIONALES_CORCHETE_A)) {
            match(TipoDeToken.CORCHETE_A);
            match(TipoDeToken.CORCHETE_C);
            dimensionesOpcionales();
        } else {
            // DimensionesOpcionales -> EPSILON
        }
    }

    // TipoReferencia -> idClase TipoGenericoOpcional
    public void tipoReferencia() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.ID_CLASE);
        tipoGenericoOpcional();
    }

    // TipoPrimitivo -> boolean | char | int
    // TipoPrimitivo -> float
    //
    // `float` es token del léxico desde la Etapa 1 pero no estaba en la gramática
    // de la cátedra; sin esta alternativa el logro de Etapa 1 queda inutilizable.
    public void tipoPrimitivo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_BOOLEAN)) {
            match(TipoDeToken.PR_BOOLEAN);
        } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_CHAR)) {
            match(TipoDeToken.PR_CHAR);
        } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_INT)) {
            match(TipoDeToken.PR_INT);
        } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_FLOAT)) {
            match(TipoDeToken.PR_FLOAT);
        } else {
            throw errorEsperando(PrimerosDeTipos.TIPO_PRIMITIVO_BOOLEAN,
                    PrimerosDeTipos.TIPO_PRIMITIVO_CHAR, PrimerosDeTipos.TIPO_PRIMITIVO_INT,
                    PrimerosDeTipos.TIPO_PRIMITIVO_FLOAT);
        }
    }

    // TipoGenericoOpcional -> < ArgumentoDeTipo > | EPSILON
    //
    // En POSICIÓN DE TIPO no se admite el diamante: `List<> x;` es inválido.
    public void tipoGenericoOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.TIPO_GENERICO_OPCIONAL_MENOR)) {
            match(TipoDeToken.OP_MENOR);
            argumentoDeTipo();
            match(TipoDeToken.OP_MAYOR);
        } else {
            // TipoGenericoOpcional -> EPSILON
        }
    }

    // ArgumentoDeTipo -> idGen
    // ArgumentoDeTipo -> idClase TipoGenericoOpcional
    //
    // La recursión por TipoGenericoOpcional es la que habilita el anidamiento
    // `List<List<Persona>>`.
    public void argumentoDeTipo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.ARGUMENTO_DE_TIPO_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
        } else if (estaEn(PrimerosDeTipos.ARGUMENTO_DE_TIPO_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            tipoGenericoOpcional();
        } else {
            throw errorEsperando(PrimerosDeTipos.ARGUMENTO_DE_TIPO_ID_GEN,
                    PrimerosDeTipos.ARGUMENTO_DE_TIPO_ID_CLASE);
        }
    }

    // ArgsFormales -> ( ListaArgsFormalesOpcional )
    // ListaArgsFormalesOpcional -> ListaArgsFormales | EPSILON
    // ListaArgsFormales -> ArgFormal RestoListaArgsFormales
    // RestoListaArgsFormales -> , ArgFormal RestoListaArgsFormales | EPSILON
    public void argsFormales() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);
        if (estaEn(PrimerosDeTipos.LISTA_ARGS_FORMALES_OPCIONAL_LISTA_ARGS_FORMALES)) {
            argFormal();
            restoListaArgsFormales();
        } else {
            // ListaArgsFormalesOpcional -> EPSILON
        }
        match(TipoDeToken.PAREN_C);
    }

    // RestoListaArgsFormales -> , ArgFormal RestoListaArgsFormales | EPSILON
    private void restoListaArgsFormales() throws ExcepcionLexica, ExcepcionSintactica {
        if (!estaEn(PrimerosDeTipos.RESTO_LISTA_ARGS_FORMALES_COMA)) {
            return;
        }
        match(TipoDeToken.COMA);
        argFormal();
        restoListaArgsFormales();
    }

    // ArgFormal -> Tipo idMetVar
    private void argFormal() throws ExcepcionLexica, ExcepcionSintactica {
        tipo();
        match(TipoDeToken.ID_MV);
    }
}
