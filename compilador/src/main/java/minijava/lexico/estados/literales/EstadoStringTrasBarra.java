package minijava.lexico.estados.literales;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;

// El carácter anterior fue una barra invertida. NO hay lista de escapes válidos:
// Aspectos Léxicos §2.3.3 sólo dice que una comilla antecedida por barra no
// cierra el literal, así que "hola \q" es un string común y corriente.
//
// Dos consecuencias de que la regla sea posicional y no de apareamiento:
//  - una barra tras otra barra deja el estado como está, porque la comilla que
//    venga después sigue estando antecedida por una barra: "a\\" no cierra;
//  - por eso mismo un string no puede terminar con la secuencia \", y ahí es
//    donde diverge de Java (documentacion/lexico/decisiones-de-logica.md §7).
//
// La única excepción es el escape Unicode, que sí se valida: es un logro, y su
// criterio quedó confirmado por el profesor.
public final class EstadoStringTrasBarra implements EstadoLexico {

    public static final EstadoStringTrasBarra INSTANCIA = new EstadoStringTrasBarra();

    private EstadoStringTrasBarra() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        // Aparte, para no meter el '\n' crudo en el lexema.
        if (contexto.esFinDeArchivo() || contexto.getCaracterActual() == '\n') {
            throw contexto.crearExcepcionLexica(TipoDeErrorLexico.STRING_SIN_CERRAR,
                    AnclaDeError.INICIO_DEL_LEXEMA);
        }
        char actual = contexto.getCaracterActual();
        if (actual == 'u') {
            contexto.avanzar();
            return new Continuar(new EstadoMarcadorUnicode(EstadoStringAbierto.INSTANCIA, true));
        }
        contexto.avanzar();
        return new Continuar(actual == '\\' ? INSTANCIA : EstadoStringAbierto.INSTANCIA);
    }
}
