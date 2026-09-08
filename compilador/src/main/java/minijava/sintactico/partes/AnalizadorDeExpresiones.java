package minijava.sintactico.partes;

import minijava.comun.config.Logro;
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

    // Expresion -> ExpresionCondicional RestoExpresion
    public void expresion() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCondicional();
        restoExpresion();
    }

    // RestoExpresion -> OperadorAsignacion ExpresionCondicional | EPSILON
    // OperadorAsignacion -> =
    //
    // El lado derecho es ExpresionCondicional y NO Expresion: la gramática
    // original pone ExpresionCompuesta ahí, o sea que `a = b = c` no es válido.
    private void restoExpresion() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_OPERADOR_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            expresionCondicional();
        }
    }

    // ExpresionCondicional -> ExpresionCompuesta RestoCondicional
    public void expresionCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        expresionCompuesta();
        restoCondicional();
    }

    // RestoCondicional -> ? Expresion : ExpresionCondicional   [+ternario]
    // RestoCondicional -> EPSILON
    //
    // La recursión sobre ExpresionCondicional en la rama derecha es lo que lo
    // hace asociativo a derecha. El operando del medio es una Expresion completa,
    // como en el JLS 15.25: `a ? b = 1 : c` es válido y `a ? b : c = d` no.
    // Con el logro apagado esta capa desaparece sin dejar rastro.
    private void restoCondicional() throws ExcepcionLexica, ExcepcionSintactica {
        if (hayLogro(Logro.TERNARIO) && estaEn(PrimerosDeExpresiones.RESTO_CONDICIONAL_SIGNO_PREGUNTA)) {
            match(TipoDeToken.SIGNO_PREGUNTA);
            expresion();
            match(TipoDeToken.DOS_PUNTOS);
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
        }
    }

    // Primitivo -> true | false | intLiteral | charLiteral | null
    // Primitivo -> floatLiteral   [+float]
    private void primitivo() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeExpresiones.PRIMITIVO_FLOAT_LITERAL) && !hayLogro(Logro.FLOATS)) {
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
