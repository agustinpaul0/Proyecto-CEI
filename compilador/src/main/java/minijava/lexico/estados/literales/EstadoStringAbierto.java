package minijava.lexico.estados.literales;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

// Terminal: se cierra con la comilla doble. Aspectos Léxicos §2.3.3 no da una
// producción para el string, da una regla POSICIONAL: "la comilla doble que
// marca el final de un string correcto nunca estará antecedida por [\]". O sea
// un lookbehind de un carácter, y este par de estados es ese bit de memoria —
// acá el anterior NO fue barra, en EstadoStringTrasBarra sí.
// Un salto de línea antes del cierre es error, también por §2.3.3.
public final class EstadoStringAbierto implements EstadoLexico {

    public static final EstadoStringAbierto INSTANCIA = new EstadoStringAbierto();

    private EstadoStringAbierto() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();
        if (contexto.esFinDeArchivo() || actual == '\n') {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.STRING_SIN_CERRAR,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        if (actual == '"') {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return new Emitir(new Token(TipoDeToken.LIT_STRING, contexto.getLexema(), linea));
        }
        contexto.avanzar();
        return new Continuar(actual == '\\' ? EstadoStringTrasBarra.INSTANCIA : INSTANCIA);
    }
}
