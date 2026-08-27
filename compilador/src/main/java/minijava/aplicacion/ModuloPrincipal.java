package minijava.aplicacion;

import minijava.config.ConfiguracionDelCompilador;
import minijava.errores.ExcepcionLexica;
import minijava.errores.FormateadorDeError;
import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.ContextoLexico;
import minijava.salida.Salida;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

import java.io.IOException;
import java.util.List;

public final class ModuloPrincipal {

    private final Salida salida;
    private final ConfiguracionDelCompilador configuracion;
    private final FormateadorDeError formateadorDeError;

    public ModuloPrincipal(Salida salida) {
        this(salida, ConfiguracionDelCompilador.DEFAULT);
    }

    public ModuloPrincipal(Salida salida, ConfiguracionDelCompilador configuracion) {
        this.salida = salida;
        this.configuracion = configuracion;
        this.formateadorDeError = new FormateadorDeError(configuracion);
    }

    public void ejecutar(String rutaFuente) throws IOException {
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(rutaFuente);
        AnalizadorLexico analizador =
                new AnalizadorLexico(new ContextoLexico(manejador, configuracion));
        boolean huboErrores = false;

        try {
            boolean termino = false;
            while (!termino) {
                try {
                    Token token = analizador.proximoToken();
                    // Los errores no fatales ocurrieron DENTRO del token, así
                    // que se reportan antes de imprimirlo.
                    boolean huboNoFatales = reportarErroresNoFatales(analizador);
                    huboErrores |= huboNoFatales;
                    imprimirToken(token);
                    termino = token.tipo() == TipoDeToken.EOF
                            || (huboNoFatales && !configuracion.multiDeteccionDeErrores());
                } catch (ExcepcionLexica excepcion) {
                    huboErrores |= reportarErroresNoFatales(analizador);
                    huboErrores = true;
                    reportarError(excepcion);
                    // Sin paso de recuperación: el estado que lanzó ya
                    // consumió lo que reportó (ver AnalizadorLexico).
                    termino = !configuracion.multiDeteccionDeErrores();
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

    private boolean reportarErroresNoFatales(AnalizadorLexico analizador) {
        List<ExcepcionLexica> pendientes = analizador.tomarErroresNoFatales();
        pendientes.forEach(this::reportarError);
        return !pendientes.isEmpty();
    }

    private void reportarError(ExcepcionLexica excepcion) {
        formateadorDeError.formatear(excepcion).forEach(salida::imprimir);
    }
}
