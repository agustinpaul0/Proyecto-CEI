package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoPrimitivo;
import minijava.semantico.tipos.TipoReferencia;
import minijava.semantico.tipos.VariableDeTipo;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeTipos;

import java.util.Optional;

// Tipos, dimensiones, genéricos y argumentos formales.
public final class AnalizadorDeTipos extends ParteDelAnalizador {

    public AnalizadorDeTipos(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Tipo -> TipoBase DimensionesOpcionales
    public Tipo tipo() throws ExcepcionLexica, ExcepcionSintactica {
        Tipo base = tipoBase();
        return TipoArreglo.de(base, dimensionesOpcionales());
    }

    // TipoBase -> TipoPrimitivo | TipoReferencia | idGen
    private Tipo tipoBase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.TIPO_BASE_TIPO_PRIMITIVO)) {
            return tipoPrimitivo();
        } else if (estaEn(PrimerosDeTipos.TIPO_BASE_TIPO_REFERENCIA)) {
            return tipoReferencia();
        } else if (estaEn(PrimerosDeTipos.TIPO_BASE_ID_GEN)) {
            Token nombre = actual();
            match(TipoDeToken.ID_GEN);
            return new VariableDeTipo(nombre);
        } else {
            throw errorEsperando(PrimerosDeTipos.TIPO_BASE_TIPO_PRIMITIVO,
                    PrimerosDeTipos.TIPO_BASE_TIPO_REFERENCIA, PrimerosDeTipos.TIPO_BASE_ID_GEN);
        }
    }

    // DimensionesOpcionales -> [ ] DimensionesOpcionales | EPSILON
    public int dimensionesOpcionales() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.DIMENSIONES_OPCIONALES_CORCHETE_A)) {
            match(TipoDeToken.CORCHETE_A);
            match(TipoDeToken.CORCHETE_C);
            return 1 + dimensionesOpcionales();
        } else {
            // DimensionesOpcionales -> EPSILON
            return 0;
        }
    }

    // TipoReferencia -> idClase TipoGenericoOpcional
    public TipoReferencia tipoReferencia() throws ExcepcionLexica, ExcepcionSintactica {
        Token nombre = actual();
        match(TipoDeToken.ID_CLASE);
        return new TipoReferencia(nombre, tipoGenericoOpcional().orElse(null));
    }

    // TipoPrimitivo -> boolean | char | int
    // TipoPrimitivo -> float
    //
    // `float` es token del léxico desde la Etapa 1 pero no estaba en la gramática
    // de la cátedra; sin esta alternativa el logro de Etapa 1 queda inutilizable.
    public TipoPrimitivo tipoPrimitivo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_BOOLEAN)) {
            match(TipoDeToken.PR_BOOLEAN);
            return TipoPrimitivo.BOOLEAN;
        } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_CHAR)) {
            match(TipoDeToken.PR_CHAR);
            return TipoPrimitivo.CHAR;
        } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_INT)) {
            match(TipoDeToken.PR_INT);
            return TipoPrimitivo.INT;
        } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_FLOAT)) {
            match(TipoDeToken.PR_FLOAT);
            return TipoPrimitivo.FLOAT;
        } else {
            throw errorEsperando(PrimerosDeTipos.TIPO_PRIMITIVO_BOOLEAN,
                    PrimerosDeTipos.TIPO_PRIMITIVO_CHAR, PrimerosDeTipos.TIPO_PRIMITIVO_INT,
                    PrimerosDeTipos.TIPO_PRIMITIVO_FLOAT);
        }
    }

    // TipoGenericoOpcional -> < InstanciadoOParametrico > | EPSILON
    //
    // En POSICIÓN DE TIPO no se admite el diamante: `List<> x;` es inválido.
    public Optional<Tipo> tipoGenericoOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.TIPO_GENERICO_OPCIONAL_MENOR)) {
            match(TipoDeToken.OP_MENOR);
            Tipo argumento = instanciadoOParametrico();
            match(TipoDeToken.OP_MAYOR);
            return Optional.of(argumento);
        } else {
            // TipoGenericoOpcional -> EPSILON
            return Optional.empty();
        }
    }

    // InstanciadoOParametrico -> idGen
    // InstanciadoOParametrico -> idClase TipoGenericoOpcional
    //
    // La recursión por TipoGenericoOpcional es la que habilita el anidamiento
    // `List<List<Persona>>`.
    public Tipo instanciadoOParametrico() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.INSTANCIADO_OPARAMETRICO_ID_GEN)) {
            Token nombre = actual();
            match(TipoDeToken.ID_GEN);
            return new VariableDeTipo(nombre);
        } else if (estaEn(PrimerosDeTipos.INSTANCIADO_OPARAMETRICO_ID_CLASE)) {
            Token nombre = actual();
            match(TipoDeToken.ID_CLASE);
            return new TipoReferencia(nombre, tipoGenericoOpcional().orElse(null));
        } else {
            throw errorEsperando(PrimerosDeTipos.INSTANCIADO_OPARAMETRICO_ID_GEN,
                    PrimerosDeTipos.INSTANCIADO_OPARAMETRICO_ID_CLASE);
        }
    }

    // ArgsFormales -> ( ListaArgsFormalesOpcional )
    public void argsFormales() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);
        listaArgsFormalesOpcional();
        match(TipoDeToken.PAREN_C);
    }

    // ListaArgsFormalesOpcional -> ListaArgsFormales | EPSILON
    private void listaArgsFormalesOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.LISTA_ARGS_FORMALES_OPCIONAL_LISTA_ARGS_FORMALES)) {
            listaArgsFormales();
        } else {
            // ListaArgsFormalesOpcional -> EPSILON
        }
    }

    // ListaArgsFormales -> ArgFormal RestoListaArgsFormales
    private void listaArgsFormales() throws ExcepcionLexica, ExcepcionSintactica {
        argFormal();
        restoListaArgsFormales();
    }

    // RestoListaArgsFormales -> , ArgFormal RestoListaArgsFormales | EPSILON
    private void restoListaArgsFormales() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeTipos.RESTO_LISTA_ARGS_FORMALES_COMA)) {
            match(TipoDeToken.COMA);
            argFormal();
            restoListaArgsFormales();
        } else {
            // RestoListaArgsFormales -> EPSILON
        }
    }

    // ArgFormal -> Tipo idMetVar
    private void argFormal() throws ExcepcionLexica, ExcepcionSintactica {
        Tipo tipo = tipo();
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        TablaDeSimbolos.unidadActual().agregarParametro(nombre, tipo);
    }
}
