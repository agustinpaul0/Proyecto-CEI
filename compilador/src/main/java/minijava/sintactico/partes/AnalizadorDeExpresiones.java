package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeExpresiones;

// Las capas de expresión, de menor a mayor precedencia: la asignación, el
// ternario, los operadores binarios —en un solo nivel, como en la gramática
// original—, los unarios prefijos y el operando.
//
// La lambda NO es una de estas capas: es una alternativa del nivel más alto,
// como en el JLS («Expression: LambdaExpression | AssignmentExpression»). Por eso
// `->` no compite en precedencia con ningún operador, y `1 + x -> x`,
// `- x -> x` y `a == x -> x` son errores, igual que en Java. Entre paréntesis sí
// llega a ser operando, porque ahí entra por Primario: ver Parentizada en
// AnalizadorDeReferencias. Las dos cabezas que comparte con una expresión
// normal, `idMetVar` y `(`, se factorizan en expresionDesdePrimarioOLambda().
public final class AnalizadorDeExpresiones extends ParteDelAnalizador {

    public AnalizadorDeExpresiones(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Expresion -> ExpresionCondicionalOLambda RestoExpresion
    public void expresion() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCondicionalOLambda();
        restoExpresion();
    }

    // RestoExpresion -> OperadorAsignacion ExpresionCondicionalOLambda | EPSILON
    // OperadorAsignacion -> =
    //
    // El lado derecho es ExpresionCondicionalOLambda y NO Expresion: la gramática
    // original pone ExpresionCompuesta ahí, o sea que `a = b = c` no es válido.
    // Admite lambda porque el JLS define `Assignment: LeftHandSide = Expression`.
    private void restoExpresion() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_OPERADOR_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            expresionCondicionalOLambda();
        } else {
            // RestoExpresion -> EPSILON
        }
    }

    // ExpresionCondicionalOLambda -> OperadorUnario Operando ColaTrasOperandoSinAsignacion
    //                              | Primitivo ColaTrasOperandoSinAsignacion
    //                              | ExpresionDesdePrimarioOLambda
    //
    // El nivel del JLS `LambdaExpression | ConditionalExpression`: admite lambda
    // y no admite `=` en la raíz. Es el operando de los seis `=` y el tercer
    // operando del ternario. Sólo duplica las CABEZAS; las colas se comparten.
    public void expresionCondicionalOLambda() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_CONDICIONAL_OLAMBDA_OPERADOR_UNARIO)) {
            operadorUnario();
            operando();
            colaTrasOperandoSinAsignacion();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_CONDICIONAL_OLAMBDA_PRIMITIVO)) {
            primitivo();
            colaTrasOperandoSinAsignacion();
        } else {
            expresionDesdePrimarioOLambda();
        }
    }

    // ExpresionDesdePrimarioOLambda -> idClase . idMetVar ArgsActuales ColaDesdePrimarioSinAsignacion
    //                                | this ColaDesdePrimarioSinAsignacion
    //                                | stringLiteral ColaDesdePrimarioSinAsignacion
    //                                | Creacion ColaDesdePrimarioSinAsignacion
    //                                | idMetVar AccesoLlamadaOLambda
    //                                | ParentizadaOLambda
    //
    // Las cuatro primeras cabezas no pueden empezar una lambda y delegan en la
    // jerarquía de siempre. Las dos últimas, `idMetVar` y `(`, son las que una
    // lambda comparte con una expresión normal: ahí está todo el trabajo de
    // factorización del logro.
    private void expresionDesdePrimarioOLambda() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_DESDE_PRIMARIO_OLAMBDA_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            match(TipoDeToken.PUNTO);
            match(TipoDeToken.ID_MV);
            sintaxis.argumentos().argsActuales();
            colaDesdePrimarioSinAsignacion();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_DESDE_PRIMARIO_OLAMBDA_THIS)) {
            match(TipoDeToken.PR_THIS);
            colaDesdePrimarioSinAsignacion();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_DESDE_PRIMARIO_OLAMBDA_STRING_LITERAL)) {
            match(TipoDeToken.LIT_STRING);
            colaDesdePrimarioSinAsignacion();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_DESDE_PRIMARIO_OLAMBDA_CREACION)) {
            sintaxis.creacion().creacion();
            colaDesdePrimarioSinAsignacion();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_DESDE_PRIMARIO_OLAMBDA_ID_MET_VAR)) {
            Token nombre = actual();
            match(TipoDeToken.ID_MV);
            sintaxis.lambdas().accesoLlamadaOLambda(nombre);
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_DESDE_PRIMARIO_OLAMBDA_PARENTIZADA_OLAMBDA)) {
            sintaxis.lambdas().parentizadaOLambda();
        } else {
            throw errorEsperando(
                    PrimerosDeExpresiones.EXPRESION_CONDICIONAL_OLAMBDA_OPERADOR_UNARIO,
                    PrimerosDeExpresiones.EXPRESION_CONDICIONAL_OLAMBDA_PRIMITIVO,
                    PrimerosDeExpresiones.EXPRESION_CONDICIONAL_OLAMBDA_EXPRESION_DESDE_PRIMARIO_OLAMBDA);
        }
    }

    // RestoCondicional -> ? Expresion : ExpresionCondicionalOLambda
    // RestoCondicional -> EPSILON
    //
    // La recursión sobre ExpresionCondicional en la rama derecha es lo que lo
    // hace asociativo a derecha. El operando del medio es una Expresion completa,
    // como en el JLS 15.25: `a ? b = 1 : c` es válido, `a ? b : c = d` no, y
    // `a ? b : x -> x` sí —la producción del JLS es `? Expression : LambdaExpression`—.
    private void restoCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_CONDICIONAL_SIGNO_PREGUNTA)) {
            match(TipoDeToken.SIGNO_PREGUNTA);
            expresion();
            match(TipoDeToken.DOS_PUNTOS);
            expresionCondicionalOLambda();
        } else {
            // RestoCondicional -> EPSILON
        }
    }

    // RestoExpresionCompuesta -> OperadorBinario ExpresionBasica RestoExpresionCompuesta
    //                          | EPSILON
    // OperadorBinario -> || | && | == | != | < | > | <= | >= | + | - | * | / | %
    //
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
    // OperadorUnario -> + | - | !
    private void expresionBasica() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO)) {
            operadorUnario();
        }
        operando();
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
    // Va sobre Referencia y no sobre Operando: `5++` no tiene sentido.
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
    //                      | PrimarioSinIdClaseNiLambda ColaDeExpresionDesdePrimario
    //                      | idMetVar AccesoLlamadaOLambda RestoExpresion
    //                      | ParentizadaOLambda RestoExpresion
    //
    // Se usa donde una expresión compite con una declaración de variable local.
    // Sólo duplica las CABEZAS: todas las colas se comparten con la jerarquía
    // normal, así que no hay lógica repetida.
    //
    // Las dos últimas son las mismas cabezas ambiguas que factoriza
    // expresionDesdePrimarioOLambda(), `idMetVar` y `(`, y por eso `p -> x;` es
    // una sentencia válida. El `restoExpresion()` de atrás es lo único que las
    // distingue de allá: acá la expresión sí lleva el `=` de la asignación.
    public void expresionSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO)) {
            operadorUnario();
            operando();
            colaTrasOperando();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMITIVO)) {
            primitivo();
            colaTrasOperando();
        } else if (estaEn(
                PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE_NI_LAMBDA)) {
            sintaxis.referencias().primarioSinIdClaseNiLambda();
            colaDeExpresionDesdePrimario();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_ID_MET_VAR)) {
            Token nombre = actual();
            match(TipoDeToken.ID_MV);
            sintaxis.lambdas().accesoLlamadaOLambda(nombre);
            restoExpresion();
        } else if (estaEn(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PARENTIZADA_OLAMBDA)) {
            sintaxis.lambdas().parentizadaOLambda();
            restoExpresion();
        } else {
            throw errorEsperando(PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_OPERADOR_UNARIO,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMITIVO,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PRIMARIO_SIN_ID_CLASE_NI_LAMBDA,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_ID_MET_VAR,
                    PrimerosDeExpresiones.EXPRESION_SIN_ID_CLASE_PARENTIZADA_OLAMBDA);
        }
    }

    // ColaTrasOperando -> ColaTrasOperandoSinAsignacion RestoExpresion
    private void colaTrasOperando() throws ExcepcionLexica, ExcepcionSintactica {
        colaTrasOperandoSinAsignacion();
        restoExpresion();
    }

    // ColaTrasOperandoSinAsignacion -> RestoExpresionCompuesta RestoCondicional
    //
    // La cola se parte en dos para que ExpresionCondicionalOLambda pueda reusarla
    // sin arrastrar el `=`, que en ese nivel no corresponde.
    private void colaTrasOperandoSinAsignacion() throws ExcepcionLexica, ExcepcionSintactica {
        restoExpresionCompuesta();
        restoCondicional();
    }

    // ColaDeExpresionDesdePrimario -> RestoReferencia PosfijoOpcional ColaTrasOperando
    public void colaDeExpresionDesdePrimario() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.referencias().restoReferencia();
        posfijoOpcional();
        colaTrasOperando();
    }

    // ColaDesdePrimarioSinAsignacion -> RestoReferencia PosfijoOpcional
    //                                   ColaTrasOperandoSinAsignacion
    public void colaDesdePrimarioSinAsignacion() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.referencias().restoReferencia();
        posfijoOpcional();
        colaTrasOperandoSinAsignacion();
    }
}
