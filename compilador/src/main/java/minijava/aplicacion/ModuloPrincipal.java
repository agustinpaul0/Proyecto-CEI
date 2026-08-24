package minijava.aplicacion;

import minijava.errores.ExcepcionLexica;
import minijava.errores.FormateadorDeError;
import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.salida.Salida;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

import java.io.IOException;

// Cliente del Analizador Léxico: pide tokens hasta EOF o error, y los
// muestra por la interfaz Salida (Pautas Etapa 1, esquema Módulo Principal).
public final class ModuloPrincipal {

    private final Salida salida;
    private final FormateadorDeError formateadorDeError = new FormateadorDeError();

    public ModuloPrincipal(Salida salida) {
        this.salida = salida;
    }

    // Logro Multi-detección de Errores: no finaliza en el primer error, se
    // recupera y sigue reportando hasta EOF (AGENTS.md §6).
    public void ejecutar(String rutaFuente) throws IOException {
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(rutaFuente);
        AnalizadorLexico analizador = new AnalizadorLexico(manejador);
        boolean huboErrores = false;

        try {
            boolean llegoAEof = false;
            while (!llegoAEof) {
                try {
                    Token token = analizador.proximoToken();
                    imprimirToken(token);
                    llegoAEof = token.tipo() == TipoDeToken.EOF;
                } catch (ExcepcionLexica excepcion) {
                    huboErrores = true;
                    reportarError(excepcion);
                    analizador.recuperarDeError();
                }
            }
        } finally {
            manejador.close();
        }
        if (!huboErrores) {
            salida.imprimir("[SinErrores]");
        }
    }

    private void imprimirToken(Token token) {
        salida.imprimir("(" + token.tipo().getNombre() + "," + token.lexema() + "," + token.linea() + ")");
    }

    private void reportarError(ExcepcionLexica excepcion) {
        formateadorDeError.formatear(excepcion).forEach(salida::imprimir);
    }
}
