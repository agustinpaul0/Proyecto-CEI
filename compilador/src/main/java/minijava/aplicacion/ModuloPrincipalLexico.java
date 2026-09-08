package minijava.aplicacion;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.FormateadorDeError;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.contexto.ContextoLexico;
import minijava.comun.salida.Salida;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;

import java.io.IOException;

public final class ModuloPrincipalLexico {

    private final Salida salida;
    private final ConfiguracionDelCompilador configuracion;
    private final FormateadorDeError formateadorDeError;

    public ModuloPrincipalLexico(Salida salida) {
        this(salida, ConfiguracionDelCompilador.DEFAULT);
    }

    public ModuloPrincipalLexico(Salida salida, ConfiguracionDelCompilador configuracion) {
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
                    imprimirToken(token);
                    termino = token.tipo() == TipoDeToken.EOF;
                } catch (ExcepcionLexica excepcion) {
                    huboErrores = true;
                    reportarError(excepcion);
                    // Sin paso de recuperación: el estado que lanzó ya consumió lo suyo.
                    termino = !configuracion.multiDeteccionDeErrores();
                }
            }
        } finally {
            manejador.close();
        }
        if (!huboErrores) {
            // El renglón en blanco de separación es el del ejemplo de Pautas, slide 26.
            salida.imprimir("");
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
