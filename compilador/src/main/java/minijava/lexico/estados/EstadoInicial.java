package minijava.lexico.estados;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.AnclaDeError;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.estados.comentarios.EstadoBarra;
import minijava.lexico.estados.identificadores.EstadoIdGenOClase;
import minijava.lexico.estados.identificadores.EstadoIdMV;
import minijava.lexico.estados.literales.EstadoCharAbierto;
import minijava.lexico.estados.literales.EstadoStringAbierto;
import minijava.lexico.estados.numeros.EstadoEntero;
import minijava.lexico.estados.operadores.EstadoOperadorConSufijoOpcional;
import minijava.lexico.estados.operadores.EstadoOperadorDoble;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

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
        // Se consume el ofensivo: es lo que se reporta, y sin eso el análisis no avanza.
        contexto.avanzar();
        throw contexto.crearExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO,
                AnclaDeError.ULTIMO_CONSUMIDO);
    }

    private static boolean esBlanco(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }
}
