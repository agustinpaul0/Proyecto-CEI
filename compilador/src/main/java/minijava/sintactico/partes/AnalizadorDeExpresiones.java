package minijava.sintactico.partes;

import minijava.comun.config.Logro;
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
// La lambda NO es una de estas capas ni un Primario: es una alternativa del
// nivel más alto, como en el JLS («Expression: LambdaExpression |
// AssignmentExpression»). Por eso `->` no compite en precedencia con ningún
// operador, y `1 + x -> x`, `- x -> x` y `a == x -> x` son errores, igual que en
// Java. Las dos cabezas que comparte con una expresión normal, `idMetVar` y `(`,
// se factorizan en expresionDesdePrimarioOLambda().
public final class AnalizadorDeExpresiones extends ParteDelAnalizador {

    public AnalizadorDeExpresiones(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Expresion -> ExpresionCondicionalOLambda RestoExpresion
    public void expresion() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCondicionalOLambda();
        restoExpresion();
    }

    // ExpresionSinLambda -> ExpresionCondicional RestoExpresion
    //
    // La Expresion de la gramática original: la que NO admite una lambda en la
    // raíz. Va en los dos lugares donde Java tampoco la admite: adentro de una
    // parentizada que después va a ser operando, y como sentencia-expresión
    // —`x -> x;` es «not a statement» en Java—.
    public void expresionSinLambda() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCondicional();
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
            // Con un `=` en la raíz la expresión es una asignación, no la lambda
            // que acaba de parsearse como lado derecho: `(a = x -> x).m()` es
            // legal en Java. Ver RestriccionesDeLambda.
            sintaxis.restriccionesDeLambda().anotarQueNoEsLambda();
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
        sintaxis.restriccionesDeLambda().anotarQueNoEsLambda();
        if (estaEn(PrimerosDeExpresiones.EXPRESION_CONDICIONAL_OLAMBDA_OPERADOR_UNARIO)) {
            matchDelActual();
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
            sintaxis.restriccionesDeLambda().verificarThisExplicito(actual());
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

    // ExpresionCondicional -> ExpresionCompuesta RestoCondicional
    public void expresionCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCompuesta();
        restoCondicional();
    }

    // RestoCondicional -> ? Expresion : ExpresionCondicionalOLambda   [+ternario]
    // RestoCondicional -> EPSILON
    //
    // La recursión sobre ExpresionCondicional en la rama derecha es lo que lo
    // hace asociativo a derecha. El operando del medio es una Expresion completa,
    // como en el JLS 15.25: `a ? b = 1 : c` es válido, `a ? b : c = d` no, y
    // `a ? b : x -> x` sí —la producción del JLS es `? Expression : LambdaExpression`—.
    // Con el logro apagado esta capa desaparece sin dejar rastro.
    private void restoCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.TERNARIO) && estaEn(PrimerosDeExpresiones.RESTO_CONDICIONAL_SIGNO_PREGUNTA)) {
            match(TipoDeToken.SIGNO_PREGUNTA);
            expresion();
            match(TipoDeToken.DOS_PUNTOS);
            expresionCondicionalOLambda();
            // Ídem: con un `?` en la raíz la expresión es un ternario, aunque una
            // de sus ramas sea una lambda.
            sintaxis.restriccionesDeLambda().anotarQueNoEsLambda();
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
    // OperadorBinario -> || | && | == | != | < | > | <= | >= | + | - | * | / | %
    //
    // La recursión a derecha se traduce en un while y no en una llamada, para que
    // una cadena larguísima de operadores no dependa de la pila de la JVM. Es la
    // única libertad que se toma respecto del esquema de Clase 5 slide 59, y no
    // cambia qué se reconoce: el bucle deriva exactamente la misma cadena.
    private void restoExpresionCompuesta() throws ExcepcionLexica, ExcepcionSintactica {
        while (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_COMPUESTA_OPERADOR_BINARIO)) {
            matchDelActual();
            expresionBasica();
        }
    }

    // ExpresionBasica -> OperadorUnario Operando | Operando
    // OperadorUnario -> + | - | !
    private void expresionBasica() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.EXPRESION_BASICA_OPERADOR_UNARIO)) {
            matchDelActual();
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

    // PosfijoOpcional -> ++ | --   [+posfijo]
    // PosfijoOpcional -> EPSILON
    //
    // Va sobre Referencia y no sobre Operando: `5++` no tiene sentido.
    public void posfijoOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (!hayLogro(Logro.POSFIJO)) {
            return;
        }
        if (estaEn(PrimerosDeExpresiones.POSFIJO_OPCIONAL_INCREMENTO)) {
            match(TipoDeToken.OP_INCREMENTO);
        } else if (estaEn(PrimerosDeExpresiones.POSFIJO_OPCIONAL_DECREMENTO)) {
            match(TipoDeToken.OP_DECREMENTO);
        } else {
            // PosfijoOpcional -> EPSILON
        }
    }

    // Primitivo -> true | false | intLiteral | charLiteral | null
    // Primitivo -> floatLiteral   [+float]
    private void primitivo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.PRIMITIVO_FLOAT_LITERAL) && !hayLogro(Logro.FLOAT)) {
            throw errorEsperando(PrimerosDeExpresiones.OPERANDO_PRIMITIVO);
        }
        matchDelActual();
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
            matchDelActual();
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
