package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.Primeros;

// Las capas de expresión, de menor a mayor precedencia: la asignación, el
// ternario, los operadores binarios —en un solo nivel, como en la gramática
// original—, los unarios prefijos y el operando.
public final class AnalizadorDeExpresiones extends ParteDelAnalizador {

    public AnalizadorDeExpresiones(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Expresion -> ExpresionCondicional RestoExpresion
    public void expresion() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCondicional();
        restoExpresion();
    }

    // RestoExpresion -> OperadorAsignacion ExpresionCondicional | EPSILON
    //
    // El lado derecho es ExpresionCondicional y NO Expresion: la gramática
    // original pone ExpresionCompuesta ahí, o sea que `a = b = c` no es válido.
    private void restoExpresion() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.RESTO_EXPRESION_OPERADOR_ASIGNACION)) {
            entrada().match(TipoDeToken.OP_ASIGNACION);
            expresionCondicional();
        }
    }

    // ExpresionCondicional -> ExpresionCompuesta RestoCondicional
    public void expresionCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCompuesta();
        restoCondicional();
    }

    // RestoCondicional -> ? ExpresionCondicional : ExpresionCondicional   [+ternario]
    // RestoCondicional -> EPSILON
    //
    // La recursión sobre ExpresionCondicional en las dos ramas es lo que lo hace
    // asociativo a derecha, como en Java. Con el logro apagado esta capa
    // desaparece sin dejar rastro y el lenguaje base queda idéntico.
    private void restoCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.TERNARIO) && estaEn(Primeros.RESTO_CONDICIONAL_SIGNO_PREGUNTA)) {
            entrada().match(TipoDeToken.SIGNO_PREGUNTA);
            expresionCondicional();
            entrada().match(TipoDeToken.DOS_PUNTOS);
            expresionCondicional();
        }
    }

    // ExpresionCompuesta -> ExpresionBasica RestoExpresionCompuesta
    private void expresionCompuesta() throws ExcepcionLexica, ExcepcionSintactica {
        expresionBasica();
        restoExpresionCompuesta();
    }

    // RestoExpresionCompuesta -> OperadorBinario ExpresionBasica RestoExpresionCompuesta
    //                          | EPSILON
    private void restoExpresionCompuesta() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(Primeros.RESTO_EXPRESION_COMPUESTA_OPERADOR_BINARIO)) {
            entrada().matchDelActual();
            expresionBasica();
        }
    }

    // ExpresionBasica -> OperadorUnario Operando | Operando
    private void expresionBasica() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.EXPRESION_BASICA_OPERADOR_UNARIO)) {
            entrada().matchDelActual();
        }
        operando();
    }

    // Operando -> Primitivo | Referencia PosfijoOpcional
    private void operando() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.OPERANDO_PRIMITIVO)) {
            primitivo();
        } else if (estaEn(Primeros.OPERANDO_REFERENCIA)) {
            sintaxis.referencias().referencia();
            posfijoOpcional();
        } else {
            throw entrada().errorEsperando(Primeros.OPERANDO_PRIMITIVO,
                    Primeros.OPERANDO_REFERENCIA);
        }
    }

    // PosfijoOpcional -> ++ | --   [+posfijo]
    // PosfijoOpcional -> EPSILON
    //
    // Va sobre Referencia y no sobre Operando: `5++` no tiene sentido.
    public void posfijoOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (!hayLogro(Logro.POSFIJO)) {
            return;
        }
        if (estaEn(Primeros.POSFIJO_OPCIONAL_INCREMENTO)) {
            entrada().match(TipoDeToken.OP_INCREMENTO);
        } else if (estaEn(Primeros.POSFIJO_OPCIONAL_DECREMENTO)) {
            entrada().match(TipoDeToken.OP_DECREMENTO);
        }
    }

    // Primitivo -> true | false | intLiteral | charLiteral | null
    // Primitivo -> floatLiteral   [+float]
    private void primitivo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.PRIMITIVO_FLOAT_LITERAL) && !hayLogro(Logro.FLOATS)) {
            throw entrada().errorEsperando(Primeros.OPERANDO_PRIMITIVO);
        }
        entrada().matchDelActual();
    }

    // ExpresionSinIdClase -> OperadorUnario Operando ColaTrasOperando
    //                      | Primitivo ColaTrasOperando
    //                      | PrimarioSinIdClase ColaDeExpresionDesdePrimario
    //
    // Se usa donde una expresión compite con una declaración de variable local.
    // Sólo duplica las CABEZAS: todas las colas se comparten con la jerarquía
    // normal, así que no hay lógica repetida.
    public void expresionSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO)) {
            entrada().matchDelActual();
            operando();
            colaTrasOperando();
        } else if (estaEn(Primeros.EXPRESION_SIN_ID_CLASE_PRIMITIVO)) {
            primitivo();
            colaTrasOperando();
        } else if (estaEn(Primeros.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE)) {
            sintaxis.referencias().primarioSinIdClase();
            colaDeExpresionDesdePrimario();
        } else {
            throw entrada().errorEsperando(Primeros.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO,
                    Primeros.EXPRESION_SIN_ID_CLASE_PRIMITIVO,
                    Primeros.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE);
        }
    }

    // ColaTrasOperando -> RestoExpresionCompuesta RestoCondicional RestoExpresion
    private void colaTrasOperando() throws ExcepcionLexica, ExcepcionSintactica {
        restoExpresionCompuesta();
        restoCondicional();
        restoExpresion();
    }

    // ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando
    public void colaDeExpresionDesdePrimario() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.referencias().restoReferencia();
        posfijoOpcional();
        colaTrasOperando();
    }
}
