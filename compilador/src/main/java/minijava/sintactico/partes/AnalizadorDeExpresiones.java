package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.expresiones.ExpresionNoControlada;
import minijava.semantico.ast.expresiones.NodoAsignacion;
import minijava.semantico.ast.expresiones.NodoBinario;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.ast.expresiones.NodoLiteral;
import minijava.semantico.ast.expresiones.NodoUnario;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeExpresiones;

// Las capas de expresión, de menor a mayor precedencia: la asignación, el
// ternario, los operadores binarios —en un solo nivel, como en la gramática
// original—, los unarios prefijos y el operando.
//
// Cada método devuelve el nodo de lo que reconoció. Los «Resto» reciben como
// atributo heredado el nodo ya armado a su izquierda y lo completan (Clase 11,
// slide 21). El ternario y los posfijos son de logros que no se controlan: se
// reconocen enteros y quedan como ExpresionNoControlada.
public final class AnalizadorDeExpresiones extends ParteDelAnalizador {

    public AnalizadorDeExpresiones(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Expresion -> ExpresionCompuesta RestoExpresion
    //
    // El lado izquierdo del `=` es una ExpresionCompuesta, como en la gramática
    // original: `a ? b : c = d` no deriva.
    public NodoExpresion expresion() throws ExcepcionLexica, ExcepcionSintactica {
        return restoExpresion(expresionCompuesta());
    }

    // RestoExpresion -> OperadorAsignacion ExpresionCondicional
    // RestoExpresion -> RestoCondicional
    //
    // El lado derecho es ExpresionCondicional y NO Expresion: la gramática
    // original pone ExpresionCompuesta ahí, o sea que `a = b = c` no es válido.
    private NodoExpresion restoExpresion(NodoExpresion izquierda) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_OPERADOR_ASIGNACION)) {
            Token igual = operadorAsignacion();
            return new NodoAsignacion(izquierda, igual, expresionCondicional());
        } else if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_RESTO_CONDICIONAL)) {
            return restoCondicional(izquierda);
        } else {
            // RestoExpresion -> RestoCondicional
            // RestoCondicional -> EPSILON
            return izquierda;
        }
    }

    // OperadorAsignacion -> =
    private Token operadorAsignacion() throws ExcepcionLexica, ExcepcionSintactica {
        Token igual = actual();
        match(TipoDeToken.OP_ASIGNACION);
        return igual;
    }

    // ExpresionCondicional -> ExpresionCompuesta RestoCondicional
    //
    // Es el lado derecho de los cinco `=` y el tercer operando del ternario.
    public NodoExpresion expresionCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        return restoCondicional(expresionCompuesta());
    }

    // RestoCondicional -> ? Expresion : ExpresionCondicional
    // RestoCondicional -> EPSILON
    //
    // La recursión sobre ExpresionCondicional en la rama derecha es lo que lo
    // hace asociativo a derecha. El operando del medio es una Expresion completa,
    // como en el JLS 15.25: `a ? b = 1 : c` es válido y `a ? b : c = d` no.
    private NodoExpresion restoCondicional(NodoExpresion condicion) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_CONDICIONAL_SIGNO_PREGUNTA)) {
            Token pregunta = actual();
            match(TipoDeToken.SIGNO_PREGUNTA);
            expresion();
            match(TipoDeToken.DOS_PUNTOS);
            expresionCondicional();
            return new ExpresionNoControlada(pregunta);
        } else {
            // RestoCondicional -> EPSILON
            return condicion;
        }
    }

    // ExpresionCompuesta -> ExpresionBasica RestoExpresionCompuesta
    private NodoExpresion expresionCompuesta() throws ExcepcionLexica, ExcepcionSintactica {
        return restoExpresionCompuesta(expresionBasica());
    }

    // RestoExpresionCompuesta -> OperadorBinario ExpresionBasica RestoExpresionCompuesta
    //                          | EPSILON
    //
    // Es el único lugar que decide cómo se agrupan los binarios. Documento §11, 2.º
    // párrafo: el semántico no introduce la precedencia de Java; los trece
    // operadores van en un mismo nivel, como en la regla original
    // `ExpresionCompuesta -> ExpresionCompuesta OperadorBinario ExpresionBasica`,
    // que asocia a izquierda: `a - b + c` es `(a - b) + c`. La recursión a
    // izquierda se recupera pasando el nodo armado hasta acá (Clase 11, slide 21).
    private NodoExpresion restoExpresionCompuesta(NodoExpresion izquierda)
            throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_COMPUESTA_OPERADOR_BINARIO)) {
            Token operador = operadorBinario();
            NodoExpresion derecha = expresionBasica();
            return restoExpresionCompuesta(new NodoBinario(izquierda, operador, derecha));
        } else {
            // RestoExpresionCompuesta -> EPSILON
            return izquierda;
        }
    }

    // OperadorBinario -> || | && | == | != | < | > | <= | >= | + | - | * | / | %
    //
    // Trece producciones de un solo terminal, y una rama por cada una: es el
    // esquema de Clase 5 slide 60, donde el `match` nombra el terminal que
    // consume en vez de consumir «el actual, sea cual sea».
    private Token operadorBinario() throws ExcepcionLexica, ExcepcionSintactica {
        Token operador = actual();
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
        return operador;
    }

    // OperadorUnario -> + | - | !
    private Token operadorUnario() throws ExcepcionLexica, ExcepcionSintactica {
        Token operador = actual();
        if (estaEn(PrimerosDeExpresiones.OPERADOR_UNARIO_SUMA)) {
            match(TipoDeToken.OP_SUMA);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_UNARIO_RESTA)) {
            match(TipoDeToken.OP_RESTA);
        } else if (estaEn(PrimerosDeExpresiones.OPERADOR_UNARIO_NEGACION)) {
            match(TipoDeToken.OP_NEGACION);
        } else {
            throw errorEsperando(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO);
        }
        return operador;
    }

    // ExpresionBasica -> OperadorUnario Operando | Operando
    private NodoExpresion expresionBasica() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO)) {
            Token operador = operadorUnario();
            return new NodoUnario(operador, operando());
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_BASICA_OPERANDO)) {
            return operando();
        } else {
            throw errorEsperando(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO,
                    PrimerosDeExpresiones.EXPRESION_BASICA_OPERANDO);
        }
    }

    // Operando -> Primitivo | Referencia PosfijoOpcional
    private NodoExpresion operando() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.OPERANDO_PRIMITIVO)) {
            return primitivo();
        } else if (estaEn(PrimerosDeExpresiones.OPERANDO_REFERENCIA)) {
            return posfijoOpcional(sintaxis.referencias().referencia());
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
    private NodoExpresion posfijoOpcional(NodoExpresion referencia) throws ExcepcionLexica, ExcepcionSintactica {
        Token posfijo = actual();
        if (estaEn(PrimerosDeExpresiones.POSFIJO_OPCIONAL_INCREMENTO)) {
            match(TipoDeToken.OP_INCREMENTO);
            return new ExpresionNoControlada(posfijo);
        } else if (estaEn(PrimerosDeExpresiones.POSFIJO_OPCIONAL_DECREMENTO)) {
            match(TipoDeToken.OP_DECREMENTO);
            return new ExpresionNoControlada(posfijo);
        } else {
            // PosfijoOpcional -> EPSILON
            return referencia;
        }
    }

    // Primitivo -> true | false | intLiteral | charLiteral | null
    // Primitivo -> floatLiteral
    private NodoLiteral primitivo() throws ExcepcionLexica, ExcepcionSintactica {
        Token literal = actual();
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
        return new NodoLiteral(literal);
    }

    // ExpresionSinIdClase -> OperadorUnario Operando ColaTrasOperando
    //                      | Primitivo ColaTrasOperando
    //                      | PrimarioSinIdClase ColaDeExpresionDesdePrimario
    //
    // Se usa donde una expresión compite con una declaración de variable local.
    // Sólo duplica las CABEZAS: todas las colas se comparten con la jerarquía
    // normal, así que no hay lógica repetida.
    public NodoExpresion expresionSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO)) {
            Token operador = operadorUnario();
            return colaTrasOperando(new NodoUnario(operador, operando()));
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMITIVO)) {
            return colaTrasOperando(primitivo());
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE)) {
            return colaDeExpresionDesdePrimario(sintaxis.referencias().primarioSinIdClase());
        } else {
            throw errorEsperando(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMITIVO,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE);
        }
    }

    // ColaTrasOperando -> RestoExpresionCompuesta RestoExpresion
    private NodoExpresion colaTrasOperando(NodoExpresion operando) throws ExcepcionLexica, ExcepcionSintactica {
        return restoExpresion(restoExpresionCompuesta(operando));
    }

    // ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando
    public NodoExpresion colaDeExpresionDesdePrimario(NodoExpresion primario)
            throws ExcepcionLexica, ExcepcionSintactica {
        return colaTrasOperando(posfijoOpcional(sintaxis.referencias().restoReferencia(primario)));
    }
}
