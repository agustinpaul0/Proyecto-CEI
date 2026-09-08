package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.ConjuntoDePrimeros;
import minijava.sintactico.primeros.Primeros;

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
        if (estaEn(Primeros.TIPO_BASE_TIPO_PRIMITIVO)) {
            tipoPrimitivo();
        } else if (estaEn(Primeros.TIPO_BASE_TIPO_REFERENCIA)) {
            tipoReferencia();
        } else if (estaEn(Primeros.TIPO_BASE_ID_GEN)) {
            entrada().match(TipoDeToken.ID_GEN);
        } else {
            throw entrada().errorEsperando(Primeros.TIPO_BASE_TIPO_PRIMITIVO,
                    Primeros.TIPO_BASE_TIPO_REFERENCIA, Primeros.TIPO_BASE_ID_GEN);
        }
    }

    // DimensionesOpcionales -> [ ] DimensionesOpcionales | EPSILON
    public void dimensionesOpcionales() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(Primeros.DIMENSIONES_OPCIONALES_CORCHETE_A)) {
            entrada().match(TipoDeToken.CORCHETE_A);
            entrada().match(TipoDeToken.CORCHETE_C);
        }
    }

    // TipoReferencia -> idClase TipoGenericoOpcional
    public void tipoReferencia() throws ExcepcionLexica, ExcepcionSintactica {
        entrada().match(TipoDeToken.ID_CLASE);
        tipoGenericoOpcional();
    }

    // TipoPrimitivo -> boolean | char | int
    // TipoPrimitivo -> float   [+float]
    //
    // `float` es token del léxico desde la Etapa 1 pero no estaba en la gramática
    // de la cátedra; sin esta alternativa el logro de Etapa 1 queda inutilizable.
    public void tipoPrimitivo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.TIPO_PRIMITIVO_BOOLEAN)) {
            entrada().match(TipoDeToken.PR_BOOLEAN);
        } else if (estaEn(Primeros.TIPO_PRIMITIVO_CHAR)) {
            entrada().match(TipoDeToken.PR_CHAR);
        } else if (estaEn(Primeros.TIPO_PRIMITIVO_INT)) {
            entrada().match(TipoDeToken.PR_INT);
        } else if (hayLogro(Logro.FLOATS) && estaEn(Primeros.TIPO_PRIMITIVO_FLOAT)) {
            entrada().match(TipoDeToken.PR_FLOAT);
        } else {
            throw entrada().errorEsperando(Primeros.TIPO_PRIMITIVO_BOOLEAN,
                    Primeros.TIPO_PRIMITIVO_CHAR, Primeros.TIPO_PRIMITIVO_INT,
                    hayLogro(Logro.FLOATS) ? Primeros.TIPO_PRIMITIVO_FLOAT : ConjuntoDePrimeros.de());
        }
    }

    // TipoGenericoOpcional -> < ArgumentoDeTipo > | EPSILON
    //
    // En POSICIÓN DE TIPO no se admite el diamante: `List<> x;` es inválido.
    public void tipoGenericoOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.TIPO_GENERICO_OPCIONAL_MENOR)) {
            entrada().match(TipoDeToken.OP_MENOR);
            argumentoDeTipo();
            entrada().match(TipoDeToken.OP_MAYOR);
        }
    }

    // ArgumentoDeTipo -> idGen
    // ArgumentoDeTipo -> idClase TipoGenericoOpcional   [+generico]
    // ArgumentoDeTipo -> idClase                        [-generico]
    //
    // La recursión por TipoGenericoOpcional es la que habilita el anidamiento
    // `List<List<Persona>>`, y es todo lo que el logro agrega.
    public void argumentoDeTipo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.ARGUMENTO_DE_TIPO_ID_GEN)) {
            entrada().match(TipoDeToken.ID_GEN);
        } else if (estaEn(Primeros.ARGUMENTO_DE_TIPO_ID_CLASE)) {
            entrada().match(TipoDeToken.ID_CLASE);
            if (hayLogro(Logro.GENERICO)) {
                tipoGenericoOpcional();
            }
        } else {
            throw entrada().errorEsperando(Primeros.ARGUMENTO_DE_TIPO_ID_GEN,
                    Primeros.ARGUMENTO_DE_TIPO_ID_CLASE);
        }
    }

    // ArgsFormales -> ( ListaArgsFormalesOpcional )
    // ListaArgsFormales -> ArgFormal RestoListaArgsFormales
    // RestoListaArgsFormales -> , ArgFormal RestoListaArgsFormales | EPSILON
    public void argsFormales() throws ExcepcionLexica, ExcepcionSintactica {
        entrada().match(TipoDeToken.PAREN_A);
        if (estaEn(Primeros.LISTA_ARGS_FORMALES_OPCIONAL_LISTA_ARGS_FORMALES)) {
            argFormal();
            while (estaEn(Primeros.RESTO_LISTA_ARGS_FORMALES_COMA)) {
                entrada().match(TipoDeToken.COMA);
                argFormal();
            }
        }
        entrada().match(TipoDeToken.PAREN_C);
    }

    // ArgFormal -> Tipo idMetVar
    private void argFormal() throws ExcepcionLexica, ExcepcionSintactica {
        tipo();
        entrada().match(TipoDeToken.ID_MV);
    }
}
