package minijava.lexico.estados;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
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
import minijava.lexico.tokens.TablaDeLexemas;
import minijava.lexico.tokens.TipoDeToken;

import java.util.Map;

public final class EstadoInicial implements EstadoLexico {

    public static final EstadoInicial INSTANCIA = new EstadoInicial();

    // Los que ya forman un token completos ellos solos: se emiten sin lookahead.
    // Puntuación (10) más '%' y '*'; el tipo lo resuelve la tabla por su lexema.
    private static final String SIMBOLOS_DE_UN_CARACTER = "(){}[];,.:%*";

    // Sólo dicen a qué estado ir: el tipo del token lo da la tabla por su lexema.
    private static final Map<Character, EstadoOperadorConSufijoOpcional> OPERADORES_CON_SUFIJO = Map.of(
            '>', EstadoOperadorConSufijoOpcional.SUFIJO_IGUAL,
            '<', EstadoOperadorConSufijoOpcional.SUFIJO_IGUAL,
            '!', EstadoOperadorConSufijoOpcional.SUFIJO_IGUAL,
            '=', EstadoOperadorConSufijoOpcional.SUFIJO_IGUAL,
            '+', EstadoOperadorConSufijoOpcional.SUFIJO_MAS,
            '-', EstadoOperadorConSufijoOpcional.SUFIJO_MENOS);

    private static final String OPERADORES_DOBLES = "&|";

    private EstadoInicial() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica {
        char actual = contexto.getCaracterActual();

        // Fin de archivo = llegó el centinela Y la posición confirma que se acabó
        // el contenido. El primer conjunto no se testea porque el segundo ya lo
        // implica; un centinela sin fin de archivo cae en el throw del final.
        if (contexto.esFinDeArchivo()) {
            // El lexema no se lee del fuente: el fin de archivo no consume nada.
            return Emitir.porLexemaFijo(TablaDeLexemas.lexemaFijoDe(TipoDeToken.EOF).orElseThrow(),
                    contexto.getLinea());
        }
        if (esBlanco(actual)) {
            contexto.descartar();
            return new Continuar(INSTANCIA);
        }
        if (SIMBOLOS_DE_UN_CARACTER.indexOf(actual) >= 0) {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return Emitir.porLexemaFijo(contexto.getLexema(), linea);
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
        if (OPERADORES_DOBLES.indexOf(actual) >= 0) {
            contexto.avanzar();
            return new Continuar(EstadoOperadorDoble.INSTANCIA);
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
        if (actual == '?') {
            int linea = contexto.getLinea();
            contexto.avanzar();
            return Emitir.porLexemaFijo(contexto.getLexema(), linea);
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
