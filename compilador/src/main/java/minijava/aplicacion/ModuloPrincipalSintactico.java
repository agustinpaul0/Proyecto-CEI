package minijava.aplicacion;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.comun.salida.Salida;
import minijava.lexico.AnalizadorLexico;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.FormateadorDeError;
import minijava.sintactico.AnalizadorSintactico;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.io.IOException;

public final class ModuloPrincipalSintactico {

    private static final String MENSAJE_DE_EXITO = "Compilacion Exitosa";

    private final Salida salida;
    private final ConfiguracionDelCompilador configuracion;
    private final FormateadorDeError formateadorDeError;

    public ModuloPrincipalSintactico(Salida salida, ConfiguracionDelCompilador configuracion) {
        this.salida = salida;
        this.configuracion = configuracion;
        this.formateadorDeError = new FormateadorDeError(configuracion);
    }

    public void ejecutar(String rutaFuente) throws IOException {
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(rutaFuente);
        var analizador = new AnalizadorSintactico(
                new AnalizadorLexico(new ContextoLexico(manejador, configuracion)), configuracion);

        try {
            analizador.analizar();
            salida.imprimir(MENSAJE_DE_EXITO);
            salida.imprimir("");
            salida.imprimir("[SinErrores]");
        } catch (ExcepcionSintactica excepcion) {
            reportar(excepcion.getMessage(), excepcion.getLexemaOfensivo(), excepcion.getLinea());
        } catch (ExcepcionLexica excepcion) {
            formateadorDeError.formatear(excepcion).forEach(salida::imprimir);
        } finally {
            manejador.close();
        }
    }

    // El renglón en blanco entre el mensaje y el código es el del ejemplo de
    // Pautas Etapa 2, slide 25.
    private void reportar(String mensaje, String lexema, int linea) {
        salida.imprimir(mensaje);
        salida.imprimir("");
        salida.imprimir("[Error:" + lexema + "|" + linea + "]");
    }
}
