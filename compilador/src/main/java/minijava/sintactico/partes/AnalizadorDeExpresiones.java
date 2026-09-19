package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeExpresiones;

// Las capas de expresión, de menor a mayor precedencia: la asignación, el
// ternario, los operadores binarios —en un solo nivel, como en la gramática
// original—, los unarios prefijos y el operando.
public final class AnalizadorDeExpresiones extends ParteDelAnalizador {

    public AnalizadorDeExpresiones(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Expresion -> ExpresionCompuesta RestoExpresion
    //
    // El lado izquierdo del `=` es una ExpresionCompuesta, como en la gramática
    // original: `a ? b : c = d` no deriva.
    public void expresion() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCompuesta();
        restoExpresion();
    }

    // RestoExpresion -> OperadorAsignacion ExpresionCondicional
    // RestoExpresion -> RestoCondicional
    //
    // El lado derecho es ExpresionCondicional y NO Expresion: la gramática
    // original pone ExpresionCompuesta ahí, o sea que `a = b = c` no es válido.
    private void restoExpresion() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_OPERADOR_ASIGNACION)) {
            operadorAsignacion();
            expresionCondicional();
        } else if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_RESTO_CONDICIONAL)) {
            restoCondicional();
        } else {
            // RestoExpresion -> RestoCondicional
            // RestoCondicional -> EPSILON
        }
    }

    // OperadorAsignacion -> =
    private void operadorAsignacion() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.OP_ASIGNACION);
    }

    // ExpresionCondicional -> ExpresionCompuesta RestoCondicional
    //
    // Es el lado derecho de los cinco `=` y el tercer operando del ternario.
    public void expresionCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCompuesta();
        restoCondicional();
    }

    // RestoCondicional -> ? Expresion : ExpresionCondicional
    // RestoCondicional -> EPSILON
    //
    // La recursión sobre ExpresionCondicional en la rama derecha es lo que lo
    // hace asociativo a derecha. El operando del medio es una Expresion completa,
    // como en el JLS 15.25: `a ? b = 1 : c` es válido y `a ? b : c = d` no.
    private void restoCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_CONDICIONAL_SIGNO_PREGUNTA)) {
            match(TipoDeToken.SIGNO_PREGUNTA);
            expresion();
            match(TipoDeToken.DOS_PUNTOS);
            expresionCondicional();
        } else {
            // RestoCondicional -> EPSILON
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
        if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_COMPUESTA_OPERADOR_BINARIO)) {
            operadorBinario();
            expresionBasica();
            restoExpresionCompuesta();
        } else {
            // RestoExpresionCompuesta -> EPSILON
        }
    }

    // OperadorBinario -> || | && | == | != | < | > | <= | >= | + | - | * | / | %
    //
    // Trece producciones de un solo terminal, y una rama por cada una: es el
    // esquema de Clase 5 slide 60, donde el `match` nombra el terminal que
    // consume en vez de consumir «el actual, sea cual sea».
    private void operadorBinario() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_OR)) {
            match(TipoDeToken.OP_OR);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_AND)) {
            match(TipoDeToken.OP_AND);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_IGUAL)) {
            match(TipoDeToken.OP_IGUAL);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_DISTINTO)) {
            match(TipoDeToken.OP_DISTINTO);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_MENOR)) {
            match(TipoDeToken.OP_MENOR);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_MAYOR)) {
            match(TipoDeToken.OP_MAYOR);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_MENOR_IGUAL)) {
            match(TipoDeToken.OP_MENOR_IGUAL);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_MAYOR_IGUAL)) {
            match(TipoDeToken.OP_MAYOR_IGUAL);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_SUMA)) {
            match(TipoDeToken.OP_SUMA);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_RESTA)) {
            match(TipoDeToken.OP_RESTA);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_MULT)) {
            match(TipoDeToken.OP_MULT);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_DIV)) {
            match(TipoDeToken.OP_DIV);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_BINARIO_MODULO)) {
            match(TipoDeToken.OP_MODULO);
        } else {
            throw errorEsperando(PrimerosDeExpresiones.RESTO_EXPRESION_COMPUESTA_OPERADOR_BINARIO);
        }
    }

    // OperadorUnario -> + | - | !
    private void operadorUnario() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.OPERADOR_UNARIO_SUMA)) {
            match(TipoDeToken.OP_SUMA);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_UNARIO_RESTA)) {
            match(TipoDeToken.OP_RESTA);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_UNARIO_NEGACION)) {
            match(TipoDeToken.OP_NEGACION);
        } else {
            throw errorEsperando(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO);
        }
    }

    // ExpresionBasica -> OperadorUnario Operando | Operando
    private void expresionBasica() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO)) {
            operadorUnario();
            operando();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_BASICA_OPERANDO)) {
            operando();
        } else {
            throw errorEsperando(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO,
                    PrimerosDeExpresiones.EXPRESION_BASICA_OPERANDO);
        }
    }

    // Operando -> Primitivo | Referencia PosfijoOpcional
    private void operando() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.OPERANDO_PRIMITIVO)) {
            primitivo();
        } else if (estaEn(PrimerosDeExpresiones.OPERANDO_REFERENCIA)) {
            sintaxis.referencias().referencia();
            posfijoOpcional();
        } else {
            throw errorEsperando(PrimerosDeExpresiones.OPERANDO_PRIMITIVO,
                    PrimerosDeExpresiones.OPERANDO_REFERENCIA);
        }
    }

    // PosfijoOpcional -> ++ | --
    // PosfijoOpcional -> EPSILON
    //
    // Va sobre Referencia: un Primitivo es un literal y nunca una variable. Que
    // la Referencia sea una variable lo verifica el semántico, como javac.
    public void posfijoOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.POSFIJO_OPCIONAL_INCREMENTO)) {
            match(TipoDeToken.OP_INCREMENTO);
        } else if (estaEn(PrimerosDeExpresiones.POSFIJO_OPCIONAL_DECREMENTO)) {
            match(TipoDeToken.OP_DECREMENTO);
        } else {
            // PosfijoOpcional -> EPSILON
        }
    }

    // Primitivo -> true | false | intLiteral | charLiteral | null
    // Primitivo -> floatLiteral
    private void primitivo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.PRIMITIVO_TRUE)) {
            match(TipoDeToken.PR_TRUE);
        } else if (estaEn(PrimerosDeExpresiones.PRIMITIVO_FALSE)) {
            match(TipoDeToken.PR_FALSE);
        } else if (estaEn(PrimerosDeExpresiones.PRIMITIVO_INT_LITERAL)) {
            match(TipoDeToken.INT_LITERAL);
        } else if (estaEn(PrimerosDeExpresiones.PRIMITIVO_CHAR_LITERAL)) {
            match(TipoDeToken.CHAR_LITERAL);
        } else if (estaEn(PrimerosDeExpresiones.PRIMITIVO_NULL)) {
            match(TipoDeToken.PR_NULL);
        } else if (estaEn(PrimerosDeExpresiones.PRIMITIVO_FLOAT_LITERAL)) {
            match(TipoDeToken.FLOAT_LITERAL);
        } else {
            throw errorEsperando(PrimerosDeExpresiones.OPERANDO_PRIMITIVO);
        }
    }

    // ExpresionSinIdClase -> OperadorUnario Operando ColaTrasOperando
    //                      | Primitivo ColaTrasOperando
    //                      | PrimarioSinIdClase ColaDeExpresionDesdePrimario
    //
    // Se usa donde una expresión compite con una declaración de variable local.
    // Sólo duplica las CABEZAS: todas las colas se comparten con la jerarquía
    // normal, así que no hay lógica repetida.
    public void expresionSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO)) {
            operadorUnario();
            operando();
            colaTrasOperando();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMITIVO)) {
            primitivo();
            colaTrasOperando();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE)) {
            sintaxis.referencias().primarioSinIdClase();
            colaDeExpresionDesdePrimario();
        } else {
            throw errorEsperando(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMITIVO,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE);
        }
    }

    // ColaTrasOperando -> RestoExpresionCompuesta RestoExpresion
    private void colaTrasOperando() throws ExcepcionLexica, ExcepcionSintactica {
        restoExpresionCompuesta();
        restoExpresion();
    }

    // ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando
    public void colaDeExpresionDesdePrimario() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.referencias().restoReferencia();
        posfijoOpcional();
        colaTrasOperando();
    }
}
