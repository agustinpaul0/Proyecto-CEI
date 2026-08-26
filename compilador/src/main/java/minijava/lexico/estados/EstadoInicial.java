package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

import java.util.Map;

public final class EstadoInicial implements EstadoLexico {

    public static final EstadoInicial INSTANCIA = new EstadoInicial();

    private static final Map<Character, TipoDeToken> PUNTUACION = Map.of(
            '(', TipoDeToken.PAREN_A, ')', TipoDeToken.PAREN_C,
            '{', TipoDeToken.LLAVE_A, '}', TipoDeToken.LLAVE_C,
            '[', TipoDeToken.CORCHETE_A, ']', TipoDeToken.CORCHETE_C,
            ';', TipoDeToken.PUNTO_Y_COMA, ',', TipoDeToken.COMA,
            '.', TipoDeToken.PUNTO, ':', TipoDeToken.DOS_PUNTOS);

    private static final Map<Character, EstadoOperadorConSufijoOpcional> OPERADORES_CON_SUFIJO = Map.of(
            '>', EstadoOperadorConSufijoOpcional.MAYOR,
            '<', EstadoOperadorConSufijoOpcional.MENOR,
            '!', EstadoOperadorConSufijoOpcional.NEGACION,
            '=', EstadoOperadorConSufijoOpcional.ASIGNACION,
            '+', EstadoOperadorConSufijoOpcional.SUMA,
            '-', EstadoOperadorConSufijoOpcional.RESTA);

    private static final Map<Character, EstadoOperadorDoble> OPERADORES_DOBLES = Map.of(
            '&', EstadoOperadorDoble.AND,
            '|', EstadoOperadorDoble.OR);

    private static final Map<Character, TipoDeToken> SIMBOLOS_SIMPLES = Map.of(
            '%', TipoDeToken.OP_MODULO,
            '*', TipoDeToken.OP_MULT);

    private EstadoInicial() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();

        if (contexto.esFinDeArchivo()) {
            return new Emitir(new Token(TipoDeToken.EOF, "$", contexto.getLinea()));
        }
        if (esBlanco(actual)) {
            contexto.descartar();
            return new Continuar(INSTANCIA);
        }
        if (PUNTUACION.containsKey(actual)) {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return new Emitir(new Token(PUNTUACION.get(actual), contexto.getLexema(), linea));
        }
        if (Character.isLowerCase(actual)) {
            contexto.avanzar();
            return new Continuar(EstadoIdMV.INSTANCIA);
        }
        if (Character.isUpperCase(actual)) {
            contexto.avanzar();
            return new Continuar(EstadoIdGenOClase.INSTANCIA);
        }
        if (Character.isDigit(actual)) {
            contexto.avanzar();
            return new Continuar(EstadoEntero.INSTANCIA);
        }
        if (OPERADORES_CON_SUFIJO.containsKey(actual)) {
            contexto.avanzar();
            return new Continuar(OPERADORES_CON_SUFIJO.get(actual));
        }
        if (OPERADORES_DOBLES.containsKey(actual)) {
            contexto.avanzar();
            return new Continuar(OPERADORES_DOBLES.get(actual));
        }
        if (SIMBOLOS_SIMPLES.containsKey(actual)) {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return new Emitir(new Token(SIMBOLOS_SIMPLES.get(actual), contexto.getLexema(), linea));
        }
        if (actual == '/') {
            contexto.avanzar();
            return new Continuar(EstadoBarra.INSTANCIA);
        }
        if (actual == '\'') {
            contexto.avanzar();
            return new Continuar(EstadoCharAbierto.INSTANCIA);
        }
        if (actual == '"') {
            contexto.avanzar();
            return new Continuar(EstadoStringAbierto.INSTANCIA);
        }
        // Criterio de recuperación: se consume el carácter ofensivo, porque
        // es exactamente lo que se reporta como lexema. Sin esto el análisis
        // no avanzaría y el error se repetiría para siempre.
        contexto.avanzar();
        throw contexto.crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico.CARACTER_DESCONOCIDO);
    }

    private static boolean esBlanco(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }
}
